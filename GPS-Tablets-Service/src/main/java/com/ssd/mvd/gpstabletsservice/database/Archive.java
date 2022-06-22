package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.payload.ReqExchangeLocation;
import com.ssd.mvd.gpstabletsservice.payload.ReqLocationExchange;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Stream;

@Data
public class Archive implements Runnable {
    public Boolean flag = true;
    private static Archive archive = new Archive();
    private final Map< UUID, Card > cardMap = new HashMap<>(); // for Cards
    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder();
    private final Map< String, Trackers > inspector = new HashMap<>(); // for Trackers
    private final Map< String, ReqLocationExchange > map = new HashMap<>(); // for saving data from KafkaConsumer
    private final LinkedHashSet< Notification > notificationList = new LinkedHashSet<>(); // for all notifications
    private final Map< UUID, SelfEmploymentTask > selfEmploymentTaskMap = new HashMap<>();
    private final Map< com.ssd.mvd.gpstabletsservice.constants.Status, List< Patrul > > patrulMonitoring = new HashMap<>(); // to check all Patruls

    public static Archive getAchieve () { return archive != null ? archive : ( archive = new Archive() ); }

    private Archive() {
        this.getPatrulMonitoring().put( com.ssd.mvd.gpstabletsservice.constants.Status.NOT_AVAILABLE, new ArrayList<>() );
        this.getPatrulMonitoring().put( com.ssd.mvd.gpstabletsservice.constants.Status.AVAILABLE, new ArrayList<>() );
        this.getPatrulMonitoring().put( com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED, new ArrayList<>() );
        this.getPatrulMonitoring().put( com.ssd.mvd.gpstabletsservice.constants.Status.FINISHED, new ArrayList<>() );
        this.getPatrulMonitoring().put( com.ssd.mvd.gpstabletsservice.constants.Status.ARRIVED, new ArrayList<>() );
        this.getPatrulMonitoring().put( com.ssd.mvd.gpstabletsservice.constants.Status.BUSY, new ArrayList<>() );
        this.getPatrulMonitoring().put( com.ssd.mvd.gpstabletsservice.constants.Status.FREE, new ArrayList<>() );
        CassandraDataControl.getInstance().resetData(); }

    public Flux< Notification > getCard() { return Flux.fromStream( this.notificationList.stream() ).sort(); }

    public Mono< Card > getCard ( UUID cardId ) { return this.cardMap.containsKey( cardId ) ? Mono.just( this.cardMap.get( cardId ) ) : Mono.empty() ; }

    public Mono< SelfEmploymentTask > get ( UUID uuid ) { return this.selfEmploymentTaskMap.containsKey( uuid ) ? Mono.just( this.selfEmploymentTaskMap.get( uuid ) ) : Mono.empty(); }

    public Flux< Patrul > getPatrulStatus ( String status ) { return Flux.fromStream( this.getPatrulMonitoring().get( status ).stream() ); }

    // to get all existing SelfEmploymentTask
    public Flux< SelfEmploymentTask > getAllSelfEmploymentTask () { return Flux.fromStream( this.selfEmploymentTaskMap.values().stream() ); }

    public Flux< Card > getAllCards () { return Flux.fromStream( this.cardMap.values().stream() ); }

    public Mono< ApiResponseModel > save ( Card card ) { return !this.getCardMap().containsKey( card.getUuid() ) ?
            Mono.just( ApiResponseModel.builder().success( this.getCardMap().putIfAbsent( card.getUuid(), card ) != null && this.getNotificationList().add( Notification.builder().status( false ).title( card.getCardId() + " card yaratildi" ).build() ) )
                    .status( Status.builder().message( "new Card was added" ).code( 200 ).build() ).build() ) : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "this Card exists" ).code( 201 ).build() ).build() ); }

    // link new Patrul to existing SelfEmployment object
    public Mono< ApiResponseModel > save ( UUID uuid, Patrul patrul ) { return this.get( uuid ).flatMap( selfEmploymentTask -> {
        selfEmploymentTask.getPatruls().add( patrul.changeTaskStatus( com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED ).getPassportNumber() );
        CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) );
        this.save( Notification.builder().status( true ).patrul( patrul ).notificationWasCreated( new Date() ).title( patrul.getName() + "joined the selfEmployment" + selfEmploymentTask.getTitle() ).build() );
        return RedisDataControl.getRedis().update( patrul ); } ); }

    // uses to link Card to current Patrul object, either additional Patrul in case of necessary
    public Mono< ApiResponseModel > save ( Patrul patrul, UUID cardId ) { return this.getCard( cardId ).flatMap( card -> {
        patrul.setCard( cardId );
        card.getPatruls().add( patrul.changeTaskStatus( com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED ) );
        return RedisDataControl.getRedis().update( patrul ).flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder().success( this.notificationList.add( Notification.builder().patrul( patrul ).status( false ).title( patrul.getName() + "Topshiriq qabul qildi" ).build() ) ).status( Status.builder().message( card + " was linked to: " + patrul.getName()  ).build() ).build() ) ); } ); }

    public void save ( Notification notification ) { this.getNotificationList().add( KafkaDataControl.getInstance().writeToKafka( notification ) ); }

    public Mono< ApiResponseModel > save ( SelfEmploymentTask selfEmploymentTask, Patrul patrul ) {
        if ( !this.selfEmploymentTaskMap.containsKey( selfEmploymentTask.getUuid() ) ) {
            patrul.setTaskDate( new Date() );
            patrul.setStatus( com.ssd.mvd.gpstabletsservice.constants.Status.BUSY );
            this.selfEmploymentTaskMap.putIfAbsent( selfEmploymentTask.getUuid(), selfEmploymentTask ); // saving in Archive to manipulate in future
            CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) );
            patrul.changeTaskStatus( com.ssd.mvd.gpstabletsservice.constants.Status.ARRIVED ).setSelfEmploymentId( selfEmploymentTask.getUuid() );
