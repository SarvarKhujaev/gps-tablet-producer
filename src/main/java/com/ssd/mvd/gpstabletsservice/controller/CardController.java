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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class CardController {

    @MessageMapping ( value = "getListOfCards" )
    public Flux< ActiveTask > getListOfCards () { return CassandraDataControlForTasks
            .getInstance()
            .getActiveTasks()
            .sort( Comparator.comparing( ActiveTask::getCreatedDate ).reversed() ); }

    @MessageMapping ( value = "getCurrentActiveTask" ) // for Android
    public Mono< ApiResponseModel > getCurrentActiveTask ( String token ) { return CassandraDataControl
            .getInstance()
            .getPatrul( CassandraDataControl.getInstance().decode( token ) )
            .flatMap( patrul -> TaskInspector
                    .getInstance()
                    .getCurrentActiveTask( patrul ) ); }

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
                            .getPatrul( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getArchive()
                                    .save( patrul1, card ) ) ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_EVENT_FACE ) == 0 ) {
            EventFace eventFace = SerDes.getSerDes().deserializeEventFace( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getPatrul( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getArchive()
                                    .save( patrul1, eventFace ) ) ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_PERSON ) == 0 ) {
            FaceEvent facePerson = SerDes.getSerDes().deserializeFaceEvents( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getPatrul( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getArchive().save( patrul1, facePerson ) ) ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_CAR ) == 0 ) {
            CarEvent carEvents = SerDes.getSerDes().deserializeCarEvents ( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getPatrul( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getArchive().save( patrul1, carEvents ) ) ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_EVENT_BODY ) == 0 ) {
            EventBody eventBody = SerDes.getSerDes().deserializeEventBody( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getPatrul( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getArchive()
                                    .save( patrul1, eventBody ) ) ); }

        else { EventCar eventCar = SerDes.getSerDes().deserializeEventCar( request.getCard() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getPatrul( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> Archive.getArchive().save( patrul1, eventCar ) ) ); } }

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
                                        .writeToKafka( carTotalData ) ) ) ); }

    @MessageMapping ( value = "getViolationsInformationList" )
    public Mono< List< ViolationsInformation > > getViolationsInformationList ( String gosnumber ) { return Mono.just(
            CassandraDataControlForTasks
                    .getInstance()
                    .getViolationsInformationList( gosnumber ) ); }

    @MessageMapping ( value = "getWarningCarDetails" )
    public Mono< ApiResponseModel > getWarningCarDetails ( String gosnumber ) { return CassandraDataControlForTasks
            .getInstance()
            .getWarningCarDetails( gosnumber ); }

    @MessageMapping ( value = "getAllCarTotalData" )
    public Flux< CarTotalData > getAllCarTotalData () { return CassandraDataControlForTasks
            .getInstance()
            .getAllCarTotalData(); }

    @MessageMapping ( value = "removePatrulFromTask" )
    public Mono< ApiResponseModel > removePatrulFromTask ( UUID uuid ) {
        return CassandraDataControl
                .getInstance()
                .getPatrul( uuid )
                .filter( patrul -> patrul.getTaskTypes().compareTo( TaskTypes.FREE ) != 0 )
                .flatMap( patrul -> TaskInspector
                        .getInstance()
                        .removePatrulFromTask( patrul ) ); }

    @MessageMapping ( value = "addNewPatrulsToTask" )
    public Mono< ApiResponseModel > addNewPatrulsToTask ( CardRequest< ? > request ) {
        return switch ( request.getTaskType() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getCard102( request.getCard().toString() )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getPatrul( uuid ) )
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
                    .getEventFace( request.getCard().toString() )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getPatrul( uuid ) )
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
                    .getEventCar( request.getCard().toString() )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getPatrul( uuid ) )
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
                    .getEventBody( request.getCard().toString() )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getPatrul( uuid ) )
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
                    .getCarEvents( request.getCard().toString() )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getPatrul( uuid ) )
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
                    .getFaceEvents( request.getCard().toString() )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getPatrul( uuid ) )
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
                    .getSelfEmploymentTask( UUID.fromString( request.getCard().toString() ) )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .map( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getPatrul( uuid ) )
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
