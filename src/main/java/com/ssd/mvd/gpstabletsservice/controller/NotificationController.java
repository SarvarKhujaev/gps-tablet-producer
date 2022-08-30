package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.Notification;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.UUID;

@RestController
public class NotificationController {
    @MessageMapping ( value = "getAllNotifications" )
    public Flux< Notification > getAllNotifications () { return CassandraDataControl
            .getInstance()
            .getAllNotification()
            .sort( Comparator.comparing( Notification::getNotificationWasCreated ) ); }

    @MessageMapping ( value = "getUnreadNotifications" )
    public Flux< Notification > getUnreadNotifications () { return CassandraDataControl
            .getInstance()
            .getAllNotification()
            .filter( notification -> !notification.getWasRead() )
            .sort( Comparator.comparing( Notification::getNotificationWasCreated ) ); }

    @MessageMapping ( value = "setAsRead" )
    public Mono< ApiResponseModel > setAsRead ( String id ) { return CassandraDataControl
            .getInstance()
            .setNotificationAsRead( UUID.fromString( id ) ); }
}
