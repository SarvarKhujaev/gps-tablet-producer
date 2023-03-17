package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.entity.AtlasLustra;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class LustraController extends LogInspector {
    @MessageMapping ( value = "updateLustra" )
    public Mono< ApiResponseModel > updateLustra ( AtlasLustra atlasLustra ) { return CassandraDataControl
            .getInstance()
            .getSaveLustra()
            .apply( atlasLustra, false )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping( value = "addLustra" ) // saving new AtlasLustra
    public Mono< ApiResponseModel > addLustra ( AtlasLustra atlasLustra ) { return CassandraDataControl
            .getInstance()
            .getSaveLustra()
            .apply( atlasLustra, true )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "deleteLustra" )
    public Mono< ApiResponseModel > deleteLustra ( String uuid ) { return CassandraDataControl
            .getInstance()
            .delete( CassandraTables
                            .LUSTRA
                            .name(),
                    "uuid",
                    uuid )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping( value = "searchByNameLustra" ) // filters by name
    public Flux< AtlasLustra > searchByName ( String name ) { return this.getAllLustra()
            .filter( atlasLustra -> atlasLustra.getLustraName().contains( name ) ); }

    @MessageMapping( value = "allLustra" ) // the list of all created camera
    public Flux< AtlasLustra > getAllLustra () { return CassandraDataControl
            .getInstance()
            .getGetAllLustra()
            .get()
            .onErrorContinue( super::logging ); }
}
