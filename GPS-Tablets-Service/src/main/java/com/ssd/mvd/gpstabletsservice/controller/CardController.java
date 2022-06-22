package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import com.ssd.mvd.gpstabletsservice.entity.Data;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class CardController {

    @MessageMapping ( value = "getListOfCards" )
    public Flux< Card > getListOfCards () { return Archive.getAchieve().getAllCards(); }

    @MessageMapping ( value = "getCurrentCard" )
    public Mono< Card > getCurrentCard ( UUID cardId ) { return Archive.getAchieve().getCard( cardId ); }

    @MessageMapping ( value = "linkCardToPatrul" )
    public Mono< ApiResponseModel > linkCardToPatrul ( Request request ) { return RedisDataControl.getRedis().getPatrul( request.getEndTime() ).flatMap( patrul -> Archive.getAchieve().save( patrul, UUID.fromString( request.getStartTime() ) ) ); }

    @MessageMapping ( value = "getCurrentActiveTask" ) // for Android
    public Mono< ApiResponseModel > getCurrentActiveTask ( String token ) { return RedisDataControl.getRedis().getPatrul( RedisDataControl.getRedis().decode( token ) ).flatMap( patrul -> {
        if ( patrul.getSelfEmploymentId() != null ) return Archive.getAchieve().get( patrul.getSelfEmploymentId() ).flatMap( selfEmploymentTask -> Mono.just( ApiResponseModel.builder().data( Data.builder().object( selfEmploymentTask ).type( "selfEmployment" ).build() ).status( Status.builder().code( 200 ).message( "U have 102 Task" ).build() ).success( true ).build() ) );
        else if ( patrul.getCard() != null ) return Archive.getAchieve().getCard( patrul.getCard() ).flatMap( card -> Mono.just( ApiResponseModel.builder().data( Data.builder().object( card ).type( "card" ).build() ).status( Status.builder().code( 200 ).message( "U have 102 Task" ).build() ).success( true ).build() ) );
        else return Mono.just( ApiResponseModel.builder().data( null ).status( Status.builder().code( 200 ).message( "U have 102 Task" ).build() ).success( true ).build() ); } ); }
}
