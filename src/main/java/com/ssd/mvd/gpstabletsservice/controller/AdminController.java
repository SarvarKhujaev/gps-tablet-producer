package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.entity.TimeInspector;
import java.util.Map;

@RestController
public class AdminController extends LogInspector {
    @MessageMapping( value = "setTime" ) // setting time of checking all tablets
    public Mono< ApiResponseModel > setTime ( Long time ) {
        TimeInspector.getInspector().setTimestamp( time );
        return super.getFunction().apply( Map.of(
                        "message", "Time for timer was established as: " + time,
                        "success", true,
                        "code", 200 ) ); }

    @MessageMapping ( value = "setTimeForEveningTime" )
    public Mono< ApiResponseModel > setTime ( Integer start, Integer end ) {
        TimeInspector.getInspector().setEndTimeForEvening( end );
        TimeInspector.getInspector().setStartTimeForEvening( start );
        return super.getFunction().apply( Map.of(
                        "message", "Time for evening was established",
                        "success", true,
                        "code", 200 ) ); }

    @MessageMapping ( value = "setTimeForMorningTime" )
    public Mono< ApiResponseModel > setTimeForMorning ( Integer start, Integer end ) {
        TimeInspector.getInspector().setStartTimeForMorning( start );
        TimeInspector.getInspector().setEndTimeForMorning( end );
        return super.getFunction().apply( Map.of(
                        "message", "Time for morning was established",
                        "success", true,
                        "code", 200 ) ); }
}
