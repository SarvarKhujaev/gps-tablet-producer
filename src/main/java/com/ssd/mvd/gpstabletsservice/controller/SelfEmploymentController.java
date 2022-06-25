package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import com.ssd.mvd.gpstabletsservice.database.SerDes;
import com.ssd.mvd.gpstabletsservice.request.SelfEmploymentRequest;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class SelfEmploymentController {

    @MessageMapping ( value = "getSelfEmployment" ) // returns the current Card
    public Mono< SelfEmploymentTask > getSelfEmployment ( UUID uuid ) { return Archive.getAchieve().get( uuid ); }

    @MessageMapping ( value = "getAllSelfEmploymentTask" )
    public Flux< SelfEmploymentTask > getAllSelfEmploymentTask () { return Archive.getAchieve().getAllSelfEmploymentTask(); }

    @MessageMapping ( value = "addSelfEmployment" ) // saves new Task and link the Patrul who created it
    public Mono< ApiResponseModel > addSelfEmployment ( SelfEmploymentTask selfEmploymentTask ) { return RedisDataControl.getRedis().getPatrul( selfEmploymentTask.getPatruls().get( 0 ) ).flatMap( patrul -> Archive.getAchieve().save( selfEmploymentTask, patrul ) ); }

    @MessageMapping ( value = "removePatrulFromSelfEmployment" )
    public Mono< ApiResponseModel > removePatrulFromSelfEmployment ( SelfEmploymentRequest request ) { return Archive.getAchieve().removePatrulFromSelfEmployment( request.getUuid(), request.getPatrul() ); }

    @MessageMapping ( value = "addNewPatrulToSelfEmployment" ) // join new Patrul to existing selfEmployment
    public Mono< ApiResponseModel > addNewPatrulToSelfEmployment ( SelfEmploymentRequest selfEmploymentRequest ) { return Archive.getAchieve().save( selfEmploymentRequest.getUuid(), selfEmploymentRequest.getPatrul() ); }

    @MessageMapping ( value = "addReportForSelfEmployment" )
    public Mono< ApiResponseModel > addReportForSelfEmployment ( ReportForCard reportForCard ) {
        return RedisDataControl.getRedis().getPatrul( reportForCard.getPassportSeries() ).flatMap( patrul -> patrul.getCard() != null ? Archive.getAchieve().getCard( reportForCard.getCardId() ).flatMap( card -> {
            patrul.changeTaskStatus( Status.FINISHED );
            card.getReportForCards().add( reportForCard );
            return RedisDataControl.getRedis().update( patrul ).flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder().success( true ).status( com.ssd.mvd.gpstabletsservice.response.Status.builder().message( "Report from: " + patrul.getName() + " was saved" ).code( 201 ).build() ).build() ) );
        } ) : Archive.getAchieve().get( reportForCard.getCardId() ).flatMap( selfEmploymentTask -> {
            patrul.changeTaskStatus( Status.FINISHED );
            selfEmploymentTask.getReportForCards().add( reportForCard );
            return RedisDataControl.getRedis().update( patrul ).flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) ) ).status( com.ssd.mvd.gpstabletsservice.response.Status.builder().message( "Report from: " + patrul.getName() + " was saved" ).code( 201 ).build() ).build() ) );
        } ) ); }
}
