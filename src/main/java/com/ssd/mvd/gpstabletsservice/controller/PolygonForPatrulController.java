package com.ssd.mvd.gpstabletsservice.controller;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.entity.Polygon;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.entity.ScheduleForPolygonPatrul;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.handler.annotation.MessageMapping;

@Slf4j
@RestController
public class PolygonForPatrulController { // SAM - 76

    @MessageMapping( value = "listOfPoligonsForPatrul" )
    public Flux< Polygon > listOfPoligonsForPatrul () { return CassandraDataControl
            .getInstance()
            .getGetAllPolygonForPatrul()
            .get()
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "deletePolygonForPatrul" )
    public Mono< ApiResponseModel > deletePolygonForPatrul ( String uuid ) { return CassandraDataControl
            .getInstance()
            .deletePolygonForPatrul( uuid )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "updatePolygonForPatrul" )
    public Mono< ApiResponseModel > updatePolygonForPatrul ( Polygon polygon ) { return CassandraDataControl
            .getInstance()
            .updatePolygonForPatrul( polygon )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "addPatrulToPolygon" )
    public Mono< ApiResponseModel > addPatrulToPolygon ( ScheduleForPolygonPatrul scheduleForPolygonPatrul ) {
        return scheduleForPolygonPatrul.getPatrulUUIDs() == null
                || scheduleForPolygonPatrul.getPatrulUUIDs().size() == 0
                ? Archive
                .getArchive()
                .getFunction()
                .apply( Map.of(
                        "message", "Wrong params",
                        "success", false,
                        "code", 201 ) )
                : CassandraDataControl
                .getInstance()
                .getAddPatrulToPolygon()
                .apply( scheduleForPolygonPatrul )
                .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                        error.getMessage(), object ) ) )
                .onErrorReturn( Archive
                        .getArchive()
                        .getErrorResponse()
                        .get() ); }

    @MessageMapping ( value = "addPolygonForPatrul" )
    public Mono< ApiResponseModel > addPolygonForPatrul ( Polygon polygon ) { return CassandraDataControl
            .getInstance()
            .addPolygonForPatrul( polygon )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

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
