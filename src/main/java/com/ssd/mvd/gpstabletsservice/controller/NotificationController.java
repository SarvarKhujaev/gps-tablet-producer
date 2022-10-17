package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.extern.slf4j.Slf4j;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.Notification;
import com.ssd.mvd.gpstabletsservice.database.Archive;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.UUID;

@Slf4j
@RestController
public class NotificationController {
    @MessageMapping ( value = "getAllNotifications" )
    public Flux< Notification > getAllNotifications () { return CassandraDataControl
            .getInstance()
            .getGetAllNotification()
            .get()
            .sort( Comparator.comparing( Notification::getNotificationWasCreated ) )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "getUnreadNotifications" )
    public Flux< Notification > getUnreadNotifications () { return CassandraDataControl
            .getInstance()
            .getGetAllNotification()
            .get()
            .filter( notification -> !notification.getWasRead() )
            .sort( Comparator.comparing( Notification::getNotificationWasCreated ) )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "setAsRead" )
    public Mono< ApiResponseModel > setAsRead ( String id ) { return CassandraDataControl
            .getInstance()
            .setNotificationAsRead( UUID.fromString( id ) )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }
}
