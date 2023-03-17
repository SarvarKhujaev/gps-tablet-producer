package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.entity.Polygon;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
public class PolygonController extends LogInspector {

    @MessageMapping( value = "deletePolygon" )
    public Mono< ApiResponseModel > deletePolygon ( String uuid ) { return CassandraDataControl
            .getInstance()
            .delete( CassandraTables.POLYGON.name(),
                    "uuid",
                    uuid )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "addNewPolygon" )
    public Mono< ApiResponseModel > addNewPolygon ( Polygon polygon ) { return CassandraDataControl
            .getInstance()
            .getSavePolygon()
            .apply( polygon )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "updatePolygon" )
    public Mono< ApiResponseModel > updatePolygon ( Polygon polygon ) { return CassandraDataControl
            .getInstance()
            .getUpdatePolygon()
            .apply( polygon )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping( value = "getPolygonList" )
    public Flux< Polygon > getPolygonList () { return CassandraDataControl
            .getInstance()
            .getGetAllPolygons()
            .get()
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "getCurrentPolygon" )
    public Mono< Polygon > getCurrentPolygon ( UUID uuid ) { return CassandraDataControl
            .getInstance()
            .getGetPolygonByUUID()
            .apply( uuid )
            .onErrorContinue( super::logging ); }
}
