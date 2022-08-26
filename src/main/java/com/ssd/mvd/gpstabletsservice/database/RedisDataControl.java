package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.response.PatrulActivityStatistics;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import com.ssd.mvd.gpstabletsservice.controller.UnirestController;
import com.ssd.mvd.gpstabletsservice.request.PatrulLoginRequest;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.entity.*;

import java.nio.charset.StandardCharsets;
import java.util.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

    public Mono< ApiResponseModel > deletePatrul ( String passportNumber ) {
        String passport = passportNumber.split( "_" )[0];
        String token = passportNumber.split( "_" )[1];
        return this.patrulMap
            .containsKey( passport )
            .flatMap( aBoolean -> aBoolean ?
                this.patrulMap.remove( passport )
                        .log()
                        .onErrorStop()
                        .flatMap( aLong -> this.getPatrul( passport )
                                .flatMap( patrul -> {
                                    patrul.setSpecialToken( token );
                                    return CassandraDataControl
                                            .getInstance()
                                            .delete(
                                                    CassandraDataControl
                                                            .getInstance()
                                                            .getPatrols(),
                                                    "uuid",
                                                    patrul.getUuid().toString() ); } ) )
                : Mono.just( ApiResponseModel.builder()
                    .success( false )
                    .status( Status.builder()
                            .code( 201 )
                            .message( passport + " does not exists" )
                            .build() )
                    .build() ) ); } // deleting current car

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
                                                                .success( CassandraDataControl
                                                                        .getInstance()
                                                                        .deleteCar( gosno ) )
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
        CassandraDataControl
                .getInstance()
                .addValue( patrul );
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
                        CassandraDataControl.getInstance().addValue( reqCar, this.key ); // saving updated version of car
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
                                    .success( CassandraDataControl
                                            .getInstance()
                                            .addValue( patrul )
                                            && UnirestController
                                            .getInstance()
                                            .updateUser( patrul ) )
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
                                                                        .addValue( reqCar, this.key ) ).build() ) ) ); } ) );
                        } );
                else return this.carMap.fastPutIfExists( reqCar.getUuid().toString(),
                                ( this.key = SerDes.getSerDes().serialize( reqCar ) ) )
                        .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                                            .status( Status.builder()
                                                    .message( "Car data was successfully updated" )
                                                    .code( 200 )
                                                    .build() )
                                    .success( CassandraDataControl.getInstance().addValue( reqCar, this.key ) ).build() ) );
            } ) : Mono.just( ApiResponseModel.builder().success( false )
            .status( Status.builder().code( 201 ).message( "Wrong Car data" ).build() ).build() ) ); }

    public String decode ( String token ) { return new String( Base64.getDecoder()
            .decode( token ) )
            .split( "@" )[ 0 ]; }

    // uses when Patrul wants to change his status from active to pause
    public Mono< ApiResponseModel > setInPause ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) )
            .flatMap( aBoolean -> aBoolean ?
            this.patrulMap.get( this.key )
                    .map( s -> SerDes.getSerDes().deserialize( s ) )
                    .flatMap( patrul -> this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) )
                    .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGIN ) )
                            .status( Status.builder().message( "Patrul set in pause" ).code( 200 ).build() ).build() ) ) )
            : Mono.just( ApiResponseModel.builder()
                    .success( false )
                    .status( Status.builder()
                            .message( "Wrong login or password" )
                            .code( 201 )
                            .build() )
                    .build() ) ); }

    // uses when Patrul wants to change his status from pause to active
    public Mono< ApiResponseModel > backToWork ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) )
            .flatMap( aBoolean -> aBoolean ?
            this.patrulMap.get( this.key )
                    .map( s -> SerDes.getSerDes().deserialize( s ) )
                    .flatMap( patrul -> this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) )
                    .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.RETURNED_TO_WORK ) )
                            .status( Status.builder().message( "Patrul returned to work" ).code( 200 ).build() ).build() ) ) )
                    : Mono.just( ApiResponseModel.builder()
                        .success( false )
                                .status( Status.builder()
                                        .message( "Wrong login or password" )
                                        .code( 201 )
                                        .build() ).build() ) ); }

    // sets every day when Patrul start to work in morning
    public Mono< ApiResponseModel > startToWork ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) )
            .flatMap( aBoolean -> aBoolean ?
            this.getPatrul( this.key )
                    .flatMap( patrul -> {
                        patrul.setTotalActivityTime( 0L ); // set to 0 every day
                        patrul.setStartedToWorkDate( new Date() ); // registration of time every day
                        return this.update( patrul )
                                .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                                        .success( CassandraDataControl.getInstance()
                                                .login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.START_TO_WORK ) )
                                        .status( Status.builder()
                                                .message( "Patrul started to work" )
                                                .code( 200 )
                                                .build() ).build() ) );
                    } ) : Mono.just( ApiResponseModel.builder().success( false )
                    .status( Status.builder()
                    .message( "Wrong login or password" )
                    .code( 201 )
                    .build() ).build() ) ); }

    // uses when patrul finishes his work in the evening
    public Mono< ApiResponseModel > stopToWork ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) )
            .flatMap( aBoolean -> aBoolean ?
            this.patrulMap.get( this.key )
                    .map( s -> SerDes.getSerDes().deserialize( s ) )
                    .flatMap( patrul -> this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) )
                    .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance()
                                    .login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.STOP_TO_WORK ) )
                            .status( Status.builder()
                                    .message( "Patrul stopped his job" )
                                    .code( 200 )
                                    .build() )
                            .build() ) ) )
            : Mono.just( ApiResponseModel.builder()
                    .success( false )
                    .status( Status.builder()
                            .message( "Wrong login or password" )
                            .code( 201 )
                            .build() ).build() ) ); }

    // uses when Patrul login to account after some time
    public Mono< ApiResponseModel > login ( PatrulLoginRequest patrulLoginRequest ) { return this.patrulMap
            .containsKey( patrulLoginRequest.getPassportSeries() )
            .flatMap( aBoolean -> aBoolean ?
                this.patrulMap.get( patrulLoginRequest.getPassportSeries() )
                        .map( s -> SerDes.getSerDes().deserialize( s ) )
                        .flatMap( patrul -> {
                            if ( patrul.getPassword()
                                    .equals( patrulLoginRequest.getPassword() ) ) {
                                patrul.setStartedToWorkDate( new Date() );
                                patrul.setSimCardNumber( patrulLoginRequest.getSimCardNumber() );
                                patrul.setTokenForLogin( Base64.getEncoder().encodeToString( ( patrul.getPassportNumber()
                                        + "@" + patrul.getPassword()
                                        + "@" + Archive.getAchieve().generateToken() )
                                        .getBytes( StandardCharsets.UTF_8 ) ) );
                                return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) )
                                        .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                                                .data( Data.builder().data( patrul ).build() )
                                                .success( CassandraDataControl.getInstance()
                                                        .login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGIN ) )
                                                .status( Status.builder()
                                                        .message( "Authentication successfully passed" )
                                                        .code( 200 ).build() )
                                                .build() ) );
                            } else return Mono.just( ApiResponseModel.builder()
                                    .status( Status.builder()
                                            .code( 201 )
                                            .message( "Wrong Login or password" )
                                            .build() )
                                    .success( false )
                                    .build() );
                } ) : Mono.just( ApiResponseModel.builder()
                    .status( Status.builder()
                            .message( "Wrong Login or Password" )
                            .code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > logout ( String token ) { return this.patrulMap.get( this.decode( token ) )
            .map( s -> SerDes.getSerDes().deserialize( s ) )
            .flatMap( patrul -> {
                patrul.setTokenForLogin( null );
                return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) )
                        .flatMap( aBoolean -> Mono.just( ApiResponseModel.builder()
                                .success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGOUT ) )
                                .status( Status.builder().message( "See you soon my darling )))" ).code( 200 ).build() ).build() ) ); } ); }

    public Mono< PatrulActivityStatistics > getPatrulStatistics ( Request request ) { return this.patrulMap
            .get( request.getData() )
            .map( s -> SerDes.getSerDes().deserialize( s ) )
            .flatMap( patrul -> CassandraDataControl.getInstance().getPatrulStatistics( request ) ); }

    public Mono< ApiResponseModel > accepted ( String token ) { return this.patrulMap.get( this.decode( token ) )
            .map( s -> SerDes.getSerDes().deserialize( s ) )
            .flatMap( patrul -> TaskInspector.getInstance().changeTaskStatus( patrul, ACCEPTED )
                    .flatMap( apiResponseModel -> this.update( patrul )
                            .flatMap( apiResponseModel1 -> Mono.just( apiResponseModel ) ) ) ); }

    public Mono< ApiResponseModel > arrived ( String token ) { return this.patrulMap.get( this.decode( token ) )
            .map( s -> SerDes.getSerDes().deserialize( s ) )
            .flatMap( patrul -> TaskInspector.getInstance().changeTaskStatus( patrul, ARRIVED )
                    .flatMap( apiResponseModel -> this.update( patrul )
                            .flatMap( apiResponseModel1 -> Mono.just( apiResponseModel ) ) ) ); }

    public Mono< ApiResponseModel > checkToken ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) )
            .flatMap( aBoolean -> aBoolean ?
                this.patrulMap.get( this.key )
                        .map( s -> SerDes.getSerDes().deserialize( s ) )
                        .flatMap( patrul -> Mono.just( ApiResponseModel.builder()
                                .data( Data.builder().data( patrul ).build() )
                                .status( Status.builder()
                                        .message( patrul.getUuid().toString() )
                                        .code( 200 )
                                        .build() )
                                .success( true ).build() ) )
                : Mono.just( ApiResponseModel.builder().status( Status.builder()
                    .message( "Wrong token" )
                    .code( 201 )
                    .build() )
                    .success( false )
                    .build() ) ); }

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
