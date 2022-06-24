package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.response.PatrulActivityStatistics;
import com.ssd.mvd.gpstabletsservice.request.PatrulLoginRequest;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.entity.*;

import java.util.*;
import org.redisson.api.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.nio.charset.StandardCharsets;

import org.redisson.Redisson;
import org.redisson.config.Config;

public final class RedisDataControl {
    private String key;
    private final RListReactive< String > policeTypes;
    private final RMapReactive< String, String > carMap;
    private final RMapReactive< UUID, String > lustraMap;
    private final RMapReactive< String, String > patrulMap;
    private final RMapReactive< String, String > polygonMap;
    private final RMapReactive< UUID, String > polygonTypeMap;
    private final RedissonReactiveClient redissonReactiveClient;
    private final RMapReactive< String, String > polygonForPatrulMap;

    private static RedisDataControl redisDataControl = new RedisDataControl();

    public static RedisDataControl getRedis () { return redisDataControl != null ? redisDataControl : ( redisDataControl = new RedisDataControl() ); }

    private RedisDataControl () {
        Config config = new Config();
        config.useSingleServer().setAddress( "redis://10.254.1.227:6367" ).setClientName( "default" ).setPassword( "8tRk62" );
        this.redissonReactiveClient = Redisson.createReactive( config );
        this.polygonForPatrulMap = this.redissonReactiveClient.getMap( "polygonForPatrulMap" ); // for polygons with schedule
        this.polygonTypeMap = this.redissonReactiveClient.getMap( "polygonTypeMap" ); // for polygons
        this.polygonMap = this.redissonReactiveClient.getMap( "polygonMap" ); // for polygons
        this.policeTypes = this.redissonReactiveClient.getList( "policeType" );
        this.lustraMap = this.redissonReactiveClient.getMap( "lustraMap" ); // for lustra cameras
        this.patrulMap = this.redissonReactiveClient.getMap( "patrulMap" ); // for patrul
        this.carMap = this.redissonReactiveClient.getMap( "carMap" ); } // for cars

    public Flux< PoliceType > getAllPoliceTypes () { return this.policeTypes.iterator().map( PoliceType::new ); }

    public Flux< ReqCar > getAllCars () { return this.carMap.valueIterator().map( data -> SerDes.getSerDes().deserializeCar( data ) ); }

    public Flux< Patrul > getAllPatruls () { return this.patrulMap.valueIterator().map( value -> SerDes.getSerDes().deserialize( value ) ); }

    public Flux< Polygon > getAllPolygons () { return this.polygonMap.valueIterator().map( value -> SerDes.getSerDes().deserializePolygon( value ) ); }

    public Flux< AtlasLustra > getAllLustra () { return this.lustraMap.valueIterator().map( value -> SerDes.getSerDes().deserializeLustra( value ) ); }

    public Flux< PolygonType > getAllPolygonTypes () { return this.polygonTypeMap.valueIterator().map( value -> SerDes.getSerDes().deserializePolygonType( value ) ); }

    public Flux< Polygon > getAllPolygonsForPatrul () { return this.polygonForPatrulMap.valueIterator().map( value -> SerDes.getSerDes().deserializePolygon( value ) ); }

    public Mono< ReqCar > getCar ( String gosNumber ) { return this.carMap.get( gosNumber ).flatMap( value -> Mono.just( SerDes.getSerDes().deserializeCar( value ) ) ); }

    public Mono< PolygonType > getPolygonType ( UUID uuid ) { return this.polygonTypeMap.get( uuid ).flatMap( value -> value != null ? Mono.just( SerDes.getSerDes().deserializePolygonType( value ) ) : Mono.empty() ); }

    public Mono< Patrul > getPatrul ( String passportNumber ) { return this.patrulMap.containsKey( passportNumber ).flatMap( value -> value ? this.patrulMap.get( passportNumber ).map( s -> SerDes.getSerDes().deserialize( s ) ) : Mono.empty() ); }

