package com.ssd.mvd.gpstracker.controller;

import com.ssd.mvd.gpstracker.database.RedisDataControl;
import com.ssd.mvd.gpstracker.entity.Patrul;
import com.ssd.mvd.gpstracker.entity.Polygon;
import com.ssd.mvd.gpstracker.entity.ScheduleForPolygonPatrul;
import com.ssd.mvd.gpstracker.response.ApiResponseModel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
public class PolygonForPatrulController { // SAM - 76

    @MessageMapping( value = "listOfPoligonsForPatrul" )
    public Flux< Polygon > listOfPoligonsForPatrul () { return RedisDataControl.getRedis().getAllPolygonsForPatrul(); }

    @MessageMapping ( value = "deletePolygonForPatrul" )
    public Mono< ApiResponseModel > deletePolygonForPatrul ( String uuid ) { return RedisDataControl.getRedis().deletePolygonForPatrul( uuid ); }

    @MessageMapping ( value = "updatePolygonForPatrul" )
    public Mono< ApiResponseModel > updatePolygonForPatrul ( Polygon polygon ) { return RedisDataControl.getRedis().updatePolygonForPatrul( polygon ); }

    @MessageMapping ( value = "addPatrulToPolygon" )
    public Mono< ApiResponseModel > addPatrulToPolygon ( ScheduleForPolygonPatrul scheduleForPolygonPatrul ) { return RedisDataControl.getRedis().addPatrulToPolygon( scheduleForPolygonPatrul ); }

    @MessageMapping ( value = "addPolygonForPatrul" )
    public Mono< ApiResponseModel > addPolygonForPatrul ( Polygon polygon ) { return RedisDataControl.getRedis().addValue( polygon, "new polygon for patrul: " + polygon.getUuid() + " was added" ); }

    @MessageMapping ( value = "getPatrulsForPolygon" )
    public Mono< List< Patrul > > getPatrulsForPolygon ( String uuid ) { return RedisDataControl.getRedis().getPolygon( uuid, "polygonForPatrul" ).flatMap( polygon -> Mono.just( polygon.getPatrulList() ) ); }
}
