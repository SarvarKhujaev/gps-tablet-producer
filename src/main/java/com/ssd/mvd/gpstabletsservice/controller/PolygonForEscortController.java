package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.tuple.PolygonForEscort;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public final class PolygonForEscortController extends LogInspector {

    @MessageMapping ( value = "getAllPolygonForEscort" )
    public Flux< PolygonForEscort > getAllPolygonForEscort () { return CassandraDataControl
            .getInstance()
            .getGetAllEntities()
            .apply( CassandraTables.ESCORT, CassandraTables.POLYGON_FOR_ESCORT )
            .map( PolygonForEscort::new )
            .sequential()
            .publishOn( Schedulers.single() ); }

    @MessageMapping ( value = "getCurrentPolygonForEscort" )
    public Mono< PolygonForEscort > getAllPolygonForEscort ( final String id ) { return CassandraDataControlForEscort
            .getInstance()
            .getGetCurrentPolygonForEscort()
            .apply( id ); }

    @MessageMapping ( value = "deletePolygonForEscort" )
    public Mono< ApiResponseModel > deletePolygonForEscort ( final String id ) { return CassandraDataControlForEscort
            .getInstance()
            .getDeletePolygonForEscort()
            .apply( id )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "updatePolygonForEscort" )
    public Mono< ApiResponseModel > updatePolygonForEscort ( final PolygonForEscort polygon ) {
        polygon.setName( polygon.getName().replaceAll( "'", "" ) );
        return CassandraDataControlForEscort
                .getInstance()
                .getUpdatePolygonForEscort()
                .apply( polygon )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "addNewPolygonForEscort" )
    public Mono< ApiResponseModel > addNewPolygonForEscort ( final PolygonForEscort polygon ) {
        polygon.setName( polygon.getName().replaceAll( "'", "" ) );
        return CassandraDataControlForEscort
                .getInstance()
                .getSavePolygonForEscort()
                .apply( polygon )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }
}
