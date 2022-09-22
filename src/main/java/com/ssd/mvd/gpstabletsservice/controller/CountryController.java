package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.tuple.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.Country;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class CountryController {
    @MessageMapping( value = "addNewCountry" )
    public Mono< ApiResponseModel > addNewCountry (Country country ) { return CassandraDataControlForEscort
            .getInstance()
            .addValue( country ); }

    @MessageMapping ( value = "updateCountry" )
    public Mono< ApiResponseModel > updateCountry ( Country country ) { return CassandraDataControlForEscort
            .getInstance()
            .update( country ); }

    @MessageMapping ( value = "getAllCountries" )
    public Flux< Country > getAllCountries () { return CassandraDataControlForEscort
            .getInstance()
            .getAllCountries(); }

    @MessageMapping ( value = "getCurrentCountry" )
    public Mono< Country > getCurrentCountry ( String countryName ) { return CassandraDataControlForEscort
            .getInstance()
            .getAllCountries( countryName ); }

    @MessageMapping ( value = "deleteCountry" )
    public Mono< ApiResponseModel > deleteCountry ( String countryName ) { return CassandraDataControlForEscort
            .getInstance()
            .deleteCountry( countryName ); }
}
