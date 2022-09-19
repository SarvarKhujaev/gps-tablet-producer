package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.redisson.config.Config;
import org.redisson.Redisson;
import org.redisson.api.*;

public final class RedisDataControl {
    private RedissonReactiveClient redissonReactiveClient;
    private final RMapReactive< String, String > activeTasks;

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
        this.activeTasks = this.redissonReactiveClient.getMap( "activeTasks" ); }

    public Flux< ActiveTask > getActiveTasks() { return this.activeTasks.valueIterator()
            .flatMap( s -> Mono.just( SerDes.getSerDes().deserializeActiveTask( s ) ) ); }

    public Mono< Boolean > addValue ( String id, ActiveTask activeTask ) {
        return this.activeTasks.containsKey( id )
                .flatMap( aBoolean -> aBoolean ? this.activeTasks.fastPutIfExists( id, KafkaDataControl
                        .getInstance()
                        .writeToKafka( SerDes.getSerDes().serialize( activeTask ) ) )
                        : this.activeTasks.fastPutIfAbsent( id, KafkaDataControl
                        .getInstance()
                        .writeToKafka( SerDes.getSerDes().serialize( activeTask ) ) ) ); }

    public void remove ( String id ) { this.activeTasks.remove( id ).subscribe(); }
}
