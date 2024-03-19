package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.entity.polygons.PolygonType;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
public final class PolygonTypeController extends LogInspector {

    @MessageMapping ( value = "updatePolygonType" )
    public Mono< ApiResponseModel > updatePolygonType ( final PolygonType polygonType ) {
        return CassandraDataControl
            .getInstance()
            .updatePolygonType
            .apply( polygonType )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "addPolygonType" )
    public Mono< ApiResponseModel > addPolygonType ( final PolygonType polygonType ) {
        return CassandraDataControl
            .getInstance()
            .savePolygonType
            .apply( polygonType )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "getCurrentPolygonType" )
    public Mono< PolygonType > getCurrentPolygonType ( final UUID uuid ) {
        return CassandraDataControl
            .getInstance()
            .getPolygonTypeByUUID
            .apply( uuid )
            .onErrorContinue( super::logging );
    }

    @MessageMapping ( value = "deletePolygonType" )
    public Mono< ApiResponseModel > deletePolygonType ( final UUID uuid ) {
        return CassandraDataControl
            .getInstance()
            .deleteRow(
                    CassandraTables.POLYGON_TYPE.name(),
                    "uuid",
                    uuid.toString() )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "getAllPolygonTypes" )
    public Flux< PolygonType > getAllPolygonTypes () {
        return CassandraDataControl
            .getInstance()
            .getAllEntities
            .apply( CassandraTables.TABLETS, CassandraTables.POLYGON_TYPE )
            .map( PolygonType::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging );
    }
}
