package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import com.ssd.mvd.gpstabletsservice.database.SerDes;
import com.ssd.mvd.gpstabletsservice.request.SelfEmploymentRequest;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class SelfEmploymentController {

//    @MessageMapping ( value = "getDetails" )
//    public Mono<> getDetails ( Patrul patrul ) {}

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
    public Mono< ApiResponseModel > addReportForSelfEmployment ( ReportForCard reportForCard ) { return reportForCard.getCardType().equals( "selfEmployment" ) ? Archive.getAchieve().get( reportForCard.getCardId() )
            .flatMap( selfEmploymentTask -> {
                selfEmploymentTask.getReportForCards().add( reportForCard );
                return RedisDataControl.getRedis().getPatrul( reportForCard.getPassportSeries() ).flatMap( patrul -> Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) ) ).success( CassandraDataControl.getInstance().addValue( patrul.changeTaskStatus( com.ssd.mvd.gpstabletsservice.constants.Status.FINISHED ), SerDes.getSerDes().serialize( patrul ) ) ).status( Status.builder().code( 200 ).message( patrul.getName() + " report's was saved to selfEmploymentTask" ).build() ).build() ) );
            } ) : Archive.getAchieve().getCard( reportForCard.getCardId() ).flatMap( card -> RedisDataControl.getRedis().getPatrul( reportForCard.getPassportSeries() ).flatMap( patrul -> Mono.just( ApiResponseModel.builder().success( true ).status( Status.builder().code( 200 ).message( patrul.getName() + " report's was saved for Card" ).build() ).build() ) ) ); }
}
