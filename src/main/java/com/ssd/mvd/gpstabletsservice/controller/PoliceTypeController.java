package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import com.ssd.mvd.gpstabletsservice.entity.PoliceType;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class PoliceTypeController {
    @MessageMapping ( value = "getPoliceTypeList" )
    public Flux< PoliceType > getPoliceTypeList () { return RedisDataControl.getRedis().getAllPoliceTypes(); }

    @MessageMapping ( value = "updatePoliceType" )
    public Mono< ApiResponseModel > updatePoliceType ( PoliceType policeType ) { return RedisDataControl.getRedis().updatePoliceTypes( policeType ); }

    @MessageMapping( value = "addPoliceType" )
    public Mono< ApiResponseModel > addPoliceType ( PoliceType policeType ) { return RedisDataControl.getRedis().addValue( policeType ); }

    @MessageMapping ( value = "deletePoliceType" )
    public Mono< ApiResponseModel > deletePoliceType ( PoliceType policeType ) { return RedisDataControl.getRedis().deletePoliceType( policeType ); }
}
