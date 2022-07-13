package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.payload.ReqExchangeLocation;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.response.Status;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class LocationExchangeController {
    @MessageMapping( value = "exchange" )
    public Mono< ApiResponseModel > exchange ( ReqExchangeLocation reqExchangeLocation ) {
        CassandraDataControl.getInstance().addValue( reqExchangeLocation ); // saving to Cassandra
        return Mono.just( ApiResponseModel.builder().status( Status.builder().code( 200 ).message( "success" ).build() ).build() ); }
}
