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
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
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
    public Flux< ActiveTask > getListOfCards () {
        return CassandraDataControl
                .getInstance()
                .getAllEntities
                .apply( CassandraTables.TABLETS, CassandraTables.ACTIVE_TASK )
                .map( row -> super.deserialize( row.getString( "object" ), ActiveTask.class ) )
                .sequential()
                .publishOn( Schedulers.single() )
                .sort( Comparator.comparing( ActiveTask::getCreatedDate ).reversed() )
                .onErrorContinue( super::logging );
    }

    @MessageMapping ( value = "getCurrentActiveTask" ) // for Android
    public Mono< ApiResponseModel > getCurrentActiveTask ( final String token ) {
        return CassandraDataControl
            .getInstance()
            .getPatrulByUUID
            .apply( super.decode( token ) )
            .flatMap( patrul -> TaskInspector
                    .getInstance()
                    .getTaskData
                    .apply( patrul, TaskTypes.ACTIVE_TASK ) )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "linkCardToPatrul" )
    public Flux< ApiResponseModel > linkCardToPatrul ( final CardRequest< ? > request ) {
        final Optional< CardRequest > optional = Optional.of( request );
        if ( optional
                .filter( cardRequest -> cardRequest.getTaskType().compareTo( TaskTypes.CARD_102 ) == 0 )
                .isPresent() ) {
            final Card card = this.objectMapper.convertValue( request.getCard(), new TypeReference<>() {} );

            if ( !super.objectIsNotNull( card.getCreated_date() ) ) {
                card.setCreated_date( super.newDate() );
            }

            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( super.checkDifference( request.getPatruls().size() ) )
                    .runOn( Schedulers.parallel() )
                    .flatMap( s -> CassandraDataControl
                            .getInstance()
                            .getPatrulByUUID
                            .apply( s )
                            .flatMap( patrul -> super.function(
                                    Map.of( "message", card + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, ATTACHED, card )
                                            .getPatrulFIOData()
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.errorResponse() );
        }

        else if ( optional
                .filter( cardRequest -> cardRequest.getTaskType().compareTo( TaskTypes.FIND_FACE_CAR ) == 0 )
                .isPresent() ) {
            final CarEvent carEvents = this.objectMapper.convertValue( request.getCard(), new TypeReference<>() {} );

            if ( !super.objectIsNotNull( carEvents.getCreated_date() ) ) {
                carEvents.setCreated_date( super.newDate().toString() );
            }

            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( super.checkDifference( request.getPatruls().size() ) )
                    .runOn( Schedulers.parallel() )
                    .flatMap( s -> CassandraDataControl
                            .getInstance()
                            .getPatrulByUUID
                            .apply( s )
                            .flatMap( patrul -> super.function(
                                    Map.of( "message", carEvents + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, ATTACHED, carEvents )
                                            .getPatrulFIOData()
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.errorResponse() );
        }

        else if ( optional
                .filter( cardRequest -> cardRequest.getTaskType().compareTo( TaskTypes.FIND_FACE_PERSON ) == 0 )
                .isPresent() ) {
            final FaceEvent facePerson = this.objectMapper.convertValue( request.getCard(), new TypeReference<>() {} );

            if ( !super.objectIsNotNull( facePerson.getCreated_date() ) && facePerson.getCreated_date().isEmpty() ) {
                facePerson.setCreated_date( super.newDate().toString() );
            }

            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( super.checkDifference( request.getPatruls().size() ) )
                    .runOn( Schedulers.parallel() )
                    .flatMap( s -> CassandraDataControl
                            .getInstance()
                            .getPatrulByUUID
                            .apply( s )
                            .flatMap( patrul -> super.function(
                                    Map.of( "message", facePerson.getTaskCommonParams().getUuid() + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, ATTACHED, facePerson )
                                            .getPatrulFIOData()
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.errorResponse() );
        }

        else if ( optional
                .filter( cardRequest -> cardRequest.getTaskType().compareTo( TaskTypes.FIND_FACE_EVENT_FACE ) == 0 )
                .isPresent() ) {
            final EventFace eventFace = this.objectMapper.convertValue( request.getCard(), new TypeReference<>() {} );

            if ( !super.objectIsNotNull( eventFace.getCreated_date() ) ) {
                eventFace.setCreated_date( super.newDate() );
            }

            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( super.checkDifference( request.getPatruls().size() ) )
                    .runOn( Schedulers.parallel() )
                    .flatMap( s -> CassandraDataControl
                            .getInstance()
                            .getPatrulByUUID
                            .apply( s )
                            .flatMap( patrul1 -> super.function(
                                    Map.of( "message", eventFace + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul1, ATTACHED, eventFace )
                                            .getPatrulFIOData()
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.errorResponse() );
        }

        else if ( optional
                .filter( cardRequest -> cardRequest.getTaskType().compareTo( TaskTypes.FIND_FACE_EVENT_BODY ) == 0 )
                .isPresent() ) {
            final EventBody eventBody = this.objectMapper.convertValue( request.getCard(), new TypeReference<>() {} );

            if ( !super.objectIsNotNull( eventBody.getCreated_date() ) ) {
                eventBody.setCreated_date( super.newDate() );
            }

            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( super.checkDifference( request.getPatruls().size() ) )
                    .runOn( Schedulers.parallel() )
                    .flatMap( s -> CassandraDataControl
                            .getInstance()
                            .getPatrulByUUID
                            .apply( s )
                            .flatMap( patrul -> super.function(
                                    Map.of( "message", eventBody + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, ATTACHED, eventBody )
                                            .getPatrulFIOData()
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.errorResponse() );
        }

        else { final EventCar eventCar = this.objectMapper.convertValue( request.getCard(), new TypeReference<>() {} );

            if ( !super.objectIsNotNull( eventCar.getCreated_date() ) ) {
                eventCar.setCreated_date( super.newDate() );
            }

            return Flux.fromStream( request.getPatruls().stream() )
                    .parallel( super.checkDifference( request.getPatruls().size() ) )
                    .runOn( Schedulers.parallel() )
                    .flatMap( s -> CassandraDataControl
                            .getInstance()
                            .getPatrulByUUID
                            .apply( s )
                            .flatMap( patrul1 -> super.function(
                                    Map.of( "message", eventCar + " was linked to: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul1, ATTACHED, eventCar )
                                            .getPatrulFIOData()
                                            .getName() ) ) ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.errorResponse() );
        }
    }

    @MessageMapping ( value = "addNewWarningCar" )
    public Mono< ApiResponseModel > addNewWarningCar ( final CarTotalData carTotalData ) {
        return super.function(
                Map.of( "message", "Car was saved successfully",
                        "success", CassandraDataControlForTasks
                                .getInstance()
                                .saveCarTotalData
                                .apply( KafkaDataControl
                                        .getKafkaDataControl()
                                        .getWriteCarTotalDataToKafka()
                                        .apply( carTotalData ) ) ) )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "getActiveTaskForFront" )
    public Mono< ActiveTask > getActiveTaskForFront ( final TaskDetailsRequest taskDetailsRequest ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getActiveTask
                .apply( taskDetailsRequest );
    }

    @MessageMapping ( value = "getViolationsInformationList" )
    public Mono< List< ViolationsInformation > > getViolationsInformationList ( final String gosnumber ) {
            return super.convert(
                    CassandraDataControlForTasks
                            .getInstance()
                            .getViolationsInformationList
                            .apply( gosnumber ) )
                .onErrorContinue( super::logging );
    }

    @MessageMapping ( value = "getAllCarTotalData" )
    public Flux< CarTotalData > getAllCarTotalData () {
        return CassandraDataControl
            .getInstance()
            .getAllEntities
            .apply( CassandraTables.TABLETS, CassandraTables.CARTOTALDATA )
            .map( row -> super.deserialize( row.getString( "object" ), CarTotalData.class ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging );
    }

    @MessageMapping ( value = "removePatrulFromTask" )
    public Mono< ApiResponseModel > removePatrulFromTask ( final UUID uuid ) {
        return CassandraDataControl
                .getInstance()
                .getPatrulByUUID
                .apply( uuid )
                .filter( patrul -> patrul.getPatrulTaskInfo().getTaskTypes().isFree() )
                .flatMap( patrul -> TaskInspector
                        .getInstance()
                        .getTaskData
                        .apply( patrul, TaskTypes.FREE ) )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "getWarningCarDetails" )
    public Mono< ApiResponseModel > getWarningCarDetails ( final String gosnumber ) {
        return CassandraDataControlForTasks
            .getInstance()
            .getWarningCarDetails
            .apply( gosnumber )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "getDetailsOfTask" )
    public Mono< TaskDetails > getDetailsOfTask ( final TaskDetailsRequest request ) {
        return super.checkObject( request )
                ? CassandraDataControlForTasks
                .getInstance()
                .getTaskDetails
                .apply( request )
                : Mono.empty();
    }

    @MessageMapping ( value = "addNewPatrulsToTask" )
    public Mono< ApiResponseModel > addNewPatrulsToTask ( final CardRequest< ? > request ) {
        return switch ( request.getTaskType() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getTask
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString( "object" ), Card.class ) )
                    .flatMap( card -> {
                        super.analyze(
                                request.getPatruls(),
                                uuid -> {
                                    final Patrul patrul = Patrul.empty().generate(
                                            CassandraDataControl
                                                    .getInstance()
                                                    .getRowFromTabletsKeyspace(
                                                            CassandraTables.PATRULS,
                                                            "uuid",
                                                            uuid.toString()
                                                    )
                                    );

                                    TaskInspector
                                            .getInstance()
                                            .changeTaskStatus(
                                                    patrul,
                                                    com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                    card );
                                }
                        );

                        return super.function( Map.of( "message", request.getTaskType() + " has got new patrul" ) );
                    } );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getTask
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString( "object" ), EventFace.class ) )
                    .flatMap( eventFace -> {
                        super.analyze(
                                request.getPatruls(),
                                uuid -> {
                                    final Patrul patrul = Patrul.empty().generate(
                                            CassandraDataControl
                                                    .getInstance()
                                                    .getRowFromTabletsKeyspace(
                                                            CassandraTables.PATRULS,
                                                            "uuid",
                                                            uuid.toString()
                                                    )
                                    );

                                    TaskInspector
                                            .getInstance()
                                            .changeTaskStatus(
                                                    patrul,
                                                    com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                    eventFace );
                                }
                        );

                        return super.function( Map.of( "message", request.getTaskType() + " has got new patrul" ) );
                    } );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getTask
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString("object" ), EventCar.class ) )
                    .flatMap( eventCar -> {
                        super.analyze(
                                request.getPatruls(),
                                uuid -> {
                                    final Patrul patrul = Patrul.empty().generate(
                                            CassandraDataControl
                                                    .getInstance()
                                                    .getRowFromTabletsKeyspace(
                                                            CassandraTables.PATRULS,
                                                            "uuid",
                                                            uuid.toString()
                                                    )
                                    );

                                    TaskInspector
                                            .getInstance()
                                            .changeTaskStatus(
                                                    patrul,
                                                    com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                    eventCar );
                                }
                        );

                        return super.function( Map.of( "message", request.getTaskType() + " has got new patrul" ) );
                    } );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getTask
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString("object" ), EventBody.class ) )
                    .flatMap( eventBody -> {
                        super.analyze(
                                request.getPatruls(),
                                uuid -> {
                                    final Patrul patrul = Patrul.empty().generate(
                                            CassandraDataControl
                                                    .getInstance()
                                                    .getRowFromTabletsKeyspace(
                                                            CassandraTables.PATRULS,
                                                            "uuid",
                                                            uuid.toString()
                                                    )
                                    );

                                    TaskInspector
                                            .getInstance()
                                            .changeTaskStatus(
                                                    patrul,
                                                    com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                    eventBody );
                                }
                        );

                        return super.function( Map.of( "message", request.getTaskType() + " has got new patrul" ) );
                    } );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getTask
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString("object" ), CarEvent.class ) )
                    .flatMap( carEvent -> {
                        super.analyze(
                                request.getPatruls(),
                                uuid -> {
                                    final Patrul patrul = Patrul.empty().generate(
                                            CassandraDataControl
                                                    .getInstance()
                                                    .getRowFromTabletsKeyspace(
                                                            CassandraTables.PATRULS,
                                                            "uuid",
                                                            uuid.toString()
                                                    )
                                    );

                                    TaskInspector
                                            .getInstance()
                                            .changeTaskStatus(
                                                    patrul,
                                                    com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                    carEvent );
                                }
                        );

                        return super.function( Map.of( "message", request.getTaskType() + " has got new patrul" ) );
                    } );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getTask
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString("object" ), FaceEvent.class ) )
                    .flatMap( faceEvent -> {
                        super.analyze(
                                request.getPatruls(),
                                uuid -> {
                                    final Patrul patrul = Patrul.empty().generate(
                                            CassandraDataControl
                                                    .getInstance()
                                                    .getRowFromTabletsKeyspace(
                                                            CassandraTables.PATRULS,
                                                            "uuid",
                                                            uuid.toString()
                                                    )
                                    );

                                    TaskInspector
                                            .getInstance()
                                            .changeTaskStatus(
                                                    patrul,
                                                    com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                    faceEvent );
                                }
                        );

                        return super.function( Map.of( "message", request.getTaskType() + " has got new patrul" ) );
                    } );

            default -> CassandraDataControlForTasks
                    .getInstance()
                    .getTask
                    .apply( request.getCard().toString() )
                    .map( row -> super.deserialize( row.getString("object" ), SelfEmploymentTask.class ) )
                    .flatMap( selfEmploymentTask -> {
                        super.analyze(
                                request.getPatruls(),
                                uuid -> {
                                    final Patrul patrul = Patrul.empty().generate(
                                            CassandraDataControl
                                                    .getInstance()
                                                    .getRowFromTabletsKeyspace(
                                                            CassandraTables.PATRULS,
                                                            "uuid",
                                                            uuid.toString()
                                                    )
                                    );

                                    TaskInspector
                                            .getInstance()
                                            .changeTaskStatus(
                                                    patrul,
                                                    com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                    selfEmploymentTask );
                                }
                        );

                        return super.function( Map.of( "message", request.getTaskType() + " has got new patrul" ) );
                    } );
        };
    }

    @MessageMapping ( value = "getTaskTimingStatistics" )
    public Mono< TaskTimingStatisticsList > getTaskTimingStatistics ( final TaskTimingRequest request ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getTaskTimingStatistics
                .apply( request )
                .onErrorContinue( super::logging );
    }
}
