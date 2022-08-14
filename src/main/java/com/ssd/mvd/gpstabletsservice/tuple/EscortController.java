package com.ssd.mvd.gpstabletsservice.tuple;

import org.springframework.messaging.handler.annotation.MessageMapping;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class EscortController {
    @MessageMapping ( value = "addNewEscort" )
    public Flux< ApiResponseModel > addNewTupleOfPatrul ( EscortTuple escortTuple ) { return CassandraDataControlForEscort
            .getInstance()
            .addValue( escortTuple ); }

    @MessageMapping ( value = "getAllEscort" )
    public Flux< EscortTuple > getAllTupleOfPatrul () { return CassandraDataControlForEscort
            .getInstance()
            .getAllTupleOfPatrul(); }

    @MessageMapping ( value = "getCurrentEscort" )
    public Flux< EscortTuple > getCurrentTupleOfPatrul ( String id ) { return CassandraDataControlForEscort
            .getInstance()
            .getAllTupleOfPatrul( id ); }

    @MessageMapping ( value = "deleteEscort" )
    public Mono< ApiResponseModel > deleteTupleOfPatrul ( String id ) { return CassandraDataControlForEscort
            .getInstance()
            .deleteTupleOfPatrul( id ); }

    @MessageMapping ( value = "updateEscort" )
    public Mono< ApiResponseModel > updateTupleOfPatrul ( EscortTuple escortTuple ) { return CassandraDataControlForEscort
            .getInstance()
            .update( escortTuple ); }
}
