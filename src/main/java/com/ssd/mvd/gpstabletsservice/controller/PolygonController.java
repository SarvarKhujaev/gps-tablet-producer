package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.entity.polygons.Polygon;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
public final class PolygonController extends LogInspector {

    @MessageMapping( value = "deletePolygon" )
    public Mono< ApiResponseModel > deletePolygon ( final String uuid ) {
        return CassandraDataControl
            .getInstance()
            .deleteRow( CassandraTables.POLYGON.name(),
                    "uuid",
                    uuid )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "addNewPolygon" )
    public Mono< ApiResponseModel > addNewPolygon ( final Polygon polygon ) {
        return CassandraDataControl
            .getInstance()
            .savePolygon
            .apply( polygon )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "updatePolygon" )
    public Mono< ApiResponseModel > updatePolygon ( final Polygon polygon ) {
        return CassandraDataControl
            .getInstance()
            .updatePolygon
            .apply( polygon )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping( value = "getPolygonList" )
    public Flux< Polygon > getPolygonList () {
        return CassandraDataControl
            .getInstance()
            .getAllEntities
            .apply( CassandraTables.TABLETS, CassandraTables.POLYGON )
            .map( Polygon::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging );
    }

    @MessageMapping ( value = "getCurrentPolygon" )
    public Mono< Polygon > getCurrentPolygon ( final UUID uuid ) {
        return CassandraDataControl
            .getInstance()
            .getPolygonByUUID
            .apply( uuid )
            .onErrorContinue( super::logging );
    }
}
