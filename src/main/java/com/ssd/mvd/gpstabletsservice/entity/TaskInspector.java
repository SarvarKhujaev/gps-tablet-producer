package com.ssd.mvd.gpstabletsservice.entity;

import lombok.Data;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.ssd.mvd.gpstabletsservice.database.*;
import com.ssd.mvd.gpstabletsservice.task.card.*;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.tuple.EscortTuple;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import static com.ssd.mvd.gpstabletsservice.constants.Status.FREE;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.*;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.tuple.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.FinishedTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FIND_FACE_CAR;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;

@Data
public final class TaskInspector {
    private static TaskInspector taskInspector;

    public static TaskInspector getInstance() { return taskInspector != null ? taskInspector : new TaskInspector(); }

    private String generateText ( Patrul patrul, Status status ) {
        return switch ( status ) {
            case ATTACHED -> patrul.getName()
                    + " got new task: " + patrul.getTaskId()
                    + " " + patrul.getTaskTypes();

            case ARRIVED -> patrul.getName()
                    + " : " + patrul.getTaskTypes()
                    + " arrived task location: "
                    + " at: " + new Date();

            case ACCEPTED -> patrul.getName()
                    + " ACCEPTED his task: " + patrul.getTaskId()
                    + " " + patrul.getTaskTypes()
                    + " at " + new Date();

            case FINISHED -> patrul.getName()
                    + " completed his task "
                    + " at " + new Date();

            default -> patrul.getName()
                    + " has been canceled from task "
                    + " at " + new Date(); }; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, Card card ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) patrul.getListOfTasks()
                        .putIfAbsent( patrul.getTaskId(), CARD_102.name() );
                else card.getPatruls().remove( patrul.getUuid() );
                if ( card.getPatruls().size() == card.getReportForCardList().size() ) {
                    card.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( card.getCardId().toString() );
                    KafkaDataControl
                            .getInstance()
                            .writeToKafka( SerDes.getSerDes().serialize( new ActiveTask( card ) ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setTaskDate( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( CARD_102 );
                patrul.setLatitudeOfTask( card.getLatitude() );
                patrul.setTaskId( card.getCardId().toString() ); // saving card id into patrul object
                patrul.setLongitudeOfTask( card.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> card.getPatrulStatuses().putIfAbsent( patrul.getPassportNumber(), PatrulStatus.builder()
                    .patrul( patrul )
                    .inTime( patrul.check() )
                    .totalTimeConsumption( TimeInspector
                            .getInspector()
                            .getTimeDifference( patrul.getTaskDate().toInstant() ) )
                    .build() );
        } if ( status.compareTo( CANCEL ) != 0 ) card.getPatruls().put( patrul.getUuid(), patrul );
        CassandraDataControl
                .getInstance()
                .update( patrul )
                .subscribe();

        if ( card.getStatus().compareTo( FINISHED ) != 0 ) RedisDataControl
                .getRedis()
                .addValue( card.getCardId().toString(),
                new ActiveTask( card ) ).subscribe();

        CassandraDataControlForTasks
                .getInstance()
                .addValue( card );

        KafkaDataControl.getInstance()
                .writeToKafka(
                        CassandraDataControl
                                .getInstance()
                                .addValue(
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
                                                .title( this.generateText( patrul, status ) )
                                                .build() ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EventCar eventCar ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) patrul.getListOfTasks()
                        .putIfAbsent( patrul.getTaskId(), FIND_FACE_EVENT_CAR.name() );
                else eventCar.getPatruls().remove( patrul.getUuid() );
                if ( eventCar.getPatruls().size() == eventCar.getReportForCardList().size() ) {
                    eventCar.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( eventCar.getId() );
                    KafkaDataControl
                            .getInstance()
                            .writeToKafka( SerDes
                                    .getSerDes()
                                    .serialize( new ActiveTask( eventCar ) ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setTaskDate( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskId( eventCar.getId() ); // saving card id into patrul object
                patrul.setTaskTypes( FIND_FACE_EVENT_CAR );
                patrul.setLatitudeOfTask( eventCar.getLatitude() );
                patrul.setLongitudeOfTask( eventCar.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> eventCar.getPatrulStatuses().putIfAbsent( patrul.getPassportNumber(),
                    PatrulStatus.builder()
                            .patrul( patrul )
                            .inTime( patrul.check() )
                            .totalTimeConsumption( TimeInspector
                                    .getInspector()
                                    .getTimeDifference( patrul
                                            .getTaskDate()
                                            .toInstant() ) ).build() );
        } if ( eventCar.getStatus().compareTo( FINISHED ) != 0 ) RedisDataControl
                .getRedis()
                .addValue( eventCar.getId(), new ActiveTask( eventCar ) )
                .subscribe();

        if ( status.compareTo( CANCEL ) != 0 ) eventCar.getPatruls().put( patrul.getUuid(), patrul );

        CassandraDataControl
                .getInstance()
                .update( patrul )
                .subscribe();

        CassandraDataControlForTasks
                .getInstance()
                .addValue( eventCar );

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
                                                .address( this.generateText( patrul, status ) )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .build() ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, CarEvent carEvents ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) patrul.getListOfTasks()
                        .putIfAbsent( patrul.getTaskId(), FIND_FACE_CAR.name() );
                else carEvents.getPatruls().remove( patrul.getUuid() );
                if ( carEvents.getPatruls().size() == carEvents.getReportForCardList().size() ) {
                    carEvents.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( carEvents.getId() );
                    KafkaDataControl
                            .getInstance()
                            .writeToKafka( SerDes
                                    .getSerDes()
                                    .serialize( new ActiveTask( carEvents ) ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setTaskDate( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( FIND_FACE_CAR );
                patrul.setTaskId( carEvents.getId() ); // saving card id into patrul object
                if ( carEvents.getDataInfo() != null
                        && carEvents.getDataInfo().getData() != null ) {
                    patrul.setLatitudeOfTask( carEvents.getDataInfo().getData().getLatitude() );
                    patrul.setLongitudeOfTask( carEvents.getDataInfo().getData().getLongitude() ); } }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> carEvents
                    .getPatrulStatuses()
                    .putIfAbsent( patrul.getPassportNumber(), PatrulStatus.builder()
                        .patrul( patrul )
                        .inTime( patrul.check() )
                        .totalTimeConsumption( TimeInspector
                                .getInspector()
                                .getTimeDifference( patrul.getTaskDate().toInstant() ) ).build() );
        } if ( carEvents.getStatus().compareTo( FINISHED ) != 0 ) RedisDataControl
                .getRedis()
                .addValue( carEvents.getId(), new ActiveTask( carEvents ) ).subscribe();

        if ( status.compareTo( CANCEL ) != 0 ) carEvents.getPatruls().put( patrul.getUuid(), patrul );

        CassandraDataControlForTasks
                .getInstance()
                .addValue( carEvents );

        CassandraDataControl
                .getInstance()
                .update( patrul )
                .subscribe();

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
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .latitudeOfTask( carEvents.getDataInfo() != null
                                                        && carEvents.getDataInfo().getData() != null ?
                                                        carEvents.getDataInfo().getData().getLatitude() : null )
                                                .longitudeOfTask( carEvents.getDataInfo() != null
                                                        && carEvents.getDataInfo().getData() != null ?
                                                        carEvents.getDataInfo().getData().getLongitude() : null )
                                                .address( carEvents.getDataInfo() != null
                                                        && carEvents.getDataInfo().getData() != null
                                                        && carEvents.getDataInfo().getData().getAddress() != null ?
                                                        carEvents.getDataInfo().getData().getAddress() : "unknown" )
                                                .title( this.generateText( patrul, status ) )
                                                .build() ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EventFace eventFace ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) patrul.getListOfTasks()
                        .putIfAbsent( patrul.getTaskId(), FIND_FACE_EVENT_FACE.name() );
                else eventFace.getPatruls().remove( patrul.getUuid() );
                if ( eventFace.getPatruls().size() == eventFace.getReportForCardList().size() ) {
                    eventFace.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( eventFace.getId() );
                    KafkaDataControl
                            .getInstance()
                            .writeToKafka( SerDes.getSerDes().serialize( new ActiveTask( eventFace ) ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setTaskDate( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskId( eventFace.getId() ); // saving card id into patrul object
                patrul.setTaskTypes( FIND_FACE_EVENT_FACE );
                patrul.setLatitudeOfTask( eventFace.getLatitude() );
                patrul.setLongitudeOfTask( eventFace.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> eventFace.getPatrulStatuses().putIfAbsent( patrul.getPassportNumber(),
                    PatrulStatus.builder()
                            .patrul( patrul )
                            .inTime( patrul.check() )
                            .totalTimeConsumption( TimeInspector
                                    .getInspector()
                                    .getTimeDifference( patrul
                                            .getTaskDate()
                                            .toInstant() ) ).build() );
        } if ( eventFace.getStatus().compareTo( FINISHED ) != 0 ) RedisDataControl
                .getRedis()
                .addValue( eventFace.getId(), new ActiveTask( eventFace ) )
                .subscribe();
        if ( status.compareTo( CANCEL ) != 0 ) eventFace.getPatruls().put( patrul.getUuid(), patrul );
        CassandraDataControlForTasks
                .getInstance()
                .addValue( eventFace );

        CassandraDataControl
                .getInstance()
                .update( patrul )
                .subscribe();

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
                                                .title( this.generateText( patrul, status ) )
                                                .longitudeOfTask( eventFace.getLongitude() )
                                                .passportSeries( patrul.getPassportNumber() )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .address( eventFace.getAddress() != null ? eventFace.getAddress() : "unknown" )
                                                .build() ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EventBody eventBody ) {
        patrul.setStatus( status );
        switch ( patrul.getStatus() ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) patrul.getListOfTasks()
                        .putIfAbsent( patrul.getTaskId(), FIND_FACE_EVENT_BODY.name() );
                else eventBody.getPatruls().remove( patrul.getUuid() );
                if ( eventBody.getPatruls().size() == eventBody.getReportForCardList().size() ) {
                    eventBody.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( eventBody.getId() );
                    KafkaDataControl
                            .getInstance()
                            .writeToKafka( SerDes
                                    .getSerDes()
                                    .serialize( new ActiveTask( eventBody ) ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setTaskDate( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskId( eventBody.getId() ); // saving card id into patrul object
                patrul.setTaskTypes( FIND_FACE_EVENT_BODY );
                patrul.setLatitudeOfTask( eventBody.getLatitude() );
                patrul.setLongitudeOfTask( eventBody.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> eventBody.getPatrulStatuses().putIfAbsent( patrul.getPassportNumber(),
                    PatrulStatus.builder()
                    .patrul( patrul )
                    .inTime( patrul.check() )
                    .totalTimeConsumption( TimeInspector
                            .getInspector()
                            .getTimeDifference( patrul
                                    .getTaskDate()
                                    .toInstant() ) ).build() );
        } if ( eventBody.getStatus().compareTo( FINISHED ) != 0 ) RedisDataControl
                .getRedis()
                .addValue( eventBody.getId(), new ActiveTask( eventBody ) )
                .subscribe();
        if ( status.compareTo( CANCEL ) != 0 ) eventBody.getPatruls().put( patrul.getUuid(), patrul );

        CassandraDataControlForTasks
                .getInstance()
                .addValue( eventBody );

        CassandraDataControl
                .getInstance()
                .update( patrul )
                .subscribe();

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
                                                .title( this.generateText( patrul, status ) )
                                                .passportSeries( patrul.getPassportNumber() )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .address( eventBody.getAddress() != null ? eventBody.getAddress() : "unknown" )
                                                .build() ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, FaceEvent faceEvent ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) patrul.getListOfTasks()
                        .putIfAbsent( patrul.getTaskId(), FIND_FACE_CAR.name() );
                else faceEvent.getPatruls().remove( patrul.getUuid() );
                if ( faceEvent.getPatruls().size() == faceEvent.getReportForCardList().size() ) {
                    faceEvent.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( faceEvent.getId() );
                    KafkaDataControl
                            .getInstance()
                            .writeToKafka( SerDes.getSerDes().serialize( new ActiveTask( faceEvent ) ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setTaskDate( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskId( faceEvent.getId() ); // saving card id into patrul object
                patrul.setTaskTypes( FIND_FACE_PERSON );
                if ( faceEvent.getDataInfo() != null
                        && faceEvent.getDataInfo().getData() != null ) {
                    patrul.setLatitudeOfTask( faceEvent.getDataInfo().getData().getLatitude() );
                    patrul.setLongitudeOfTask( faceEvent.getDataInfo().getData().getLongitude() ); } }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> faceEvent.getPatrulStatuses().putIfAbsent( patrul.getPassportNumber(),
                    PatrulStatus.builder()
                            .patrul( patrul )
                            .inTime( patrul.check() )
                            .totalTimeConsumption( TimeInspector
                                    .getInspector()
                                    .getTimeDifference( patrul
                                            .getTaskDate()
                                            .toInstant() ) ).build() );
        } if ( status.compareTo( CANCEL ) != 0 ) faceEvent.getPatruls().put( patrul.getUuid(), patrul );
        if ( faceEvent.getStatus().compareTo( FINISHED ) != 0 ) RedisDataControl
                .getRedis()
                .addValue( faceEvent.getId(), new ActiveTask( faceEvent ) )
                .subscribe();

        CassandraDataControlForTasks
                .getInstance()
                .addValue( faceEvent );

        CassandraDataControl
                .getInstance()
                .update( patrul )
                .subscribe();

        KafkaDataControl.getInstance()
                .writeToKafka(
                        CassandraDataControl
                                .getInstance()
                                .addValue(
                                        Notification.builder()
                                                .id( faceEvent.getId() )
                                                .uuid( UUID.randomUUID() )
                                                .status( patrul.getStatus() )
                                                .type( FIND_FACE_PERSON.name() )
                                                .carNumber( patrul.getCarNumber() )
                                                .taskTypes( patrul.getTaskTypes() )
                                                .notificationWasCreated( new Date() )
                                                .policeType( patrul.getPoliceType() )
                                                .passportSeries( patrul.getPassportNumber() )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .latitudeOfTask( faceEvent.getDataInfo() != null
                                                        && faceEvent.getDataInfo().getData() != null
                                                        ? faceEvent.getDataInfo().getData().getLatitude() : null )
                                                .longitudeOfTask( faceEvent.getDataInfo() != null
                                                        && faceEvent.getDataInfo().getData() != null
                                                        ? faceEvent.getDataInfo().getData().getLongitude() : null )
                                                .address( faceEvent.getDataInfo() != null
                                                        && faceEvent.getDataInfo().getData() != null
                                                        && faceEvent.getDataInfo().getData().getAddress() != null ?
                                                        faceEvent.getDataInfo().getData().getAddress() : "unknown" )
                                                .title( this.generateText( patrul, status ) )
                                                .build() ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EscortTuple escortTuple ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) patrul.getListOfTasks()
                        .putIfAbsent( patrul.getTaskId(), ESCORT.name() );
                else escortTuple.getPatrulList().remove( patrul.getUuid() );
                CassandraDataControlForEscort
                        .getInstance()
                        .getAllTupleOfCar( patrul.getUuidForEscortCar() )
                        .subscribe( tupleOfCar -> {
                            tupleOfCar.setUuidOfPatrul( null );
                            CassandraDataControlForEscort
                                    .getInstance()
                                    .update( tupleOfCar )
                                    .subscribe(); } );
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setUuidForEscortCar( null );
                patrul.setUuidOfEscort( null );
                patrul.setTaskDate( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( TaskTypes.ESCORT );
                patrul.setTaskId( escortTuple.getUuid().toString() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); }
        CassandraDataControl
                .getInstance()
                .update( patrul )
                .subscribe();

        KafkaDataControl.getInstance()
                .writeToKafka(
                        CassandraDataControl
                                .getInstance().addValue(
                                        Notification.builder()
                                                .type( ESCORT.name() )
                                                .uuid( UUID.randomUUID() )
                                                .status( patrul.getStatus() )
                                                .carNumber( patrul.getCarNumber() )
                                                .taskTypes( patrul.getTaskTypes() )
                                                .notificationWasCreated( new Date() )
                                                .policeType( patrul.getPoliceType() )
                                                .id( escortTuple.getUuid().toString() )
                                                .passportSeries( patrul.getPassportNumber() )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .title( "My dear: " + patrul.getName() + " you got " + ESCORT
                                                        + ", so be so kind to check active Task and start to work )))" )
                                                .build() ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, SelfEmploymentTask selfEmploymentTask ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) patrul.getListOfTasks()
                        .putIfAbsent( patrul.getTaskId(), TaskTypes.SELF_EMPLOYMENT.name() );
                else selfEmploymentTask.getPatruls().remove( patrul.getUuid() );
                if ( selfEmploymentTask.getPatruls().size() == selfEmploymentTask.getReportForCards().size() ) {
                    selfEmploymentTask.setTaskStatus( FINISHED );
                    RedisDataControl.getRedis().remove( selfEmploymentTask.getUuid().toString() );
                    KafkaDataControl
                            .getInstance()
                            .writeToKafka( SerDes
                                    .getSerDes()
                                    .serialize( new ActiveTask( selfEmploymentTask ) ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED, ACCEPTED, ARRIVED -> {
                patrul.setTaskTypes( TaskTypes.SELF_EMPLOYMENT );
                patrul.setTaskId( selfEmploymentTask.getUuid().toString() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( selfEmploymentTask.getLatOfAccident() );
                patrul.setLongitudeOfTask( selfEmploymentTask.getLanOfAccident() ); } }
        if ( selfEmploymentTask.getTaskStatus().compareTo( FINISHED ) != 0 ) RedisDataControl
                .getRedis()
                .addValue( selfEmploymentTask.getUuid().toString(),
                    new ActiveTask( selfEmploymentTask ) ).subscribe();
        if ( status.compareTo( CANCEL ) != 0 ) selfEmploymentTask.getPatruls().put( patrul.getUuid(), patrul );
        CassandraDataControlForTasks
                .getInstance()
                .addValue( selfEmploymentTask );

        CassandraDataControl
                .getInstance()
                        .update( patrul )
                                .subscribe();

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
                                                .title( this.generateText( patrul, status ) )
                                                .build() ) );
        return patrul; }

    public Mono< ApiResponseModel > saveReportForTask ( Patrul patrul, ReportForCard reportForCard ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getCard102( patrul.getTaskId() )
                    .flatMap( card -> {
                    card.getReportForCardList().add( reportForCard );
                    return CassandraDataControl
                            .getInstance()
                            .update( TaskInspector
                                    .getInstance()
                                    .changeTaskStatus( patrul, Status.FINISHED, card ) )
                            .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                    .success( true )
                                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "Report from: " + patrul.getName() + " was saved" )
                                            .code( 200 )
                                            .build() )
                                    .build() ) ); } );

            case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                    .getInstance()
                    .getSelfEmploymentTask( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> {
                        selfEmploymentTask.getReportForCards().add( reportForCard );
                        return CassandraDataControl
                                .getInstance()
                                .update( TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul, Status.FINISHED, selfEmploymentTask ) )
                                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                .message( "Report from: " + patrul.getName() + " was saved" )
                                                .code( 200 )
                                                .build() )
                                        .build() ) ); } );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventBody( patrul.getTaskId() )
                    .flatMap( eventBody -> {
                        eventBody.getReportForCardList().add( reportForCard );
                        return CassandraDataControl
                                .getInstance()
                                .update( TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul, Status.FINISHED, eventBody ) )
                                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                .message( "Report from: " + patrul.getName() + " was saved" )
                                                .code( 200 )
                                                .build() )
                                        .build() ) ); } );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventFace( patrul.getTaskId() )
                    .flatMap( eventFace -> {
                        eventFace.getReportForCardList().add( reportForCard );
                        return CassandraDataControl
                                .getInstance()
                                .update( TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul, Status.FINISHED, eventFace ) )
                                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                .message( "Report from: " + patrul.getName() + " was saved" )
                                                .code( 200 )
                                                .build() ).build() ) ); } );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getCarEvents( patrul.getTaskId() )
                    .flatMap( carEvents -> {
                        carEvents.getReportForCardList().add( reportForCard );
                        return CassandraDataControl
                                .getInstance()
                                .update( TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul, Status.FINISHED, carEvents ) )
                                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                .message( "Report from: " + patrul.getName() + " was saved" )
                                                .code( 200 )
                                                .build() )
                                        .build() ) ); } );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getFaceEvents( patrul.getTaskId() )
                    .flatMap( faceEvents -> {
                        faceEvents.getReportForCardList().add( reportForCard );
                        return CassandraDataControl
                                .getInstance()
                                .update( TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul, Status.FINISHED, faceEvents ) )
                                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                .message( "Report from: " + patrul.getName() + " was saved" )
                                                .code( 200 )
                                                .build() )
                                        .build() ) ); } );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventCar( patrul.getTaskId() )
                    .flatMap( eventCar -> {
                        eventCar.getReportForCardList().add( reportForCard );
                        return CassandraDataControl
                                .getInstance()
                                .update( TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul, Status.FINISHED, eventCar ) )
                                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                .message( "Report from: " + patrul.getName() + " was saved" )
                                                .code( 200 )
                                                .build() )
                                        .build() ) ); } );

            default -> Mono.just( ApiResponseModel
                    .builder()
                            .success( false )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "U have no tasks, thus u cannot send report" )
                                    .code( 201 )
                                    .build() )
                    .build() ); }; }

    public Mono< ApiResponseModel > changeTaskStatus ( Patrul patrul, Status status ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getCard102( patrul.getTaskId() )
                    .flatMap( card -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, card )
                                            .getPassportNumber() + " changed his status task to: " + status  )
                                    .code( 200 )
                                    .build() )
                            .build() ) );

            case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                    .getInstance()
                    .getSelfEmploymentTask( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, selfEmploymentTask )
                                            .getPassportNumber() + " changed his status task to: " + status )
                                    .code( 200 )
                                    .build() )
                            .build() ) );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventCar( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, eventCar )
                                            .getPassportNumber() + " changed his status task to: " + status  )
                                    .code( 200 )
                                    .build() )
                            .build() ) );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventFace( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, eventFace )
                                            .getPassportNumber() + " changed his status task to: " + status  )
                                    .code( 200 )
                                    .build() )
                            .build() ) );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getFaceEvents( patrul.getTaskId() )
                    .flatMap( faceEvents -> Mono.just( ApiResponseModel
                            .builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status
                                    .builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, faceEvents )
                                            .getPassportNumber() + " changed his status task to: " + status  )
                                    .code( 200 )
                                    .build() )
                            .build() ) );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getCarEvents( patrul.getTaskId() )
                    .flatMap( carEvents -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, carEvents )
                                            .getPassportNumber() + " changed his status task to: " + status  )
                                    .code( 200 )
                                    .build() )
                            .build() ) );

            case ESCORT -> CassandraDataControlForEscort
                    .getInstance()
                    .getAllTupleOfEscort( patrul.getTaskId() )
                    .flatMap( escortTuple -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Patrul: " + this.changeTaskStatus( patrul, status, escortTuple )
                                            .getPassportNumber() + " changed his status task to: " + status  )
                                    .code( 200 )
                                    .build() )
                            .build() ) );

            default -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventBody( patrul.getTaskId() )
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
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getCard102( patrul.getTaskId() )
                    .flatMap( card -> Mono.just( ApiResponseModel.builder().data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new ActiveTask( card, patrul.getStatus() ) )
                                    .type( TaskTypes.CARD_102.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.CARD_102.name() + " Task" ).build() ).success( true ).build() ) );

            case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                    .getInstance()
                    .getSelfEmploymentTask( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new ActiveTask( selfEmploymentTask, patrul.getStatus() ) )
                                    .type( TaskTypes.SELF_EMPLOYMENT.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.SELF_EMPLOYMENT.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventCar( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new ActiveTask( eventCar, patrul.getStatus() ) )
                                    .type( TaskTypes.FIND_FACE_EVENT_CAR.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.FIND_FACE_EVENT_CAR.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventBody( patrul.getTaskId() )
                    .flatMap( eventBody -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder().data( new ActiveTask( eventBody, patrul.getStatus() ) )
                                    .type( TaskTypes.FIND_FACE_EVENT_BODY.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.FIND_FACE_EVENT_BODY.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventFace( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new ActiveTask( eventFace, patrul.getStatus() ) )
                                    .type( TaskTypes.FIND_FACE_EVENT_FACE.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.FIND_FACE_EVENT_FACE.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getCarEvents( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new ActiveTask( eventFace, patrul.getStatus() ) )
                                    .type( TaskTypes.FIND_FACE_CAR.name() ).build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder().code( 200 )
                                    .message( "U have " + TaskTypes.FIND_FACE_CAR.name() + " Task" )
                                    .build() ).success( true ).build() ) );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getFaceEvents( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new ActiveTask( eventFace, patrul.getStatus() ) )
                                    .type( TaskTypes.FIND_FACE_PERSON.name() )
                                    .build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .code( 200 )
                                    .message( "U have " + TaskTypes.FIND_FACE_PERSON.name() + " Task" )
                                    .build() )
                            .success( true )
                            .build() ) );

            case ESCORT -> CassandraDataControlForEscort
                    .getInstance()
                    .getAllTupleOfEscort( patrul.getTaskId() )
                    .flatMap( escortTuple -> Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new ActiveTask( escortTuple, patrul.getStatus() ) )
                                    .type( ESCORT.name() )
                                    .build() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .code( 200 )
                                    .message( "U have " + ESCORT.name() + " Task" )
                                    .build() )
                            .success( true )
                            .build() ) );

            default -> Mono.just( ApiResponseModel.builder()
                    .success( false )
                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                            .code( 201 )
                            .message( "U have no task, so u can do smth else, my darling )))" )
                            .build() )
                    .build() ); }; }

    public Mono< ApiResponseModel > removePatrulFromTask ( Patrul patrul ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getCard102( patrul.getTaskId() )
                    .flatMap( card -> Mono.just(
                            ApiResponseModel.builder()
                                    .success( true )
                                    .status(
                                            com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                    .message( this.changeTaskStatus( patrul, CANCEL, card )
                                                            .getName() + " was removed from " + card.getCardId() )
                                                    .code( 200 )
                                                    .build()
                                    ).build() ) );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventCar( patrul.getTaskId() )
                    .flatMap( card -> Mono.just(
                            ApiResponseModel.builder()
                                    .success( true )
                                    .status(
                                            com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                    .message( this.changeTaskStatus( patrul, CANCEL, card )
                                                            .getName() + " was removed from " + card.getId() )
                                                    .code( 200 )
                                                    .build()
                                    ).build() ) );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventBody( patrul.getTaskId() )
                    .flatMap( card -> Mono.just(
                            ApiResponseModel.builder()
                                    .success( true )
                                    .status(
                                            com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                    .message( this.changeTaskStatus( patrul, CANCEL, card )
                                                            .getName() + " was removed from " + card.getId() )
                                                    .code( 200 )
                                                    .build()
                                    ).build() ) );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventFace ( patrul.getTaskId() )
                    .flatMap( card -> Mono.just(
                            ApiResponseModel.builder()
                                    .success( true )
                                    .status(
                                            com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                    .message( this.changeTaskStatus( patrul, CANCEL, card )
                                                            .getName() + " was removed from " + card.getId() )
                                                    .code( 200 )
                                                    .build()
                                    ).build() ) );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getCarEvents( patrul.getTaskId() )
                    .flatMap( card -> Mono.just(
                            ApiResponseModel.builder()
                                    .success( true )
                                    .status(
                                            com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                    .message( this.changeTaskStatus( patrul, CANCEL, card )
                                                            .getName() + " was removed from " + card.getId() )
                                                    .code( 200 )
                                                    .build()
                                    ).build() ) );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getFaceEvents( patrul.getTaskId() )
                    .flatMap( card -> Mono.just(
                            ApiResponseModel.builder()
                                    .success( true )
                                    .status(
                                            com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                    .message( this.changeTaskStatus( patrul, CANCEL, card )
                                                            .getName() + " was removed from " + card.getId() )
                                                    .code( 200 )
                                                    .build()
                                    ).build() ) );

            default -> CassandraDataControlForTasks
                    .getInstance()
                    .getSelfEmploymentTask( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( card -> Mono.just(
                            ApiResponseModel.builder()
                                    .success( true )
                                    .status(
                                            com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                    .message( this.changeTaskStatus( patrul, CANCEL, card )
                                                            .getName() + " was removed from " + card.getUuid() )
                                                    .code( 200 )
                                                    .build()
                                    ).build() ) ); }; }

    private Integer getReportIndex ( List< ReportForCard > reportForCardList, UUID uuid ) {
        for ( int i = 0; i < reportForCardList.size(); i++ ) if ( reportForCardList.get( i ).getUuidOfPatrul().compareTo( uuid ) == 0 ) return i;
        return 0; }

    public Mono< ApiResponseModel > getListOfPatrulTasks ( Patrul patrul ) {
        return Flux.fromStream( patrul.getListOfTasks().keySet().stream() )
                .flatMap( key -> switch ( TaskTypes.valueOf( patrul.getListOfTasks().get( key ) ) ) {
                    case CARD_102 -> CassandraDataControlForTasks
                            .getInstance()
                            .getCard102( key )
                            .map( card -> com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .type( CARD_102.name() )
                                    .data( FinishedTask
                                            .builder()
                                            .taskTypes( CARD_102 )
                                            .task( card.getFabula() )
                                            .createdDate( card.getCreated_date().toString() )
                                            .cardDetails( new CardDetails( card, patrul, "ru" ) )
                                            .reportForCard( card
                                                    .getReportForCardList()
                                                    .get( this.getReportIndex( card
                                                            .getReportForCardList(), patrul.getUuid() ) ) )
                                            .totalTimeConsumption( card
                                                    .getPatrulStatuses()
                                                    .get( patrul.getPassportNumber() )
                                                    .getTotalTimeConsumption() )
                                            .build() )
                                    .build() );

                    case FIND_FACE_CAR -> CassandraDataControlForTasks
                            .getInstance()
                            .getCarEvents( key )
                            .map( carEvent -> com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .type( FIND_FACE_CAR.name() )
                                    .data( FinishedTask
                                            .builder()
                                            .taskTypes( CARD_102 )
                                            .task( carEvent.getName() )
                                            .createdDate( carEvent.getCreated_date() )
                                            .cardDetails( new CardDetails( new CarDetails( carEvent ) ) )
                                            .reportForCard( carEvent
                                                    .getReportForCardList()
                                                    .get( this.getReportIndex( carEvent
                                                                    .getReportForCardList(), patrul.getUuid() ) ) )
                                            .totalTimeConsumption( carEvent
                                                    .getPatrulStatuses()
                                                    .get( patrul.getPassportNumber() )
                                                    .getTotalTimeConsumption() )
                                            .build() )
                                    .build() );

                    case FIND_FACE_PERSON -> CassandraDataControlForTasks
                            .getInstance()
                            .getFaceEvents( key )
                            .map( faceEvent -> com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .type( FIND_FACE_PERSON.name() )
                                    .data( FinishedTask
                                            .builder()
                                            .taskTypes( CARD_102 )
                                            .task( faceEvent.getName() )
                                            .createdDate( faceEvent.getCreated_date() )
                                            .cardDetails( new CardDetails( new PersonDetails( faceEvent ) ) )
                                            .reportForCard( faceEvent
                                                    .getReportForCardList()
                                                    .get( this.getReportIndex( faceEvent
                                                            .getReportForCardList(), patrul.getUuid() ) ) )
                                            .totalTimeConsumption( faceEvent
                                                    .getPatrulStatuses()
                                                    .get( patrul.getPassportNumber() )
                                                    .getTotalTimeConsumption() )
                                            .build() )
                                    .build() );

                    case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                            .getInstance()
                            .getEventCar( key )
                            .map( eventCar -> com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .type( FIND_FACE_EVENT_CAR.name() )
                                    .data( FinishedTask
                                            .builder()
                                            .taskTypes( CARD_102 )
                                            .task( eventCar.getId() )
                                            .createdDate( eventCar.getCreated_date().toString() )
                                            .cardDetails( new CardDetails( new CarDetails( eventCar ) ) )
                                            .reportForCard( eventCar
                                                    .getReportForCardList()
                                                    .get( this.getReportIndex( eventCar
                                                            .getReportForCardList(), patrul.getUuid() ) ) )
                                            .totalTimeConsumption( eventCar
                                                    .getPatrulStatuses()
                                                    .get( patrul.getPassportNumber() )
                                                    .getTotalTimeConsumption() )
                                            .build() )
                                    .build() );

                    case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                            .getInstance()
                            .getEventBody( key )
                            .map( eventBody -> com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .type( FIND_FACE_EVENT_BODY.name() )
                                    .data( FinishedTask
                                            .builder()
                                            .taskTypes( CARD_102 )
                                            .task( eventBody.getId() )
                                            .createdDate( eventBody.getCreated_date().toString() )
                                            .cardDetails( new CardDetails( new PersonDetails( eventBody ) ) )
                                            .reportForCard( eventBody
                                                    .getReportForCardList()
                                                    .get( this.getReportIndex( eventBody
                                                            .getReportForCardList(), patrul.getUuid() ) ) )
                                            .totalTimeConsumption( eventBody
                                                    .getPatrulStatuses()
                                                    .get( patrul.getPassportNumber() )
                                                    .getTotalTimeConsumption() )
                                            .build() )
                                    .build() );

                    case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                            .getInstance()
                            .getEventFace( key )
                            .map( eventFace -> com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .type( FIND_FACE_EVENT_FACE.name() )
                                    .data( FinishedTask
                                            .builder()
                                            .taskTypes( CARD_102 )
                                            .task( eventFace.getId() )
                                            .createdDate( eventFace.getCreated_date().toString() )
                                            .cardDetails( new CardDetails( new PersonDetails( eventFace ) ) )
                                            .reportForCard( eventFace
                                                    .getReportForCardList()
                                                    .get( this.getReportIndex( eventFace
                                                            .getReportForCardList(), patrul.getUuid() ) ) )
                                            .totalTimeConsumption( eventFace
                                                    .getPatrulStatuses()
                                                    .get( patrul.getPassportNumber() )
                                                    .getTotalTimeConsumption() )
                                            .build() )
                                    .build() );

                    default -> CassandraDataControlForTasks
                            .getInstance()
                            .getSelfEmploymentTask( UUID.fromString( key ) )
                            .map( selfEmploymentTask -> com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .type( TaskTypes.SELF_EMPLOYMENT.name() )
                                    .data( FinishedTask
                                            .builder()
                                            .taskTypes( CARD_102 )
                                            .task( selfEmploymentTask.getDescription() )
                                            .createdDate( selfEmploymentTask.getIncidentDate().toString() )
                                            .cardDetails( new CardDetails( selfEmploymentTask, "ru", patrul ) )
                                            .reportForCard( selfEmploymentTask
                                                    .getReportForCards()
                                                    .get( this.getReportIndex( selfEmploymentTask
                                                            .getReportForCards(), patrul.getUuid() ) ) )
                                            .build() )
                                    .build() ); } )
                .collectList()
                .flatMap( data -> Mono.just( ApiResponseModel
                        .builder()
                        .success( true )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .message( "Your list of tasks" )
                                .code( 200 )
                                .build() )
                        .data( com.ssd.mvd.gpstabletsservice.entity.Data
                                .builder()
                                .data( data )
                                .build() )
                        .build() ) ); }

    public Mono< ApiResponseModel > getTaskDetails ( Patrul patrul ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getCard102( patrul.getTaskId() )
                    .flatMap( card -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Your task details" )
                                    .code( 200 )
                                    .build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( card, patrul, "ru" ) )
                                    .type( CARD_102.name() )
                                    .build() )
                            .build() ) );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventBody( patrul.getTaskId() )
                    .flatMap( eventBody -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Your task details" )
                                    .code( 200 )
                                    .build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( new PersonDetails( eventBody ) ) )
                                    .type( FIND_FACE_PERSON.name() )
                                    .build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventFace( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Your task details" )
                                    .code( 200 )
                                    .build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( new PersonDetails( eventFace ) ) )
                                    .type( FIND_FACE_PERSON.name() )
                                    .build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventCar( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Your task details" )
                                    .code( 200 )
                                    .build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( new CarDetails( eventCar ) ) )
                                    .type( FIND_FACE_CAR.name() )
                                    .build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getCarEvents( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Your task details" )
                                    .code( 200 )
                                    .build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( new CarDetails( eventCar ) ) )
                                    .type( FIND_FACE_CAR.name() )
                                    .build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getFaceEvents( patrul.getTaskId() )
                    .flatMap( faceEvent -> Mono.just( ApiResponseModel
                            .builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status
                                    .builder()
                                    .message( "Your task details" )
                                    .code( 200 )
                                    .build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new CardDetails( new PersonDetails( faceEvent ) ) )
                                    .type( FIND_FACE_PERSON.name() )
                                    .build() ) // TO-DO
                            .build() ) );

            case ESCORT -> CassandraDataControlForEscort
                    .getInstance()
                    .getAllTupleOfEscort( patrul.getTaskId() )
                    .flatMap( escortTuple -> CassandraDataControlForEscort
                            .getInstance()
                            .getAllTupleOfCar(
                                    escortTuple.getTupleOfCarsList()
                                            .get(
                                                    escortTuple
                                                            .getPatrulList()
                                                            .indexOf( patrul.getUuid() ) )
                            ).flatMap( tupleOfCar -> Mono.just( ApiResponseModel.builder()
                                    .success( true )
                                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "Your task details" )
                                            .code( 200 )
                                            .build() )
                                    .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                            .data( new CardDetails( escortTuple, "ru", tupleOfCar ) )
                                            .type( ESCORT.name() )
                                            .build() )
                                    .build() ) ) );

            case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                    .getInstance()
                    .getSelfEmploymentTask( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Your task details" )
                                    .code( 200 )
                                    .build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( selfEmploymentTask, "ru", patrul ) )
                                    .type( TaskTypes.SELF_EMPLOYMENT.name() )
                                    .build() )
                            .build() ) );

            default -> Mono.just( ApiResponseModel.builder()
                            .success( false )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "You have no tasks" )
                                    .code( 201 )
                                    .build() )
                    .build() ); }; }
}
