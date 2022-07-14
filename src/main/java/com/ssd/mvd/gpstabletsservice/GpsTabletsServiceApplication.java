package com.ssd.mvd.gpstabletsservice;

import org.springframework.boot.SpringApplication;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( String[] args ) {
        new Thread( Archive.getAchieve(), "archive" ).start(); // launching Archieve to monitor all patruls, Card and SelfEmployment
        context = SpringApplication.run( GpsTabletsServiceApplication.class, args ); }
}