    public Mono< Polygon > getPolygon ( String uuid, String type ) { return type.equals( "polygon" ) ? this.polygonMap.get( uuid ).flatMap( s -> Mono.just( SerDes.getSerDes().deserializePolygon( s ) ) ) : this.polygonForPatrulMap.get( uuid ).flatMap( s -> Mono.just( SerDes.getSerDes().deserializePolygon( s ) ) ); }

    public Mono< ApiResponseModel > deleteCar ( String gosno ) { return this.carMap.containsKey( gosno ).log().onErrorStop().flatMap( aBoolean -> aBoolean ?
            this.carMap.get( gosno ).map( s -> SerDes.getSerDes().deserializeCar( s )).flatMap(reqCar1 ->
                this.getPatrul( reqCar1.getPatrulPassportSeries() ).flatMap( patrul1 -> {
                    patrul1.setCarNumber( null );
                    return this.carMap.fastRemove( gosno ).log().onErrorStop().flatMap( aLong -> Mono.just( ApiResponseModel.builder().data( Data.builder().data( patrul1 ).subject( reqCar1 ).build() ).success( true ).status( Status.builder().code( 200 ).message( gosno + " was deleted" ).build() ).build() ) ); } ) )
            : Mono.just( ApiResponseModel.builder().status( Status.builder().code( 201 ).message( gosno + " does not exists" ).build() ).build() ) ); } // deleting current car

