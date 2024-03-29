package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.request.AndroidVersionUpdate;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import reactor.core.publisher.Mono;

@RestController
public final class VersionController extends LogInspector {
    @MessageMapping ( value = "saveLastVersion" )
    public Mono< ApiResponseModel > saveLastVersion ( final AndroidVersionUpdate androidVersionUpdate ) {
        return super.checkObject( androidVersionUpdate )
                ? CassandraDataControl
                .getInstance()
                .saveLastVersion
                .apply( androidVersionUpdate )
                : super.errorResponse( "Wrong Params" );
    }

    @MessageMapping ( value = "checkVersionForAndroid" )
    public Mono< ApiResponseModel > checkVersionForAndroid ( final String version ) {
        return super.objectIsNotNull( version )
                ? CassandraDataControl
                .getInstance()
                .checkVersionForAndroid
                .apply( version )
                : super.errorResponse( "Wrong Params" );
    }

    @MessageMapping ( value = "getLastAndroidVersion" )
    public Mono< ApiResponseModel > getLastAndroidVersio () {
        return CassandraDataControl
                .getInstance()
                .getLastVersion
                .get();
    }
}