package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.PolygonType;
import com.ssd.mvd.gpstabletsservice.database.Archive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Slf4j
@RestController
public class PolygonTypeController {

    @MessageMapping ( value = "updatePolygonType" )
    public Mono< ApiResponseModel > updatePolygonType ( PolygonType polygonType ) { return CassandraDataControl
            .getInstance()
            .getUpdatePolygonType()
            .apply( polygonType )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "addPolygonType" )
    public Mono< ApiResponseModel > addPolygonType ( PolygonType polygonType ) { return CassandraDataControl
            .getInstance()
            .getSavePolygonType()
            .apply( polygonType )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "getCurrentPolygonType" )
    public Mono< PolygonType > getCurrentPolygonType ( UUID uuid ) { return CassandraDataControl
            .getInstance()
            .getGetAllPolygonTypeByUUID()
            .apply( uuid )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "deletePolygonType" )
    public Mono< ApiResponseModel > deletePolygonType ( UUID uuid ) { return CassandraDataControl
            .getInstance()
            .delete( CassandraTables.POLYGON_TYPE.name(),
                    "uuid",
                    uuid.toString() )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "getAllPolygonTypes" )
    public Flux< PolygonType > getAllPolygonTypes () { return CassandraDataControl
            .getInstance()
            .getGetAllPolygonType()
            .get()
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }
}
