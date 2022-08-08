package com.ssd.mvd.gpstabletsservice.database;

import lombok.Data;
import java.util.*;
import java.security.SecureRandom;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.response.Status;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.*;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvents;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvents;

@Data
public class Archive {
    public Boolean flag = true;
    private static Archive archive = new Archive();
    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder();

    private final Map< String, CarEvents > carEvents = new HashMap<>(); // Assomidin
    private final Map< String, FaceEvents > faceEvents = new HashMap<>(); // Assomidin

    private final Map< String, EventCar > eventCarMap = new HashMap<>();
    private final Map< String, EventBody > eventBodyMap = new HashMap<>();
    private final Map< String, EventFace > eventFaceMap = new HashMap<>();

    private final Map< UUID, SelfEmploymentTask > selfEmploymentTaskMap = new HashMap<>();
    private final List< String > detailsList = List.of( "Ф.И.О", "", "ПОДРАЗДЕЛЕНИЕ", "ДАТА И ВРЕМЯ", "ID", "ШИРОТА", "ДОЛГОТА", "ВИД ПРОИСШЕСТВИЯ", "НАЧАЛО СОБЫТИЯ", "КОНЕЦ СОБЫТИЯ", "КОЛ.СТВО ПОСТРАДАВШИХ", "КОЛ.СТВО ПОШИБЩИХ", "ФАБУЛА" );

    public static Archive getAchieve () { return archive != null ? archive : ( archive = new Archive() ); }

    private Archive () { CassandraDataControl.getInstance().resetData(); }

    public Mono< EventCar > getEventCar ( String id ) { return this.eventCarMap.containsKey( id ) ? Mono.just( this.eventCarMap.get( id ) ) : Mono.empty(); }

    public Mono< EventBody > getEventBody ( String id ) { return this.eventBodyMap.containsKey( id ) ? Mono.just( this.eventBodyMap.get( id ) ) : Mono.empty(); }

    public Mono< EventFace > getEventFace ( String id ) { return this.eventFaceMap.containsKey( id ) ? Mono.just( this.eventFaceMap.get( id ) ) : Mono.empty(); }

    public Mono< CarEvents > getCarEvent ( String id ) { return this.getCarEvents().containsKey( id ) ? Mono.just( this.getCarEvents().get( id ) ) : Mono.empty(); } // coming from Assamidin

    public Mono< FaceEvents > getFaceEvent ( String id ) { return this.getFaceEvents().containsKey( id ) ? Mono.just( this.getFaceEvents().get( id ) ) : Mono.empty(); } // coming from Assamidin

    public Mono< SelfEmploymentTask > get ( UUID uuid ) { return this.selfEmploymentTaskMap.containsKey( uuid ) ? Mono.just( this.selfEmploymentTaskMap.get( uuid ) ) : Mono.empty(); }

    // to get all existing SelfEmploymentTask
    public Flux< SelfEmploymentTask > getAllSelfEmploymentTask () { return Flux.fromStream( this.selfEmploymentTaskMap.values().stream() ); }

