package com.ssd.mvd.gpstabletsservice.tuple;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.database.Archive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Slf4j
@RestController
public class EscortController {
    @MessageMapping ( value = "getAllEscort" )
    public Flux< EscortTuple > getAllTupleOfPatrul () { return CassandraDataControlForEscort
            .getInstance()
            .getGetAllTupleOfEscort()
            .get(); }

    @MessageMapping ( value = "getTupleTotalData" )
    public Mono< TupleTotalData > getTupleTotalData ( String uuid ) { return CassandraDataControlForEscort
            .getInstance()
            .getGetTupleTotalData()
            .apply( uuid ); }

    @MessageMapping ( value = "getCurrentEscort" )
    public Mono< EscortTuple > getCurrentTupleOfPatrul ( String id ) { return CassandraDataControlForEscort
            .getInstance()
            .getGetCurrentTupleOfEscort()
            .apply( id ); }

    @MessageMapping ( value = "deleteEscort" )
    public Mono< ApiResponseModel > deleteTupleOfPatrul ( String id ) { return CassandraDataControlForEscort
            .getInstance()
            .getDeleteTupleOfEscort()
            .apply( id )
            .onErrorContinue( ( throwable, o ) -> log.error(
                    "Error: " + throwable.getMessage()
                            + " Reason: " + o ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "removeEscortCarFromEscort" )
    public Mono< ApiResponseModel > removeEscortCarFromEscort ( UUID uuid ) { return CassandraDataControlForEscort
            .getInstance()
            .getGetCurrentTupleOfCar()
            .apply( uuid )
            .flatMap( tupleOfCar -> {
                CassandraDataControlForEscort
                        .getInstance()
                        .getGetCurrentTupleOfEscort()
                        .apply( tupleOfCar.getUuidOfEscort().toString() )
                        .subscribe( escortTuple -> {
                            escortTuple.getTupleOfCarsList().remove( tupleOfCar.getUuid() );
                            escortTuple.getPatrulList().remove( tupleOfCar.getUuidOfPatrul() );
                            CassandraDataControlForEscort
                                    .getInstance()
                                    .getUpdateEscortTuple()
                                    .apply( escortTuple )
                                    .subscribe(); } );
                CassandraDataControl
                        .getInstance()
                        .getGetPatrulByUUID()
                        .apply( tupleOfCar.getUuidOfPatrul() )
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
                        .getUpdateTupleOfcar()
                        .apply( tupleOfCar )
                        .onErrorContinue( ( throwable, o ) -> log.error(
                                "Error: " + throwable.getMessage()
                                        + " Reason: " + o ) )
                        .onErrorReturn( Archive
                                .getArchive()
                                .getErrorResponse()
                                .get() ); } ); }

    @MessageMapping ( value = "addNewEscort" )
    public Flux< ApiResponseModel > addNewTupleOfPatrul ( EscortTuple escortTuple ) { return CassandraDataControlForEscort
            .getInstance()
            .getSaveEscortTuple()
            .apply( escortTuple )
            .onErrorContinue( ( throwable, o ) -> log.error(
                    "Error: " + throwable.getMessage()
                            + " Reason: " + o ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "updateEscort" )
    public Mono< ApiResponseModel > updateTupleOfPatrul ( EscortTuple escortTuple ) { return CassandraDataControlForEscort
            .getInstance()
            .getUpdateEscortTuple()
            .apply( escortTuple )
            .onErrorContinue( ( throwable, o ) -> log.error(
                    "Error: " + throwable.getMessage()
                            + " Reason: " + o ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }
}