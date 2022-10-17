package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.card.CardRequest;
import com.ssd.mvd.gpstabletsservice.entity.TaskInspector;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.database.*;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.Map;

@Slf4j
@RestController
public class CardController {

    @MessageMapping ( value = "getListOfCards" )
    public Flux< ActiveTask > getListOfCards () { return CassandraDataControlForTasks
            .getInstance()
            .getGetActiveTasks()
            .get()
            .sort( Comparator.comparing( ActiveTask::getCreatedDate ).reversed() )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "getCurrentActiveTask" ) // for Android
    public Mono< ApiResponseModel > getCurrentActiveTask ( String token ) { return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( CassandraDataControl.getInstance().decode( token ) )
            .flatMap( patrul -> TaskInspector
                    .getInstance()
                    .getCurrentActiveTask( patrul ) )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "linkCardToPatrul" )
    public Flux< ApiResponseModel > linkCardToPatrul ( CardRequest< ? > request ) {
        if ( request.getTaskType().compareTo( TaskTypes.CARD_102 ) == 0 ) {
            Card card = SerDes.getSerDes().deserializeCard( request.getCard() );
            CassandraDataControlForTasks
                    .getInstance()
                    .addValue( card );

            CassandraDataControlForTasks
                    .getInstance()
                    .addValue( card.getCardId().toString(), new ActiveTask( card ) );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getArchive()
                                    .save( patrul1, card ) ) )
                    .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                            error.getMessage(), object ) ) )
                    .onErrorReturn( Archive
                            .getArchive()
                            .getErrorResponse()
                            .get() ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_EVENT_FACE ) == 0 ) {
            EventFace eventFace = SerDes.getSerDes().deserializeEventFace( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getArchive()
                                    .save( patrul1, eventFace ) ) )
                    .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                            error.getMessage(), object ) ) )
                    .onErrorReturn( Archive
                            .getArchive()
                            .getErrorResponse()
                            .get() ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_PERSON ) == 0 ) {
            FaceEvent facePerson = SerDes.getSerDes().deserializeFaceEvents( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getArchive().save( patrul1, facePerson ) ) )
                    .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                            error.getMessage(), object ) ) )
                    .onErrorReturn( Archive
                            .getArchive()
                            .getErrorResponse()
                            .get() ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_CAR ) == 0 ) {
            CarEvent carEvents = SerDes.getSerDes().deserializeCarEvents ( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getArchive().save( patrul1, carEvents ) ) )
                    .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                            error.getMessage(), object ) ) )
                    .onErrorReturn( Archive
                            .getArchive()
                            .getErrorResponse()
                            .get() ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_EVENT_BODY ) == 0 ) {
            EventBody eventBody = SerDes.getSerDes().deserializeEventBody( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getArchive()
                                    .save( patrul1, eventBody ) ) )
                    .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                            error.getMessage(), object ) ) )
                    .onErrorReturn( Archive
                            .getArchive()
                            .getErrorResponse()
                            .get() ); }

        else { EventCar eventCar = SerDes.getSerDes().deserializeEventCar( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getArchive().save( patrul1, eventCar ) ) )
                    .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                            error.getMessage(), object ) ) )
                    .onErrorReturn( Archive
                            .getArchive()
                            .getErrorResponse()
                            .get() ); } }

    @MessageMapping ( value = "addNewWarningCar" )
    public Mono< ApiResponseModel > addNewWarningCar ( CarTotalData carTotalData ) {
        return Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", "Car was saved successfully",
                        "success", CassandraDataControlForTasks
                                .getInstance()
                                .addValue( KafkaDataControl
                                        .getInstance()
                                        .writeToKafka( carTotalData ) ) ) )
                .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                        error.getMessage(), object ) ) )
                .onErrorReturn( Archive
                        .getArchive()
                        .getErrorResponse()
                        .get() ); }

    @MessageMapping ( value = "getViolationsInformationList" )
    public Mono< List< ViolationsInformation > > getViolationsInformationList ( String gosnumber ) { return Mono.just(
            CassandraDataControlForTasks
                    .getInstance()
                    .getGetViolationsInformationList()
                    .apply( gosnumber ) )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "getWarningCarDetails" )
    public Mono< ApiResponseModel > getWarningCarDetails ( String gosnumber ) { return CassandraDataControlForTasks
            .getInstance()
            .getGetWarningCarDetails()
            .apply( gosnumber )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "getAllCarTotalData" )
    public Flux< CarTotalData > getAllCarTotalData () { return CassandraDataControlForTasks
            .getInstance()
            .getGetAllCarTotalData()
            .get()
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "removePatrulFromTask" )
    public Mono< ApiResponseModel > removePatrulFromTask ( UUID uuid ) {
        return CassandraDataControl
                .getInstance()
                .getGetPatrulByUUID()
                .apply( uuid )
                .filter( patrul -> patrul.getTaskTypes().compareTo( TaskTypes.FREE ) != 0 )
                .flatMap( patrul -> TaskInspector
                        .getInstance()
                        .removePatrulFromTask( patrul ) )
                .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                        error.getMessage(), object ) ) )
                .onErrorReturn( Archive
                        .getArchive()
                        .getErrorResponse()
                        .get() ); }

    @MessageMapping ( value = "addNewPatrulsToTask" )
    public Mono< ApiResponseModel > addNewPatrulsToTask ( CardRequest< ? > request ) {
        return switch ( request.getTaskType() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCard102()
                    .apply( request.getCard().toString() )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .subscribe( patrulMono -> patrulMono
                                        .subscribe( patrul -> TaskInspector
                                                .getInstance()
                                                .changeTaskStatus( patrul,
                                                        com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                        card ) ) );
                        return Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of( "message", request.getCard()
                                        + " has got new patrul" ) ); } );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventFace()
                    .apply( request.getCard().toString() )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .subscribe( patrulMono -> patrulMono
                                        .subscribe( patrul -> TaskInspector
                                                .getInstance()
                                                .changeTaskStatus( patrul,
                                                        com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                        card ) ) );
                        return Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of( "message", request.getCard()
                                        + " has got new patrul" ) ); } );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventCar()
                    .apply( request.getCard().toString() )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .subscribe( patrulMono -> patrulMono
                                        .subscribe( patrul -> TaskInspector
                                                .getInstance()
                                                .changeTaskStatus( patrul,
                                                        com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                        card ) ) );
                        return Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of( "message", request.getCard()
                                        + " has got new patrul" ) ); } );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventBody()
                    .apply( request.getCard().toString() )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .subscribe( patrulMono -> patrulMono
                                        .subscribe( patrul -> TaskInspector
                                                .getInstance()
                                                .changeTaskStatus( patrul,
                                                        com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                        card ) ) );
                        return Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of( "message", request.getCard()
                                        + " has got new patrul" ) ); } );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCarEvents()
                    .apply( request.getCard().toString() )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .subscribe( patrulMono -> patrulMono
                                        .subscribe( patrul -> TaskInspector
                                                .getInstance()
                                                .changeTaskStatus( patrul,
                                                        com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                        card ) ) );
                        return Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of( "message", request.getCard()
                                        + " has got new patrul" ) ); } );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetFaceEvents()
                    .apply( request.getCard().toString() )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .subscribe( patrulMono -> patrulMono
                                        .subscribe( patrul -> TaskInspector
                                                .getInstance()
                                                .changeTaskStatus( patrul,
                                                        com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                        card ) ) );
                        return Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of( "message", request.getCard()
                                        + " has got new patrul" ) ); } );

            default -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetSelfEmploymentTask()
                    .apply( UUID.fromString( request.getCard().toString() ) )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .subscribe( patrulMono -> patrulMono
                                        .subscribe( patrul -> TaskInspector
                                                .getInstance()
                                                .changeTaskStatus( patrul,
                                                        com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                        card ) ) );
                        return Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of( "message", request.getCard()
                                        + " has got new patrul" ) ); } ); }; }
}
