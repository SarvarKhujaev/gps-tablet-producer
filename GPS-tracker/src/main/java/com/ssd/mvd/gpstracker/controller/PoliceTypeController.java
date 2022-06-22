package com.ssd.mvd.gpstracker.controller;

import com.ssd.mvd.gpstracker.database.RedisDataControl;
import com.ssd.mvd.gpstracker.entity.PoliceType;
import com.ssd.mvd.gpstracker.response.ApiResponseModel;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class PoliceTypeController {
    @MessageMapping ( value = "getPoliceTypeList" )
    public Flux< PoliceType > getPoliceTypeList () { return RedisDataControl.getRedis().getAllPoliceTypes(); }

    @MessageMapping( value = "addPoliceType" )
    public Mono< ApiResponseModel > addPoliceType ( PoliceType policeType ) { return RedisDataControl.getRedis().addValue( policeType ); }

    @MessageMapping ( value = "deletePoliceType" )
    public Mono< ApiResponseModel > deletePoliceType ( String policeType ) { return RedisDataControl.getRedis().deletePoliceType( policeType ); }
}
