package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.entity.ReqCar;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

@RestController
public final class CarController extends LogInspector {
    @MessageMapping( value = "carList" ) // the list of all cars
    public Flux< ReqCar > getAllCars () {
        return CassandraDataControl
            .getInstance()
            .getAllEntities
            .apply( CassandraTables.TABLETS, CassandraTables.CARS )
            .map( ReqCar::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging );
    }

    @MessageMapping ( value = "getCurrentCar" )
    public Mono< ReqCar > getCurrentCar ( final String gosno ) {
        return CassandraDataControl
            .getInstance()
            .getCarByUUID
            .apply( UUID.fromString( gosno ) );
    }

    @MessageMapping( value = "searchByGosnoCar" )
    public Flux< ReqCar > searchByGosno ( final String gosno ) {
        return CassandraDataControl
            .getInstance()
            .getAllEntities
            .apply( CassandraTables.TABLETS, CassandraTables.CARS )
            .filter( row -> row.getString( "gosNumber" ).equals( gosno ) )
            .map( ReqCar::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging );
    }

    @MessageMapping( value = "addCar" )
    public Mono< ApiResponseModel > addCar ( final ReqCar reqCar ) {
        return CassandraDataControl
            .getInstance()
            .saveCar
            .apply( reqCar )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping( value = "deleteCar" )
    public Mono< ApiResponseModel > deleteCar ( final String gosno ) {
        return CassandraDataControl
                .getInstance()
                .deleteCar
                .apply( gosno )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "updateCar" )
    public Mono< ApiResponseModel > updateCar ( final ReqCar reqCar ) {
        return CassandraDataControl
            .getInstance()
            .updateCar
            .apply( reqCar )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }
}
