package com.ssd.mvd.gpstracker.controller;

import com.ssd.mvd.gpstracker.database.RedisDataControl;
import com.ssd.mvd.gpstracker.entity.AtlasLustra;
import com.ssd.mvd.gpstracker.response.ApiResponseModel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

// SAM - 74
@RestController
public class LustraController {

    @MessageMapping( value = "allLustra" ) // the list of all created camera
    public Flux< AtlasLustra > getAllLustra () { return RedisDataControl.getRedis().getAllLustra(); }

    @MessageMapping( value = "addLustra" ) // saving new AtlasLustra
    public Mono< ApiResponseModel > addLustra ( AtlasLustra atlasLustra ) { return RedisDataControl.getRedis().addValue( atlasLustra ); }

    @MessageMapping ( value = "updateLustra" )
    public Mono< ApiResponseModel > updateLustra ( AtlasLustra atlasLustra ) { return RedisDataControl.getRedis().update( atlasLustra ); }

    @MessageMapping ( value = "deleteLustra" )
    public Mono< ApiResponseModel > deleteLustra ( String uuid ) { return RedisDataControl.getRedis().deleteLustra( UUID.fromString( uuid ) ); }

    @MessageMapping( value = "searchByNameLustra" ) // filters by name
    public Flux< AtlasLustra > searchByName ( String name ) { return this.getAllLustra().filter( atlasLustra -> atlasLustra.getLustraName().contains( name ) ); }
}
