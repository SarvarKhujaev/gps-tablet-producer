package com.ssd.mvd.gpstabletsservice.controller;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.entity.Polygon;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.entity.ScheduleForPolygonPatrul;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.handler.annotation.MessageMapping;

@RestController
public class PolygonForPatrulController extends LogInspector { // SAM - 76

    @MessageMapping( value = "listOfPoligonsForPatrul" )
    public Flux< Polygon > listOfPoligonsForPatrul () { return CassandraDataControl
            .getInstance()
            .getGetAllPolygonForPatrul()
            .get()
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "deletePolygonForPatrul" )
    public Mono< ApiResponseModel > deletePolygonForPatrul ( String uuid ) { return CassandraDataControl
            .getInstance()
            .getDeletePolygonForPatrul()
            .apply( uuid )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "updatePolygonForPatrul" )
    public Mono< ApiResponseModel > updatePolygonForPatrul ( Polygon polygon ) { return CassandraDataControl
            .getInstance()
            .getUpdatePolygonForPatrul()
            .apply( polygon )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "addPatrulToPolygon" )
    public Mono< ApiResponseModel > addPatrulToPolygon ( ScheduleForPolygonPatrul scheduleForPolygonPatrul ) {
        return super.getCheckList().test( scheduleForPolygonPatrul.getPatrulUUIDs() )
                ? super.getFunction().apply( Map.of(
                        "message", "Wrong params",
                        "success", false,
                        "code", 201 ) )
                : CassandraDataControl
                .getInstance()
                .getAddPatrulToPolygon()
                .apply( scheduleForPolygonPatrul )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "addPolygonForPatrul" )
    public Mono< ApiResponseModel > addPolygonForPatrul ( Polygon polygon ) { return CassandraDataControl
            .getInstance()
            .getAddPolygonForPatrul()
            .apply( polygon )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "getPatrulsForPolygon" )
    public Mono< List< Patrul > > getPatrulsForPolygon ( String uuid ) {
        List< Patrul > patrulList = new ArrayList<>();
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
        return Mono.just( patrulList ); }
}