    // link new Patrul to existing SelfEmployment object
    public Mono< ApiResponseModel > save ( UUID uuid, Patrul patrul ) { return this.get( uuid ).flatMap( selfEmploymentTask -> {
        selfEmploymentTask.getPatruls().put( patrul.getPassportNumber(), TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, selfEmploymentTask ) );
        RedisDataControl.getRedis().addValue( selfEmploymentTask.getUuid().toString(), new ActiveTask( selfEmploymentTask ) );
        CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) );
        this.save( Notification.builder()
                .type( "selfEmployment" )
                .notificationWasCreated( new Date() )
                .passportSeries( patrul.getPassportNumber() )
                .id( selfEmploymentTask.getUuid().toString() )
                .latitudeOfTask( selfEmploymentTask.getLatOfAccident() )
                .longitudeOfTask( selfEmploymentTask.getLanOfAccident() )
                .address( selfEmploymentTask.getAddress() != null ? selfEmploymentTask.getAddress() : "unknown" )
                .title( "My dear: " + patrul.getName() + " you got selfEmploymentTask task, so be so kind to check active Task and start to work )))" ).build() );
        return RedisDataControl.getRedis().update( patrul ); } ); }

    public void save ( Notification notification ) { KafkaDataControl.getInstance().writeToKafka( notification ); }

    public Mono< ApiResponseModel > save ( SelfEmploymentTask selfEmploymentTask, Patrul patrul ) {
        if ( !this.selfEmploymentTaskMap.containsKey( selfEmploymentTask.getUuid() ) ) {
            TaskInspector.getInstance().changeTaskStatus( patrul, selfEmploymentTask.getTaskStatus(), selfEmploymentTask );
            this.selfEmploymentTaskMap.putIfAbsent( selfEmploymentTask.getUuid(), selfEmploymentTask ); // saving in Archive to manipulate in future
            RedisDataControl.getRedis().addValue( selfEmploymentTask.getUuid().toString(), new ActiveTask( selfEmploymentTask ) );
            return RedisDataControl.getRedis().update( patrul )
                    .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                            .status( Status.builder().message( "SelfEmployment was saved" ).code( 200 ).build() )
                            .success( CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) ) ).build() ) );
        } else return Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "Wrong Data for Task" ).code( 201 ).build() ).build() ); }

    // taking off some Patrul from current Card
    public Mono< ApiResponseModel > removePatrulFromSelfEmployment ( UUID uuid, Patrul patrul ) { return this.selfEmploymentTaskMap.containsKey( uuid ) ?
            this.get( uuid ).flatMap( selfEmploymentTask -> {
                TaskInspector.getInstance().changeTaskStatus( patrul, CANCEL, selfEmploymentTask );
                CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) );
                return RedisDataControl.getRedis().update( patrul ); } )
            : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "there is no such a task" ).code( 201 ).build() ).build() ); }

    public Mono< ApiResponseModel > addNewPatrulToCard ( Long cardId, Patrul patrul ) { return RedisDataControl.getRedis().getCard( cardId )
            .flatMap( card -> {
                TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, card );
                this.save( Notification.builder()
                        .type( "card 102" )
                        .id( card.getCardId().toString() )
                        .latitudeOfTask( card.getLatitude() )
                        .notificationWasCreated( new Date() )
                        .longitudeOfTask( card.getLongitude() )
                        .passportSeries( patrul.getPassportNumber() )
                        .address( card.getAddress() != null ? card.getAddress() : "unknown" )
                        .title( "My dear: " + patrul.getName() + " you got 102 card task, so be so kind to check active Task and start to work )))" ).build() );
                return Mono.just( ApiResponseModel.builder().success( true )
                                .status( Status.builder().message( patrul.getName() + " linked to " + card.getCardId() ).code( 200 ).build() ).build() ); } ); }

    public Mono< ApiResponseModel > removePatrulFromCard ( Long cardId, Patrul patrul ) { return RedisDataControl.getRedis().getCard( cardId )
            .flatMap( card -> {
                TaskInspector.getInstance().changeTaskStatus( patrul, CANCEL, card );
                return Mono.just( ApiResponseModel.builder()
                        .success( true )
                        .status( Status.builder().message( patrul.getName() + " removed from: " + card.getCardId() ).code( 200 ).build() )
                        .build() ); } ); }

    // uses to link Card to current Patrul object, either additional Patrul in case of necessary
    public Mono< ApiResponseModel > save ( Patrul patrul, Card card ) {
        TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, card );
        RedisDataControl.getRedis().addValue( card );
        this.save( Notification.builder()
                .type( CARD_102.name() )
                .id( card.getCardId().toString() )
                .latitudeOfTask( card.getLatitude() )
                .notificationWasCreated( new Date() )
                .longitudeOfTask( card.getLongitude() )
                .passportSeries( patrul.getPassportNumber() )
                .address( card.getAddress() != null ? card.getAddress() : "unknown" )
                .title( "My dear: " + patrul.getName() + " you got 102 card task, so be so kind to check active Task and start to work )))" ).build() );
        return RedisDataControl.getRedis().update( patrul )
                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder().success( true )
                        .status( Status.builder().message( card + " was linked to: " + patrul.getName()  ).build() ).build() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventFace card ) {
        TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, card );
        this.getEventFaceMap().putIfAbsent( card.getId(), card );
        CassandraDataControl.getInstance().addValue( card );
        this.save( Notification.builder()
                .id( card.getId() )
                .type( FIND_FACE_EVENT_FACE.name() )
                .latitudeOfTask( card.getLatitude() )
                .notificationWasCreated( new Date() )
                .longitudeOfTask( card.getLongitude() )
                .passportSeries( patrul.getPassportNumber() )
                .address( card.getAddress() != null ? card.getAddress() : "unknown" )
                .title( "My dear: " + patrul.getName() + " you got 102 card task, so be so kind to check active Task and start to work )))" ).build() );
        return RedisDataControl.getRedis().update( patrul )
                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder().success( true )
                        .status( Status.builder().message( card + " was linked to: " + patrul.getName()  ).build() ).build() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventBody card ) {
        TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, card );
        this.getEventBodyMap().putIfAbsent( card.getId(), card );
        CassandraDataControl.getInstance().addValue( card );
        this.save( Notification.builder()
                .id( card.getId() )
                .type( FIND_FACE_EVENT_BODY.name() )
                .latitudeOfTask( card.getLatitude() )
                .notificationWasCreated( new Date() )
                .longitudeOfTask( card.getLongitude() )
                .passportSeries( patrul.getPassportNumber() )
                .address( card.getAddress() != null ? card.getAddress() : "unknown" )
                .title( "My dear: " + patrul.getName() + " you got 102 card task, so be so kind to check active Task and start to work )))" ).build() );
        return RedisDataControl.getRedis().update( patrul )
                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder().success( true )
                        .status( Status.builder().message( card + " was linked to: " + patrul.getName()  ).build() ).build() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventCar card ) {
        TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, card );
        this.getEventCarMap().putIfAbsent( card.getId(), card );
        CassandraDataControl.getInstance().addValue( card );
        this.save( Notification.builder()
                .id( card.getId() )
                .type( FIND_FACE_EVENT_BODY.name() )
                .latitudeOfTask( card.getLatitude() )
                .notificationWasCreated( new Date() )
                .longitudeOfTask( card.getLongitude() )
                .passportSeries( patrul.getPassportNumber() )
                .address( card.getAddress() != null ? card.getAddress() : "unknown" )
                .title( "My dear: " + patrul.getName() + " you got 102 card task, so be so kind to check active Task and start to work )))" ).build() );
        return RedisDataControl.getRedis().update( patrul )
                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder().success( true )
                        .status( Status.builder().message( card + " was linked to: " + patrul.getName()  ).build() ).build() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, FaceEvents card ) {
        this.getFaceEvents().putIfAbsent( TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, card ).getTaskId(), card );
        CassandraDataControl.getInstance().addValue( card );
        this.save( Notification.builder()
                .id( card.getId() )
                .type( FIND_FACE_EVENT_BODY.name() )
                .notificationWasCreated( new Date() )
                .passportSeries( patrul.getPassportNumber() )
                .latitudeOfTask( card.getCamera().getLatitude() )
                .longitudeOfTask( card.getCamera().getLongitude() )
                .address( card.getCamera().getName() != null ? card.getCamera().getName() : "unknown" )
                .title( "My dear: " + patrul.getName() + " you got " + FIND_FACE_PERSON
                        + ", so be so kind to check active Task and start to work )))" ).build() );
        return RedisDataControl.getRedis().update( patrul )
                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder().success( true )
                        .status( Status.builder().message( card + " was linked to: " + patrul.getName()  ).build() ).build() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, CarEvents card ) {
        this.getCarEvents().putIfAbsent( TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, card ).getTaskId(), card );
        CassandraDataControl.getInstance().addValue( card );
        this.save( Notification.builder()
                .id( card.getId() )
                .type( FIND_FACE_CAR.name() )
                .notificationWasCreated( new Date() )
                .passportSeries( patrul.getPassportNumber() )
                .latitudeOfTask( card.getCamera().getLatitude() )
                .longitudeOfTask( card.getCamera().getLongitude() )
                .address( card.getCamera().getName() != null ? card.getCamera().getName() : "unknown" )
                .title( "My dear: " + patrul.getName() + " you got " + FIND_FACE_CAR
                        + ", so be so kind to check active Task and start to work )))" ).build() );
        return RedisDataControl.getRedis().update( patrul )
                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder().success( true )
                        .status( Status.builder().message( card + " was linked to: " + patrul.getName()  ).build() ).build() ) ); }

    public String generateToken () {
        byte[] bytes = new byte[ 24 ];
        this.secureRandom.nextBytes( bytes );
        return this.encoder.encodeToString( bytes ); }
}
