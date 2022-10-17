package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.entity.ReqCar;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Slf4j
@RestController
public class CarController {
    @MessageMapping( value = "carList" ) // the list of all cars
    public Flux< ReqCar > getAllCars () { return CassandraDataControl
            .getInstance()
            .getCar()
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

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
            .addValue( reqCar )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "updateCar" )
    public Mono< ApiResponseModel > updateCar ( ReqCar reqCar ) { return CassandraDataControl
            .getInstance()
            .update( reqCar )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping( value = "deleteCar" )
    public Mono< ApiResponseModel > deleteCar ( String gosno ) {
        return CassandraDataControl
                    .getInstance()
                    .delete( gosno )
                .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                        error.getMessage(), object ) ) )
                .onErrorReturn( Archive
                        .getArchive()
                        .getErrorResponse()
                        .get() ); }
}
