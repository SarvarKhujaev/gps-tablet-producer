package com.ssd.mvd.gpstabletsservice;

import com.ssd.mvd.gpstabletsservice.database.Archive;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public static void main(String[] args) throws InterruptedException {
        new Thread( Archive.getAchieve(), "archive" ).start(); // launching Archieve to monitor all patruls, Card and SelfEmployment
        SpringApplication.run(GpsTabletsServiceApplication.class, args);
    }
}
