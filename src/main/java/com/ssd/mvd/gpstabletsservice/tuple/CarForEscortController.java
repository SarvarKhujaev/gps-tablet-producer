//package com.ssd.mvd.gpstabletsservice.tuple;
//
//import org.springframework.messaging.handler.annotation.MessageMapping;
//import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
//import org.springframework.web.bind.annotation.RestController;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//@RestController
//public class CarForEscortController {
//    @MessageMapping( value = "addNewCarForEscort" )
//    public Mono< ApiResponseModel > addNewCarForEscort ( TupleOfCar tupleOfCar ) { return CassandraDataControlForEscort
//            .getInstance().addValue( tupleOfCar ); }
//
//    @MessageMapping ( value = "getAllCarsForEscort" )
//    public Flux< TupleOfCar > getAllCarsForEscort() { return CassandraDataControlForEscort.getInstance().getAllTupleOfCar(); }
//
//    @MessageMapping ( value = "getAllCarsForEscort" )
//    public Flux< TupleOfCar > getAllCarsForEscort( String gosNumber ) { return CassandraDataControlForEscort.getInstance()
//            .getAllTupleOfCar( gosNumber ); }
//
//    @MessageMapping ( value = "deleteCarForEscort" )
//    public Mono< ApiResponseModel > deleteCarForEscort( String gosNumber ) { return CassandraDataControlForEscort
//            .getInstance().deleteCar( gosNumber ); }
//}
