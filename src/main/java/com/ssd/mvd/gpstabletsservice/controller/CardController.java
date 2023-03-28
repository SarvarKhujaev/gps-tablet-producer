package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.request.TaskTimingRequest;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskInspector;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.task.card.*;
import com.ssd.mvd.gpstabletsservice.database.*;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.*;

@RestController
public class CardController extends SerDes {
    @MessageMapping ( value = "getListOfCards" )
    public Flux< ActiveTask > getListOfCards () { return CassandraDataControl
            .getInstance()
            .getGetAllEntities()
            .apply( CassandraTables.TABLETS, CassandraTables.ACTIVE_TASK )
            .map( row -> (ActiveTask) super.getDeserialize().apply(
                    row.getString( "object" ), TaskTypes.ACTIVE_TASK ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .sort( Comparator.comparing( ActiveTask::getCreatedDate ).reversed() )
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "getCurrentActiveTask" ) // for Android
    public Mono< ApiResponseModel > getCurrentActiveTask ( String token ) { return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( CassandraDataControl
                    .getInstance()
                    .getDecode()
                    .apply( token ) )
            .flatMap( patrul -> TaskInspector
                    .getInstance()
                    .getGetCurrentActiveTask()
                    .apply( patrul ) )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "linkCardToPatrul" )
    public Flux< ApiResponseModel > linkCardToPatrul ( CardRequest< ? > request ) {
        if ( request.getTaskType().compareTo( TaskTypes.CARD_102 ) == 0 ) {
            Card card = (Card) super.getDeserializeWithJackson().apply( request.getCard() );
            card.setUuid( UUID.randomUUID() );

            if ( card.getCreated_date() == null ) card.setCreated_date( new Date() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( request.getPatruls().size() )
                    .runOn( Schedulers.parallel() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> super.getFunction()
                                    .apply( Map.of( "message", card + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul1, ATTACHED, card )
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.getErrorResponse().get() ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_CAR ) == 0 ) {
            CarEvent carEvents = (CarEvent) super.getDeserializeWithJackson().apply( request.getCard() );
            carEvents.setUuid( UUID.randomUUID() );
            if ( carEvents.getCreated_date() == null ) carEvents.setCreated_date( new Date().toString() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( request.getPatruls().size() )
                    .runOn( Schedulers.parallel() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> super.getFunction()
                                    .apply( Map.of( "message", carEvents + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul1, ATTACHED, carEvents )
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.getErrorResponse().get() ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_PERSON ) == 0 ) {
            FaceEvent facePerson = (FaceEvent) super.getDeserializeWithJackson().apply( request.getCard() );
            facePerson.setUuid( UUID.randomUUID() );
            if ( facePerson.getCreated_date() == null && facePerson.getCreated_date().isEmpty() )
                facePerson.setCreated_date( new Date().toString() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( request.getPatruls().size() )
                    .runOn( Schedulers.parallel() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> super.getFunction()
                                    .apply( Map.of( "message", facePerson + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul1, ATTACHED, facePerson )
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.getErrorResponse().get() ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_EVENT_FACE ) == 0 ) {
            EventFace eventFace = (EventFace) super.getDeserializeWithJackson().apply( request.getCard() );
            eventFace.setUuid( UUID.randomUUID() );
            if ( eventFace.getCreated_date() == null ) eventFace.setCreated_date( new Date() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( request.getPatruls().size() )
                    .runOn( Schedulers.parallel() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> super.getFunction()
                                    .apply( Map.of( "message", eventFace + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul1, ATTACHED, eventFace )
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.getErrorResponse().get() ); }

        else if ( request.getTaskType().compareTo( TaskTypes.FIND_FACE_EVENT_BODY ) == 0 ) {
            EventBody eventBody = (EventBody) super.getDeserializeWithJackson().apply( request.getCard() );
            eventBody.setUuid( UUID.randomUUID() );
            if ( eventBody.getCreated_date() == null ) eventBody.setCreated_date( new Date() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( request.getPatruls().size() )
                    .runOn( Schedulers.parallel() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> super.getFunction()
                                    .apply( Map.of( "message", eventBody + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul1, ATTACHED, eventBody )
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.getErrorResponse().get() ); }

        else { EventCar eventCar = (EventCar) super.getDeserializeWithJackson().apply( request.getCard() );
            eventCar.setUuid( UUID.randomUUID() );
            if ( eventCar.getCreated_date() == null ) eventCar.setCreated_date( new Date() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( request.getPatruls().size() )
                    .runOn( Schedulers.parallel() )
                    .map( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s ) )
                    .flatMap( patrul -> patrul
                            .flatMap( patrul1 -> super.getFunction()
                                    .apply( Map.of( "message", eventCar + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul1, ATTACHED, eventCar )
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.getErrorResponse().get() ); } }

    @MessageMapping ( value = "addNewWarningCar" )
    public Mono< ApiResponseModel > addNewWarningCar ( CarTotalData carTotalData ) {
        return super.getFunction().apply(
                Map.of( "message", "Car was saved successfully",
                        "success", CassandraDataControlForTasks
                                .getInstance()
                                .getSaveCarTotalData()
                                .apply( KafkaDataControl
                                        .getInstance()
                                        .getWriteCarTotalDataToKafka()
                                        .apply( carTotalData ) ) ) )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "getActiveTaskForFront" )
    public Mono< ActiveTask > getActiveTaskForFront ( TaskDetailsRequest taskDetailsRequest ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getGetActiveTask()
                .apply( taskDetailsRequest ); }

    @MessageMapping ( value = "getViolationsInformationList" )
    public Mono< List< ViolationsInformation > > getViolationsInformationList ( String gosnumber ) { return Mono.just(
            CassandraDataControlForTasks
                    .getInstance()
                    .getGetViolationsInformationList()
                    .apply( gosnumber ) )
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "getAllCarTotalData" )
    public Flux< CarTotalData > getAllCarTotalData () { return CassandraDataControl
            .getInstance()
            .getGetAllEntities()
            .apply( CassandraTables.TABLETS, CassandraTables.CARTOTALDATA )
            .map( row -> (CarTotalData) super.getDeserialize().apply(
                    row.getString( "object" ), TaskTypes.ESCORT ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "removePatrulFromTask" )
    public Mono< ApiResponseModel > removePatrulFromTask ( UUID uuid ) {
        return CassandraDataControl
                .getInstance()
                .getGetPatrulByUUID()
                .apply( uuid )
                .filter( patrul -> patrul.getTaskTypes().compareTo( TaskTypes.FREE ) != 0 )
                .flatMap( patrul -> TaskInspector
                        .getInstance()
                        .getRemovePatrulFromTask()
                        .apply( patrul ) )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "getWarningCarDetails" )
    public Mono< ApiResponseModel > getWarningCarDetails ( String gosnumber ) { return CassandraDataControlForTasks
            .getInstance()
            .getGetWarningCarDetails()
            .apply( gosnumber )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "getDetailsOfTask" )
    public Mono< TaskDetails > getDetailsOfTask ( TaskDetailsRequest request ) { return CassandraDataControlForTasks
            .getInstance()
            .getGetTaskDetails()
            .apply( request ); }

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
                        return super.getFunction().apply(
                                Map.of( "message", request.getCard()
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
                        return super.getFunction().apply(
                                Map.of( "message", request.getCard()
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
                        return super.getFunction().apply(
                                Map.of( "message", request.getCard()
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
                        return super.getFunction().apply(
                                Map.of( "message", request.getCard()
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
                        return super.getFunction().apply(
                                Map.of( "message", request.getCard()
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
                        return super.getFunction().apply(
                                Map.of( "message", request.getCard()
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
                        return super.getFunction().apply(
                                Map.of( "message", request.getCard()
                                        + " has got new patrul" ) ); } ); }; }

    @MessageMapping ( value = "getTaskTimingStatistics" )
    public Mono< TaskTimingStatisticsList > getTaskTimingStatistics ( TaskTimingRequest request ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getGetTaskTimingStatistics()
                .apply( request )
                .onErrorContinue( super::logging ); }
}
