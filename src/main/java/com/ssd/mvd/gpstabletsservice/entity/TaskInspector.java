package com.ssd.mvd.gpstabletsservice.entity;

import lombok.Data;
import java.util.Date;
import java.util.UUID;
import reactor.core.publisher.Mono;

import com.ssd.mvd.gpstabletsservice.database.*;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.task.card.CardDetails;
import com.ssd.mvd.gpstabletsservice.task.card.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;

@Data
public final class TaskInspector {
    private static TaskInspector taskInspector;

    public static TaskInspector getInstance() { return taskInspector != null ? taskInspector : new TaskInspector(); }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, Card card ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL -> {
                patrul.setTaskId( null );
                patrul.setStatus( FREE );
                card.getPatruls().remove( patrul.getPassportNumber() ); }
            case ATTACHED -> {
                patrul.setTaskTypes( TaskTypes.CARD_102 );
                patrul.setTaskId( card.getCardId().toString() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( card.getLatitude() );
                patrul.setLongitudeOfTask( card.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case FINISHED -> {
                card.getPatrulStatuses().get( patrul.getPassportNumber() )
                        .setTotalTimeConsumption( TimeInspector.getInspector().getTimeDifference( patrul.getTaskDate().toInstant() ) );
                patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), "card" );
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setTaskDate( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null );
            } case ARRIVED -> card.getPatrulStatuses().putIfAbsent( patrul.getPassportNumber(), PatrulStatus.builder()
                    .patrul( patrul )
                    .inTime( patrul.check() )
                    .totalTimeConsumption( TimeInspector.getInspector().getTimeDifference( patrul.getTaskDate().toInstant() ) ).build() );
        } card.getPatruls().put( patrul.getPassportNumber(), patrul );
        RedisDataControl.getRedis().addValue( card.getCardId().toString(), new ActiveTask( card ) );
        RedisDataControl.getRedis().update( card );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EventCar eventFace ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL -> {
                patrul.setTaskId( null );
                patrul.setStatus( Status.FREE );
                eventFace.getPatruls().remove( patrul.getPassportNumber() ); }
            case ATTACHED -> {
                patrul.setTaskId( eventFace.getId() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( eventFace.getLatitude() );
                patrul.setTaskTypes( TaskTypes.FIND_FACE_EVENT_CAR );
                patrul.setLongitudeOfTask( eventFace.getLongitude() );
                eventFace.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case ACCEPTED, ARRIVED -> {
                patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
                patrul.setTaskId( eventFace.getId() );
                eventFace.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case FINISHED -> {
                patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), TaskTypes.FIND_FACE_EVENT_CAR.name() );
                eventFace.getPatruls().put( patrul.getPassportNumber(), patrul );
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( Status.FREE );
                patrul.setTaskDate( null );
                patrul.setTaskId( null ); }
        } RedisDataControl.getRedis().addValue( eventFace.getId(), new ActiveTask( eventFace ) );
        CassandraDataControl.getInstance().addValue( eventFace );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EventFace eventFace ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL -> {
                patrul.setTaskId( null );
                patrul.setStatus( Status.FREE );
                eventFace.getPatruls().remove( patrul.getPassportNumber() ); }
            case ATTACHED -> {
                patrul.setTaskId( eventFace.getId() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( eventFace.getLatitude() );
                patrul.setLongitudeOfTask( eventFace.getLongitude() );
                patrul.setTaskTypes( TaskTypes.FIND_FACE_EVENT_FACE );
                eventFace.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case ACCEPTED, ARRIVED -> {
                patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
                patrul.setTaskId( eventFace.getId() );
                eventFace.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case FINISHED -> {
                patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), TaskTypes.FIND_FACE_EVENT_FACE.name() );
                eventFace.getPatruls().put( patrul.getPassportNumber(), patrul );
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( Status.FREE );
                patrul.setTaskDate( null );
                patrul.setTaskId( null ); }
        } RedisDataControl.getRedis().addValue( eventFace.getId(), new ActiveTask( eventFace ) );
        CassandraDataControl.getInstance().addValue( eventFace );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EventBody eventFace ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL -> {
                patrul.setTaskId( null );
                patrul.setStatus( Status.FREE );
                eventFace.getPatruls().remove( patrul.getPassportNumber() ); }
            case ATTACHED -> {
                patrul.setTaskId( eventFace.getId() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( eventFace.getLatitude() );
                patrul.setLongitudeOfTask( eventFace.getLongitude() );
                patrul.setTaskTypes( TaskTypes.FIND_FACE_EVENT_BODY );
                eventFace.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case ACCEPTED, ARRIVED -> {
                patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
                patrul.setTaskId( eventFace.getId() );
                eventFace.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case FINISHED -> {
                patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), TaskTypes.FIND_FACE_EVENT_BODY.name() );
                eventFace.getPatruls().put( patrul.getPassportNumber(), patrul );
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( Status.FREE );
                patrul.setTaskDate( null );
                patrul.setTaskId( null ); }
        } RedisDataControl.getRedis().addValue( eventFace.getId(), new ActiveTask( eventFace ) );
        CassandraDataControl.getInstance().addValue( eventFace );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, SelfEmploymentTask selfEmploymentTask ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL -> {
                patrul.setTaskId( null );
                patrul.setStatus( Status.FREE );
                selfEmploymentTask.getPatruls().remove( patrul.getPassportNumber() ); }
            case ATTACHED -> {
                patrul.setTaskTypes( TaskTypes.SELF_EMPLOYMENT );
                patrul.setTaskId( selfEmploymentTask.getUuid().toString() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( selfEmploymentTask.getLatOfAccident() );
                patrul.setLongitudeOfTask( selfEmploymentTask.getLanOfAccident() );
                selfEmploymentTask.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case ACCEPTED, ARRIVED -> {
                patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
                patrul.setTaskId( selfEmploymentTask.getUuid().toString() );
                selfEmploymentTask.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case FINISHED -> {
                patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), "selfEmployment" );
                selfEmploymentTask.getPatruls().put( patrul.getPassportNumber(), patrul );
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( Status.FREE );
                patrul.setTaskDate( null );
                patrul.setTaskId( null ); }
        } RedisDataControl.getRedis().addValue( selfEmploymentTask.getUuid().toString(), new ActiveTask( selfEmploymentTask ) );
        CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) );
        return patrul; }

    public Mono< ApiResponseModel > changeTaskStatus ( Patrul patrul, Status status ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> RedisDataControl.getRedis().getCard( Long.parseLong( patrul.getTaskId() ) )
                    .flatMap( card -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, card )
                                            .getPassportNumber() + " changed his status task to: " + status  ).code( 200 ).build() ).build() ) );

            case SELF_EMPLOYMENT -> Archive.getAchieve().get( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, selfEmploymentTask )
                                            .getPassportNumber() + " changed his status task to: " + status ).code( 200 ).build() ).build() ) );

            case FIND_FACE_EVENT_CAR -> Archive.getAchieve().getEventCar( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, eventCar )
                                            .getPassportNumber() + " changed his status task to: " + status  ).code( 200 ).build() ).build() ) );

            case FIND_FACE_EVENT_FACE -> Archive.getAchieve().getEventFace( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, eventFace )
                                            .getPassportNumber() + " changed his status task to: " + status  ).code( 200 ).build() ).build() ) );

            default -> Archive.getAchieve().getEventBody( patrul.getTaskId() )
                    .flatMap( eventBody -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, eventBody )
                                            .getPassportNumber() + " changed his status task to: " + status  ).code( 200 ).build() ).build() ) ); }; }

    public Mono< ApiResponseModel > getCurrentActiveTask ( Patrul patrul ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> RedisDataControl.getRedis().getCard( Long.parseLong( patrul.getTaskId() ) )
                    .flatMap( card -> Mono.just( ApiResponseModel.builder().data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new ActiveTask( card, patrul.getStatus() ) )
                                    .type( TaskTypes.CARD_102.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.CARD_102.name() + " Task" ).build() ).success( true ).build() ) );

            case SELF_EMPLOYMENT -> Archive.getAchieve().get( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new ActiveTask( selfEmploymentTask, patrul.getStatus() ) )
                                    .type( TaskTypes.SELF_EMPLOYMENT.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.SELF_EMPLOYMENT.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            case FIND_FACE_EVENT_CAR -> Archive.getAchieve().getEventCar( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new ActiveTask( eventCar, patrul.getStatus() ) )
                                    .type( TaskTypes.FIND_FACE_EVENT_CAR.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.FIND_FACE_EVENT_CAR.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            case FIND_FACE_EVENT_BODY -> Archive.getAchieve().getEventBody( patrul.getTaskId() )
                    .flatMap( eventBody -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new ActiveTask( eventBody, patrul.getStatus() ) )
                                    .type( TaskTypes.FIND_FACE_EVENT_BODY.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.FIND_FACE_EVENT_BODY.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            case FIND_FACE_EVENT_FACE -> Archive.getAchieve().getEventFace( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new ActiveTask( eventFace, patrul.getStatus() ) )
                                    .type( TaskTypes.FIND_FACE_EVENT_FACE.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.FIND_FACE_EVENT_FACE.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            default -> Mono.just( ApiResponseModel.builder().success( false )
                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 201 )
                            .message( "U have no task, so u can do smth else, my darling )))" ).build() ) .build() ); }; }

    public Mono< ApiResponseModel > saveReportForTask ( Patrul patrul, ReportForCard reportForCard ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> RedisDataControl.getRedis().getCard( Long.parseLong( patrul.getTaskId() ) ).flatMap( card -> {
                card.getReportForCardList().add( reportForCard );
                return RedisDataControl.getRedis().update( TaskInspector.getInstance().changeTaskStatus( patrul, Status.FINISHED, card ) )
                        .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                .success( true )
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Report from: " + patrul.getName() + " was saved" ).code( 200 ).build() ).build() ) ); } );

            case SELF_EMPLOYMENT -> Archive.getAchieve().get( UUID.fromString( patrul.getTaskId() ) ).flatMap( selfEmploymentTask -> {
                selfEmploymentTask.getReportForCards().add( reportForCard );
                return RedisDataControl.getRedis().update( TaskInspector.getInstance().changeTaskStatus( patrul, Status.FINISHED, selfEmploymentTask ) )
                        .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Report from: " + patrul.getName() + " was saved" ).code( 200 ).build() ).build() ) ); } );

            case FIND_FACE_EVENT_BODY -> Archive.getAchieve().getEventBody( patrul.getTaskId() ).flatMap( eventBody -> {
                eventBody.getReportForCardList().add( reportForCard );
                return RedisDataControl.getRedis().update( TaskInspector.getInstance().changeTaskStatus( patrul, Status.FINISHED, eventBody ) )
                        .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Report from: " + patrul.getName() + " was saved" ).code( 200 ).build() ).build() ) ); } );

            case FIND_FACE_EVENT_FACE -> Archive.getAchieve().getEventFace( patrul.getTaskId() ).flatMap( eventFace -> {
                eventFace.getReportForCardList().add( reportForCard );
                return RedisDataControl.getRedis().update( TaskInspector.getInstance().changeTaskStatus( patrul, Status.FINISHED, eventFace ) )
                        .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Report from: " + patrul.getName() + " was saved" ).code( 200 ).build() ).build() ) ); } );

            default -> Archive.getAchieve().getEventCar( patrul.getTaskId() ).flatMap( eventFace -> {
                eventFace.getReportForCardList().add( reportForCard );
                return RedisDataControl.getRedis().update( TaskInspector.getInstance().changeTaskStatus( patrul, Status.FINISHED, eventFace ) )
                        .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Report from: " + patrul.getName() + " was saved" ).code( 200 ).build() ).build() ) ); } ); }; }

    public Mono< ApiResponseModel > getTaskDetails ( Patrul patrul ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> RedisDataControl.getRedis().getCard( Long.parseLong( patrul.getTaskId() ) )
                    .flatMap( card -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new CardDetails( card, "ru" ) ).build() )
                            .build() ) );

            case FIND_FACE_EVENT_BODY -> Archive.getAchieve().getEventBody( patrul.getTaskId() )
                    .flatMap( eventBody -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 ).message( "Your task details" ).build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new CardDetails( eventBody ) ).build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_EVENT_FACE -> Archive.getAchieve().getEventFace( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 ).message( "Your task details" ).build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new CardDetails( eventFace ) ).build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_EVENT_CAR -> Archive.getAchieve().getEventCar( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 ).message( "Your task details" ).build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new CardDetails( eventCar ) ).build() ) // TO-DO
                            .build() ) );

            default -> Archive.getAchieve().get( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 ).message( "Your task details" ).build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new CardDetails( selfEmploymentTask, "ru", patrul.getPassportNumber() ) ).build() )
                            .build() ) ); }; }
}
