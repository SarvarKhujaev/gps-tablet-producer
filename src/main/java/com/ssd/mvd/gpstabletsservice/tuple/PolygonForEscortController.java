package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@RestController
public class PolygonForEscortController extends LogInspector {

    @MessageMapping ( value = "getAllPolygonForEscort" )
    public Flux< PolygonForEscort > getAllPolygonForEscort () { return CassandraDataControl
            .getInstance()
            .getGetAllEntities()
            .apply( CassandraTables.ESCORT, CassandraTables.POLYGON_FOR_ESCORT )
            .map( PolygonForEscort::new )
            .sequential()
            .publishOn( Schedulers.single() ); }

    @MessageMapping ( value = "getCurrentPolygonForEscort" )
    public Mono< PolygonForEscort > getAllPolygonForEscort ( String id ) { return CassandraDataControlForEscort
            .getInstance()
            .getGetCurrentPolygonForEscort()
            .apply( id ); }

    @MessageMapping ( value = "deletePolygonForEscort" )
    public Mono< ApiResponseModel > deletePolygonForEscort ( String id ) { return CassandraDataControlForEscort
            .getInstance()
            .getDeletePolygonForEscort()
            .apply( id )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "updatePolygonForEscort" )
    public Mono< ApiResponseModel > updatePolygonForEscort ( PolygonForEscort polygon ) { return CassandraDataControlForEscort
            .getInstance()
            .getUpdatePolygonForEscort()
            .apply( polygon )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "addNewPolygonForEscort" )
    public Mono< ApiResponseModel > addNewPolygonForEscort ( PolygonForEscort polygon ) { return CassandraDataControlForEscort
            .getInstance()
            .getSavePolygonForEscort()
            .apply( polygon )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }
}
