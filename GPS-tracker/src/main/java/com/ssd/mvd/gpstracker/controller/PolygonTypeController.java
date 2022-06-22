package com.ssd.mvd.gpstracker.controller;

import com.ssd.mvd.gpstracker.database.RedisDataControl;
import com.ssd.mvd.gpstracker.entity.PolygonType;
import com.ssd.mvd.gpstracker.response.ApiResponseModel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class PolygonTypeController {

    @MessageMapping ( value = "updatePolygonType" )
    public Mono< ApiResponseModel > updatePolygonType ( PolygonType polygonType ) { return RedisDataControl.getRedis().update( polygonType ); }

    @MessageMapping ( value = "addPolygonType" )
    public Mono< ApiResponseModel > addPolygonType ( PolygonType polygonType ) { return RedisDataControl.getRedis().addValue( polygonType ); }

    @MessageMapping ( value = "getCurrentPolygonType" )
    public Mono< PolygonType > getCurrentPolygonType ( UUID uuid ) { return RedisDataControl.getRedis().getPolygonType( uuid ); }

    @MessageMapping ( value = "deletePolygonType" )
    public Mono< ApiResponseModel > deletePolygonType ( UUID uuid ) { return RedisDataControl.getRedis().deletePolygonType( uuid ); }

    @MessageMapping ( value = "getAllPolygonTypes" )
    public Flux< PolygonType > getAllPolygonTypes () { return RedisDataControl.getRedis().getAllPolygonTypes(); }
}
