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
//                    patrul.setTaskId( null );
//                    patrul.setTaskDate( null );
//                    patrul.setStatus( Status.FREE );
//                    patrul.setLatitudeOfTask( null );
//                    patrul.setLongitudeOfTask( null );
//                    patrul.setTaskTypes( TaskTypes.FREE );
//                    RedisDataControl.getRedis().update( patrul ).subscribe(); } );
        Archive.getAchieve(); }
}