//            this.save( Notification.builder().patrul( patrul ).status( false ).title( patrul.getName() + " set the Task to himself" ).notificationWasCreated( new Date() ).build() );
            return RedisDataControl.getRedis().update( patrul );
        } else return Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "Wrong Data for Task" ).code( 201 ).build() ).build() ); }

    // taking off some Patrul from current Card
    public Mono< ApiResponseModel > removePatrulFromSelfEmployment ( UUID uuid, Patrul patrul ) { return this.selfEmploymentTaskMap.containsKey( uuid ) ?
            this.get( uuid ).flatMap( selfEmploymentTask -> {
                selfEmploymentTask.getPatruls().remove( patrul.changeTaskStatus( com.ssd.mvd.gpstabletsservice.constants.Status.FINISHED ) );
                CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) );
                this.save( Notification.builder().notificationWasCreated( new Date() ).patrul( patrul ).title( patrul.getName() + " was removed from: " + selfEmploymentTask.getUuid() ).status( false ).build() );
                return RedisDataControl.getRedis().update( patrul ); } )
            : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "there is no such a task" ).code( 201 ).build() ).build() ); }

    public void save ( ReqExchangeLocation id ) {
        if ( !this.inspector.containsKey( id.getPassport() ) ) this.inspector.putIfAbsent( id.getPassport(), CassandraDataControl.getInstance().addValue( new Trackers( id.getPassport() ) ) );
        else this.inspector.get( id.getPassport() ).setStatus( true ); } // updating just time

    public Boolean switchOffInspector ( String trackerId ) { CassandraDataControl.getInstance().delete( CassandraDataControl.getInstance().tablets, trackerId );
        return this.getInspector().remove( Long.parseLong( trackerId ) ).setStatus( false ).getKafkaConsumer().status; }

    public Stream< ReqLocationExchange > getPos () { return this.getMap().values().stream(); }

    public Flux< Trackers > getAllTrackers () { return Flux.fromStream( this.getInspector().values().stream() ); }

    public Mono< ReqLocationExchange > getPos ( String id ) { return Mono.justOrEmpty( this.map.get( id ) ); }

    public synchronized void save ( String topicName, String position ) { this.map.put( topicName, SerDes.getSerDes().deserializeReqLocation( position ) ); }

    public String generateToken () {
        byte[] bytes = new byte[ 24 ];
        this.secureRandom.nextBytes( bytes );
        return this.encoder.encodeToString( bytes ); }

    public void clear () {
        archive = null;
        this.setFlag( false );
        this.getMap().clear();
        this.getCardMap().clear();
        this.getInspector().clear();
        this.getNotificationList().clear();
        this.getPatrulMonitoring().clear();
        this.getSelfEmploymentTaskMap().clear(); }

    @Override
    public void run () {
        while ( this.getFlag() ) { RedisDataControl.getRedis().getAllPatruls().subscribe( patrul -> {
            this.getPatrulMonitoring().get( patrul.getStatus() ).add( patrul );
            if ( !patrul.getStatus().equals( com.ssd.mvd.gpstabletsservice.constants.Status.NOT_AVAILABLE ) ) {
                patrul.setLastActiveDate( new Date() );
                patrul.setTotalActivityTime( patrul.getTotalActivityTime() + TimeInspector.getInspector().getTimestampForArchive() );
                if ( !patrul.getStatus().equals( com.ssd.mvd.gpstabletsservice.constants.Status.FREE ) ) this.getPatrulMonitoring().get( patrul.getTaskStatus() ).add( patrul );
                RedisDataControl.getRedis().update( patrul ).subscribe(); } } );
            try { Thread.sleep( TimeInspector.getInspector().getTimestampForArchive() * 1000 ); } catch ( InterruptedException e ) { e.printStackTrace(); } finally { this.getPatrulMonitoring().values().forEach( List::clear ); }
            Flux.fromStream( this.cardMap.values().stream() ).filter( card -> card.getPatruls().size() == 0 && card.getReportForCards().size() > 0 ).subscribe( card -> {
                KafkaDataControl.getInstance().writeToKafka( card ); // saving into Kafka all cards which are done
                this.cardMap.remove( card.getUuid() );
                card.clear(); } );
            Flux.fromStream( () -> this.inspector.values().stream().peek( trackers -> CassandraDataControl.getInstance().addValue( trackers.setStatus( TimeInspector.getInspector().compareTime( trackers.getDate() ) ) ) ) ).doOnError( throwable -> this.clear() ).subscribe();
            Flux.fromStream( this.selfEmploymentTaskMap.values().stream() ).filter( selfEmploymentTask -> selfEmploymentTask.getPatruls().size() == selfEmploymentTask.getReportForCards().size() ).subscribe( selfEmploymentTask -> {
                CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) );
                this.selfEmploymentTaskMap.remove( KafkaDataControl.getInstance().writeToKafka( selfEmploymentTask ).getUuid() );
                selfEmploymentTask.clear(); } ); } }
}
