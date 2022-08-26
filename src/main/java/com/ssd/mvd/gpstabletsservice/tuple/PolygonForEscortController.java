package com.ssd.mvd.gpstabletsservice.tuple;

import org.springframework.messaging.handler.annotation.MessageMapping;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class PolygonForEscortController {
    @MessageMapping ( value = "addNewPolygonForEscort" )
    public Mono< ApiResponseModel > addNewPolygonForEscort ( PolygonForEscort polygon ) { return CassandraDataControlForEscort
            .getInstance()
            .addValue( polygon ); }

    @MessageMapping ( value = "getAllPolygonForEscort" )
    public Flux< PolygonForEscort > getAllPolygonForEscort () { return CassandraDataControlForEscort
            .getInstance()
            .getAllPolygonForEscort(); }

    @MessageMapping ( value = "getCurrentPolygonForEscort" )
    public Mono< PolygonForEscort > getAllPolygonForEscort ( String id ) { return CassandraDataControlForEscort
            .getInstance()
            .getAllPolygonForEscort( id ); }

    @MessageMapping ( value = "deletePolygonForEscort" )
    public Mono< ApiResponseModel > deletePolygonForEscort ( String id ) { return CassandraDataControlForEscort
            .getInstance()
            .delete( id ); }

    @MessageMapping ( value = "updatePolygonForEscort" )
    public Mono< ApiResponseModel > updatePolygonForEscort ( PolygonForEscort polygon ) { return CassandraDataControlForEscort
            .getInstance()
            .update( polygon ); }
}
