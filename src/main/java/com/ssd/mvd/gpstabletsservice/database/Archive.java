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
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;

@Data
public class Archive implements Runnable {
    public Boolean flag = true;
    private static Archive archive = new Archive();
    private final Map< Long, Card > cardMap = new HashMap<>(); // for Cards
    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder();
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

    public Mono< Card > getCard ( Long cardId ) { return this.cardMap.containsKey( cardId ) ? Mono.just( this.cardMap.get( cardId ) ) : Mono.empty() ; }

    public Mono< SelfEmploymentTask > get ( UUID uuid ) { return this.selfEmploymentTaskMap.containsKey( uuid ) ? Mono.just( this.selfEmploymentTaskMap.get( uuid ) ) : Mono.empty(); }

    public Flux< Patrul > getPatrulStatus ( com.ssd.mvd.gpstabletsservice.constants.Status status ) { return Flux.fromStream( this.getPatrulMonitoring().get( status ).stream() ); }

    // to get all existing SelfEmploymentTask
    public Flux< SelfEmploymentTask > getAllSelfEmploymentTask () { return Flux.fromStream( this.selfEmploymentTaskMap.values().stream() ); }

    public Flux< Card > getAllCards () { return Flux.fromStream( this.cardMap.values().stream() ); }

    // link new Patrul to existing SelfEmployment object
    public Mono< ApiResponseModel > save ( UUID uuid, Patrul patrul ) { return this.get( uuid ).flatMap( selfEmploymentTask -> {
        selfEmploymentTask.getPatruls().add( patrul.changeTaskStatus( ATTACHED ).getPassportNumber() );
        CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) );
        this.save( Notification.builder().notificationWasCreated( new Date() ).title( patrul.getName() + "joined the selfEmployment" + selfEmploymentTask.getTitle() ).build() );
        return RedisDataControl.getRedis().update( patrul ); } ); }

    // uses to link Card to current Patrul object, either additional Patrul in case of necessary
    public Mono< ApiResponseModel > save ( Patrul patrul, Card card ) {
        patrul.setCard( card.getCardId() ); // saving card id into patrul object
        patrul.setLatitudeOfTask( card.getLatitude() );
        patrul.setLongitudeOfTask( card.getLongitude() );
        patrul.changeTaskStatus( ATTACHED ); // changing his status to ATTACHED
        card.getPatruls().add( patrul ); // saving each patrul to card list
        card.setStatus( CREATED );
        System.out.println( card );
        this.cardMap.putIfAbsent( card.getCardId(), KafkaDataControl.getInstance().writeToKafka( card ) );
        this.save( Notification.builder()
                .type( "card 102" )
                .id( card.getCardId() )
                .address( card.getAddress() )
                .latitudeOfTask( card.getLatitude() )
                .notificationWasCreated( new Date() )
                .longitudeOfTask( card.getLongitude() )
                .passportSeries( patrul.getPassportNumber() )
                .title( card.getCardId() + " was linked to: " + patrul.getName() ).build() );
        return RedisDataControl.getRedis().update( patrul ).flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder().success( true ).status( Status.builder().message( card + " was linked to: " + patrul.getName()  ).build() ).build() ) ); }

    public void save ( Notification notification ) { this.getNotificationList().add( KafkaDataControl.getInstance().writeToKafka( notification ) ); }

    public Mono< ApiResponseModel > save ( SelfEmploymentTask selfEmploymentTask, Patrul patrul ) {
        if ( !this.selfEmploymentTaskMap.containsKey( selfEmploymentTask.getUuid() ) ) {
            System.out.println( selfEmploymentTask );
            selfEmploymentTask.setArrivedTime( new Date() ); // fixing time when the patrul reached
            patrul.changeTaskStatus( ARRIVED ).setSelfEmploymentId( selfEmploymentTask.getUuid() );
            patrul.setLongitudeOfTask( selfEmploymentTask.getLanOfAccident() );
            patrul.setLatitudeOfTask( selfEmploymentTask.getLatOfAccident() );
            this.selfEmploymentTaskMap.putIfAbsent( selfEmploymentTask.getUuid(), selfEmploymentTask ); // saving in Archive to manipulate in future
            this.save( Notification.builder()
                    .title( patrul.getName() + " set the Task to himself" )
                    .notificationWasCreated( new Date() ).build() );
            return RedisDataControl.getRedis().update( patrul ).flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder().status( Status.builder().message( "SelfEmployment was saved" ).code( 200 ).build() ).success( CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) ) ).build() ) );
        } else return Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "Wrong Data for Task" ).code( 201 ).build() ).build() ); }

    // taking off some Patrul from current Card
    public Mono< ApiResponseModel > removePatrulFromSelfEmployment ( UUID uuid, Patrul patrul ) { return this.selfEmploymentTaskMap.containsKey( uuid ) ?
            this.get( uuid ).flatMap( selfEmploymentTask -> {
                selfEmploymentTask.getPatruls().remove( patrul.changeTaskStatus( com.ssd.mvd.gpstabletsservice.constants.Status.FINISHED ) );
                CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) );
                this.save( Notification.builder().notificationWasCreated( new Date() ).title( patrul.getName() + " was removed from: " + selfEmploymentTask.getUuid() ).build() );
                return RedisDataControl.getRedis().update( patrul ); } )
            : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "there is no such a task" ).code( 201 ).build() ).build() ); }

    public String generateToken () {
        byte[] bytes = new byte[ 24 ];
        this.secureRandom.nextBytes( bytes );
        return this.encoder.encodeToString( bytes ); }

    @Override
    public void run () {
        while ( this.getFlag() ) { RedisDataControl.getRedis().getAllPatruls().subscribe( patrul -> {
            this.getPatrulMonitoring().get( patrul.getStatus() ).add( patrul );
            if ( patrul.getStatus().compareTo( NOT_AVAILABLE ) != 0 ) {
                patrul.setLastActiveDate( new Date() );
                patrul.setTotalActivityTime( patrul.getTotalActivityTime() + TimeInspector.getInspector().getTimestampForArchive() );
                if ( patrul.getStatus().equals( BUSY ) ) this.getPatrulMonitoring().get( patrul.getTaskStatus() ).add( patrul );
                RedisDataControl.getRedis().update( patrul ).subscribe(); } } );
            try { Thread.sleep( TimeInspector.getInspector().getTimestampForArchive() * 1000 ); } catch ( InterruptedException e ) { e.printStackTrace(); } finally { this.getPatrulMonitoring().values().forEach( List::clear ); }
            Flux.fromStream( this.cardMap.values().stream() ).filter( card -> card.getPatruls().size() == card.getReportForCardList().size() ).subscribe( card -> {
                card.setStatus( FINISHED );
                System.out.println( card );
                this.cardMap.remove( KafkaDataControl.getInstance().writeToKafka( card ).getCardId() ); } );
            Flux.fromStream( this.selfEmploymentTaskMap.values().stream() )
                    .filter( selfEmploymentTask -> selfEmploymentTask.getPatruls().size() == selfEmploymentTask.getReportForCards().size() )
                    .subscribe( selfEmploymentTask -> {
                        selfEmploymentTask.setTaskStatus( FINISHED );
                        CassandraDataControl.getInstance().addValue( selfEmploymentTask, SerDes.getSerDes().serialize( selfEmploymentTask ) );
                        this.selfEmploymentTaskMap.remove( selfEmploymentTask.getUuid() );
                        selfEmploymentTask.clear(); } ); } }
}
