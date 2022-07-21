package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public final class TabletsController {

//    private static Double distance;
//    private static final Double p = PI / 180;
//    private static ReqLocationExchange second;
//
//    @MessageMapping( value = "fuelConsumption" ) // it has to return the average fuel consumption with the full distance and the list of all lan and lats
//    public Mono< PatrulInfo > fuelConsumption ( Request request ) { // computes the average fuel consumption of current user
//        PatrulInfo patrulInfo = new PatrulInfo( this.history( request ) );
//        return Mono.from( patrulInfo.getTransactions().map( this::calculate ) ).flatMap( value -> RedisDataControl.getRedis().getPatrul( (String) request.getSubject() ).flatMap( patrul -> {
//            RedisDataControl.getRedis().getCar( patrul.getCarNumber() ).subscribe( reqCar -> {
//                reqCar.saveAverageFuelConsumption( patrulInfo.setDistance( distance ) );
//                RedisDataControl.getRedis().update( reqCar ).subscribe(); } );
//            patrulInfo.setNSF( patrul.getSurnameNameFatherName() );
//            return Mono.just( patrulInfo ); } ) ); }

    @MessageMapping ( value = "averageFuelConsumption" ) // returns the average fuel consumption for current car without computing. this value is constant
    public Mono< Double > averageFuelConsumption ( String gosno ) { return RedisDataControl.getRedis().getCar( gosno ).flatMap( reqCar -> Mono.just( reqCar.getAverageFuelSize() ) ); }
}
