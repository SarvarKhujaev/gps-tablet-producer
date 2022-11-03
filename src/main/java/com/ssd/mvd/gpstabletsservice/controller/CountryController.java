package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.tuple.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.entity.Country;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
public class CountryController {
    @MessageMapping( value = "addNewCountry" )
    public Mono< ApiResponseModel > addNewCountry (Country country ) { return CassandraDataControlForEscort
            .getInstance()
            .getSaveNewCountry()
            .apply( country )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "updateCountry" )
    public Mono< ApiResponseModel > updateCountry ( Country country ) { return CassandraDataControlForEscort
            .getInstance()
            .getUpdate()
            .apply( country )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "getAllCountries" )
    public Flux< Country > getAllCountries () { return CassandraDataControlForEscort
            .getInstance()
            .getGetAllCountries()
            .get()
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "getCurrentCountry" )
    public Mono< Country > getCurrentCountry ( String countryName ) { return CassandraDataControlForEscort
            .getInstance()
            .getGetCurrentCountry()
            .apply( countryName )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "deleteCountry" )
    public Mono< ApiResponseModel > deleteCountry ( String countryName ) { return CassandraDataControlForEscort
            .getInstance()
            .getDeleteCountry()
            .apply( countryName )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }
}
