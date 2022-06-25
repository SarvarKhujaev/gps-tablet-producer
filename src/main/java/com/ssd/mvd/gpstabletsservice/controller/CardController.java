package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import com.ssd.mvd.gpstabletsservice.database.SerDes;
import com.ssd.mvd.gpstabletsservice.entity.Data;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
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
    public Mono< ApiResponseModel > linkCardToPatrul ( Request request ) { return RedisDataControl.getRedis().getPatrul( request.getData() ).flatMap( patrul -> Archive.getAchieve().save( patrul, SerDes.getSerDes().deserializeCard( request.getSubject() ) ) ); }

    @MessageMapping ( value = "getCurrentActiveTask" ) // for Android
    public Mono< ApiResponseModel > getCurrentActiveTask ( String token ) { return RedisDataControl.getRedis().getPatrul( RedisDataControl.getRedis().decode( token ) ).flatMap( patrul -> patrul.getStatus().compareTo( Status.FREE ) == 0
        ? Mono.just( ApiResponseModel.builder().success( true ).status( com.ssd.mvd.gpstabletsservice.response.Status.builder().message( "Y have no tasks. My congratulations ))" ).code( 200 ).build() ).build() )
            : ( patrul.getSelfEmploymentId() != null ? Archive.getAchieve().get( patrul.getSelfEmploymentId() ).flatMap( selfEmploymentTask -> Mono.just( ApiResponseModel.builder().data( Data.builder().data( selfEmploymentTask ).type( "selfEmployment" ).build() ).status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 ).message( "U have SElfEmployment Task" ).build() ).success( true ).build() ) )
            : Archive.getAchieve().getCard( patrul.getCard() ).flatMap( card -> Mono.just( ApiResponseModel.builder().data( Data.builder().data( card ).type( "card" ).build() ).status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 ).message( "U have 102 Task" ).build() ).success( true ).build() ) ) ) ); }
}