    public Mono< ApiResponseModel > deleteLustra ( UUID uuid ) { return this.lustraMap.containsKey( uuid ).flatMap( aBoolean -> aBoolean ?
        this.lustraMap.get( uuid ).map( s -> SerDes.getSerDes().deserializeLustra( s )).flatMap( atlasLustra ->
                this.carMap.get( atlasLustra.getCarGosNumber() ).map( s -> SerDes.getSerDes().deserializeCar( s ) ).flatMap(reqCar -> {
                    reqCar.setLustraId( null );
                    return this.carMap.fastPutIfExists( reqCar.getGosNumber(), SerDes.getSerDes().serialize( reqCar ) ).flatMap( aBoolean1 -> this.lustraMap.fastRemove( uuid ).log().onErrorStop().flatMap( aLong -> Mono.just( ApiResponseModel.builder().status( Status.builder().message( "Lustra was deleted" ).build() ).build() ) ) ); } ) )
        : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "Wrong Lustra data" ).code( 201 ).build() ).success( false ).build() ) ); }

    public Mono< ApiResponseModel > deletePolygonType ( UUID uuid ) { return this.polygonTypeMap.containsKey( uuid ).flatMap( value -> value ?
            this.polygonTypeMap.fastRemove( uuid ).onErrorStop().log().flatMap( aLong -> Mono.just( ApiResponseModel.builder().success( true ).status( Status.builder().message( uuid + " was deleted" ).code( 200 ).build() ).build() ) )
            : Mono.just( ApiResponseModel.builder().success( true ).status( Status.builder().message( uuid + " does not exists" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > deletePolygon ( String polygonName ) { return this.polygonMap.containsKey( polygonName ).flatMap( aBoolean -> aBoolean ?
            this.polygonMap.fastRemove( polygonName ).onErrorStop().log().flatMap( aLong -> Mono.just( ApiResponseModel.builder().status( Status.builder().message( "polygon: " + polygonName + " was deleted" ).code( 200 ).build() ).build() ) )
            : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "this polygon does not exists" ).code( 201 ).build() ).build() ) ); } // deleting current polygon

    public Mono< ApiResponseModel > deletePatrul ( String passportNumber ) { return this.patrulMap.containsKey( passportNumber ).flatMap( aBoolean -> aBoolean ?
            this.patrulMap.fastRemove( passportNumber ).log().onErrorStop().flatMap( aLong -> Mono.just( ApiResponseModel.builder().status( Status.builder().code( 200 ).message( passportNumber + " was deleted" ).build() ).build() ) )
            : Mono.just( ApiResponseModel.builder().status( Status.builder().code( 201 ).message( passportNumber + " does not exists" ).build() ).build() ) ); } // deleting current car

    public Mono< ApiResponseModel > deletePolygonForPatrul ( String uuid ) { return this.polygonForPatrulMap.containsKey( uuid ).flatMap( aBoolean -> aBoolean ?
        this.polygonForPatrulMap.get( uuid ).map( s -> SerDes.getSerDes().deserializePolygon( s )).map( Polygon::getPatrulList ).map(patruls -> Flux.fromStream( patruls.stream() ).flatMap(patrul -> this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) ) ) ).flatMap(value -> this.polygonForPatrulMap.remove( uuid ).flatMap(a -> Mono.just( ApiResponseModel.builder().status( Status.builder().message( "polygon: " + uuid + " was deleted" ).code( 200 ).build() ).build() ) ) )
        : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "this polygon does not exists" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > deletePoliceType ( String policeTypes ) { return this.policeTypes.contains( policeTypes ).flatMap( aBoolean -> aBoolean ? this.policeTypes.remove( policeTypes ).flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().success( aBoolean ).status( Status.builder().code( 200 ).message( policeTypes + " was deleted" ).build() ).build() ) )
            : Mono.just( ApiResponseModel.builder().status( Status.builder().code( 201 ).message( policeTypes + " does not exists" ).build() ).build() ) ); } // deleting current police type

    public Mono< ApiResponseModel > addValue ( Patrul patrul ) { return this.patrulMap.fastPutIfAbsent( patrul.getPassportNumber(), ( key = SerDes.getSerDes().serialize( patrul ) ) ).log().onErrorStop().flatMap( aBoolean -> aBoolean ?
            Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().addValue( patrul, this.key ) ).status( Status.builder().message( "new patrul was added" ).code( 200 ).build() ).build() )
        : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "this patrul already exists" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > addValue ( ReqCar reqCar ) { return this.carMap.containsKey( reqCar.getGosNumber() ).flatMap( aBoolean -> aBoolean ?
            Mono.just( ApiResponseModel.builder().status( Status.builder().message( "Wrong Car data" ).code( 201 ).build() ).success( false ).build() )
            : this.patrulMap.get( reqCar.getPatrulPassportSeries() ).map( s -> SerDes.getSerDes().deserialize( s ) ).flatMap( patrul1 -> {
                patrul1.setCarNumber( reqCar.getGosNumber() );
                return this.patrulMap.fastPutIfExists( reqCar.getPatrulPassportSeries(), ( this.key = SerDes.getSerDes().serialize( patrul1 ) ) ).flatMap( aBoolean1 -> {
                    CassandraDataControl.getInstance().addValue( patrul1, this.key ); // saving updated version of Patrul
                    return this.carMap.fastPutIfAbsent( reqCar.getGosNumber(), ( this.key = SerDes.getSerDes().serialize( reqCar ) ) ).flatMap( value -> Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().addValue( reqCar, this.key ) ).data( Data.builder().data( patrul1 ).subject( reqCar ).build() ).status( Status.builder().message( "Car was saved" ).code( 200 ).build() ).build() ) ); } ); } ) ); }

    public Mono< ApiResponseModel > addValue ( Polygon polygon ) { return this.polygonMap.fastPutIfAbsent( polygon.getUuid().toString(), SerDes.getSerDes().serialize( polygon ) ).onErrorStop().log().flatMap( aBoolean -> aBoolean ?
            Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().addValue( polygon ).isDone() ).status( Status.builder().message( "new polygon: " + polygon.getUuid() + " was added" ).code( 200 ).build() ).build() )
            : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "this polygon is already exists" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > addValue ( PoliceType policeType ) { return this.policeTypes.contains( policeType.getPoliceType() ).flatMap( aBoolean -> !aBoolean ? this.policeTypes.add( CassandraDataControl.getInstance().addValue( policeType ).getPoliceType() ).flatMap( aBoolean1 ->  Mono.just( ApiResponseModel.builder().success( true ).status( Status.builder().message( "PoliceType was saved" ).code( 200 ).build() ).build() ) )
            : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "Error" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > addValue ( PolygonType polygonType ) { return this.polygonTypeMap.fastPutIfAbsent( polygonType.getUuid(), ( key = SerDes.getSerDes().serialize( polygonType ) ) ).flatMap( aBoolean -> aBoolean ?
            Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().addValue( polygonType ) ).status( Status.builder().message( polygonType.getUuid().toString() ).code( 200 ).build() ).build() )
            : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "This polygonType has already been created" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > addValue ( AtlasLustra atlasLustra ) { return this.carMap.containsKey( atlasLustra.getCarGosNumber() ).flatMap( aBoolean -> aBoolean ?
        this.carMap.get( atlasLustra.getCarGosNumber() ).map( s -> SerDes.getSerDes().deserializeCar( s )).flatMap(reqCar1 -> { reqCar1.setLustraId( atlasLustra.getUUID() );
            return this.carMap.fastPutIfExists( reqCar1.getGosNumber(), SerDes.getSerDes().serialize( reqCar1 ) ).flatMap( value -> this.lustraMap.fastPutIfAbsent( atlasLustra.getUUID(), ( this.key = SerDes.getSerDes().serialize( atlasLustra ) ) ) )
                    .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().status( Status.builder().code( 200 ).message( "Lustra was saved with id: " + atlasLustra.getUUID() ).build() ).success( CassandraDataControl.getInstance().addValue( atlasLustra, this.key ).isDone() ).build() ) ); } )
        : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "This lustra has already been saved" ).code( 201 ).build() ).success( false ).build() ) ); }

    public Mono< ApiResponseModel > addValue ( Polygon polygon, String message ) { return this.polygonForPatrulMap.fastPutIfAbsent( polygon.getUuid().toString(), SerDes.getSerDes().serialize( polygon ) ).onErrorStop().log().flatMap( aBoolean -> aBoolean ?
            Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().addValue( polygon ).isDone() ).status( Status.builder().message( message ).code( 200 ).build() ).build() )
            : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "this polygon is already exists" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > update ( PolygonType polygonType ) { return this.getPolygonType( polygonType.getUuid() ).flatMap( polygonType1 -> polygonType1 != null ?
            this.polygonTypeMap.fastPutIfExists( polygonType.getUuid(), SerDes.getSerDes().serialize( polygonType ) ).flatMap( aBoolean -> Mono.just( ApiResponseModel.builder().status( Status.builder().message( polygonType1.getName() + " was updated" ).build() ).success( aBoolean ).build() ) )
            : Mono.just( ApiResponseModel.builder().status( Status.builder().message( polygonType.getName() + " does not exists" ).build() ).success( false ).build() ) ); }

    public Mono< ApiResponseModel > update ( Patrul patrul ) { return this.patrulMap.containsKey( patrul.getPassportNumber() ).flatMap( aBoolean -> {
        if ( aBoolean ) { return this.patrulMap.get( patrul.getPassportNumber() ).map( s -> SerDes.getSerDes().deserialize( s ) ).flatMap( patrul1 -> {
            if ( patrul1.getCard() != null ) patrul.setCard( patrul1.getCard() );
            return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), ( this.key = SerDes.getSerDes().serialize( patrul ) ) ).flatMap( value -> Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().addValue( patrul, this.key ) ).status( Status.builder().message( "Patrul was updated" ).code( 200 ).build() ).build() ) ); } );
        } else return Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "Wrong Patrul data" ).code( 200 ).build() ).build() ); } ); }

    public Mono< ApiResponseModel > update ( ReqCar reqCar ) { return this.carMap.containsKey( reqCar.getGosNumber() ).flatMap( aBoolean -> aBoolean ?
            this.carMap.get( reqCar.getGosNumber() ).map( s -> SerDes.getSerDes().deserializeCar( s )).flatMap(reqCar1 -> this.carMap.fastPutIfExists( reqCar.getGosNumber(), ( this.key = SerDes.getSerDes().serialize( reqCar ) ) ).flatMap(aBoolean1 -> Mono.just( ApiResponseModel.builder().status( Status.builder().message( "Car was updated" ).code( 200 ).build() ).success( CassandraDataControl.getInstance().addValue( reqCar, this.key ) ).build() ) ) )
            : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().code( 201 ).message( "Wrong Car data" ).build() ).build() ) ); }

    public Mono< ApiResponseModel > update ( Polygon polygon ) { return this.polygonMap.containsKey( polygon.getUuid().toString() ).flatMap( a -> a ?
            this.polygonMap.fastPutIfExists( polygon.getUuid().toString(), SerDes.getSerDes().serialize( polygon ) ).flatMap( aBoolean -> aBoolean ?
                    Mono.just( ApiResponseModel.builder().success( true ).status( Status.builder().message( polygon.getName() + " was updated" ).code( 200 ).build() ).build() )
                    : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "There is no such a polygon" ).code( 201 ).build() ).build() ) ) :
            Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "There is no such a polygon" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > update ( AtlasLustra atlasLustra ) { return this.lustraMap.containsKey( atlasLustra.getUUID() ).flatMap( aBoolean -> aBoolean ?
            this.lustraMap.fastPutIfExists( atlasLustra.getUUID(), ( this.key = SerDes.getSerDes().serialize( atlasLustra ) ) ).flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().addValue( atlasLustra, this.key ).isDone() ).status( Status.builder().message( "Lustra was updated" ).code( 200 ).build() ).build() ) )
            : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "Wrong Lustra data" ).code( 201 ).build() ).success( false ).build() ) ); }

    public Mono< ApiResponseModel > updatePolygonForPatrul ( Polygon polygon ) { return this.polygonForPatrulMap.containsKey( polygon.getUuid().toString() ).flatMap( a -> a ?
            this.polygonForPatrulMap.get( polygon.getUuid().toString() )
                    .map( s -> SerDes.getSerDes().deserializePolygon( s ) )
                    .flatMap( polygon1 -> Flux.fromStream( polygon.getPatrulList().stream() )
                            .map( Patrul::getPassportNumber )
                            .flatMap( this::getPatrul )
                            .collectList()
                            .flatMap( patruls -> {
                                polygon.getPatrulList().clear();
                                polygon.getPatrulList().addAll( patruls );
                                return this.polygonForPatrulMap.fastPutIfExists( polygon.getUuid().toString(), ( this.key = SerDes.getSerDes().serialize( polygon ) ) ).flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().addValue( polygon ).isDone() ).status( Status.builder().message( "Patruls was added to polygon" ).code( 200 ).build() ).build() ) ); } ) )
                : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "There is no such a polygon for patrul" ).code( 201 ).build() ).build() ) ); }

    public String decode ( String token ) { return new String( Base64.getDecoder().decode( token ) ).split( "_" )[ 0 ]; }

    // uses when Patrul wants to change his status from active to pause
    public Mono< ApiResponseModel > setInPause ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) ).flatMap( aBoolean -> aBoolean ?
            this.patrulMap.get( this.key ).map( s -> SerDes.getSerDes().deserialize( s ) ).flatMap( patrul -> {
                patrul.setStatus( com.ssd.mvd.gpstabletsservice.constants.Status.NOT_AVAILABLE );
                return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) ).flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGIN ) ).status( Status.builder().message( "Patrul set in pause" ).code( 200 ).build() ).build() ) );
            } ) : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "Wrong login or password" ).code( 201 ).build() ).build() ) ); }

    // uses when Patrul wants to change his status from pause to active
    public Mono< ApiResponseModel > backToWork ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) ).flatMap( aBoolean -> aBoolean ?
            this.patrulMap.get( this.key ).map( s -> SerDes.getSerDes().deserialize( s ) ).flatMap( patrul -> {
                patrul.setStatus( com.ssd.mvd.gpstabletsservice.constants.Status.FREE );
                return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) ).flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.RETURNED_TO_WORK ) ).status( Status.builder().message( "Patrul returned to work" ).code( 200 ).build() ).build() ) );
            } ) : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "Wrong login or password" ).code( 201 ).build() ).build() ) ); }

    // sets every day when Patrul start to work in morning
    public Mono< ApiResponseModel > startToWork ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) ).flatMap( aBoolean -> aBoolean ?
            this.patrulMap.get( this.key ).map( s -> SerDes.getSerDes().deserialize( s ) ).flatMap( patrul -> {
                patrul.setTotalActivityTime( 0L ); // set to 0 every day
                patrul.setStartedToWorkDate( new Date() ); // registration of time every day
                patrul.setStatus( com.ssd.mvd.gpstabletsservice.constants.Status.FREE );
                return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) ).flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.START_TO_WORK ) ).status( Status.builder().message( "Patrul started to work" ).code( 200 ).build() ).build() ) );
            } ) : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "Wrong login or password" ).code( 201 ).build() ).build() ) ); }

    // uses when patrul finishes his work in the evening
    public Mono< ApiResponseModel > stopToWork ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) ).flatMap( aBoolean -> aBoolean ?
            this.patrulMap.get( this.key ).map( s -> SerDes.getSerDes().deserialize( s )).flatMap( patrul -> {
                patrul.setStatus( com.ssd.mvd.gpstabletsservice.constants.Status.NOT_AVAILABLE );
                return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) ).flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.STOP_TO_WORK ) ).status( Status.builder().message( "Patrul stopped his job" ).code( 200 ).build() ).build() ) );
            } ) : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().message( "Wrong login or password" ).code( 201 ).build() ).build() ) ); }

    // uses when Patrul login to account after some time
    public Mono< ApiResponseModel > login ( PatrulLoginRequest patrulLoginRequest ) { return this.patrulMap.containsKey( patrulLoginRequest.getPassportSeries() ).flatMap( aBoolean -> aBoolean ?
            this.patrulMap.get( patrulLoginRequest.getPassportSeries() ).map( s -> SerDes.getSerDes().deserialize( s ) ).flatMap(patrul -> {
                if ( patrul.getPassword().equals( patrulLoginRequest.getPassword() ) ) {
                    patrul.setStartedToWorkDate( new Date() );
                    patrul.setToken( Base64.getEncoder().encodeToString( ( patrul.getPassportNumber() + "_" + patrul.getPassword() + "_" + Archive.getAchieve().generateToken() ).getBytes( StandardCharsets.UTF_8 ) ) );
                    return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) ).flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().data( Data.builder().data( patrul ).build() ).success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGIN ) ).status( Status.builder().message( "Welcome to Family: " + patrul.getName() ).code( 200 ).build() ).build() ) );
                } else return Mono.just( ApiResponseModel.builder().status( Status.builder().code( 201 ).message( "Wrong Login or password" ).build() ).success( false ).build() );
            } ) : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "Wrong Login or Password" ).code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > logout ( String token ) { return this.patrulMap.get( this.decode( token ) ).map( s -> SerDes.getSerDes().deserialize( s )).flatMap( patrul -> {
            patrul.setToken( null );
            patrul.setStatus( com.ssd.mvd.gpstabletsservice.constants.Status.NOT_AVAILABLE );
            return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) ).flatMap( aBoolean -> Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGOUT ) ).status( Status.builder().message( "See you soon my darling )))" ).code( 200 ).build() ).build() ) ); } ); }

    public Mono< ApiResponseModel > addPatrulToPolygon ( ScheduleForPolygonPatrul scheduleForPolygonPatrul ) { return this.polygonForPatrulMap.containsKey( scheduleForPolygonPatrul.getUuid() ).flatMap( aBoolean -> aBoolean ?
            this.polygonForPatrulMap.get( scheduleForPolygonPatrul.getUuid() ).map( s -> SerDes.getSerDes().deserializePolygon( s ) ).flatMap( polygon -> Flux.fromStream( scheduleForPolygonPatrul.getPassportSeries().stream() ).flatMap( this::getPatrul ).filter( patrul -> !patrul.getInPolygon() ).map( patrul -> {
                patrul.setInPolygon( true );
                return patrul; } ).collectList().flatMap( patruls -> {
                    polygon.getPatrulList().addAll( patruls );
                    return this.polygonForPatrulMap.fastPutIfExists( polygon.getUuid().toString(), ( this.key = SerDes.getSerDes().serialize( polygon ) ) ).flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().addValue( polygon ).isDone() ).status( Status.builder().message( "Patruls was added to polygon" ).code( 200 ).build() ).build() ) ); } ) )
            : Mono.just( ApiResponseModel.builder().success( false ).status( Status.builder().code( 201 ).message( "Wrong polygon Id" ).build() ).build() ) ); }

    public Mono< PatrulActivityStatistics > getPatrulStatistics ( String passportNumber ) { return this.patrulMap.get( passportNumber ).map( s -> SerDes.getSerDes().deserialize( s ) ).flatMap( patrul -> Mono.just( CassandraDataControl.getInstance().getPatrulStatistics( patrul ) ) ); }

    public Mono< ApiResponseModel > accepted ( String token ) { return this.patrulMap.get( this.decode( token ) ).map( s -> SerDes.getSerDes().deserialize( s ) ).flatMap( patrul -> {
        Archive.getAchieve().save( Notification.builder().patrul( patrul ).status( false ).title( "Task was accepted" ).notificationWasCreated( new Date() ).build() );
        return this.patrulMap.fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) ).flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder().success( CassandraDataControl.getInstance().login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.ACCEPTED ) ).status( Status.builder().message( "Patrul accepted new task" ).code( 200 ).build() ).build() ) ); } ); }

    public Mono< ApiResponseModel > arrived ( String token ) { return this.patrulMap.get( this.decode( token ) ).map( s -> SerDes.getSerDes().deserialize( s ) ).flatMap( patrul -> Archive.getAchieve().get( patrul.getSelfEmploymentId() ).flatMap( selfEmploymentTask -> {
            if ( patrul.getSelfEmploymentId() != null ) selfEmploymentTask.setArrivedTime( new Date() );
            return this.update( patrul.changeTaskStatus( com.ssd.mvd.gpstabletsservice.constants.Status.ARRIVED ) ); } ) ); }

    public Mono< ApiResponseModel > checkToken ( String token ) { return this.patrulMap.containsKey( ( this.key = this.decode( token ) ) ).flatMap( aBoolean -> aBoolean ?
            this.patrulMap.get( this.key ).map( s -> SerDes.getSerDes().deserialize( s ) ).flatMap( patrul -> Mono.just( ApiResponseModel.builder().data( Data.builder().data( patrul ).build() ).status( Status.builder().message( "All right!!!" ).code( 200 ).build() ).success( true ).build() ) )
            : Mono.just( ApiResponseModel.builder().status( Status.builder().message( "Wrong token" ).code( 201 ).build() ).success( false ).build() ) ); }

    public void clear () {
        this.polygonForPatrulMap.delete().onErrorStop().log().subscribe();
        this.polygonTypeMap.delete().onErrorStop().log().subscribe();
        this.polygonMap.delete().onErrorStop().log().subscribe();
        this.lustraMap.delete().onErrorStop().log().subscribe();
        this.patrulMap.delete().onErrorStop().log().subscribe();
        this.carMap.delete().onErrorStop().log().subscribe();
        this.policeTypes.delete().onErrorStop().subscribe();
        this.redissonReactiveClient.shutdown();
        redisDataControl = null; }
}
