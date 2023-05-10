package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskInspector;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.SerDes;
import com.ssd.mvd.gpstabletsservice.database.*;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import java.util.UUID;
import java.util.Map;

@RestController
public class SelfEmploymentController extends SerDes {
    @MessageMapping ( value = "getSelfEmployment" ) // returns the current Card
    public Mono< SelfEmploymentTask > getSelfEmployment ( final UUID uuid ) { return CassandraDataControlForTasks
            .getInstance()
            .getGetRowDemo()
            .apply( uuid.toString() )
            .map( row -> (SelfEmploymentTask) super.deserialize.apply( row.getString("object" ), TaskTypes.SELF_EMPLOYMENT ) )
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
    public Mono< ApiResponseModel > addSelfEmployment ( final SelfEmploymentTask selfEmploymentTask ) { return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( selfEmploymentTask.getPatruls().keySet().iterator().next() )
            .flatMap( patrul -> super.getFunction()
                    .apply( Map.of( "message", selfEmploymentTask + " was linked to: "
                            + TaskInspector
                            .getInstance()
                            .changeTaskStatus( patrul, selfEmploymentTask.getTaskStatus(), selfEmploymentTask )
                            .getName() ) ) )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }
}
