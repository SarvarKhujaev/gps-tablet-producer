package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.request.AndroidVersionUpdate;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import reactor.core.publisher.Mono;

@RestController
public class VersionController extends LogInspector {
    @MessageMapping ( value = "saveLastVersion" )
    public Mono< ApiResponseModel > saveLastVersion ( AndroidVersionUpdate androidVersionUpdate ) {
        return CassandraDataControl
                .getInstance()
                .getSaveLastVersion()
                .apply( androidVersionUpdate ); }

    @MessageMapping ( value = "checkVersionForAndroid" )
    public Mono< ApiResponseModel > checkVersionForAndroid ( String version ) {
        return CassandraDataControl
                .getInstance()
                .getCheckVersionForAndroid()
                .apply( version ); }

    @MessageMapping ( value = "getLastAndroidVersion" )
    public Mono< ApiResponseModel > getLastAndroidVersio () {
        return CassandraDataControl
                .getInstance()
                .getGetLastVersion()
                .get(); }
}