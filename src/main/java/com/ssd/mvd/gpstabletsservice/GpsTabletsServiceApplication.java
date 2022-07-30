package com.ssd.mvd.gpstabletsservice;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( String[] args ) {
        context = SpringApplication.run( GpsTabletsServiceApplication.class, args );
        RedisDataControl.getRedis().getAllPatruls()
//                .filter( activeTask -> activeTask.getTaskId() == null ^ activeTask.getStatus() == null )
//                .subscribe( activeTask -> {
//                    System.out.println( activeTask.getTaskId() );
//                } );
//                .subscribe( patrul -> {
//                    patrul.setCard( null );
//                    patrul.setStatus( Status.FREE );
//                    patrul.setSelfEmploymentId( null );;
//                    RedisDataControl.getRedis().update( patrul ).subscribe(); } );
                .subscribe( patrul -> {
                    patrul.setCard( null );
                    patrul.setToken( null );
                    patrul.setTaskDate( null );
                    patrul.setStatus( Status.FREE );
                    patrul.setSelfEmploymentId( null );
                    RedisDataControl.getRedis().update( patrul ).subscribe(); } );
//        new Thread( Archive.getAchieve(), "archive" ).start(); // launching Archive to monitor all patruls, Card and SelfEmployment
    }
}
