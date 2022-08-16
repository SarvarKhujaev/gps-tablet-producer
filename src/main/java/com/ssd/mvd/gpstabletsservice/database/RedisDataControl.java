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
    private final RMapReactive< UUID, String > lustraMap;
    private final RMapReactive< String, String > patrulMap;
    private final RMapReactive< UUID, String > policeTypes;
    private final RMapReactive< String, String > polygonMap;
    private final RMapReactive< String, String > activeTasks;
    private final RMapReactive< UUID, String > polygonTypeMap;
    private final RMapReactive< String, String > polygonForPatrulMap;
    private RedissonReactiveClient redissonReactiveClient;

    private static RedisDataControl redisDataControl = new RedisDataControl();

    public static RedisDataControl getRedis () { return redisDataControl != null ? redisDataControl : ( redisDataControl = new RedisDataControl() ); }

    private RedisDataControl () { Config config = new Config();
        config.useSingleServer()
                .setAddress( "redis://" + GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.REDIS_HOST" ) + ":"
                        + GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.REDIS_PORT" ) )
                .setClientName( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.REDIS_CLIENT_NAME" ) )
                .setPassword( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.REDIS_PASSWORD" ) );
        this.redissonReactiveClient = Redisson.createReactive( config );
        this.polygonForPatrulMap = this.redissonReactiveClient.getMap( "polygonForPatrulMap" ); // for polygons with schedule
        this.polygonTypeMap = this.redissonReactiveClient.getMap( "polygonTypeMap" ); // for polygons
        this.activeTasks = this.redissonReactiveClient.getMap( "activeTasks" );
        this.policeTypes = this.redissonReactiveClient.getMap( "policeType" );
        this.polygonMap = this.redissonReactiveClient.getMap( "polygonMap" ); // for polygons
        this.lustraMap = this.redissonReactiveClient.getMap( "lustraMap" ); // for lustra cameras
        this.patrulMap = this.redissonReactiveClient.getMap( "patrulMap" ); // for patrul
        this.cardMap = this.redissonReactiveClient.getMap( "cardMap" );
        this.carMap = this.redissonReactiveClient.getMap( "carMap" ); }

    public Flux< ReqCar > getAllCars () { return this.carMap.valueIterator()
            .flatMap( data -> Mono.just( SerDes.getSerDes().deserializeCar( data ) ) ); }

    public Flux< Patrul > getAllPatruls () { return this.patrulMap.valueIterator()
            .flatMap( value -> Mono.just( SerDes.getSerDes().deserialize( value ) ) ); }

    public Flux< AtlasLustra > getAllLustra () { return this.lustraMap.valueIterator()
            .flatMap( value -> Mono.just( SerDes.getSerDes().deserializeLustra( value ) ) ); }

    public Flux< Polygon > getAllPolygons () { return this.polygonMap.valueIterator()
            .flatMap( value -> Mono.just( SerDes.getSerDes().deserializePolygon( value ) ) ); }

    public Flux< PoliceType > getAllPoliceTypes () { return this.policeTypes.valueIterator()
            .flatMap( s -> Mono.just( SerDes.getSerDes().deserializePoliceType( s ) ) ); }

    public Flux< PolygonType > getAllPolygonTypes () { return this.polygonTypeMap.valueIterator()
            .flatMap( value -> Mono.just( SerDes.getSerDes().deserializePolygonType( value ) ) ); }

    public Flux< Polygon > getAllPolygonsForPatrul () { return this.polygonForPatrulMap.valueIterator()
            .flatMap( value -> Mono.just( SerDes.getSerDes().deserializePolygon( value ) ) ); }

    public Mono< Patrul > getPatrul ( String passportNumber ) { return this.patrulMap.get( passportNumber )
            .map( s -> SerDes.getSerDes().deserialize( s ) ); }

    public Mono< ReqCar > getCar ( String gosNumber ) { return this.carMap.get( gosNumber )
            .flatMap( value -> Mono.just( SerDes.getSerDes().deserializeCar( value ) ) ); }

    public Mono< PolygonType > getPolygonType ( UUID uuid ) { return this.polygonTypeMap.get( uuid )
            .flatMap( value -> value != null ? Mono.just( SerDes.getSerDes().deserializePolygonType( value ) )
                    : Mono.empty() ); }

    public Mono< Polygon > getPolygon ( String uuid, String type ) { return type.equals( "polygon" ) ? this.polygonMap.get( uuid )
            .flatMap( s -> Mono.just( SerDes.getSerDes().deserializePolygon( s ) ) ) : this.polygonForPatrulMap.get( uuid )
            .flatMap( s -> Mono.just( SerDes.getSerDes().deserializePolygon( s ) ) ); }

    public Mono< ApiResponseModel > deleteLustra ( UUID uuid ) { return this.lustraMap.containsKey( uuid ).flatMap( aBoolean -> aBoolean ?
        this.lustraMap.get( uuid ).map( s -> SerDes.getSerDes().deserializeLustra( s )).flatMap( atlasLustra ->
                this.carMap.get( atlasLustra.getCarGosNumber() ).map( s -> SerDes.getSerDes().deserializeCar( s ) ).flatMap( reqCar -> {
                    reqCar.setLustraId( null );
                    return this.carMap.fastPutIfExists( reqCar.getGosNumber(), SerDes.getSerDes().serialize( reqCar ) )
                            .flatMap( aBoolean1 -> this.lustraMap.fastRemove( uuid )
                                    .log()
                                    .onErrorStop()
                                    .flatMap( aLong -> Mono.just( ApiResponseModel.builder()
                                            .status( Status.builder().message( "Lustra was deleted" ).build() ).build() ) ) ); } ) )
        : Mono.just( ApiResponseModel.builder().status( Status.builder()
            .message( "Wrong Lustra data" ).code( 201 ).build() ).success( false ).build() ) ); }

    public Mono< ApiResponseModel > deletePolygonType ( UUID uuid ) { return this.polygonTypeMap.containsKey( uuid )
            .flatMap( value -> value ?
                this.polygonTypeMap.fastRemove( uuid )
                        .onErrorStop()
                        .log()
                        .flatMap( aLong -> Mono.just( ApiResponseModel.builder().success( true )
                                .status( Status.builder().message( uuid + " was deleted" ).code( 200 ).build() ).build() ) )
                : Mono.just( ApiResponseModel.builder().success( true ).status( Status.builder()
                .message( uuid + " does not exists" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > deletePolygon ( String polygonName ) { return this.polygonMap.containsKey( polygonName )
            .flatMap( aBoolean -> aBoolean ?
                this.polygonMap.fastRemove( polygonName ).onErrorStop().log().flatMap( aLong -> Mono.just( ApiResponseModel.builder()
                        .status( Status.builder().message( "polygon: " + polygonName + " was deleted" ).code( 200 ).build() ).build() ) )
                : Mono.just( ApiResponseModel.builder().status( Status.builder()
            .message( "this polygon does not exists" ).code( 201 ).build() ).build() ) ); } // deleting current polygon

    public Mono< ApiResponseModel > deletePatrul ( String passportNumber ) { return this.patrulMap.containsKey( passportNumber )
            .flatMap( aBoolean -> aBoolean ?
                this.patrulMap.remove( passportNumber )
                        .log()
                        .onErrorStop()
                        .flatMap( aLong -> this.getPatrul( passportNumber )
                                .flatMap( patrul -> {
                            UnirestController.getInstance().deleteUser( patrul );
                            return Mono.just( ApiResponseModel.builder()
                                    .success( CassandraDataControl.getInstance().deletePatrul( passportNumber ) )
                                    .status( Status.builder()
                                            .code( 200 )
                                            .message( passportNumber + " was deleted" )
                                            .build() ).build() ); } ) )
                : Mono.just( ApiResponseModel.builder()
                    .success( false )
                    .status( Status.builder()
                            .code( 201 )
                            .message( passportNumber + " does not exists" ).build() ).build() ) ); } // deleting current car

    public Mono< ApiResponseModel > deletePolygonForPatrul ( String uuid ) { return this.polygonForPatrulMap.containsKey( uuid )
            .flatMap( aBoolean -> aBoolean ?
                this.polygonForPatrulMap.get( uuid )
                        .map( s -> SerDes.getSerDes().deserializePolygon( s ) )
                        .map( Polygon::getPatrulList )
                        .map( patruls -> Flux.fromStream( patruls.stream() ).flatMap( patrul -> this.patrulMap
                                .fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) ) ) )
                        .flatMap( value -> this.polygonForPatrulMap.remove( uuid )
                                .flatMap( a -> Mono.just( ApiResponseModel.builder()
                                        .status( Status.builder().message( "polygon: " + uuid + " was deleted" )
                                                .code( 200 ).build() ).build() ) ) )
                : Mono.just( ApiResponseModel.builder().status( Status.builder()
                    .message( "this polygon does not exists" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > deletePoliceType ( PoliceType policeType ) { return this.policeTypes.containsKey( policeType.getUuid() )
            .flatMap( aBoolean -> aBoolean ?
                this.getAllPatruls()
                        .filter( patrul -> patrul.getPoliceType().equals( policeType.getPoliceType() ) )
                        .count()
                        .flatMap( aLong -> aLong == 0 ? this.policeTypes.remove( policeType.getUuid() )
                                .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().success( true )
                                        .status( Status.builder().code( 200 ).message( policeTypes + " was deleted" ).build() ).build() ) )
                        : Mono.just( ApiResponseModel.builder()
                                .status( Status.builder().code( 201 )
                                        .message( policeType.getPoliceType() + " is used. that's why u cannot delete it at all )))" ).build() ).build() ) )
            : Mono.just( ApiResponseModel.builder()
                            .status( Status.builder().code( 201 )
                                    .message( policeType.getPoliceType() + " does not exists" )
                                    .build() ).build() ) ); } // deleting current police type

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
                                                                .success( CassandraDataControl.getInstance().deleteCar( reqCar.getGosNumber() ) )
                                                                .status( Status.builder()
                                                                        .code( 200 )
                                                                        .message( gosno + " was deleted" )
                                                                        .build() ).build() ) ); } ) )
                    : Mono.just( ApiResponseModel.builder().status( Status.builder().code( 201 )
                    .message( gosno + " does not exists" ).build() ).build() ) ); } // deleting current carMap

    public Mono< ApiResponseModel > addValue ( Patrul patrul ) {
        patrul.setInPolygon( false );
        patrul.setUuid( UUID.randomUUID() );
        patrul.setStatus( com.ssd.mvd.gpstabletsservice.constants.Status.FREE );
        return this.patrulMap.fastPutIfAbsent( patrul.getPassportNumber(), ( key = SerDes.getSerDes().serialize( patrul ) ) )
                .log()
                .onErrorStop()
                .flatMap( aBoolean -> aBoolean ?
                    Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().addValue( patrul, this.key ) )
                            .status( Status.builder().message( "new patrul was added" ).code( 200 ).build() ).build() )
                    : Mono.just( ApiResponseModel.builder().status( Status.builder()
                            .message( "this patrul already exists" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > addValue ( ReqCar reqCar ) { return this.carMap
            .fastPutIfAbsent( reqCar.getUuid().toString(), ( this.key = SerDes.getSerDes().serialize( reqCar ) ) )
            .flatMap( aBoolean -> aBoolean ?
                    this.getPatrul( reqCar.getPatrulPassportSeries() )
                    .flatMap( patrul -> {
                        patrul.setCarNumber( reqCar.getGosNumber() );
                        CassandraDataControl.getInstance().addValue( reqCar, this.key ); // saving updated version of car
                        return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), ( this.key = SerDes.getSerDes().serialize( patrul ) ) )
                                .flatMap( aBoolean1 -> aBoolean1 ? Mono.just( ApiResponseModel.builder()
                                        .success( CassandraDataControl.getInstance().addValue( patrul, SerDes.getSerDes().serialize( patrul ) ) )
                                        .status( Status.builder().message( "Car was saved" ).code( 200 ).build() ).build() )
                                        : Mono.just( ApiResponseModel.builder().status( Status.builder()
                                        .message( "Wrong Patrul data" ).code( 201 ).build() ).success( false ).build() ) );
                    } ) : Mono.just( ApiResponseModel.builder().status( Status.builder()
                            .code( 201 )
                            .message( "Wrong Car data: " + reqCar.getUuid() )
                            .build() ).success( false ).build() ) ); }

    public Mono< ApiResponseModel > addValue ( Polygon polygon ) { return this.polygonMap
            .fastPutIfAbsent( polygon.getUuid().toString(), SerDes.getSerDes().serialize( polygon ) )
            .onErrorStop()
            .log()
            .flatMap( aBoolean -> aBoolean ?
                Mono.just( ApiResponseModel.builder()
                        .success( CassandraDataControl.getInstance().addValue( polygon ).isDone() ).status( Status.builder()
                                .message( "new polygon: " + polygon.getUuid() + " was added" ).code( 200 ).build() ).build() )
                : Mono.just( ApiResponseModel.builder()
                        .status( Status.builder().message( "this polygon is already exists" ).code( 201 )
                                .build() ).build() ) ); }

    public Mono< ApiResponseModel > addValue ( PoliceType policeType ) { return this.getAllPoliceTypes()
            .filter( policeType1 -> policeType1.getPoliceType().equals( policeType.getPoliceType() ) )
            .count()
            .flatMap( aBoolean1 -> aBoolean1 == 0 ? this.policeTypes.fastPutIfAbsent( policeType.getUuid(),
                    CassandraDataControl.getInstance().addValue( policeType, SerDes.getSerDes().serialize( policeType ) ) )
                    .flatMap( aBoolean -> aBoolean ?
                        Mono.just( ApiResponseModel.builder().success( true ).status( Status.builder()
                                .message( "PoliceType was saved" ).code( 200 ).build() ).build() )
                                : Mono.just( ApiResponseModel.builder().success( false )
                                        .status( Status.builder().message( "This policeType is already exists" )
                                                .code( 201 ).build() ).build() ) )
                        : Mono.just( ApiResponseModel.builder().success( false )
                                .status( Status.builder().message( "This policeType name is already defined, choose another one" )
                                        .code( 201 ).build() ).build() ) ) ; }

    public Mono< ApiResponseModel > addValue ( PolygonType polygonType ) { return this.polygonTypeMap
            .fastPutIfAbsent( polygonType.getUuid(), ( key = SerDes.getSerDes().serialize( polygonType ) ) )
            .flatMap( aBoolean -> aBoolean ?
                Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().addValue( polygonType ) )
                        .status( Status.builder().message( polygonType.getUuid().toString() ).code( 200 ).build() ).build() )
                : Mono.just( ApiResponseModel.builder().status( Status.builder()
                        .message( "This polygonType has already been created" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > addValue ( AtlasLustra atlasLustra ) { return this.carMap.containsKey( atlasLustra.getCarGosNumber() )
            .flatMap( aBoolean -> aBoolean ?
                this.carMap.get( atlasLustra.getCarGosNumber() )
                        .map( s -> SerDes.getSerDes().deserializeCar( s ) )
                        .flatMap(reqCar1 -> { reqCar1.setLustraId( atlasLustra.getUUID() );
                    return this.carMap.fastPutIfExists( reqCar1.getGosNumber(), SerDes.getSerDes().serialize( reqCar1 ) )
                            .flatMap( value -> this.lustraMap.fastPutIfAbsent( atlasLustra.getUUID(), ( this.key = SerDes.getSerDes().serialize( atlasLustra ) ) ) )
                            .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().status( Status.builder().code( 200 )
                                    .message( "Lustra was saved with id: " + atlasLustra.getUUID() ).build() )
                                    .success( CassandraDataControl.getInstance().addValue( atlasLustra, this.key ).isDone() ).build() ) ); } )
        : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "This lustra has already been saved" ).code( 201 ).build() ).success( false ).build() ) ); }

    public Mono< ApiResponseModel > addValue ( Polygon polygon, String message ) { return this.polygonForPatrulMap
            .fastPutIfAbsent( polygon.getUuid().toString(), SerDes.getSerDes().serialize( polygon ) )
            .onErrorStop()
            .log()
            .flatMap( aBoolean -> aBoolean ?
                Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().addValue( polygon ).isDone() )
                        .status( Status.builder().message( message ).code( 200 ).build() ).build() )
                : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "this polygon is already exists" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > update ( PolygonType polygonType ) { return this.getPolygonType( polygonType.getUuid() )
            .flatMap( polygonType1 -> polygonType1 != null ?
                this.polygonTypeMap.fastPutIfExists( polygonType.getUuid(), SerDes.getSerDes().serialize( polygonType ) )
                        .flatMap( aBoolean -> Mono.just( ApiResponseModel.builder()
                                .status( Status.builder().message( polygonType1.getName() + " was updated" ).build() ).success( aBoolean ).build() ) )
                : Mono.just( ApiResponseModel.builder().status( Status.builder().message( polygonType.getName() + " does not exists" )
                        .build() ).success( false ).build() ) ); }

    public Mono< ApiResponseModel > update ( Patrul patrul ) {
        return this.patrulMap.containsKey( patrul.getPassportNumber() )
            .flatMap( aBoolean -> {
                if ( aBoolean ) { return this.patrulMap.get( patrul.getPassportNumber() )
                .map( s -> SerDes.getSerDes().deserialize( s ) )
                .flatMap( patrul1 -> {
                    if ( patrul1.getCarNumber() != null && patrul1.getCarNumber().length() > 0 ) return this.getCar( patrul1.getCarNumber() )
                            .flatMap( reqCar -> this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), ( this.key = SerDes.getSerDes().serialize( patrul ) ) )
                                    .flatMap( value -> Mono.just( ApiResponseModel.builder()
                                            .success( CassandraDataControl.getInstance().addValue( patrul, this.key ) )
                                    .status( Status.builder().message( "Patrul was updated" ).code( 200 ).build() )
                                            .success( true ).build() ) ) );
                    else return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), ( this.key = SerDes.getSerDes().serialize( patrul ) ) )
                        .flatMap( value -> Mono.just( ApiResponseModel.builder()
                                .success( CassandraDataControl.getInstance().addValue( patrul, this.key ) )
                        .status( Status.builder().message( "Patrul was updated" ).code( 200 ).build() ).success( true ).build() ) ); } );
        } else return Mono.just( ApiResponseModel.builder().success( false )
                        .status( Status.builder().message( "Wrong Patrul data" ).code( 200 ).build() ).build() ); } ); }

    public Mono< ApiResponseModel > update ( Polygon polygon ) { return this.polygonMap.containsKey( polygon.getUuid().toString() ).flatMap( a -> a ?
            this.polygonMap.fastPutIfExists( polygon.getUuid().toString(), SerDes.getSerDes().serialize( polygon ) ).flatMap( aBoolean -> aBoolean ?
                    Mono.just( ApiResponseModel.builder().success( true ).status( Status.builder().message( polygon.getName() + " was updated" ).code( 200 ).build() ).build() )
                    : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "There is no such a polygon" ).code( 201 ).build() ).build() ) ) :
            Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "There is no such a polygon" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > update ( AtlasLustra atlasLustra ) { return this.lustraMap.containsKey( atlasLustra.getUUID() )
            .flatMap( aBoolean -> aBoolean ?
                this.lustraMap.fastPutIfExists( atlasLustra.getUUID(), ( this.key = SerDes.getSerDes().serialize( atlasLustra ) ) )
                        .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                                .success( CassandraDataControl.getInstance().addValue( atlasLustra, this.key ).isDone() )
                                .status( Status.builder().message( "Lustra was updated" ).code( 200 ).build() ).build() ) )
                : Mono.just( ApiResponseModel.builder()
                        .status( Status.builder().message( "Wrong Lustra data" ).code( 201 ).build() ).success( false ).build() ) ); }

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
                                                                .status( Status.builder().message( "Car was updated" ).code( 200 ).build() )
                                                                .success( CassandraDataControl.getInstance().addValue( reqCar, this.key ) ).build() ) ) ); } ) );
                        } );
                else return this.carMap.fastPutIfExists( reqCar.getUuid().toString(), ( this.key = SerDes.getSerDes().serialize( reqCar ) ) )
                        .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                                            .status( Status.builder()
                                                    .message( "Car data was successfully updated" )
                                                    .code( 200 )
                                                    .build() )
                                    .success( CassandraDataControl.getInstance().addValue( reqCar, this.key ) ).build() ) );
            } ) : Mono.just( ApiResponseModel.builder().success( false )
            .status( Status.builder().code( 201 ).message( "Wrong Car data" ).build() ).build() ) );
    }

    public Mono< ApiResponseModel > updatePolygonForPatrul ( Polygon polygon ) { return this.polygonForPatrulMap
            .containsKey( polygon.getUuid().toString() ).flatMap( a -> a ?
            this.polygonForPatrulMap.get( polygon.getUuid().toString() )
                    .map( s -> SerDes.getSerDes().deserializePolygon( s ) )
                    .flatMap( polygon1 -> Flux.fromStream( polygon.getPatrulList().stream() )
                            .map( Patrul::getPassportNumber )
                            .flatMap( this::getPatrul )
                            .collectList()
                            .flatMap( patruls -> {
                                polygon.getPatrulList().clear();
                                polygon.getPatrulList().addAll( patruls );
                                return this.polygonForPatrulMap.fastPutIfExists( polygon.getUuid().toString(),
                                        ( this.key = SerDes.getSerDes().serialize( polygon ) ) )
                                        .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                                                .success( CassandraDataControl.getInstance().addValue( polygon ).isDone() )
                                                .status( Status.builder().message( "Patruls was added to polygon" ).code( 200 ).build() ).build() ) ); } ) )
                : Mono.just( ApiResponseModel.builder().success( false )
            .status( Status.builder().message( "There is no such a polygon for patrul" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > update ( PoliceType policeType ) { return this.policeTypes.containsKey( policeType.getUuid() )
            .flatMap( aBoolean -> aBoolean ? this.getAllPoliceTypes()
                    .filter( policeType1 -> policeType1.getPoliceType().equals( policeType.getPoliceType() ) )
                    .count()
                    .flatMap( aLong -> { if ( aLong == 0 ) {
                        this.getAllPatruls().filter( patrul -> patrul.getPoliceType().equals( policeType.getPoliceType() ) )
                                .subscribe( patrul -> patrul.setPoliceType( policeType.getPoliceType() ) );
                        return this.policeTypes.fastPutIfExists( policeType.getUuid(), SerDes.getSerDes().serialize( policeType ) )
                                .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().status( Status.builder().code( 200 )
                                        .message( "PoliceType was updated" ).build() ).success( false ).build() ) );
                    } else return Mono.just( ApiResponseModel.builder().status( Status.builder().code( 201 )
                            .message( "There is such a name so please choose another one ))" ).build() ).build() ); } ) :
        Mono.just( ApiResponseModel.builder().status( Status.builder().code( 201 )
                .message( "This policeType does not exists. so if u wish u can check the list and choose another one ((" ).build() ).build() ) ); }

    public String decode ( String token ) { return new String( Base64.getDecoder().decode( token ) ).split( "_" )[ 0 ]; }

    // uses when Patrul wants to change his status from active to pause
    public Mono< ApiResponseModel > setInPause ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) ).flatMap( aBoolean -> aBoolean ?
            this.patrulMap.get( this.key )
                    .map( s -> SerDes.getSerDes().deserialize( s ) )
                    .flatMap( patrul -> this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) )
                    .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGIN ) )
                            .status( Status.builder().message( "Patrul set in pause" ).code( 200 ).build() ).build() ) ) )
            : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "Wrong login or password" ).code( 201 ).build() ).build() ) ); }

    // uses when Patrul wants to change his status from pause to active
    public Mono< ApiResponseModel > backToWork ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) ).flatMap( aBoolean -> aBoolean ?
            this.patrulMap.get( this.key )
                    .map( s -> SerDes.getSerDes().deserialize( s ) )
                    .flatMap( patrul -> this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) )
                    .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.RETURNED_TO_WORK ) )
                            .status( Status.builder().message( "Patrul returned to work" ).code( 200 ).build() ).build() ) )) : Mono.just( ApiResponseModel.builder()
            .success( false ).status( Status.builder().message( "Wrong login or password" ).code( 201 ).build() ).build() ) ); }

    // sets every day when Patrul start to work in morning
    public Mono< ApiResponseModel > startToWork ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) ).flatMap( aBoolean -> aBoolean ?
            this.patrulMap.get( this.key ).map( s -> SerDes.getSerDes().deserialize( s ) ).flatMap( patrul -> {
                patrul.setTotalActivityTime( 0L ); // set to 0 every day
                patrul.setStartedToWorkDate( new Date() ); // registration of time every day
                return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) )
                        .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                                .success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.START_TO_WORK ) )
                                .status( Status.builder().message( "Patrul started to work" ).code( 200 ).build() ).build() ) );
            } ) : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "Wrong login or password" ).code( 201 ).build() ).build() ) ); }

    // uses when patrul finishes his work in the evening
    public Mono< ApiResponseModel > stopToWork ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) ).flatMap( aBoolean -> aBoolean ?
            this.patrulMap.get( this.key )
                    .map( s -> SerDes.getSerDes().deserialize( s ) )
                    .flatMap( patrul -> this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) )
                    .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                            .success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.STOP_TO_WORK ) )
                            .status( Status.builder().message( "Patrul stopped his job" ).code( 200 ).build() ).build() ) ) )
            : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "Wrong login or password" ).code( 201 ).build() ).build() ) ); }

    // uses when Patrul login to account after some time
    public Mono< ApiResponseModel > login ( PatrulLoginRequest patrulLoginRequest ) { return this.patrulMap
            .containsKey( patrulLoginRequest.getPassportSeries() )
            .flatMap( aBoolean -> aBoolean ?
                this.patrulMap.get( patrulLoginRequest.getPassportSeries() )
                        .map( s -> SerDes.getSerDes().deserialize( s ) )
                        .flatMap(patrul -> {
                            if ( patrul.getPassword().equals( patrulLoginRequest.getPassword() ) ) {
                                patrul.setStartedToWorkDate( new Date() );
                                patrul.setSimCardNumber( patrulLoginRequest.getSimCardNumber() );
                                patrul.setToken( Base64.getEncoder().encodeToString( ( patrul.getPassportNumber()
                                        + "_" + patrul.getPassword()
                                        + "_" + Archive.getAchieve().generateToken() ).getBytes( StandardCharsets.UTF_8 ) ) );
                                return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) )
                                        .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().data( Data.builder().data( patrul ).build() )
                                                .success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGIN ) )
                                                .status( Status.builder().message( "Welcome to Family: " + patrul.getName() ).code( 200 ).build() ).build() ) );
                            } else return Mono.just( ApiResponseModel.builder().status( Status.builder().code( 201 )
                                    .message( "Wrong Login or password" ).build() ).success( false ).build() );
                } ) : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "Wrong Login or Password" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > logout ( String token ) { return this.patrulMap.get( this.decode( token ) )
            .map( s -> SerDes.getSerDes().deserialize( s ) )
            .flatMap( patrul -> {
                patrul.setToken( null );
                return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) )
                        .flatMap( aBoolean -> Mono.just( ApiResponseModel.builder()
                                .success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGOUT ) )
                                .status( Status.builder().message( "See you soon my darling )))" ).code( 200 ).build() ).build() ) ); } ); }

    public Mono< ApiResponseModel > addPatrulToPolygon ( ScheduleForPolygonPatrul scheduleForPolygonPatrul ) { return this.polygonForPatrulMap
            .containsKey( scheduleForPolygonPatrul.getUuid() )
            .flatMap( aBoolean -> aBoolean ?
                this.polygonForPatrulMap.get( scheduleForPolygonPatrul.getUuid() )
                        .map( s -> SerDes.getSerDes().deserializePolygon( s ) )
                        .flatMap( polygon -> Flux.fromStream( scheduleForPolygonPatrul.getPassportSeries().stream() )
                                .flatMap( this::getPatrul )
                                .filter( patrul -> !patrul.getInPolygon() )
                                .flatMap( patrul -> {
                                    patrul.setInPolygon( true );
                                    return this.update( patrul )
                                            .flatMap( apiResponseModel -> Mono.just( patrul ) ); } )
                                .collectList()
                                .flatMap( patruls -> {
                                        polygon.getPatrulList().addAll( patruls );
                                        return this.polygonForPatrulMap.fastPutIfExists( polygon.getUuid().toString(), ( this.key = SerDes.getSerDes().serialize( polygon ) ) )
                                                .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                                                        .success( CassandraDataControl.getInstance().addValue( polygon ).isDone() )
                                                        .status( Status.builder()
                                                                .message( "Patruls was added to polygon" )
                                                                .code( 200 ).build() )
                                                        .build() ) ); } ) )
                : Mono.just( ApiResponseModel.builder()
                    .success( false )
                    .status( Status.builder().code( 201 )
                            .message( "Wrong polygon Id" ).build() ).build() ) ); }

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
                    .message( "Wrong token" ).code( 201 ).build() ).success( false ).build() ) ); }

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

    public Flux< ActiveTask > getActiveTasks() { return this.activeTasks.valueIterator()
            .flatMap( s -> Mono.just( SerDes.getSerDes().deserializeActiveTask( s ) ) ); }
}
