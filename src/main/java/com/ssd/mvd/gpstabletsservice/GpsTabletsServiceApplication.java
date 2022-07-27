package com.ssd.mvd.gpstabletsservice;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import org.springframework.boot.SpringApplication;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( String[] args ) {
        context = SpringApplication.run( GpsTabletsServiceApplication.class, args );
        CassandraDataControl.getInstance();
//        RedisDataControl.getRedis().getAllPatruls()
//                .subscribe( patrul -> {
//                    patrul.setCard( null );
//                    patrul.setToken( null );
//                    patrul.setTaskDate( null );
//                    patrul.setStatus( Status.FREE );
//                    patrul.setLatitudeOfTask( null );
//                    patrul.setLongitudeOfTask( null );
//                    patrul.setSelfEmploymentId( null );
//                    patrul.setTaskStatus( Status.FREE );
//                    RedisDataControl.getRedis().update( patrul ).subscribe(); } );
//        new Thread( Archive.getAchieve(), "archive" ).start(); // launching Archive to monitor all patruls, Card and SelfEmployment
    }
}
