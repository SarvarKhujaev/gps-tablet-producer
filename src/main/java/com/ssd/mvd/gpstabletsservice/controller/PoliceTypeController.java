package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.entity.PoliceType;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public final class PoliceTypeController extends LogInspector {
    @MessageMapping ( value = "getPoliceTypeList" )
    public Flux< PoliceType > getPoliceTypeList () {
        return CassandraDataControl
            .getInstance()
            .getAllEntities
            .apply( CassandraTables.TABLETS, CassandraTables.POLICE_TYPE )
            .map( PoliceType::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging );
    }

    @MessageMapping( value = "addPoliceType" )
    public Mono< ApiResponseModel > addPoliceType ( final PoliceType policeType ) {
        return CassandraDataControl
            .getInstance()
            .savePoliceType
            .apply( policeType )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "updatePoliceType" )
    public Mono< ApiResponseModel > updatePoliceType ( final PoliceType policeType ) {
        return CassandraDataControl
            .getInstance()
            .updatePoliceType
            .apply( policeType )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "deletePoliceType" )
    public Mono< ApiResponseModel > deletePoliceType ( final PoliceType policeType ) {
        return CassandraDataControl
            .getInstance()
            .delete( CassandraTables.POLICE_TYPE.name(),
                    "uuid",
                    policeType.getUuid().toString() )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }
}
