package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvents;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvents;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.card.CardDetails;
import com.ssd.mvd.gpstabletsservice.task.card.CardRequest;
import com.ssd.mvd.gpstabletsservice.entity.TaskInspector;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.database.*;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Comparator;
import java.util.List;

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
    public Flux< ApiResponseModel > linkCardToPatrul ( CardRequest< ? > request ) {
        if ( request.getTaskType().compareTo( TaskTypes.CARD_102 ) == 0 ) {
            Card card = SerDes.getSerDes().deserializeCard( request.getCard() );
            RedisDataControl.getRedis().addValue( card );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> RedisDataControl.getRedis().getPatrul( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getAchieve()
                                    .save( patrul1, card ) ) ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_EVENT_FACE ) == 0 ) {
            EventFace eventFace = SerDes.getSerDes().deserializeEventFace( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> RedisDataControl.getRedis().getPatrul( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getAchieve()
                                    .save( patrul1, eventFace ) ) ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_PERSON ) == 0 ) {
            FaceEvents facePerson = SerDes.getSerDes().deserializeFaceEvents( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> RedisDataControl.getRedis().getPatrul( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getAchieve().save( patrul1, facePerson ) ) ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_CAR ) == 0 ) {
            CarEvents carEvents = SerDes.getSerDes().deserializeCarEvents ( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> RedisDataControl.getRedis().getPatrul( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getAchieve().save( patrul1, carEvents ) ) ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_EVENT_BODY ) == 0 ) {
            EventBody eventBody = SerDes.getSerDes().deserializeEventBody( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> RedisDataControl.getRedis().getPatrul( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getAchieve()
                                    .save( patrul1, eventBody ) ) ); }

        else { EventCar eventCar = SerDes.getSerDes().deserializeEventCar( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> RedisDataControl.getRedis().getPatrul( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getAchieve().save( patrul1, eventCar ) ) ); }
    }

    @MessageMapping ( value = "getCurrentActiveTask" ) // for Android
    public Mono< ApiResponseModel > getCurrentActiveTask ( String token ) { return RedisDataControl.getRedis()
            .getPatrul( RedisDataControl.getRedis().decode( token ) )
            .flatMap( patrul -> TaskInspector.getInstance().getCurrentActiveTask( patrul ) ); }

    @MessageMapping ( value = "addNewWarningCar" )
    public Mono< ApiResponseModel > addNewWarningCar ( CarTotalData carTotalData ) { CassandraDataControl
            .getInstance()
            .addValue(
                    KafkaDataControl
                            .getInstance()
                            .writeToKafka( carTotalData ) );
        return Mono.just(
                ApiResponseModel.builder()
                        .success( true )
                        .status( Status.builder()
                                .message( "Car was saved successfully" )
                                .code( 200 )
                                .build() )
                        .build() ); }

    @MessageMapping ( value = "getViolationsInformationsList" )
    public Mono< List< ViolationsInformation > > getViolationsInformationsList ( String gosnumber ) { return Mono.just(
            CassandraDataControl
                    .getInstance()
                    .getViolationsInformationsList( gosnumber ) ); }

    @MessageMapping ( value = "getWarningCarDetails" )
    public Mono< CardDetails > getWarningCarDetails ( String gosnumber ) { return CassandraDataControl
            .getInstance()
            .getWarningCarDetails( gosnumber ); }
}
