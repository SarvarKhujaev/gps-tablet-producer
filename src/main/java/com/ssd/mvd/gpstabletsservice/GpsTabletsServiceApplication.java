package com.ssd.mvd.gpstabletsservice;

import org.springframework.boot.SpringApplication;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public static void main( String[] args ) {
        new Thread( Archive.getAchieve(), "archive" ).start(); // launching Archieve to monitor all patruls, Card and SelfEmployment
        SpringApplication.run( GpsTabletsServiceApplication.class, args ); }
}
