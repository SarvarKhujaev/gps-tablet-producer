package com.ssd.mvd.gpstabletsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( String[] args ) {
        context = SpringApplication.run( GpsTabletsServiceApplication.class, args );
//        RedisDataControl.getRedis().getAllPatruls()
//                .subscribe( patrul -> {
//                    patrul.setCard( null );
//                    patrul.setToken( null );
//                    patrul.setTaskDate( null );
//                    patrul.setStatus( Status.FREE );
//                    patrul.setSelfEmploymentId( null );
//                    patrul.setTaskStatus( Status.FREE );
//                    RedisDataControl.getRedis().update( patrul ).subscribe(); } );
        new Thread( Archive.getAchieve(), "archive" ).start(); // launching Archive to monitor all patruls, Card and SelfEmployment
    }
}
