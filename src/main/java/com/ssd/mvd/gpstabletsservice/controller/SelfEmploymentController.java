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
public final class SelfEmploymentController extends SerDes {
    @MessageMapping ( value = "getSelfEmployment" ) // returns the current Card
    public Mono< SelfEmploymentTask > getSelfEmployment ( final String uuid ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getTask
                .apply( uuid )
                .filter( super::objectIsNotNull )
                .map( row -> {
                    final SelfEmploymentTask selfEmploymentTask =
                            super.deserialize( row.getString("object" ), SelfEmploymentTask.class );

                    selfEmploymentTask.getTaskCommonParams().close();
                    return selfEmploymentTask;
                } )
                .onErrorContinue( super::logging );
    }

    @MessageMapping ( value = "addReportForSelfEmployment" )
    public Mono< ApiResponseModel > addReportForSelfEmployment ( final ReportForCard reportForCard ) {
        return CassandraDataControl
                .getInstance()
                .getPatrulByUUID
                .apply( reportForCard.getUuidOfPatrul() )
                .flatMap( patrul -> {
                    CassandraDataControl
                            .getInstance()
                            .updatePatrulActivity
                            .accept( patrul );

                    return TaskInspector
                            .getInstance()
                            .saveReportForTask
                            .apply( patrul, reportForCard );
                } )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "addSelfEmployment" ) // saves new Task and link the Patrul who created it
    public Mono< ApiResponseModel > addSelfEmployment ( final SelfEmploymentTask selfEmploymentTask ) {
        selfEmploymentTask.setAddress(
                super.removeAllDotes(
                    UnirestController
                            .getInstance()
                            .getAddressByLocation
                            .apply( selfEmploymentTask.getLatOfAccident(), selfEmploymentTask.getLanOfAccident() ) ) );

        return CassandraDataControl
                .getInstance()
                .getPatrulByUUID
                .apply( selfEmploymentTask.getTaskCommonParams().getPatruls().keySet().iterator().next() )
                .flatMap( patrul -> super.function(
                        Map.of( "message", selfEmploymentTask.getTaskCommonParams().getUuid()
                                + " was linked to: "
                                + TaskInspector
                                .getInstance()
                                .changeTaskStatus(
                                        patrul,
                                        selfEmploymentTask.getTaskCommonParams().getStatus(),
                                        selfEmploymentTask )
                                .getPatrulFIOData()
                                .getName() ) ) )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.errorResponse() );
    }
}
