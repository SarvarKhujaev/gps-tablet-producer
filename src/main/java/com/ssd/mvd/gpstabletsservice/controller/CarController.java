package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.response.Status;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.ReqCar;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
public class CarController {
    @MessageMapping( value = "carList" ) // the list of all cars
    public Flux< ReqCar > getAllCars () { return CassandraDataControl
            .getInstance()
            .getCar(); }

    @MessageMapping ( value = "getCurrentCar" )
    public Mono< ReqCar > getCurrentCar ( String gosno ) { return CassandraDataControl
            .getInstance()
            .getCar( UUID.fromString( gosno ) ); }

    @MessageMapping( value = "searchByGosnoCar" )
    public Flux< ReqCar > searchByGosno ( String gosno ) { return CassandraDataControl
            .getInstance()
            .getCar()
            .filter( reqCar -> reqCar.getGosNumber().equals( gosno ) ); }

    @MessageMapping( value = "addCar" )
    public Mono< ApiResponseModel > addCar ( ReqCar reqCar ) { return CassandraDataControl
            .getInstance()
            .addValue( reqCar ); }

    @MessageMapping ( value = "updateCar" )
    public Mono< ApiResponseModel > updateCar ( ReqCar reqCar ) { return CassandraDataControl
            .getInstance()
            .update( reqCar ); }

    @MessageMapping( value = "deleteCar" )
    public Mono< ApiResponseModel > deleteCar ( String gosno ) {
        try { return CassandraDataControl
                    .getInstance()
                    .delete( gosno );
        } catch ( Exception e ) { return Mono.just(
                ApiResponseModel.builder()
                        .success( false )
                        .status(
                                Status.builder()
                                        .code( 201 )
                                        .message( "Wrong Car params" )
                                        .build() ) .build() ); } }
}
