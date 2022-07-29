package com.ssd.mvd.gpstabletsservice.database;

import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.security.SecureRandom;

import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.response.Status;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;

@Data
public class Archive implements Runnable {
    public Boolean flag = true;
    private static Archive archive = new Archive();
    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder();
    private final Map< UUID, SelfEmploymentTask > selfEmploymentTaskMap = new HashMap<>();

    public static Archive getAchieve () { return archive != null ? archive : ( archive = new Archive() ); }

    private Archive () { CassandraDataControl.getInstance().resetData(); }

    public Mono< SelfEmploymentTask > get ( UUID uuid ) { return this.selfEmploymentTaskMap.containsKey( uuid ) ? Mono.just( this.selfEmploymentTaskMap.get( uuid ) ) : Mono.empty(); }

    // to get all existing SelfEmploymentTask
    public Flux< SelfEmploymentTask > getAllSelfEmploymentTask () { return Flux.fromStream( this.selfEmploymentTaskMap.values().stream() ); }

    // link new Patrul to existing SelfEmployment object
    public Mono< ApiResponseModel > save ( UUID uuid, Patrul patrul ) { return this.get( uuid ).flatMap( selfEmploymentTask -> {
        selfEmploymentTask.getPatruls().put( patrul.getPassportNumber(), patrul.changeTaskStatus( ATTACHED, selfEmploymentTask ) );
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
            if ( selfEmploymentTask.getTaskStatus().compareTo( ARRIVED ) == 0 ) patrul.changeTaskStatus( ARRIVED, selfEmploymentTask );
            else patrul.changeTaskStatus( ACCEPTED, selfEmploymentTask );
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
                patrul.changeTaskStatus( CANCEL, selfEmploymentTask );
                CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) );
                return RedisDataControl.getRedis().update( patrul ); } )
            : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "there is no such a task" ).code( 201 ).build() ).build() ); }

    public Mono< ApiResponseModel > addNewPatrulToCard ( Long cardId, Patrul patrul ) { return RedisDataControl.getRedis().getCard( cardId )
            .flatMap( card -> {
                patrul.changeTaskStatus( ATTACHED, card );
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
                                .status( Status.builder().message( patrul.getName() + " linked to " + card.getCardId() ).code( 200 ).build() )
                        .build() ); } ); }

    public Mono< ApiResponseModel > removePatrulFromCard ( Long cardId, Patrul patrul ) { return RedisDataControl.getRedis().getCard( cardId )
            .flatMap( card -> {
                patrul.changeTaskStatus( CANCEL, card );
                return Mono.just( ApiResponseModel.builder()
                        .success( true )
                        .status( Status.builder().message( patrul.getName() + " removed from: " + card.getCardId() ).code( 200 ).build() )
                        .build() ); } ); }

    // uses to link Card to current Patrul object, either additional Patrul in case of necessary
    public Mono< ApiResponseModel > save ( Patrul patrul, Card card ) {
        patrul.changeTaskStatus( ATTACHED, card ); // changing his status to ATTACHED
        RedisDataControl.getRedis().addValue( KafkaDataControl.getInstance().writeToKafka( card ) );
        this.save( Notification.builder()
                .type( "card 102" )
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

    public String generateToken () {
        byte[] bytes = new byte[ 24 ];
        this.secureRandom.nextBytes( bytes );
        return this.encoder.encodeToString( bytes ); }

    @Override
    public void run () {
        while ( this.getFlag() ) {
//            RedisDataControl.getRedis().getAllPatruls().subscribe( patrul -> {
//            if ( patrul.getStatus().compareTo( NOT_AVAILABLE ) != 0 ) {
//                patrul.setLastActiveDate( new Date() );
//                patrul.setTotalActivityTime( patrul.getTotalActivityTime() + TimeInspector.getInspector().getTimestampForArchive() );
//                RedisDataControl.getRedis().update( patrul ).subscribe(); } } );
            try { Thread.sleep( TimeInspector.getInspector().getTimestampForArchive() * 1000 ); } catch ( InterruptedException e ) { e.printStackTrace(); }
            RedisDataControl.getRedis().getAllCards()
                    .filter( card -> card.getPatruls().size() == card.getReportForCardList().size() )
                    .subscribe( card -> {
                        card.setStatus( FINISHED );
                        RedisDataControl.getRedis().remove( card.getCardId() );
                        RedisDataControl.getRedis().remove( card.getCardId().toString() ); } );
                this.getAllSelfEmploymentTask()
                        .filter( selfEmploymentTask -> selfEmploymentTask.getPatruls().size() == selfEmploymentTask.getReportForCards().size() )
                        .subscribe( selfEmploymentTask -> {
                            selfEmploymentTask.setTaskStatus( FINISHED );
                            RedisDataControl.getRedis().remove( selfEmploymentTask.getUuid().toString() );
                            CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) );
                            this.selfEmploymentTaskMap.remove( selfEmploymentTask.getUuid() ); } ); }
        }
}
