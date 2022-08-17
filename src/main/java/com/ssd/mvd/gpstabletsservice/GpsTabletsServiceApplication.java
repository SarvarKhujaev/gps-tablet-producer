package com.ssd.mvd.gpstabletsservice;

import org.springframework.boot.SpringApplication;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( String[] args ) {
        context = SpringApplication.run( GpsTabletsServiceApplication.class, args );
        Archive.getAchieve(); }
}
