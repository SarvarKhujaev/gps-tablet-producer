package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import com.ssd.mvd.gpstabletsservice.controller.UnirestController;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.entity.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.*;

import org.redisson.config.Config;
import org.redisson.Redisson;
import org.redisson.api.*;

public final class RedisDataControl {
    private String key;
    private final RMapReactive< Long, String > cardMap;
    private final RMapReactive< String, String > carMap;
    private final RMapReactive< String, String > patrulMap;
    private final RMapReactive< String, String > activeTasks;
    private RedissonReactiveClient redissonReactiveClient;

    private static RedisDataControl redisDataControl = new RedisDataControl();

    public static RedisDataControl getRedis () { return redisDataControl != null ? redisDataControl
            : ( redisDataControl = new RedisDataControl() ); }

    private RedisDataControl () { Config config = new Config();
        config.useSingleServer()
                .setAddress( "redis://" + GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.REDIS_HOST" ) + ":"
                        + GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.REDIS_PORT" ) )
                .setClientName( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.REDIS_CLIENT_NAME" ) )
                .setPassword( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.REDIS_PASSWORD" ) );
        this.redissonReactiveClient = Redisson.createReactive( config );
        this.activeTasks = this.redissonReactiveClient.getMap( "activeTasks" );
        this.patrulMap = this.redissonReactiveClient.getMap( "patrulMap" ); // for patrul
        this.cardMap = this.redissonReactiveClient.getMap( "cardMap" );
        this.carMap = this.redissonReactiveClient.getMap( "carMap" ); }

    public Flux< ReqCar > getAllCars () { return this.carMap.valueIterator()
            .flatMap( data -> Mono.just( SerDes.getSerDes().deserializeCar( data ) ) ); }

    public Flux< Patrul > getAllPatruls () { return this.patrulMap.valueIterator()
            .flatMap( value -> Mono.just( SerDes.getSerDes().deserialize( value ) ) ); }

    public Mono< Patrul > getPatrul ( String passportNumber ) { return this.patrulMap.get( passportNumber )
            .map( s -> SerDes.getSerDes().deserialize( s ) ); }

    public Mono< ReqCar > getCar ( String gosNumber ) { return this.carMap.get( gosNumber )
            .flatMap( value -> Mono.just( SerDes.getSerDes().deserializeCar( value ) ) ); }

    public Mono< ApiResponseModel > deleteCar ( String gosno ) { return this.carMap.containsKey( gosno )
            .onErrorStop()
            .flatMap( aBoolean -> aBoolean ?
                    this.carMap.remove( gosno )
                            .map( s -> SerDes.getSerDes().deserializeCar( s ) )
                            .flatMap( reqCar ->
                                    this.getPatrul( reqCar.getPatrulPassportSeries() )
                                            .flatMap( patrul1 -> {
                                                patrul1.setCarNumber( null );
                                                return this.update( patrul1 )
                                                        .onErrorStop()
                                                        .flatMap( aLong -> Mono.just( ApiResponseModel.builder()
                                                                .success( true )
                                                                .status( Status.builder()
                                                                        .code( 200 )
                                                                        .message( gosno + " was deleted" )
                                                                        .build() ).build() ) ); } ) )
                    : Mono.just( ApiResponseModel.builder().status( Status.builder().code( 201 )
                    .message( gosno + " does not exists" ).build() ).build() ) ); } // deleting current carMap

    public Mono< ApiResponseModel > addValue ( Patrul patrul ) {
        patrul.setInPolygon( false );
        patrul.setTuplePermission( false );
        patrul.setUuid( UUID.randomUUID() );
        patrul.setStatus( com.ssd.mvd.gpstabletsservice.constants.Status.FREE );
        patrul.setTaskTypes( com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FREE );
        patrul.setSurnameNameFatherName( patrul.getName() + " " + patrul.getSurname() + " " + patrul.getFatherName() );
        return this.patrulMap.fastPutIfAbsent( patrul.getPassportNumber(), ( key = SerDes.getSerDes().serialize( patrul ) ) )
                .log()
                .onErrorStop()
                .flatMap( aBoolean -> aBoolean ?
                    Mono.just( ApiResponseModel.builder()
                            .success( UnirestController.getInstance()
                                    .addUser( patrul ) )
                            .status( Status.builder()
                                    .message( "new patrul was added" )
                                    .code( 200 )
                                    .build() )
                            .build() )
                    : Mono.just( ApiResponseModel.builder()
                        .status( Status.builder()
                            .message( "this patrul already exists" )
                                .code( 201 )
                                .build() )
                        .build() ) ); }

    public Mono< ApiResponseModel > addValue ( ReqCar reqCar ) { return this.carMap
            .fastPutIfAbsent( reqCar.getUuid().toString(), ( this.key = SerDes.getSerDes().serialize( reqCar ) ) )
            .flatMap( aBoolean -> aBoolean ?
                    this.getPatrul( reqCar.getPatrulPassportSeries() )
                    .flatMap( patrul -> {
                        patrul.setCarNumber( reqCar.getGosNumber() );
                        CassandraDataControl.getInstance().addValue( reqCar ); // saving updated version of car
                        return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), ( this.key = SerDes.getSerDes().serialize( patrul ) ) )
                                .flatMap( aBoolean1 -> aBoolean1 ? Mono.just( ApiResponseModel.builder()
                                        .success( true )
                                        .status( Status.builder()
                                                .message( "Car was saved" )
                                                .code( 200 )
                                                .build() )
                                        .build() )
                                        : Mono.just( ApiResponseModel.builder()
                                        .status( Status.builder()
                                        .message( "Wrong Patrul data" )
                                                .code( 201 ).build() )
                                        .success( false )
                                        .build() ) );
                    } ) : Mono.just( ApiResponseModel.builder()
                    .status( Status.builder()
                            .code( 201 )
                            .message( "Wrong Car data: " + reqCar.getUuid() )
                            .build() ).success( false ).build() ) ); }

    public Mono< ApiResponseModel > update ( Patrul patrul ) { return this.patrulMap
            .fastPutIfExists( patrul.getPassportNumber(), ( this.key = SerDes.getSerDes().serialize( patrul ) ) )
            .flatMap( aBoolean -> aBoolean ?
                    Mono.just( ApiResponseModel.builder()
                                    .success( true )
                                    .status( Status.builder()
                                            .message( "patrul was updated successfully" )
                                            .code( 200 )
                                            .build() )
                            .build() )
                            : Mono.just( ApiResponseModel.builder()
                            .success( false )
                            .status( Status.builder()
                                    .message( "this patrul does not exist" )
                                    .code( 201 )
                                    .build() )
                            .build() ) ); }

    public Mono< ApiResponseModel > update ( ReqCar reqCar ) {
        return this.carMap.containsKey( reqCar.getUuid().toString() )
            .flatMap( aBoolean -> aBoolean ?
            this.carMap.get( reqCar.getUuid().toString() )
                    .map( s -> SerDes.getSerDes().deserializeCar( s ) )
                    .flatMap( reqCar1 -> {
                if ( !reqCar1.getPatrulPassportSeries().equals( reqCar.getPatrulPassportSeries() ) ) return this.patrulMap
                        .get( reqCar1.getPatrulPassportSeries() )
                        .map( s -> SerDes.getSerDes().deserialize( s ) )
                        .flatMap( patrul -> { patrul.setCarNumber( null );
                            return this.update( patrul )
                                    .flatMap( aBoolean1 -> this.patrulMap.get( reqCar.getPatrulPassportSeries() )
                                    .map( s -> SerDes.getSerDes().deserialize( s ) )
                                            .flatMap( patrul1 -> { patrul1.setCarNumber( reqCar.getGosNumber() );
                                                return this.update( patrul1 )
                                                .flatMap( aBoolean2 -> this.carMap.fastPutIfExists( reqCar.getUuid().toString(),
                                                                ( this.key = SerDes.getSerDes().serialize( reqCar ) ) )
                                                        .flatMap( aBoolean3 -> Mono.just( ApiResponseModel.builder()
                                                                .status( Status.builder()
                                                                        .message( "Car was updated" )
                                                                        .code( 200 )
                                                                        .build() )
                                                                .success( CassandraDataControl.getInstance()
                                                                        .addValue( reqCar ) ).build() ) ) ); } ) );
                        } );
                else return this.carMap.fastPutIfExists( reqCar.getUuid().toString(),
                                ( this.key = SerDes.getSerDes().serialize( reqCar ) ) )
                        .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                                            .status( Status.builder()
                                                    .message( "Car data was successfully updated" )
                                                    .code( 200 )
                                                    .build() )
                                    .success( CassandraDataControl.getInstance().addValue( reqCar ) ).build() ) );
            } ) : Mono.just( ApiResponseModel.builder().success( false )
            .status( Status.builder().code( 201 ).message( "Wrong Car data" ).build() ).build() ) ); }

    public void addValue ( Card card ) {
        this.addValue( card.getCardId().toString(), new ActiveTask( card ) );
        this.cardMap.fastPutIfAbsent( card.getCardId(), SerDes.getSerDes().serialize( card ) ).subscribe(); }

    public void update ( Card card ) { this.cardMap.fastPutIfExists( card.getCardId(),
            SerDes.getSerDes().serialize( card ) ).subscribe(); }

    public Mono< Card > getCard ( Long cardId ) { return this.cardMap.get( cardId )
            .flatMap( s -> Mono.just( SerDes.getSerDes().deserializeCard( s ) ) ); }

    public void remove ( Long cardId ) { this.cardMap.remove( cardId ).subscribe(); }

    public Mono< Boolean > addValue ( String id, ActiveTask activeTask ) {
        return this.activeTasks.containsKey( id )
                .flatMap( aBoolean -> aBoolean ? this.activeTasks.fastPutIfExists( id, KafkaDataControl.getInstance()
                        .writeToKafka( SerDes.getSerDes().serialize( activeTask ) ) )
                        : this.activeTasks.fastPutIfAbsent( id, KafkaDataControl.getInstance()
                        .writeToKafka( SerDes.getSerDes().serialize( activeTask ) ) ) ); }

    public void remove ( String id ) { this.activeTasks.remove( id ).subscribe(); }

    public Flux< ApiResponseModel > addAllPatrulsToChatService ( String token ) { return this.getAllPatruls()
            .flatMap( patrul -> {
                patrul.setSpecialToken( token );
                return Mono.just(
                        ApiResponseModel.builder()
                                .success( UnirestController.getInstance().addUser( patrul ) )
                                .status( Status.builder()
                                        .message( patrul.getPassportNumber() + "Successfully added to chat service" )
                                        .code( 200 )
                                        .build()
                                ).build() ); } ); }

    public Flux< ActiveTask > getActiveTasks() { return this.activeTasks.valueIterator()
            .flatMap( s -> Mono.just( SerDes.getSerDes().deserializeActiveTask( s ) ) ); }
}
