package com.ssd.mvd.gpstabletsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.ssd.mvd.gpstabletsservice.database.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.tuple.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;

@SpringBootApplication
public class    GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( String[] args ) {
        context = SpringApplication.run( GpsTabletsServiceApplication.class, args );
        CassandraDataControl.getInstance().register();
        CassandraDataControlForEscort.getInstance();
        CassandraDataControlForTasks.getInstance();
        KafkaDataControl.getInstance(); }
}
