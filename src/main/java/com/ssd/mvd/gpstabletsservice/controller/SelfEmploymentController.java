package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import static com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.entity.TaskInspector;
import com.ssd.mvd.gpstabletsservice.database.*;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;
import java.util.UUID;
import java.util.Map;

@RestController
public class SelfEmploymentController extends LogInspector {
    @MessageMapping ( value = "getSelfEmployment" ) // returns the current Card
    public Mono< SelfEmploymentTask > getSelfEmployment ( UUID uuid ) { return CassandraDataControlForTasks
            .getInstance()
            .getGetSelfEmploymentTask()
            .apply( uuid )
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "addReportForSelfEmployment" )
    public Mono< ApiResponseModel > addReportForSelfEmployment ( ReportForCard reportForCard ) { return CassandraDataControl
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
    public Mono< ApiResponseModel > addSelfEmployment ( SelfEmploymentTask selfEmploymentTask ) { return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( selfEmploymentTask.getPatruls().keySet().iterator().next() )
            .flatMap( patrul -> super.getFunction()
                    .apply( Map.of( "message", selfEmploymentTask + " was linked to: "
                            + TaskInspector
                            .getInstance()
                            .changeTaskStatus( patrul, ATTACHED, selfEmploymentTask )
                            .getName() ) ) )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }
}
