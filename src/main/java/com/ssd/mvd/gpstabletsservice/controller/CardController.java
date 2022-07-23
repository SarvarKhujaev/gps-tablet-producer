package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.card.CardRequest;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.entity.Data;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;

@RestController
public class CardController {
    @MessageMapping ( value = "getListOfCards" )
    public Flux< ActiveTask > getListOfCards () { return RedisDataControl.getRedis().getActiveTasks().sort( Comparator.comparing( ActiveTask::getCreatedDate ).reversed() ); }

    @MessageMapping ( value = "getCurrentCard" )
    public Mono< Card > getCurrentCard ( Long cardId ) { return RedisDataControl.getRedis().getCard( cardId ); }

    @MessageMapping ( value = "linkCardToPatrul" )
    public Flux< ApiResponseModel > linkCardToPatrul ( CardRequest request ) { return Flux.fromStream( request.getPatruls().stream() )
            .map( s -> RedisDataControl.getRedis().getPatrul( s ) )
            .flatMap( patrul -> patrul.flatMap( patrul1 -> Archive.getAchieve().save( patrul1, request.getCard() ) ) ); }

    @MessageMapping ( value = "getCurrentActiveTask" ) // for Android
    public Mono< ApiResponseModel > getCurrentActiveTask ( String token ) { return RedisDataControl.getRedis()
            .getPatrul( RedisDataControl.getRedis().decode( token ) )
            .flatMap( patrul -> {
                if ( patrul.getSelfEmploymentId() != null ) return Archive.getAchieve().get( patrul.getSelfEmploymentId() )
                        .flatMap( selfEmploymentTask -> Mono.just( ApiResponseModel.builder()
                                .data( Data.builder().data( new ActiveTask( selfEmploymentTask ) ).type( "selfEmployment" ).build() )
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                        .message( "U have SelfEmployment Task" ).build() ).success( true ).build() ) );
                else if ( patrul.getCard() != null ) return RedisDataControl.getRedis().getCard( patrul.getCard() )
                        .flatMap( card -> Mono.just( ApiResponseModel.builder().data( Data.builder().data( new ActiveTask( card ) ).type( "card" ).build() )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                .message( "U have 102 Task" ).build() ).success( true ).build() ) );
                else return Mono.just( ApiResponseModel.builder().success( false )
                            .status( Status.builder().code( 201 )
                                    .message( "U have no task, so u can do smth else, my darling )))" ).build() ).build() ); } ); }
}
