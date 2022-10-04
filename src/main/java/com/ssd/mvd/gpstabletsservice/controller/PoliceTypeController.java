package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.PoliceType;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class PoliceTypeController {
    @MessageMapping ( value = "getPoliceTypeList" )
    public Flux< PoliceType > getPoliceTypeList () { return CassandraDataControl
            .getInstance()
            .getAllPoliceTypes(); }

    @MessageMapping( value = "addPoliceType" )
    public Mono< ApiResponseModel > addPoliceType ( PoliceType policeType ) { return CassandraDataControl
            .getInstance()
            .addValue( policeType ); }

    @MessageMapping ( value = "updatePoliceType" )
    public Mono< ApiResponseModel > updatePoliceType ( PoliceType policeType ) { return CassandraDataControl
            .getInstance()
            .update( policeType ); }

    @MessageMapping ( value = "deletePoliceType" )
    public Mono< ApiResponseModel > deletePoliceType ( PoliceType policeType ) { return CassandraDataControl
            .getInstance()
            .delete( CassandraTables.POLICE_TYPE.name(),
                    "uuid",
                    policeType.getUuid().toString() ); }
}
