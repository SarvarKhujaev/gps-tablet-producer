package com.ssd.mvd.gpstabletsservice;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;
import com.ssd.mvd.gpstabletsservice.database.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class    GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( String[] args ) {
        context = SpringApplication.run( GpsTabletsServiceApplication.class, args );
        CassandraDataControl.getInstance().register();
//        KafkaDataControl.getInstance();
        CassandraDataControlForTasks
                .getInstance()
                .getGetSelfEmploymentTask()
                .apply( UUID.fromString( "02d620be-97bc-4cb7-94e3-cb938774e574" ) )
                .subscribe( System.out::println );
    }
}
