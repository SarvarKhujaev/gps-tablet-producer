package com.ssd.mvd.gpstracker.controller;

import com.ssd.mvd.gpstracker.database.RedisDataControl;
import com.ssd.mvd.gpstracker.entity.ReqCar;
import com.ssd.mvd.gpstracker.response.ApiResponseModel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class CarController {
    @MessageMapping( value = "carList" ) // the list of all cars
    public Flux< ReqCar > getAllCars () { return RedisDataControl.getRedis().getAllCars(); }

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
