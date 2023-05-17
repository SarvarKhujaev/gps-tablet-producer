package com.ssd.mvd.gpstabletsservice;

import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskInspector;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.UUID;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( final String[] args ) {
        context = SpringApplication.run( GpsTabletsServiceApplication.class, args );
        CassandraDataControl.getInstance().register();
//        KafkaDataControl.getInstance();

        CassandraDataControl
                .getInstance()
                .getGetPatrulByUUID()
                .apply( UUID.fromString( "6c979988-905b-4159-a797-bb721f51cac9" ) )
                .subscribe( patrul -> TaskInspector
                        .getInstance()
                        .getGetTaskData()
                        .apply( patrul, TaskTypes.CARD_DETAILS )
                        .subscribe( apiResponseModel -> System.out.println( apiResponseModel.getData().getData() ) ) );
    }
}
