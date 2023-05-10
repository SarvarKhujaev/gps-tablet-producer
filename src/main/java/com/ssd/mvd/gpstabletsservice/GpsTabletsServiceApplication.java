package com.ssd.mvd.gpstabletsservice;

import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public final static ApplicationContext context = SpringApplication.run( GpsTabletsServiceApplication.class );

    public static void main( final String[] args ) {
        CassandraDataControl.getInstance().register();
        KafkaDataControl.getInstance(); }
}
