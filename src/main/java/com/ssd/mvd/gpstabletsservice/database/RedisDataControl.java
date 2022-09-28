//package com.ssd.mvd.gpstabletsservice.database;
//
//import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
//import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
//
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//import org.redisson.config.Config;
//import org.redisson.Redisson;
//import org.redisson.api.*;
//
//public final class RedisDataControl {
//    private RedissonReactiveClient redissonReactiveClient;
//    private final RMapReactive< String, String > activeTasks;
//
//    private static RedisDataControl redisDataControl = new RedisDataControl();
//
//    public static RedisDataControl getRedis () { return redisDataControl != null ? redisDataControl
//            : ( redisDataControl = new RedisDataControl() ); }
//
//    private RedisDataControl () { Config config = new Config();
//        config.useSingleServer()
//                .setAddress( "redis://" + GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.REDIS_HOST" ) + ":"
//                        + GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.REDIS_PORT" ) )
//                .setClientName( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.REDIS_CLIENT_NAME" ) )
//                .setPassword( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.REDIS_PASSWORD" ) );
//        this.redissonReactiveClient = Redisson.createReactive( config );
//        this.activeTasks = this.redissonReactiveClient.getMap( "activeTasks" ); }
//}
