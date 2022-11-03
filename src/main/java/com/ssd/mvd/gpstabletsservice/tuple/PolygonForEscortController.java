package com.ssd.mvd.gpstabletsservice.tuple;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.database.Archive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class PolygonForEscortController {

    @MessageMapping ( value = "getAllPolygonForEscort" )
    public Flux< PolygonForEscort > getAllPolygonForEscort () { return CassandraDataControlForEscort
            .getInstance()
            .getGetAllPolygonForEscort()
            .get(); }

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
            .onErrorContinue( ( throwable, o ) -> log.error(
                    "Error: " + throwable.getMessage()
                            + " Reason: " + o ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "updatePolygonForEscort" )
    public Mono< ApiResponseModel > updatePolygonForEscort ( PolygonForEscort polygon ) { return CassandraDataControlForEscort
            .getInstance()
            .getUpdatePolygonForEscort()
            .apply( polygon )
            .onErrorContinue( ( throwable, o ) -> log.error(
                    "Error: " + throwable.getMessage()
                            + " Reason: " + o ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "addNewPolygonForEscort" )
    public Mono< ApiResponseModel > addNewPolygonForEscort ( PolygonForEscort polygon ) { return CassandraDataControlForEscort
            .getInstance()
            .getSavePolygonForEscort()
            .apply( polygon )
            .onErrorContinue( ( throwable, o ) -> log.error(
                    "Error: " + throwable.getMessage()
                            + " Reason: " + o ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }
}
