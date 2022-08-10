package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvents;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvents;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.card.CardRequest;
import com.ssd.mvd.gpstabletsservice.entity.TaskInspector;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.database.SerDes;
import com.ssd.mvd.gpstabletsservice.task.card.Card;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Comparator;

@RestController
public class CardController {
    @MessageMapping ( value = "removePatrulFromCard" )
    public Mono< ApiResponseModel > removePatrulFromCard ( Request request ) { return RedisDataControl.getRedis().getPatrul( request.getData() )
            .flatMap( patrul -> Archive.getAchieve().removePatrulFromCard( Long.parseLong( request.getAdditional() ), patrul ) ); }

    @MessageMapping ( value = "addNewPatrulToCard" )
    public Mono< ApiResponseModel > addNewPatrulToCard ( Request request ) { return RedisDataControl.getRedis().getPatrul( request.getData() )
            .flatMap( patrul -> Archive.getAchieve().addNewPatrulToCard( Long.parseLong( request.getAdditional() ), patrul ) ); }

    @MessageMapping ( value = "getListOfCards" )
    public Flux< ActiveTask > getListOfCards () { return RedisDataControl.getRedis().getActiveTasks().sort( Comparator.comparing( ActiveTask::getCreatedDate ).reversed() ); }

    @MessageMapping ( value = "getCurrentCard" )
    public Mono< Card > getCurrentCard ( Long cardId ) { return RedisDataControl.getRedis().getCard( cardId ); }

    @MessageMapping ( value = "linkCardToPatrul" )
    public Mono< ApiResponseModel > linkCardToPatrul ( CardRequest< ? > request ) {
        switch ( request.getTaskType() ) {
            case CARD_102 : {
                Card card = SerDes.getSerDes().deserializeCard( request.getCard() );
                RedisDataControl.getRedis().addValue( card );
                Flux.fromStream( request.getPatruls().stream() )
                        .map( s -> RedisDataControl.getRedis().getPatrul( s ) )
                        .flatMap( patrul -> patrul
                                .flatMap( patrul1 -> Archive.getAchieve()
                                        .save( patrul1, card ) ) ); }

            case FIND_FACE_EVENT_FACE : {
                EventFace eventFace = SerDes.getSerDes().deserializeEventFace( request.getCard() );
                Flux.fromStream( request.getPatruls().stream() )
                        .map( s -> RedisDataControl.getRedis().getPatrul( s ) )
                        .flatMap( patrul -> patrul
                                .flatMap( patrul1 -> Archive.getAchieve()
                                        .save( patrul1, eventFace ) ) ); }

            case FIND_FACE_EVENT_BODY : {
                EventBody eventBody = SerDes.getSerDes().deserializeEventBody( request.getCard() );
                Flux.fromStream( request.getPatruls().stream() )
                        .map( s -> RedisDataControl.getRedis().getPatrul( s ) )
                        .flatMap( patrul -> patrul
                                .flatMap( patrul1 -> Archive.getAchieve()
                                        .save( patrul1, eventBody ) ) ); }

            case FIND_FACE_PERSON : {
                FaceEvents facePerson = SerDes.getSerDes().deserializeFaceEvents( request.getCard() );
                Flux.fromStream( request.getPatruls().stream() )
                        .map( s -> RedisDataControl.getRedis().getPatrul( s ) )
                        .flatMap( patrul -> patrul
                                .flatMap( patrul1 -> Archive.getAchieve().save( patrul1, facePerson ) ) ); }

            case FIND_FACE_CAR : {
                CarEvents carEvents = SerDes.getSerDes().deserializeCarEvents ( request.getCard() );
                Flux.fromStream( request.getPatruls().stream() )
                        .map( s -> RedisDataControl.getRedis().getPatrul( s ) )
                        .flatMap( patrul -> patrul
                                .flatMap( patrul1 -> Archive.getAchieve().save( patrul1, carEvents ) ) ); }

            default : {
                EventCar eventCar = SerDes.getSerDes().deserializeEventCar( request.getCard() );
                Flux.fromStream( request.getPatruls().stream() )
                        .map( s -> RedisDataControl.getRedis().getPatrul( s ) )
                        .flatMap( patrul -> patrul
                                .flatMap( patrul1 -> Archive.getAchieve().save( patrul1, eventCar ) ) ); } }
        return Mono.just( ApiResponseModel.builder().status( Status.builder().message( "Time for" ).build() ).build() ); }

    @MessageMapping ( value = "getCurrentActiveTask" ) // for Android
    public Mono< ApiResponseModel > getCurrentActiveTask ( String token ) { return RedisDataControl.getRedis()
            .getPatrul( RedisDataControl.getRedis().decode( token ) )
            .flatMap( patrul -> TaskInspector.getInstance().getCurrentActiveTask( patrul ) ); }
}
