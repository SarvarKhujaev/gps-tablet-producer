package com.ssd.mvd.gpstabletsservice.tuple;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
public class EscortController {
    @MessageMapping ( value = "addNewEscort" )
    public Flux< ApiResponseModel > addNewTupleOfPatrul ( EscortTuple escortTuple ) { return CassandraDataControlForEscort
            .getInstance()
            .addValue( escortTuple ); }

    @MessageMapping ( value = "getAllEscort" )
    public Flux< EscortTuple > getAllTupleOfPatrul () { return CassandraDataControlForEscort
            .getInstance()
            .getAllTupleOfEscort(); }

    @MessageMapping ( value = "getCurrentEscort" )
    public Mono< EscortTuple > getCurrentTupleOfPatrul ( String id ) { return CassandraDataControlForEscort
            .getInstance()
            .getAllTupleOfEscort( id ); }

    @MessageMapping ( value = "deleteEscort" )
    public Mono< ApiResponseModel > deleteTupleOfPatrul ( String id ) { return CassandraDataControlForEscort
            .getInstance()
            .deleteTupleOfPatrul( id ); }

    @MessageMapping ( value = "updateEscort" )
    public Mono< ApiResponseModel > updateTupleOfPatrul ( EscortTuple escortTuple ) { return CassandraDataControlForEscort
            .getInstance()
            .update( escortTuple ); }

    @MessageMapping ( value = "getTupleTotalData" )
    public Mono< TupleTotalData > getTupleTotalData ( String uuid ) { return CassandraDataControlForEscort
            .getInstance()
            .getTupleTotalData( uuid ); }

    @MessageMapping ( value = "removeEscortCarFromEscort" )
    public Mono< ApiResponseModel > removeEscortCarFromEscort ( UUID uuid ) { return CassandraDataControlForEscort
            .getInstance()
            .getAllTupleOfCar( uuid )
            .flatMap( tupleOfCar -> {
                CassandraDataControlForEscort
                        .getInstance()
                                .getAllTupleOfEscort( tupleOfCar.getUuidOfEscort().toString() )
                                        .subscribe( escortTuple -> {
                                            escortTuple.getTupleOfCarsList().remove( tupleOfCar.getUuid() );
                                            escortTuple.getPatrulList().remove( tupleOfCar.getUuidOfPatrul() );
                                            CassandraDataControlForEscort
                                                    .getInstance()
                                                    .update( escortTuple )
                                                    .subscribe(); } );
                CassandraDataControl
                        .getInstance()
                                .getPatrul( tupleOfCar.getUuidOfPatrul() )
                                        .subscribe( patrul -> {
                                            patrul.setUuidOfEscort( null );
                                            patrul.setUuidForEscortCar( null );
                                            CassandraDataControl
                                                    .getInstance()
                                                    .update( patrul )
                                                    .subscribe(); } );
                tupleOfCar.setUuidOfEscort( null );
                tupleOfCar.setUuidOfPatrul( null );
                return CassandraDataControlForEscort
                        .getInstance()
                        .update( tupleOfCar ); } ); }
}
