package com.ssd.mvd.gpstabletsservice;

import com.ssd.mvd.gpstabletsservice.database.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( String[] args ) {
        context = SpringApplication.run( GpsTabletsServiceApplication.class, args ); // 71c79268-b6bf-42b4-88bf-1109b4a00ff2
        CassandraDataControl.getInstance().register();
        Archive.getAchieve(); }
}
