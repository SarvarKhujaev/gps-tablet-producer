package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.TupleOfPatrul;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class TupleOfPatrulController {
    @MessageMapping ( value = "addNewTupleOfPatrul" )
    public Flux< ApiResponseModel > addNewTupleOfPatrul( TupleOfPatrul tupleOfPatrul ) { return CassandraDataControl
            .getInstance().addValue( tupleOfPatrul ); }

    @MessageMapping ( value = "getAllTupleOfPatrul" )
    public Flux< TupleOfPatrul > getAllTupleOfPatrul () { return CassandraDataControl.getInstance().getAllTupleOfPatrul(); }

    @MessageMapping ( value = "getCurrentTupleOfPatrul" )
    public Flux< TupleOfPatrul > getCurrentTupleOfPatrul ( String id ) { return CassandraDataControl.getInstance()
            .getAllTupleOfPatrul( id ); }

    @MessageMapping ( value = "deleteTupleOfPatrul" )
    public Mono< ApiResponseModel > deleteTupleOfPatrul ( String id ) { return CassandraDataControl.getInstance().deleteTupleOfPatrul( id ); }
}
