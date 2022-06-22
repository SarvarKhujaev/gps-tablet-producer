package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.database.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.payload.ReqExchangeLocation;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.response.Status;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping( "/api/location" )
public class LocationExchangeController {

    @PostMapping( "/clear" )
    public HttpEntity< String > save () {
        KafkaDataControl.getInstance().clear();
        return ResponseEntity.ok( "all cleared" );
    }

    @MessageMapping( value = "exchange" )
    public Mono< ApiResponseModel > exchange ( ReqExchangeLocation reqExchangeLocation ) {
        CassandraDataControl.getInstance().addValue( reqExchangeLocation ); // saving to Cassandra
        return Mono.just( ApiResponseModel.builder().status( Status.builder().code( 200 ).message( "success" ).build() ).build() );
    }

    @MessageMapping( value = "clear" )
    public Mono< ApiResponseModel > clear () {
        KafkaDataControl.getInstance().clear();
        return Mono.just( ApiResponseModel.builder().status( Status.builder().code( 200 ).message( "all clear my lord" ).build() ).build() );
    }
}
