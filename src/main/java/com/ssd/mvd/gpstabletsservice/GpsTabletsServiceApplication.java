package com.ssd.mvd.gpstabletsservice;

import com.ssd.mvd.gpstabletsservice.database.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( String[] args ) {
        context = SpringApplication.run( GpsTabletsServiceApplication.class, args );
        CassandraDataControl.getInstance().register();
        KafkaDataControl.getInstance(); }
}
