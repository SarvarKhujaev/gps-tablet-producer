package com.ssd.mvd.gpstabletsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class    GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( String[] args ) {
        context = SpringApplication.run( GpsTabletsServiceApplication.class, args );
        CassandraDataControl.getInstance().register();
//        CassandraDataControl
//                .getInstance()
//                .getPatrul( UUID.fromString( "2a5db0ae-09ec-49a8-a5c4-1f2b11874623" ) )
//                .subscribe( patrul -> {
//                    PatrulLoginRequest request = new PatrulLoginRequest();
//                    request.setPassword( patrul.getPassword() );
//                    request.setLogin( patrul.getLogin() );
//                    request.setSimCardNumber( "123" );
//                    CassandraDataControl
//                            .getInstance()
//                            .login( request )
////                            .logout( "MmE1ZGIwYWUtMDllYy00OWE4LWE1YzQtMWYyYjExODc0NjIzQEFETV8xODIxOF8wNzNfMDAyNUBBRE1fMTgyMThfMDczXzAwMjVAMTIzNEB0dHVFNW9jZ2xDT3lHcmdkWUthVGRHTWxHRXQxMV9QTQ==" )
//                            .subscribe( System.out::println );
//                } );
    }
}
