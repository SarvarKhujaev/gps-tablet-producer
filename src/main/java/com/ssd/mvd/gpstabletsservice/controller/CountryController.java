package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.entity.Country;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class CountryController extends LogInspector {
    @MessageMapping ( value = "getAllCountries" )
    public Flux< Country > getAllCountries () { return CassandraDataControl
            .getInstance()
            .getGetAllEntities()
            .apply( CassandraTables.ESCORT, CassandraTables.COUNTRIES )
            .map( Country::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "getCurrentCountry" )
    public Mono< Country > getCurrentCountry ( final String countryName ) { return CassandraDataControlForEscort
            .getInstance()
            .getGetCurrentCountry()
            .apply( countryName )
            .onErrorContinue( super::logging ); }

    @MessageMapping( value = "addNewCountry" )
    public Mono< ApiResponseModel > addNewCountry ( final Country country ) { return CassandraDataControlForEscort
            .getInstance()
            .getSaveNewCountry()
            .apply( country )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "updateCountry" )
    public Mono< ApiResponseModel > updateCountry ( final Country country ) { return CassandraDataControlForEscort
            .getInstance()
            .getUpdate()
            .apply( country )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "deleteCountry" )
    public Mono< ApiResponseModel > deleteCountry ( final String countryName ) { return CassandraDataControlForEscort
            .getInstance()
            .getDeleteCountry()
            .apply( countryName )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }
}
