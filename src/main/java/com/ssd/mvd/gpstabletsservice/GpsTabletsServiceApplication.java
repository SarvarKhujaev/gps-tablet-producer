package com.ssd.mvd.gpstabletsservice;

import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.PatrulAuthData;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.PatrulFIOData;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.PatrulRegionData;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.scheduler.Schedulers;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( final String[] args ) {
//        context = SpringApplication.run( GpsTabletsServiceApplication.class, args );
        CassandraDataControl.getInstance().close();
    }
}
