package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.tuple.TupleTotalData;
import com.ssd.mvd.gpstabletsservice.tuple.EscortTuple;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
public final class EscortController extends LogInspector {
    @MessageMapping ( value = "getAllEscort" )
    public Flux< EscortTuple > getAllTupleOfPatrul () {
        return CassandraDataControl
            .getInstance()
            .getAllEntities
            .apply( CassandraTables.ESCORT, CassandraTables.TUPLE_OF_ESCORT )
            .map( EscortTuple::new )
            .sequential()
            .publishOn( Schedulers.single() );
    }

    @MessageMapping ( value = "getTupleTotalData" )
    public Mono< TupleTotalData > getTupleTotalData ( final String uuid ) {
        return CassandraDataControlForEscort
            .getInstance()
            .getGetTupleTotalData()
            .apply( uuid, new TupleTotalData() );
    }

    @MessageMapping ( value = "getCurrentEscort" )
    public Mono< EscortTuple > getCurrentTupleOfPatrul ( final String id ) {
        return CassandraDataControlForEscort
            .getInstance()
            .getGetCurrentTupleOfEscort()
            .apply( id );
    }

    @MessageMapping ( value = "deleteEscort" )
    public Mono< ApiResponseModel > deleteEscort ( final String id ) {
        return CassandraDataControlForEscort
            .getInstance()
            .getDeleteTupleOfEscort()
            .apply( id )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "removeEscortCarFromEscort" )
    public Mono< ApiResponseModel > removeEscortCarFromEscort ( final UUID uuid ) {
        return CassandraDataControlForEscort
            .getInstance()
            .getGetCurrentTupleOfCar()
            .apply( uuid )
            .flatMap( tupleOfCar -> CassandraDataControlForEscort
                    .getInstance()
                    .getUpdateEscortAfterDeleting()
                    .apply( tupleOfCar ) );
    }

    @MessageMapping ( value = "addNewEscort" )
    public Flux< ApiResponseModel > addNewEscort ( final EscortTuple escortTuple ) {
        return CassandraDataControlForEscort
            .getInstance()
            .getSaveEscortTuple()
            .apply( escortTuple )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "updateEscort" )
    public Mono< ApiResponseModel > updateTupleOfPatrul ( final EscortTuple escortTuple ) {
        return CassandraDataControlForEscort
            .getInstance()
            .getUpdateEscortTuple()
            .apply( escortTuple )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }
}