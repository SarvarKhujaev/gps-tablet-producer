package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.ReqCar;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class CarController {
    @MessageMapping( value = "carList" ) // the list of all cars
    public Flux< ReqCar > getAllCars () { return CassandraDataControl.getInstance().getCars(); }

    @MessageMapping ( value = "getCurrentCar" )
    public Mono< ReqCar > getCurrentCar ( String gosno ) { return RedisDataControl.getRedis().getCar( gosno ); }

    @MessageMapping( value = "searchByGosnoCar" )
    public Mono< ReqCar > searchByGosno ( String gosno ) { return RedisDataControl.getRedis().getCar( gosno ); }

    @MessageMapping( value = "addCar" )
    public Mono< ApiResponseModel > addCar ( ReqCar reqCar ) { return RedisDataControl.getRedis().addValue( reqCar ); }

    @MessageMapping ( value = "updateCar" )
    public Mono< ApiResponseModel > updateCar ( ReqCar reqCar ) { return RedisDataControl.getRedis().update( reqCar ); }

    @MessageMapping( value = "deleteCar" )
    public Mono< ApiResponseModel > deleteCar ( String gosno ) { return RedisDataControl.getRedis().deleteCar( gosno ); }
}
