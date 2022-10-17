package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.entity.TaskInspector;
import com.ssd.mvd.gpstabletsservice.database.*;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Mono;
import java.util.UUID;

@Slf4j
@RestController
public class SelfEmploymentController {
    @MessageMapping ( value = "getSelfEmployment" ) // returns the current Card
    public Mono< SelfEmploymentTask > getSelfEmployment ( UUID uuid ) { return CassandraDataControlForTasks
            .getInstance()
            .getGetSelfEmploymentTask()
            .apply( uuid )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "addReportForSelfEmployment" )
    public Mono< ApiResponseModel > addReportForSelfEmployment ( ReportForCard reportForCard ) { return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( reportForCard.getUuidOfPatrul() )
            .flatMap( patrul -> TaskInspector
                    .getInstance()
                    .saveReportForTask( patrul, reportForCard ) )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "addSelfEmployment" ) // saves new Task and link the Patrul who created it
    public Mono< ApiResponseModel > addSelfEmployment ( SelfEmploymentTask selfEmploymentTask ) { return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( selfEmploymentTask.getPatruls().keySet().iterator().next() )
            .flatMap( patrul -> Archive
                    .getArchive()
                    .save( selfEmploymentTask, patrul ) )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }
}
