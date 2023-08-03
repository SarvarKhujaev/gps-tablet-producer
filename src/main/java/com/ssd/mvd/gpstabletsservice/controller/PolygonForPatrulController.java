package com.ssd.mvd.gpstabletsservice.controller;

import java.util.List;
import java.util.ArrayList;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.ssd.mvd.gpstabletsservice.entity.polygons.Polygon;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.ScheduleForPolygonPatrul;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.handler.annotation.MessageMapping;

@RestController
public final class PolygonForPatrulController extends LogInspector {
    @MessageMapping( value = "listOfPoligonsForPatrul" )
    public Flux< Polygon > listOfPoligonsForPatrul () { return CassandraDataControl
            .getInstance()
            .getGetAllEntities()
            .apply( CassandraTables.TABLETS, CassandraTables.POLYGON_FOR_PATRUl )
            .map( Polygon::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "getPatrulsForPolygon" )
    public Mono< List< Patrul > > getPatrulsForPolygon ( final String uuid ) {
        final List< Patrul > patrulList = new ArrayList<>();
        CassandraDataControl
                .getInstance()
                .getGetPolygonForPatrul()
                .apply( uuid )
                .map( Polygon::getPatrulList )
                .subscribe( uuids -> uuids.forEach( uuid1 -> CassandraDataControl
                        .getInstance()
                        .getGetPatrulByUUID()
                        .apply( uuid1 )
                        .subscribe( patrulList::add ) ) );
        return super.convert( patrulList ); }

    @MessageMapping ( value = "deletePolygonForPatrul" )
    public Mono< ApiResponseModel > deletePolygonForPatrul ( final String uuid ) { return CassandraDataControl
            .getInstance()
            .getDeletePolygonForPatrul()
            .apply( uuid )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "addPolygonForPatrul" )
    public Mono< ApiResponseModel > addPolygonForPatrul ( final Polygon polygon ) {
        polygon.setName( polygon.getName().replaceAll( "'", "" ) );
        return CassandraDataControl
                .getInstance()
                .getAddPolygonForPatrul()
                .apply( polygon )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "updatePolygonForPatrul" )
    public Mono< ApiResponseModel > updatePolygonForPatrul ( final Polygon polygon ) {
        polygon.setName( polygon.getName().replaceAll( "'", "" ) );
        return CassandraDataControl
                .getInstance()
                .getUpdatePolygonForPatrul()
                .apply( polygon )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "addPatrulToPolygon" )
    public Mono< ApiResponseModel > addPatrulToPolygon ( final ScheduleForPolygonPatrul scheduleForPolygonPatrul ) {
        return super.checkRequest.test( scheduleForPolygonPatrul.getPatrulUUIDs(), 6 )
                ? super.error.apply( 2 )
                : CassandraDataControl
                .getInstance()
                .getAddPatrulToPolygon()
                .apply( scheduleForPolygonPatrul )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }
}
