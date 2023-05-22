package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskInspector;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.SerDes;
import com.ssd.mvd.gpstabletsservice.database.*;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
public class SelfEmploymentController extends SerDes {
    @MessageMapping ( value = "getSelfEmployment" ) // returns the current Card
    public Mono< SelfEmploymentTask > getSelfEmployment ( final String uuid ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getGetTask()
                .apply( uuid )
                .map( row -> super.deserialize( row.getString("object" ), SelfEmploymentTask.class ) )
                .map( selfEmploymentTask -> {
                    selfEmploymentTask.setPatruls( null );
                    selfEmploymentTask.setReportForCards( null );
                    return selfEmploymentTask; } )
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "addReportForSelfEmployment" )
    public Mono< ApiResponseModel > addReportForSelfEmployment ( final ReportForCard reportForCard ) { return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( reportForCard.getUuidOfPatrul() )
            .flatMap( patrul -> TaskInspector
                    .getInstance()
                    .getSaveReportForTask()
                    .apply( patrul, reportForCard ) )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "addSelfEmployment" ) // saves new Task and link the Patrul who created it
    public Mono< ApiResponseModel > addSelfEmployment ( final SelfEmploymentTask selfEmploymentTask ) {
        selfEmploymentTask.setAddress( super.concatNames.apply(
                UnirestController
                        .getInstance()
                        .getGetAddressByLocation()
                        .apply( selfEmploymentTask.getLatOfAccident(), selfEmploymentTask.getLanOfAccident() ), 3 ) );
        return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( selfEmploymentTask.getPatruls().keySet().iterator().next() )
            .flatMap( patrul -> super.getFunction().apply(
                    Map.of( "message", selfEmploymentTask + " was linked to: "
                            + TaskInspector
                            .getInstance()
                            .changeTaskStatus( patrul, selfEmploymentTask.getTaskStatus(), selfEmploymentTask )
                            .getName() ) ) )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }
}
