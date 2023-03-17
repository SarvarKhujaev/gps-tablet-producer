package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.tuple.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.entity.Country;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class CountryController extends LogInspector {
    @MessageMapping ( value = "getAllCountries" )
    public Flux< Country > getAllCountries () { return CassandraDataControlForEscort
            .getInstance()
            .getGetAllCountries()
            .get()
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "getCurrentCountry" )
    public Mono< Country > getCurrentCountry ( String countryName ) { return CassandraDataControlForEscort
            .getInstance()
            .getGetCurrentCountry()
            .apply( countryName )
            .onErrorContinue( super::logging ); }

    @MessageMapping( value = "addNewCountry" )
    public Mono< ApiResponseModel > addNewCountry (Country country ) { return CassandraDataControlForEscort
            .getInstance()
            .getSaveNewCountry()
            .apply( country )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "updateCountry" )
    public Mono< ApiResponseModel > updateCountry ( Country country ) { return CassandraDataControlForEscort
            .getInstance()
            .getUpdate()
            .apply( country )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "deleteCountry" )
    public Mono< ApiResponseModel > deleteCountry ( String countryName ) { return CassandraDataControlForEscort
            .getInstance()
            .getDeleteCountry()
            .apply( countryName )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }
}
