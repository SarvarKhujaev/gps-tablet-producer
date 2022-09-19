package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.TimeInspector;
import com.ssd.mvd.gpstabletsservice.response.Status;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class AdminController {
    @MessageMapping( value = "setTime" ) // setting time of checking all tablets
    public Mono< ApiResponseModel > setTime ( Long time ) {
        TimeInspector.getInspector().setTimestamp( time );
        return Mono.just( ApiResponseModel
                .builder()
                .status( Status
                        .builder()
                        .message( "Time for timer was established as: " + time )
                        .code( 200 )
                        .build() )
                .build() ); }

    @MessageMapping ( value = "setTimeForEveningTime" )
    public Mono< ApiResponseModel > setTime ( Integer start, Integer end ) {
        TimeInspector.getInspector().setEndTimeForEvening( end );
        TimeInspector.getInspector().setStartTimeForEvening( start );
        return Mono.just( ApiResponseModel
                .builder()
                .status( Status
                        .builder()
                        .message( "Time for evening was established" )
                        .code( 200 )
                        .build() )
                .build() ); }

    @MessageMapping ( value = "setTimeForMorningTime" )
    public Mono< ApiResponseModel > setTimeForMorning ( Integer start, Integer end ) {
        TimeInspector.getInspector().setEndTimeForMorning( end );
        TimeInspector.getInspector().setStartTimeForMorning( start );
        return Mono.just( ApiResponseModel
                .builder()
                .status( Status
                        .builder()
                        .message( "Time for morning was established" )
                        .code( 200 )
                        .build() )
                .build() ); }
}
