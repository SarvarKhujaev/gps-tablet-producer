package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.AtlasLustra;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// SAM - 74
@RestController
public class LustraController {

    @MessageMapping( value = "allLustra" ) // the list of all created camera
    public Flux< AtlasLustra > getAllLustra () { return CassandraDataControl
            .getInstance()
            .getAllLustra(); }

    @MessageMapping( value = "addLustra" ) // saving new AtlasLustra
    public Mono< ApiResponseModel > addLustra ( AtlasLustra atlasLustra ) { return CassandraDataControl
            .getInstance()
            .addValue( atlasLustra, true ); }

    @MessageMapping ( value = "updateLustra" )
    public Mono< ApiResponseModel > updateLustra ( AtlasLustra atlasLustra ) { return CassandraDataControl
            .getInstance()
            .addValue( atlasLustra, false ); }

    @MessageMapping ( value = "deleteLustra" )
    public Mono< ApiResponseModel > deleteLustra ( String uuid ) { return CassandraDataControl
            .getInstance()
            .delete(
                    CassandraDataControl
                            .getInstance()
                            .getLustre(),
                    "uuid",
                    uuid ); }

    @MessageMapping( value = "searchByNameLustra" ) // filters by name
    public Flux< AtlasLustra > searchByName ( String name ) { return this.getAllLustra()
            .filter( atlasLustra -> atlasLustra.getLustraName().contains( name ) ); }
}
