package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.request.AndroidVersionUpdate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;

import reactor.core.publisher.Mono;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class VersionController {
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
}