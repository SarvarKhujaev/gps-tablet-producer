package com.ssd.mvd.gpstabletsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.text.ParseException;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( String[] args ) throws ParseException {
        context = SpringApplication.run( GpsTabletsServiceApplication.class, args );
        Archive.getAchieve(); }
}
