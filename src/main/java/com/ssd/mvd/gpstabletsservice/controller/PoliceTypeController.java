package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.PoliceType;
import com.ssd.mvd.gpstabletsservice.database.Archive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class PoliceTypeController {
    @MessageMapping ( value = "getPoliceTypeList" )
    public Flux< PoliceType > getPoliceTypeList () { return CassandraDataControl
            .getInstance()
            .getGetAllPoliceTypes()
            .get()
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping( value = "addPoliceType" )
    public Mono< ApiResponseModel > addPoliceType ( PoliceType policeType ) { return CassandraDataControl
            .getInstance()
            .addValue( policeType )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "updatePoliceType" )
    public Mono< ApiResponseModel > updatePoliceType ( PoliceType policeType ) { return CassandraDataControl
            .getInstance()
            .update( policeType )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "deletePoliceType" )
    public Mono< ApiResponseModel > deletePoliceType ( PoliceType policeType ) { return CassandraDataControl
            .getInstance()
            .delete( CassandraTables.POLICE_TYPE.name(),
                    "uuid",
                    policeType.getUuid().toString() )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }
}
