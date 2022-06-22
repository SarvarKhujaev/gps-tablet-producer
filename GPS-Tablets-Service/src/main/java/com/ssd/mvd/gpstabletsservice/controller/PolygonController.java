package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import com.ssd.mvd.gpstabletsservice.entity.Polygon;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class PolygonController {

    @MessageMapping( value = "deletePolygon" )
    public Mono< ApiResponseModel > deletePolygon ( String polygonName ) { return RedisDataControl.getRedis().deletePolygon( polygonName ); }

    @MessageMapping ( value = "addNewPolygon" )
    public Mono< ApiResponseModel > addNewPolygon ( Polygon polygon ) { return RedisDataControl.getRedis().addValue( polygon ); }

    @MessageMapping ( value = "updatePolygon" )
    public Mono< ApiResponseModel > updatePolygon ( Polygon polygon ) { return RedisDataControl.getRedis().update( polygon ); }

    @MessageMapping( value = "getPolygonList" )
    public Flux< Polygon > getPolygonList () { return RedisDataControl.getRedis().getAllPolygons(); }
}
