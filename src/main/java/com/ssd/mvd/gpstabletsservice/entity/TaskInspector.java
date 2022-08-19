package com.ssd.mvd.gpstabletsservice.entity;

import lombok.Data;
import java.util.Date;
import java.util.UUID;
import reactor.core.publisher.Mono;

import com.ssd.mvd.gpstabletsservice.database.*;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.tuple.EscortTuple;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.task.card.CardDetails;
import com.ssd.mvd.gpstabletsservice.task.card.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import static com.ssd.mvd.gpstabletsservice.constants.Status.FREE;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.*;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FIND_FACE_CAR;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvents;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvents;

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
                patrul.setLatitudeOfTask( card.getLatitude() );
                patrul.setTaskId( card.getCardId().toString() ); // saving card id into patrul object
                patrul.setLongitudeOfTask( card.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case FINISHED -> {
                card.getPatrulStatuses().get( patrul.getPassportNumber() )
                        .setTotalTimeConsumption( TimeInspector.getInspector().getTimeDifference( patrul.getTaskDate().toInstant() ) );
                patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), "card" );
                patrul.setTaskTypes( TaskTypes.FREE );
                if ( card.getPatruls().size() == card.getReportForCardList().size() ) {
                    card.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( card.getCardId() );
                    RedisDataControl.getRedis().remove( card.getCardId().toString() );
                    KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( card ) ); }
                patrul.setTaskDate( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null );
            } case ARRIVED -> card.getPatrulStatuses().putIfAbsent( patrul.getPassportNumber(), PatrulStatus.builder()
                    .patrul( patrul )
                    .inTime( patrul.check() )
                    .totalTimeConsumption( TimeInspector.getInspector().getTimeDifference( patrul.getTaskDate().toInstant() ) ).build() );
        } card.getPatruls().put( patrul.getPassportNumber(), patrul );
        RedisDataControl.getRedis().addValue( card.getCardId().toString(), new ActiveTask( card ) ).subscribe();
        RedisDataControl.getRedis().update( card );
        KafkaDataControl.getInstance()
                .writeToKafka(
                        CassandraDataControl
                                .getInstance().addValue(
                                        Notification.builder()
                                                .type( CARD_102.name() )
                                                .uuid( UUID.randomUUID() )
                                                .status( patrul.getStatus() )
                                                .id( card.getCardId().toString() )
                                                .carNumber( patrul.getCarNumber() )
                                                .taskTypes( patrul.getTaskTypes() )
                                                .policeType( patrul.getPoliceType() )
                                                .latitudeOfTask( card.getLatitude() )
                                                .notificationWasCreated( new Date() )
                                                .longitudeOfTask( card.getLongitude() )
                                                .passportSeries( patrul.getPassportNumber() )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .address( card.getAddress() != null ? card.getAddress() : "unknown" )
                                                .title( "My dear: " + patrul.getName()
                                                        + " you got 102 card task," +
                                                        "so be so kind to check active Task and start to work )))" )
                                                .build() ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EscortTuple card ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL -> {
                patrul.setTaskId( null );
                patrul.setStatus( FREE ); }
            case ATTACHED -> {
                patrul.setTaskTypes( TaskTypes.ESCORT );
                patrul.setTaskId( card.getPolygon().getUuid().toString() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case FINISHED -> {
                patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), TaskTypes.CARD_102.name() );
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setTaskDate( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null );
            } }
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EventCar eventCar ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL -> {
                patrul.setTaskId( null );
                patrul.setStatus( FREE );
                eventCar.getPatruls().remove( patrul.getPassportNumber() ); }
            case ATTACHED -> {
                patrul.setTaskId( eventCar.getId() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( eventCar.getLatitude() );
                patrul.setTaskTypes( FIND_FACE_EVENT_CAR );
                patrul.setLongitudeOfTask( eventCar.getLongitude() );
                eventCar.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case ACCEPTED, ARRIVED -> {
                patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
                patrul.setTaskId( eventCar.getId() );
                eventCar.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case FINISHED -> {
                patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), FIND_FACE_EVENT_CAR.name() );
                eventCar.getPatruls().put( patrul.getPassportNumber(), patrul );
                if ( eventCar.getPatruls().size() == eventCar.getReportForCardList().size() ) {
                    eventCar.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( eventCar.getId() );
                    CassandraDataControl.getInstance().addValue( eventCar );
                    Archive.getAchieve().getSelfEmploymentTaskMap().remove( eventCar.getId() );
                    KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( eventCar ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskDate( null );
                patrul.setTaskId( null ); }
        } RedisDataControl.getRedis().addValue( eventCar.getId(), new ActiveTask( eventCar ) ).subscribe();
        CassandraDataControl.getInstance().addValue( eventCar );
        KafkaDataControl.getInstance()
                .writeToKafka(
                        CassandraDataControl
                                .getInstance().addValue(
                                        Notification.builder()
                                                .id( eventCar.getId() )
                                                .uuid( UUID.randomUUID() )
                                                .status( patrul.getStatus() )
                                                .carNumber( patrul.getCarNumber() )
                                                .taskTypes( patrul.getTaskTypes() )
                                                .type( FIND_FACE_EVENT_CAR.name() )
                                                .notificationWasCreated( new Date() )
                                                .policeType( patrul.getPoliceType() )
                                                .latitudeOfTask( eventCar.getLatitude() )
                                                .longitudeOfTask( eventCar.getLongitude() )
                                                .passportSeries( patrul.getPassportNumber() )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .address( eventCar.getAddress() != null ? eventCar.getAddress() : "unknown" )
                                                .title( "My dear: " + patrul.getName() + " you got " + FIND_FACE_EVENT_CAR
                                                        + ", so be so kind to check active Task and start to work )))" )
                                                .build() ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EventFace eventFace ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL -> {
                patrul.setTaskId( null );
                patrul.setStatus( FREE );
                eventFace.getPatruls().remove( patrul.getPassportNumber() ); }
            case ATTACHED -> {
                patrul.setTaskId( eventFace.getId() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( eventFace.getLatitude() );
                patrul.setLongitudeOfTask( eventFace.getLongitude() );
                patrul.setTaskTypes( FIND_FACE_EVENT_FACE );
                eventFace.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case ACCEPTED, ARRIVED -> {
                patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
                patrul.setTaskId( eventFace.getId() );
                eventFace.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case FINISHED -> {
                patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), FIND_FACE_EVENT_FACE.name() );
                eventFace.getPatruls().put( patrul.getPassportNumber(), patrul );
                if ( eventFace.getPatruls().size() == eventFace.getReportForCardList().size() ) {
                    eventFace.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( eventFace.getId() );
                    CassandraDataControl.getInstance().addValue( eventFace );
                    Archive.getAchieve().getSelfEmploymentTaskMap().remove( eventFace.getId() );
                    KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( eventFace ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskDate( null );
                patrul.setTaskId( null ); }
        } RedisDataControl.getRedis().addValue( eventFace.getId(), new ActiveTask( eventFace ) ).subscribe();
        CassandraDataControl.getInstance().addValue( eventFace );
        KafkaDataControl.getInstance()
                .writeToKafka(
                        CassandraDataControl
                                .getInstance().addValue(
                                        Notification.builder()
                                                .id( eventFace.getId() )
                                                .uuid( UUID.randomUUID() )
                                                .status(patrul.getStatus() )
                                                .carNumber(patrul.getCarNumber() )
                                                .taskTypes( patrul.getTaskTypes() )
                                                .type( FIND_FACE_EVENT_FACE.name() )
                                                .notificationWasCreated( new Date() )
                                                .policeType( patrul.getPoliceType() )
                                                .latitudeOfTask( eventFace.getLatitude() )
                                                .longitudeOfTask( eventFace.getLongitude() )
                                                .passportSeries( patrul.getPassportNumber() )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .address( eventFace.getAddress() != null ? eventFace.getAddress() : "unknown" )
                                                .title( "My dear: " + patrul.getName() + " you got " + FIND_FACE_EVENT_FACE
                                                        + ", so be so kind to check active Task and start to work )))" )
                                                .build() ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EventBody eventBody ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL -> {
                patrul.setTaskId( null );
                patrul.setStatus( FREE );
                eventBody.getPatruls().remove( patrul.getPassportNumber() ); }
            case ATTACHED -> {
                patrul.setTaskId( eventBody.getId() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( eventBody.getLatitude() );
                patrul.setLongitudeOfTask( eventBody.getLongitude() );
                patrul.setTaskTypes( FIND_FACE_EVENT_BODY );
                eventBody.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case ACCEPTED, ARRIVED -> {
                patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
                patrul.setTaskId( eventBody.getId() );
                eventBody.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case FINISHED -> {
                patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), FIND_FACE_EVENT_BODY.name() );
                eventBody.getPatruls().put( patrul.getPassportNumber(), patrul );
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskDate( null );
                patrul.setTaskId( null );
                if ( eventBody.getPatruls().size() == eventBody.getReportForCardList().size() ) {
                    eventBody.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( eventBody.getId() );
                    CassandraDataControl.getInstance().addValue( eventBody );
                    Archive.getAchieve().getSelfEmploymentTaskMap().remove( eventBody.getId() );
                    KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( eventBody ) ); }
            }
        } RedisDataControl.getRedis().addValue( eventBody.getId(), new ActiveTask( eventBody ) ).subscribe();
        CassandraDataControl.getInstance().addValue( eventBody );
        KafkaDataControl.getInstance()
                .writeToKafka(
                        CassandraDataControl
                                .getInstance().addValue(
                                        Notification.builder()
                                                .id( eventBody.getId() )
                                                .uuid( UUID.randomUUID() )
                                                .status( patrul.getStatus() )
                                                .carNumber( patrul.getCarNumber() )
                                                .taskTypes( patrul.getTaskTypes() )
                                                .type( FIND_FACE_EVENT_BODY.name() )
                                                .notificationWasCreated( new Date() )
                                                .policeType( patrul.getPoliceType() )
                                                .latitudeOfTask( eventBody.getLatitude() )
                                                .longitudeOfTask( eventBody.getLongitude() )
                                                .passportSeries( patrul.getPassportNumber() )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .address( eventBody.getAddress() != null ? eventBody.getAddress() : "unknown" )
                                                .title( "My dear: " + patrul.getName() + " you got " + FIND_FACE_EVENT_BODY
                                                        + ", so be so kind to check active Task and start to work )))" )
                                                .build() ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, CarEvents carEvents ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL -> {
                patrul.setTaskId( null );
                patrul.setStatus( FREE );
                carEvents.getPatruls().remove( patrul.getPassportNumber() ); }
            case ATTACHED -> {
                patrul.setTaskId( carEvents.getId() ); // saving card id into patrul object
                patrul.setTaskTypes( FIND_FACE_CAR );
                patrul.setLatitudeOfTask( carEvents.getCamera().getLatitude() );
                patrul.setLongitudeOfTask( carEvents.getCamera().getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case FINISHED -> {
                carEvents.getPatrulStatuses().get( patrul.getPassportNumber() )
                        .setTotalTimeConsumption( TimeInspector.getInspector().getTimeDifference( patrul.getTaskDate().toInstant() ) );
                patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), FIND_FACE_CAR.name() );
                patrul.setTaskTypes( TaskTypes.FREE );
                if ( carEvents.getPatruls().size() == carEvents.getReportForCardList().size() ) {
                    carEvents.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( carEvents.getId() );
                    KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( carEvents ) ); }
                patrul.setStatus( FREE );
                patrul.setTaskDate( null );
                patrul.setTaskId( null );
            } case ARRIVED -> carEvents.getPatrulStatuses().putIfAbsent( patrul.getPassportNumber(), PatrulStatus.builder()
                    .patrul( patrul )
                    .inTime( patrul.check() )
                    .totalTimeConsumption( TimeInspector.getInspector().getTimeDifference( patrul.getTaskDate().toInstant() ) ).build() );
        } carEvents.getPatruls().put( patrul.getPassportNumber(), patrul );
        RedisDataControl.getRedis().addValue( carEvents.getId(), new ActiveTask( carEvents ) ).subscribe();
        CassandraDataControl.getInstance().addValue( carEvents );
        KafkaDataControl.getInstance()
                .writeToKafka(
                        CassandraDataControl
                                .getInstance().addValue(
                                        Notification.builder()
                                                .id( carEvents.getId() )
                                                .uuid( UUID.randomUUID() )
                                                .status( patrul.getStatus() )
                                                .type( FIND_FACE_CAR.name() )
                                                .carNumber( patrul.getCarNumber() )
                                                .taskTypes( patrul.getTaskTypes() )
                                                .notificationWasCreated( new Date() )
                                                .policeType( patrul.getPoliceType() )
                                                .passportSeries( patrul.getPassportNumber() )
                                                .latitudeOfTask( carEvents.getCamera().getLatitude() )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .longitudeOfTask( carEvents.getCamera().getLongitude() )
                                                .address( carEvents.getCamera().getName() != null ? carEvents.getCamera().getName() : "unknown" )
                                                .title( "My dear: " + patrul.getName() + " you got " + FIND_FACE_CAR
                                                        + ", so be so kind to check active Task and start to work )))" ).build() ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, FaceEvents faceEvents ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL -> {
                patrul.setTaskId( null );
                patrul.setStatus( FREE );
                faceEvents.getPatruls().remove( patrul.getPassportNumber() ); }
            case ATTACHED -> {
                patrul.setTaskId( faceEvents.getId() ); // saving card id into patrul object
                patrul.setTaskTypes( FIND_FACE_PERSON );
                patrul.setLatitudeOfTask( faceEvents.getCamera().getLatitude() );
                patrul.setLongitudeOfTask( faceEvents.getCamera().getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case FINISHED -> {
                faceEvents.getPatrulStatuses().get( patrul.getPassportNumber() )
                        .setTotalTimeConsumption( TimeInspector.getInspector().getTimeDifference( patrul.getTaskDate().toInstant() ) );
                patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), FIND_FACE_PERSON.name() );
                patrul.setTaskTypes( TaskTypes.FREE );
                if ( faceEvents.getPatruls().size() == faceEvents.getReportForCardList().size() ) {
                    faceEvents.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( faceEvents.getId() );
                    KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( faceEvents ) ); }
                patrul.setTaskDate( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null );
            } case ARRIVED -> faceEvents.getPatrulStatuses().putIfAbsent( patrul.getPassportNumber(), PatrulStatus.builder()
                    .patrul( patrul )
                    .inTime( patrul.check() )
                    .totalTimeConsumption( TimeInspector.getInspector().getTimeDifference( patrul.getTaskDate().toInstant() ) ).build() );
        } faceEvents.getPatruls().put( patrul.getPassportNumber(), patrul );
        RedisDataControl.getRedis().addValue( faceEvents.getId(), new ActiveTask( faceEvents ) ).subscribe();
        CassandraDataControl.getInstance().addValue( faceEvents );
        KafkaDataControl.getInstance()
                .writeToKafka(
                        CassandraDataControl
                                .getInstance()
                                .addValue(
                                        Notification.builder()
                                                .id( faceEvents.getId() )
                                                .uuid( UUID.randomUUID() )
                                                .status( patrul.getStatus() )
                                                .type( FIND_FACE_PERSON.name() )
                                                .carNumber( patrul.getCarNumber() )
                                                .taskTypes( patrul.getTaskTypes() )
                                                .notificationWasCreated( new Date() )
                                                .policeType( patrul.getPoliceType() )
                                                .passportSeries( patrul.getPassportNumber() )
                                                .latitudeOfTask( faceEvents.getCamera().getLatitude() )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .longitudeOfTask( faceEvents.getCamera().getLongitude() )
                                                .address( faceEvents.getCamera().getName() != null ? faceEvents.getCamera().getName() : "unknown" )
                                                .title( "My dear: " + patrul.getName() + " you got " + FIND_FACE_PERSON
                                                        + ", so be so kind to check active Task and start to work )))" )
                                                .build() ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, SelfEmploymentTask selfEmploymentTask ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL -> {
                patrul.setTaskId( null );
                patrul.setStatus( FREE );
                patrul.setTaskTypes( TaskTypes.FREE );
                selfEmploymentTask.getPatruls().remove( patrul.getPassportNumber() ); }
            case ATTACHED -> {
                patrul.setTaskTypes( TaskTypes.SELF_EMPLOYMENT );
                patrul.setTaskId( selfEmploymentTask.getUuid().toString() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( selfEmploymentTask.getLatOfAccident() );
                patrul.setLongitudeOfTask( selfEmploymentTask.getLanOfAccident() );
                selfEmploymentTask.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case ACCEPTED, ARRIVED -> {
                patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
                patrul.setTaskTypes( TaskTypes.SELF_EMPLOYMENT );
                patrul.setTaskId( selfEmploymentTask.getUuid().toString() );
                selfEmploymentTask.getPatruls().put( patrul.getPassportNumber(), patrul ); }
            case FINISHED -> {
                patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), "selfEmployment" );
                selfEmploymentTask.getPatruls().put( patrul.getPassportNumber(), patrul );
                if ( selfEmploymentTask.getPatruls().size() == selfEmploymentTask.getReportForCards().size() ) {
                    selfEmploymentTask.setTaskStatus( FINISHED );
                    RedisDataControl.getRedis().remove( selfEmploymentTask.getUuid().toString() );
                    CassandraDataControl.getInstance().addValue( selfEmploymentTask,
                            KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( selfEmploymentTask ) ) );
                    Archive.getAchieve().getSelfEmploymentTaskMap().remove( selfEmploymentTask.getUuid() ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskDate( null );
                patrul.setTaskId( null ); }
        } RedisDataControl.getRedis().addValue( selfEmploymentTask.getUuid().toString(), new ActiveTask( selfEmploymentTask ) ).subscribe();
        CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) );
        KafkaDataControl.getInstance()
                .writeToKafka(
                        CassandraDataControl
                                .getInstance()
                                .addValue(
                                        Notification.builder()
                                                .uuid( UUID.randomUUID() )
                                                .status( patrul.getStatus() )
                                                .carNumber( patrul.getCarNumber() )
                                                .taskTypes( patrul.getTaskTypes() )
                                                .notificationWasCreated( new Date() )
                                                .policeType( patrul.getPoliceType() )
                                                .type( TaskTypes.SELF_EMPLOYMENT.name() )
                                                .passportSeries( patrul.getPassportNumber() )
                                                .id( selfEmploymentTask.getUuid().toString() )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .latitudeOfTask( selfEmploymentTask.getLatOfAccident() )
                                                .longitudeOfTask( selfEmploymentTask.getLanOfAccident() )
                                                .address( selfEmploymentTask.getAddress() != null ?
                                                        selfEmploymentTask.getAddress() : "unknown" )
                                                .title( patrul.getName() + " created selfEmploymentTask" )
                                                .build() ) );
        return patrul; }

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

            case FIND_FACE_CAR -> Archive.getAchieve().getCarEvent( patrul.getTaskId() ).flatMap( carEvents -> {
                carEvents.getReportForCardList().add( reportForCard );
                return RedisDataControl.getRedis().update( TaskInspector.getInstance().changeTaskStatus( patrul, Status.FINISHED, carEvents ) )
                        .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Report from: " + patrul.getName() + " was saved" ).code( 200 ).build() ).build() ) ); } );

            case FIND_FACE_PERSON -> Archive.getAchieve().getFaceEvent( patrul.getTaskId() ).flatMap( faceEvents -> {
                faceEvents.getReportForCardList().add( reportForCard );
                return RedisDataControl.getRedis().update( TaskInspector.getInstance().changeTaskStatus( patrul, Status.FINISHED, faceEvents ) )
                        .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Report from: " + patrul.getName() + " was saved" ).code( 200 ).build() ).build() ) ); } );

            default -> Archive.getAchieve().getEventCar( patrul.getTaskId() ).flatMap( eventFace -> {
                eventFace.getReportForCardList().add( reportForCard );
                return RedisDataControl.getRedis().update( TaskInspector.getInstance().changeTaskStatus( patrul, Status.FINISHED, eventFace ) )
                        .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Report from: " + patrul.getName() + " was saved" ).code( 200 ).build() ).build() ) ); } ); }; }

    public Mono< ApiResponseModel > changeTaskStatus ( Patrul patrul, Status status ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> RedisDataControl.getRedis().getCard( Long.parseLong( patrul.getTaskId() ) )
                    .flatMap( card -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, card )
                                            .getPassportNumber() + " changed his status task to: " + status  )
                                    .code( 200 )
                                    .build() )
                            .build() ) );

            case SELF_EMPLOYMENT -> Archive.getAchieve().get( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, selfEmploymentTask )
                                            .getPassportNumber() + " changed his status task to: " + status )
                                    .code( 200 )
                                    .build() )
                            .build() ) );

            case FIND_FACE_EVENT_CAR -> Archive.getAchieve().getEventCar( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, eventCar )
                                            .getPassportNumber() + " changed his status task to: " + status  )
                                    .code( 200 )
                                    .build() )
                            .build() ) );

            case FIND_FACE_EVENT_FACE -> Archive.getAchieve().getEventFace( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, eventFace )
                                            .getPassportNumber() + " changed his status task to: " + status  )
                                    .code( 200 )
                                    .build() )
                            .build() ) );

            case FIND_FACE_PERSON -> Archive.getAchieve().getFaceEvent( patrul.getTaskId() )
                    .flatMap( faceEvents -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, faceEvents )
                                            .getPassportNumber() + " changed his status task to: " + status  )
                                    .code( 200 )
                                    .build() )
                            .build() ) );

            case FIND_FACE_CAR -> Archive.getAchieve().getCarEvent( patrul.getTaskId() )
                    .flatMap( carEvents -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, carEvents )
                                            .getPassportNumber() + " changed his status task to: " + status  )
                                    .code( 200 )
                                    .build() )
                            .build() ) );

            default -> Archive.getAchieve().getEventBody( patrul.getTaskId() )
                    .flatMap( eventBody -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, eventBody )
                                            .getPassportNumber() + " changed his status task to: " + status  )
                                    .code( 200 )
                                    .build() )
                            .build() ) ); }; }

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

            case FIND_FACE_EVENT_CAR -> CassandraDataControl.getInstance().getEventCar( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new ActiveTask( eventCar, patrul.getStatus() ) )
                                    .type( TaskTypes.FIND_FACE_EVENT_CAR.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.FIND_FACE_EVENT_CAR.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            case FIND_FACE_EVENT_BODY -> CassandraDataControl.getInstance().getEventBody( patrul.getTaskId() )
                    .flatMap( eventBody -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new ActiveTask( eventBody, patrul.getStatus() ) )
                                    .type( TaskTypes.FIND_FACE_EVENT_BODY.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.FIND_FACE_EVENT_BODY.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            case FIND_FACE_EVENT_FACE -> CassandraDataControl.getInstance().getEventFace( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new ActiveTask( eventFace, patrul.getStatus() ) )
                                    .type( TaskTypes.FIND_FACE_EVENT_FACE.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.FIND_FACE_EVENT_FACE.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            case FIND_FACE_CAR -> CassandraDataControl.getInstance().getCarEvents( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new ActiveTask( eventFace, patrul.getStatus() ) )
                                    .type( TaskTypes.FIND_FACE_CAR.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.FIND_FACE_CAR.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            case FIND_FACE_PERSON -> CassandraDataControl.getInstance().getFaceEvents( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new ActiveTask( eventFace, patrul.getStatus() ) )
                                    .type( TaskTypes.FIND_FACE_PERSON.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.FIND_FACE_PERSON.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            default -> Mono.just( ApiResponseModel.builder().success( false )
                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 201 )
                            .message( "U have no task, so u can do smth else, my darling )))" ).build() ) .build() ); }; }

    public Mono< ApiResponseModel > getTaskDetails ( Patrul patrul ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> RedisDataControl.getRedis().getCard( Long.parseLong( patrul.getTaskId() ) )
                    .flatMap( card -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new CardDetails( card, "ru" ) ).build() )
                            .build() ) );

            case FIND_FACE_EVENT_BODY -> CassandraDataControl.getInstance().getEventBody( patrul.getTaskId() )
                    .flatMap( eventBody -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 ).message( "Your task details" ).build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new CardDetails( eventBody ) ).build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_EVENT_FACE -> CassandraDataControl.getInstance().getEventFace( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 ).message( "Your task details" ).build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new CardDetails( eventFace ) ).build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_EVENT_CAR -> CassandraDataControl.getInstance().getEventCar( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 ).message( "Your task details" ).build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new CardDetails( eventCar ) ).build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_CAR -> CassandraDataControl.getInstance().getCarEvents( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 ).message( "Your task details" ).build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new CardDetails( eventCar ) ).build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_PERSON -> CassandraDataControl.getInstance().getFaceEvents( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 ).message( "Your task details" ).build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new CardDetails( eventCar ) ).build() ) // TO-DO
                            .build() ) );

            default -> CassandraDataControl.getInstance().getSelfEmploymentTask( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 ).message( "Your task details" ).build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new CardDetails( selfEmploymentTask, "ru", patrul.getPassportNumber() ) ).build() )
                            .build() ) ); }; }
}
