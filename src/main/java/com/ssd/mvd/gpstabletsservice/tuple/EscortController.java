package com.ssd.mvd.gpstabletsservice.tuple;

import org.springframework.messaging.handler.annotation.MessageMapping;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class EscortController {
    @MessageMapping ( value = "addNewTupleOfPatrul" )
    public Flux< ApiResponseModel > addNewTupleOfPatrul( EscortTuple escortTuple) { return CassandraDataControlForEscort
            .getInstance().addValue( escortTuple ); }

    @MessageMapping ( value = "getAllTupleOfPatrul" )
    public Flux< EscortTuple > getAllTupleOfPatrul () { return CassandraDataControlForEscort.getInstance().getAllTupleOfPatrul(); }

    @MessageMapping ( value = "getCurrentTupleOfPatrul" )
    public Flux< EscortTuple > getCurrentTupleOfPatrul (String id ) { return CassandraDataControlForEscort.getInstance()
            .getAllTupleOfPatrul( id ); }

    @MessageMapping ( value = "deleteTupleOfPatrul" )
    public Mono< ApiResponseModel > deleteTupleOfPatrul ( String id ) { return CassandraDataControlForEscort.getInstance().deleteTupleOfPatrul( id ); }
}
