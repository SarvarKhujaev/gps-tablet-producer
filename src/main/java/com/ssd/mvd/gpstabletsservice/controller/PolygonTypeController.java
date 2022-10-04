package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.PolygonType;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
public class PolygonTypeController {

    @MessageMapping ( value = "updatePolygonType" )
    public Mono< ApiResponseModel > updatePolygonType ( PolygonType polygonType ) { return CassandraDataControl
            .getInstance()
            .addValue( polygonType ); }

    @MessageMapping ( value = "addPolygonType" )
    public Mono< ApiResponseModel > addPolygonType ( PolygonType polygonType ) { return CassandraDataControl
            .getInstance()
            .addValue( polygonType ); }

    @MessageMapping ( value = "getCurrentPolygonType" )
    public Mono< PolygonType > getCurrentPolygonType ( UUID uuid ) { return CassandraDataControl
            .getInstance()
            .getAllPolygonType( uuid ); }

    @MessageMapping ( value = "deletePolygonType" )
    public Mono< ApiResponseModel > deletePolygonType ( UUID uuid ) { return CassandraDataControl
            .getInstance()
            .delete( CassandraTables.POLYGON_TYPE.name(),
                    "uuid",
                    uuid.toString() ); }

    @MessageMapping ( value = "getAllPolygonTypes" )
    public Flux< PolygonType > getAllPolygonTypes () { return CassandraDataControl
            .getInstance()
            .getAllPolygonType(); }
}
