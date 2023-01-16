package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.entity.Polygon;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Slf4j
@RestController
public class PolygonController {

    @MessageMapping( value = "deletePolygon" )
    public Mono< ApiResponseModel > deletePolygon ( String uuid ) { return CassandraDataControl
            .getInstance()
            .delete( CassandraTables.POLYGON.name(),
                    "uuid",
                    uuid )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "addNewPolygon" )
    public Mono< ApiResponseModel > addNewPolygon ( Polygon polygon ) { return CassandraDataControl
            .getInstance()
            .getSavePolygon()
            .apply( polygon )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "updatePolygon" )
    public Mono< ApiResponseModel > updatePolygon ( Polygon polygon ) { return CassandraDataControl
            .getInstance()
            .getUpdatePolygon()
            .apply( polygon )
            .onErrorContinue( ( error, object ) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping( value = "getPolygonList" )
    public Flux< Polygon > getPolygonList () { return CassandraDataControl
            .getInstance()
            .getGetAllPolygons()
            .get()
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "getCurrentPolygon" )
    public Mono< Polygon > getCurrentPolygon ( UUID uuid ) { return CassandraDataControl
            .getInstance()
            .getGetPolygonByUUID()
            .apply( uuid )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }
}
