package com.ssd.mvd.gpstracker;

import com.ssd.mvd.gpstracker.database.Archive;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpsTrackerApplication {

    public static void main(String[] args) {
        new Thread( Archive.getAchieve(), "archive" ).start(); // launching Archieve to monitor all patruls, Card and SelfEmployment
        SpringApplication.run( GpsTrackerApplication.class, args);
    }
}
