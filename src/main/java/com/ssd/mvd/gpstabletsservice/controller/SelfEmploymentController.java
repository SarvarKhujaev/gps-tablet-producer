package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.entity.TaskInspector;
import com.ssd.mvd.gpstabletsservice.database.*;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
public class SelfEmploymentController {
    @MessageMapping ( value = "getAllSelfEmploymentTask" )
    public Flux< SelfEmploymentTask > getAllSelfEmploymentTask () { return CassandraDataControlForTasks
            .getInstance()
            .getSelfEmploymentTasks(); }

    @MessageMapping ( value = "getSelfEmployment" ) // returns the current Card
    public Mono< SelfEmploymentTask > getSelfEmployment ( UUID uuid ) { return CassandraDataControlForTasks
            .getInstance()
            .getSelfEmploymentTask( uuid ); }

    @MessageMapping ( value = "addReportForSelfEmployment" )
    public Mono< ApiResponseModel > addReportForSelfEmployment ( ReportForCard reportForCard ) { return CassandraDataControl
            .getInstance()
            .getPatrul( reportForCard.getUuidOfPatrul() )
            .flatMap( patrul -> TaskInspector
                    .getInstance()
                    .saveReportForTask( patrul, reportForCard ) ); }

    @MessageMapping ( value = "addSelfEmployment" ) // saves new Task and link the Patrul who created it
    public Mono< ApiResponseModel > addSelfEmployment ( SelfEmploymentTask selfEmploymentTask ) { return CassandraDataControl
            .getInstance()
            .getPatrul( selfEmploymentTask.getPatruls().keySet().iterator().next() )
            .flatMap( patrul -> Archive
                    .getAchieve()
                    .save( selfEmploymentTask, patrul ) ); }
}
