package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.TaskTimingStatisticsList;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.entity.responseForAndroid.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.TaskDetails;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.request.TaskDetailsRequest;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.request.TaskTimingRequest;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskInspector;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.SerDes;
import com.ssd.mvd.gpstabletsservice.request.CardRequest;
import com.ssd.mvd.gpstabletsservice.task.card.*;
import com.ssd.mvd.gpstabletsservice.database.*;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.*;

@RestController
public final class CardController extends SerDes {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MessageMapping ( value = "getListOfCards" )
    public Flux< ActiveTask > getListOfCards () { return CassandraDataControl
            .getInstance()
            .getGetAllEntities()
            .apply( CassandraTables.TABLETS, CassandraTables.ACTIVE_TASK )
            .map( row -> super.deserialize( row.getString( "object" ), ActiveTask.class ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .sort( Comparator.comparing( ActiveTask::getCreatedDate ).reversed() )
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "getCurrentActiveTask" ) // for Android
    public Mono< ApiResponseModel > getCurrentActiveTask ( final String token ) {
        return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( super.getDecode().apply( token ) )
            .flatMap( patrul -> TaskInspector
                    .getInstance()
                    .getTaskData
                    .apply( patrul, TaskTypes.ACTIVE_TASK ) )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "linkCardToPatrul" )
    public Flux< ApiResponseModel > linkCardToPatrul ( final CardRequest< ? > request ) {
        final Optional< CardRequest > optional = Optional.of( request );
        if ( optional
                .filter( cardRequest -> cardRequest.getTaskType().compareTo( TaskTypes.CARD_102 ) == 0 )
                .isPresent() ) {
            final Card card = this.objectMapper.convertValue( request.getCard(), new TypeReference<>() {} );
            card.setUuid( UUID.randomUUID() );

            if ( card.getCreated_date() == null ) card.setCreated_date( new Date() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( super.checkDifference.apply( request.getPatruls().size() ) )
                    .runOn( Schedulers.parallel() )
                    .flatMap( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s )
                            .flatMap( patrul -> super.getFunction().apply(
                                    Map.of( "message", card + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, ATTACHED, card )
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.getErrorResponse().get() ); }

        else if ( optional
                .filter( cardRequest -> cardRequest.getTaskType().compareTo( TaskTypes.FIND_FACE_CAR ) == 0 )
                .isPresent() ) {
            final CarEvent carEvents = this.objectMapper.convertValue( request.getCard(), new TypeReference<>() {} );
            carEvents.setUuid( UUID.randomUUID() );
            if ( carEvents.getCreated_date() == null ) carEvents.setCreated_date( new Date().toString() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( super.checkDifference.apply( request.getPatruls().size() ) )
                    .runOn( Schedulers.parallel() )
                    .flatMap( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s )
                            .flatMap( patrul -> super.getFunction().apply(
                                    Map.of( "message", carEvents + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, ATTACHED, carEvents )
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.getErrorResponse().get() ); }

        else if ( optional
                .filter( cardRequest -> cardRequest.getTaskType().compareTo( TaskTypes.FIND_FACE_PERSON ) == 0 )
                .isPresent() ) {
            final FaceEvent facePerson = this.objectMapper.convertValue( request.getCard(), new TypeReference<>() {} );
            facePerson.setUuid( UUID.randomUUID() );
            if ( facePerson.getCreated_date() == null && facePerson.getCreated_date().isEmpty() )
                facePerson.setCreated_date( new Date().toString() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( super.checkDifference.apply( request.getPatruls().size() ) )
                    .runOn( Schedulers.parallel() )
                    .flatMap( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s )
                            .flatMap( patrul -> super.getFunction().apply(
                                    Map.of( "message", facePerson.getUUID() + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, ATTACHED, facePerson )
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.getErrorResponse().get() ); }

        else if ( optional
                .filter( cardRequest -> cardRequest.getTaskType().compareTo( TaskTypes.FIND_FACE_EVENT_FACE ) == 0 )
                .isPresent() ) {
            final EventFace eventFace = this.objectMapper.convertValue( request.getCard(), new TypeReference<>() {} );
            eventFace.setUuid( UUID.randomUUID() );
            if ( eventFace.getCreated_date() == null ) eventFace.setCreated_date( new Date() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( super.checkDifference.apply( request.getPatruls().size() ) )
                    .runOn( Schedulers.parallel() )
                    .flatMap( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s )
                            .flatMap( patrul1 -> super.getFunction().apply(
                                    Map.of( "message", eventFace + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul1, ATTACHED, eventFace )
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.getErrorResponse().get() ); }

        else if ( optional
                .filter( cardRequest -> cardRequest.getTaskType().compareTo( TaskTypes.FIND_FACE_EVENT_BODY ) == 0 )
                .isPresent() ) {
            final EventBody eventBody = this.objectMapper.convertValue( request.getCard(), new TypeReference<>() {} );
            eventBody.setUuid( UUID.randomUUID() );
            if ( eventBody.getCreated_date() == null ) eventBody.setCreated_date( new Date() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( super.checkDifference.apply( request.getPatruls().size() ) )
                    .runOn( Schedulers.parallel() )
                    .flatMap( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s )
                            .flatMap( patrul -> super.getFunction().apply(
                                    Map.of( "message", eventBody + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, ATTACHED, eventBody )
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.getErrorResponse().get() ); }

        else { final EventCar eventCar = this.objectMapper.convertValue( request.getCard(), new TypeReference<>() {} );
            eventCar.setUuid( UUID.randomUUID() );
            if ( eventCar.getCreated_date() == null ) eventCar.setCreated_date( new Date() );
            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( super.checkDifference.apply( request.getPatruls().size() ) )
                    .runOn( Schedulers.parallel() )
                    .flatMap( s -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( s )
                            .flatMap( patrul1 -> super.getFunction().apply(
                                    Map.of( "message", eventCar + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul1, ATTACHED, eventCar )
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.getErrorResponse().get() ); } }

    @MessageMapping ( value = "addNewWarningCar" )
    public Mono< ApiResponseModel > addNewWarningCar ( final CarTotalData carTotalData ) {
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
    public Mono< ActiveTask > getActiveTaskForFront ( final TaskDetailsRequest taskDetailsRequest ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getGetActiveTask()
                .apply( taskDetailsRequest ); }

    @MessageMapping ( value = "getViolationsInformationList" )
    public Mono< List< ViolationsInformation > > getViolationsInformationList ( final String gosnumber ) { return Mono.just(
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
            .map( row -> super.deserialize( row.getString( "object" ), CarTotalData.class ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "removePatrulFromTask" )
    public Mono< ApiResponseModel > removePatrulFromTask ( final UUID uuid ) {
        return CassandraDataControl
                .getInstance()
                .getGetPatrulByUUID()
                .apply( uuid )
                .filter( patrul -> patrul.getTaskTypes().compareTo( TaskTypes.FREE ) != 0 )
                .flatMap( patrul -> TaskInspector
                        .getInstance()
                        .getTaskData
                        .apply( patrul, TaskTypes.FREE ) )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "getWarningCarDetails" )
    public Mono< ApiResponseModel > getWarningCarDetails ( final String gosnumber ) { return CassandraDataControlForTasks
            .getInstance()
            .getGetWarningCarDetails()
            .apply( gosnumber )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "getDetailsOfTask" )
    public Mono< TaskDetails > getDetailsOfTask ( final TaskDetailsRequest request ) {
        return super.checkParam.test( request )
                && super.checkParam.test( request.getId() )
                && super.checkParam.test( request.getTaskTypes() )
                && super.checkParam.test( request.getPatrulUUID() )
                ? CassandraDataControlForTasks
                .getInstance()
                .getGetTaskDetails()
                .apply( request )
                : Mono.empty(); }

    @MessageMapping ( value = "addNewPatrulsToTask" )
    public Mono< ApiResponseModel > addNewPatrulsToTask ( final CardRequest< ? > request ) {
        return switch ( request.getTaskType() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetTask()
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString( "object" ), Card.class ) )
                    .flatMap( card -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .parallel( super.checkDifference.apply( request.getPatruls().size() ) )
                                .runOn( Schedulers.parallel() )
                                .flatMap( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .map( patrul -> TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul,
                                                com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                card ) )
                                .sequential()
                                .publishOn( Schedulers.single() )
                                .subscribe();
                        return super.getFunction().apply( Map.of( "message", request.getCard() + " has got new patrul" ) ); } );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetTask()
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString( "object" ), EventFace.class ) )
                    .flatMap( eventFace -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .parallel( super.checkDifference.apply( request.getPatruls().size() ) )
                                .runOn( Schedulers.parallel() )
                                .flatMap( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .map( patrul -> TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul,
                                                com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                eventFace ) )
                                .sequential()
                                .publishOn( Schedulers.single() )
                                .subscribe();
                        return super.getFunction().apply( Map.of( "message", request.getCard() + " has got new patrul" ) ); } );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetTask()
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString("object" ), EventCar.class ) )
                    .flatMap( eventCar -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .parallel( super.checkDifference.apply( request.getPatruls().size() ) )
                                .runOn( Schedulers.parallel() )
                                .flatMap( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .map( patrul -> TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul,
                                                com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                eventCar ) )
                                .sequential()
                                .publishOn( Schedulers.single() )
                                .subscribe();
                        return super.getFunction().apply( Map.of( "message", request.getCard() + " has got new patrul" ) ); } );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetTask()
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString("object" ), EventBody.class ) )
                    .flatMap( eventBody -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .parallel( super.checkDifference.apply( request.getPatruls().size() ) )
                                .runOn( Schedulers.parallel() )
                                .flatMap( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .map( patrul -> TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul,
                                                com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                eventBody ) )
                                .sequential()
                                .publishOn( Schedulers.single() )
                                .subscribe();
                        return super.getFunction().apply( Map.of( "message", request.getCard() + " has got new patrul" ) ); } );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetTask()
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString("object" ), CarEvent.class ) )
                    .flatMap( carEvent -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .parallel( super.checkDifference.apply( request.getPatruls().size() ) )
                                .runOn( Schedulers.parallel() )
                                .flatMap( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .map( patrul -> TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul,
                                                com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                carEvent ) )
                                .sequential()
                                .publishOn( Schedulers.single() )
                                .subscribe();
                        return super.getFunction().apply( Map.of( "message", request.getCard() + " has got new patrul" ) ); } );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetTask()
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString("object" ), FaceEvent.class ) )
                    .flatMap( faceEvent -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .parallel( super.checkDifference.apply( request.getPatruls().size() ) )
                                .runOn( Schedulers.parallel() )
                                .flatMap( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .map( patrul -> TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul,
                                                com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                faceEvent ) )
                                .sequential()
                                .publishOn( Schedulers.single() )
                                .subscribe();
                        return super.getFunction().apply( Map.of( "message", request.getCard() + " has got new patrul" ) ); } );

            default -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetTask()
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString("object" ), SelfEmploymentTask.class ) )
                    .flatMap( selfEmploymentTask -> {
                        Flux.fromStream( request.getPatruls().stream() )
                                .parallel( super.checkDifference.apply( request.getPatruls().size() ) )
                                .runOn( Schedulers.parallel() )
                                .flatMap( uuid -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid ) )
                                .map( patrul -> TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul,
                                                com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                selfEmploymentTask ) )
                                .sequential()
                                .publishOn( Schedulers.single() )
                                .subscribe();
                        return super.getFunction().apply( Map.of( "message", request.getCard() + " has got new patrul" ) ); } ); }; }

    @MessageMapping ( value = "getTaskTimingStatistics" )
    public Mono< TaskTimingStatisticsList > getTaskTimingStatistics ( final TaskTimingRequest request ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getGetTaskTimingStatistics()
                .apply( request )
                .onErrorContinue( super::logging ); }
}
