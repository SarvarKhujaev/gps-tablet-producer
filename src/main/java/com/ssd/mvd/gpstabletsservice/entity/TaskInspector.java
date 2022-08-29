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
import com.ssd.mvd.gpstabletsservice.tuple.CassandraDataControlForEscort;
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

    public Patrul changeTaskStatus ( Patrul patrul, Status status, Card card ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) patrul.getListOfTasks()
                        .putIfAbsent( patrul.getTaskId(), CARD_102.name() );
                else card.getPatruls().remove( patrul.getUuid() );
                if ( card.getPatruls().size() == card.getReportForCardList().size() ) {
                    card.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( card.getCardId() );
                    KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( card ) ); }
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
        RedisDataControl.getRedis().update( card );
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
                                                .title( "My dear: " + patrul.getName()
                                                        + " you got 102 card task," +
                                                        "so be so kind to check active Task and start to work )))" )
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
                    KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( eventCar ) ); }
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
                            .totalTimeConsumption( TimeInspector.getInspector().getTimeDifference( patrul.getTaskDate().toInstant() ) ).build() );
        } if ( eventCar.getStatus().compareTo( FINISHED ) != 0 ) RedisDataControl.getRedis().addValue( eventCar.getId(), new ActiveTask( eventCar ) ).subscribe();
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
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .address( eventCar.getAddress() != null ? eventCar.getAddress() : "unknown" )
                                                .title( "My dear: " + patrul.getName() + " you got " + FIND_FACE_EVENT_CAR
                                                        + ", so be so kind to check active Task and start to work )))" )
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
                    KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( eventFace ) ); }
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
                            .totalTimeConsumption( TimeInspector.getInspector().getTimeDifference( patrul.getTaskDate().toInstant() ) ).build() );
        } if ( eventFace.getStatus().compareTo( FINISHED ) != 0 ) RedisDataControl.getRedis().addValue( eventFace.getId(), new ActiveTask( eventFace ) ).subscribe();
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
        switch ( patrul.getStatus() ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) patrul.getListOfTasks()
                        .putIfAbsent( patrul.getTaskId(), FIND_FACE_EVENT_BODY.name() );
                else eventBody.getPatruls().remove( patrul.getUuid() );
                if ( eventBody.getPatruls().size() == eventBody.getReportForCardList().size() ) {
                    eventBody.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( eventBody.getId() );
                    KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( eventBody ) ); }
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
                    .totalTimeConsumption( TimeInspector.getInspector().getTimeDifference( patrul.getTaskDate().toInstant() ) ).build() );
        } if ( eventBody.getStatus().compareTo( FINISHED ) != 0 ) RedisDataControl.getRedis().addValue( eventBody.getId(), new ActiveTask( eventBody ) ).subscribe();
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
                                                .passportSeries( patrul.getPassportNumber() )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
                                                .address( eventBody.getAddress() != null ? eventBody.getAddress() : "unknown" )
                                                .title( "My dear: " + patrul.getName() + " you got " + FIND_FACE_EVENT_BODY
                                                        + ", so be so kind to check active Task and start to work )))" )
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
                    KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( carEvents ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setTaskDate( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( FIND_FACE_CAR );
                patrul.setTaskId( carEvents.getId() ); // saving card id into patrul object
//                patrul.setLatitudeOfTask( carEvents.getCamera().getLatitude() );
//                patrul.setLongitudeOfTask( carEvents.getCamera().getLongitude() );
            }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> carEvents.getPatrulStatuses().putIfAbsent( patrul.getPassportNumber(), PatrulStatus.builder()
                    .patrul( patrul )
                    .inTime( patrul.check() )
                    .totalTimeConsumption( TimeInspector.getInspector().getTimeDifference( patrul.getTaskDate().toInstant() ) ).build() );
        } if ( carEvents.getStatus().compareTo( FINISHED ) != 0 ) RedisDataControl.getRedis().addValue( carEvents.getId(), new ActiveTask( carEvents ) ).subscribe();
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
//                                                .latitudeOfTask( carEvents.getCamera().getLatitude() )
//                                                .longitudeOfTask( carEvents.getCamera().getLongitude() )
//                                                .address( carEvents.getCamera().getName() != null ? carEvents.getCamera().getName() : "unknown" )
                                                .title( "My dear: " + patrul.getName() + " you got " + FIND_FACE_CAR
                                                        + ", so be so kind to check active Task and start to work )))" ).build() ) );
        return patrul; }

    public void changeTaskStatus ( Patrul patrul, Status status, EscortTuple escortTuple ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) patrul.getListOfTasks()
                        .putIfAbsent( patrul.getTaskId(), FIND_FACE_EVENT_CAR.name() );
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
                                                .build() ) ); }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, FaceEvent faceEvents ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) patrul.getListOfTasks()
                        .putIfAbsent( patrul.getTaskId(), FIND_FACE_CAR.name() );
                else faceEvents.getPatruls().remove( patrul.getUuid() );
                if ( faceEvents.getPatruls().size() == faceEvents.getReportForCardList().size() ) {
                    faceEvents.setStatus( FINISHED );
                    RedisDataControl.getRedis().remove( faceEvents.getId() );
                    KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( faceEvents ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setTaskDate( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskId( faceEvents.getId() ); // saving card id into patrul object
                patrul.setTaskTypes( FIND_FACE_PERSON );
//                patrul.setLatitudeOfTask( faceEvents.getCamera().getLatitude() );
//                patrul.setLongitudeOfTask( faceEvents.getCamera().getLongitude() );
            }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> faceEvents.getPatrulStatuses().putIfAbsent( patrul.getPassportNumber(),
                    PatrulStatus.builder()
                    .patrul( patrul )
                    .inTime( patrul.check() )
                    .totalTimeConsumption( TimeInspector.getInspector().getTimeDifference( patrul.getTaskDate().toInstant() ) ).build() );
        } if ( status.compareTo( CANCEL ) != 0 ) faceEvents.getPatruls().put( patrul.getUuid(), patrul );
        if ( faceEvents.getStatus().compareTo( FINISHED ) != 0 ) RedisDataControl.getRedis()
                .addValue( faceEvents.getId(), new ActiveTask( faceEvents ) ).subscribe();
        CassandraDataControlForTasks
                .getInstance()
                .addValue( faceEvents );

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
                                                .id( faceEvents.getId() )
                                                .uuid( UUID.randomUUID() )
                                                .status( patrul.getStatus() )
                                                .type( FIND_FACE_PERSON.name() )
                                                .carNumber( patrul.getCarNumber() )
                                                .taskTypes( patrul.getTaskTypes() )
                                                .notificationWasCreated( new Date() )
                                                .policeType( patrul.getPoliceType() )
                                                .passportSeries( patrul.getPassportNumber() )
//                                                .latitudeOfTask( faceEvents.getCamera().getLatitude() )
                                                .nsfOfPatrul( patrul.getSurnameNameFatherName() )
//                                                .longitudeOfTask( faceEvents.getCamera().getLongitude() )
//                                                .address( faceEvents.getCamera().getName() != null ? faceEvents.getCamera().getName() : "unknown" )
                                                .title( "My dear: " + patrul.getName() + " you got " + FIND_FACE_PERSON
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
                    KafkaDataControl.getInstance().writeToKafka( SerDes.getSerDes().serialize( selfEmploymentTask ) ); }
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
                                                .title( patrul.getName() + " created selfEmploymentTask" )
                                                .build() ) );
        return patrul; }

    public Mono< ApiResponseModel > saveReportForTask ( Patrul patrul, ReportForCard reportForCard ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> RedisDataControl.getRedis()
                    .getCard( Long.parseLong( patrul.getTaskId() ) )
                    .flatMap( card -> {
                    card.getReportForCardList().add( reportForCard );
                    return CassandraDataControl
                            .getInstance()
                            .update( TaskInspector.getInstance().changeTaskStatus( patrul, Status.FINISHED, card ) )
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
                                .update( TaskInspector.getInstance()
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
                                .update( TaskInspector.getInstance().changeTaskStatus( patrul, Status.FINISHED, eventBody ) )
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
                                .update( TaskInspector.getInstance().changeTaskStatus( patrul, Status.FINISHED, eventFace ) )
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
                                .update( TaskInspector.getInstance()
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
                                .update( TaskInspector.getInstance().changeTaskStatus( patrul, Status.FINISHED, faceEvents ) )
                                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                .message( "Report from: " + patrul.getName() + " was saved" )
                                                .code( 200 )
                                                .build() )
                                        .build() ) ); } );

            default -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventCar( patrul.getTaskId() )
                    .flatMap( eventCar -> {
                        eventCar.getReportForCardList().add( reportForCard );
                        return CassandraDataControl
                                .getInstance()
                                .update( TaskInspector.getInstance()
                                        .changeTaskStatus( patrul, Status.FINISHED, eventCar ) )
                                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                .message( "Report from: " + patrul.getName() + " was saved" )
                                                .code( 200 )
                                                .build() )
                                        .build() ) ); } ); }; }

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
                    .flatMap( faceEvents -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, status ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
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
            case CARD_102 -> RedisDataControl.getRedis().getCard( Long.parseLong( patrul.getTaskId() ) )
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
            case CARD_102 -> RedisDataControl
                    .getRedis()
                    .getCard( Long.parseLong( patrul.getTaskId() ) )
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

    public Mono< ApiResponseModel > getTaskDetails ( Patrul patrul ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> RedisDataControl.getRedis()
                    .getCard( Long.parseLong( patrul.getTaskId() ) )
                    .flatMap( card -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( card, "ru" ) )
                                    .build() )
                            .build() ) );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventBody( patrul.getTaskId() )
                    .flatMap( eventBody -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .code( 200 )
                                    .message( "Your task details" )
                                    .build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( eventBody ) )
                                    .build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventFace( patrul.getTaskId() )
                    .flatMap( eventFace -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .code( 200 )
                                    .message( "Your task details" )
                                    .build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( eventFace ) ).build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getEventCar( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .code( 200 )
                                    .message( "Your task details" )
                                    .build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( eventCar ) )
                                    .build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getCarEvents( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .code( 200 )
                                    .message( "Your task details" )
                                    .build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( eventCar ) )
                                    .build() ) // TO-DO
                            .build() ) );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getFaceEvents( patrul.getTaskId() )
                    .flatMap( eventCar -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .code( 200 )
                                    .message( "Your task details" )
                                    .build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( eventCar ) )
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
                                                            .indexOf( patrul.getUuid() )
                                            )
                            ).flatMap( tupleOfCar -> Mono.just( ApiResponseModel.builder()
                                    .success( true )
                                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .code( 200 )
                                            .message( "Your task details" )
                                            .build() )
                                    .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                            .data( new CardDetails( escortTuple, "ru", tupleOfCar ) )
                                            .build() )
                                    .build() ) ) );

            default -> CassandraDataControlForTasks
                    .getInstance()
                    .getSelfEmploymentTask( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> Mono.just( ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .code( 200 )
                                    .message( "Your task details" )
                                    .build() )
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( selfEmploymentTask, "ru", patrul.getPassportNumber() ) )
                                    .build() )
                            .build() ) ); }; }
}
