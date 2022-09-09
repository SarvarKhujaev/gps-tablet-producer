package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.entity.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.redisson.config.Config;
import org.redisson.Redisson;
import org.redisson.api.*;

public final class RedisDataControl {
    private final RMapReactive< Long, String > cardMap;
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
        this.cardMap = this.redissonReactiveClient.getMap( "cardMap" ); }

    public Flux< Patrul > getAllPatruls () { return this.patrulMap.valueIterator()
            .flatMap( value -> Mono.just( SerDes.getSerDes().deserialize( value ) ) ); }

    public Mono< ApiResponseModel > update ( Patrul patrul ) { return this.patrulMap
            .fastPutIfExists( patrul.getPassportNumber(), SerDes.getSerDes().serialize( patrul ) )
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
