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
//        Point point = new Point( 65.6, 66.7 );
//        RedisDataControl.getRedis().getAllPatruls()
//                    .filter( patrul -> patrul.getStatus().compareTo( com.ssd.mvd.gpstabletsservice.constants.Status.FREE ) == 0
//                            && patrul.getTaskTypes().compareTo( TaskTypes.FREE ) == 0
//                            && patrul.getLatitude() != null && patrul.getLongitude() != null )
//                    .flatMap( patrul -> { patrul.setDistance(  calculate( point, patrul ) );
//                        return Mono.just( patrul ); } )
//                    .sort( Comparator.comparing( Patrul::getDistance ) )
//                .subscribe( patrul -> System.out.println( patrul.getPassportNumber() ) );
        Archive.getAchieve(); }
}
