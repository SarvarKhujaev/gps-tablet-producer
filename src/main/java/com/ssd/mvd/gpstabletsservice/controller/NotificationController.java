package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ssd.mvd.gpstabletsservice.entity.notifications.Notification;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.UUID;

@RestController
public final class NotificationController extends LogInspector {
    @MessageMapping ( value = "getAllNotifications" )
    public Flux< Notification > getAllNotifications () { return CassandraDataControl
            .getInstance()
            .getGetAllEntities()
            .apply( CassandraTables.TABLETS, CassandraTables.NOTIFICATION )
            .map( Notification::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .sort( Comparator.comparing( Notification::getNotificationWasCreated ).reversed() )
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "getUnreadNotifications" )
    public Flux< Notification > getUnreadNotifications () { return CassandraDataControl
            .getInstance()
            .getGetUnreadNotifications()
            .get()
            .sort( Comparator.comparing( Notification::getNotificationWasCreated ).reversed() )
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "setAsRead" )
    public Mono< ApiResponseModel > setAsRead ( final String id ) { return CassandraDataControl
            .getInstance()
            .getSetNotificationAsRead()
            .apply( UUID.fromString( id ) )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "getUnreadNotificationQuantity" )
    public Mono< Long > getUnreadNotificationQuantity () { return CassandraDataControl
            .getInstance()
            .getGetUnreadNotificationQuantity()
            .get()
            .onErrorContinue( super::logging )
            .onErrorReturn( -1L ); }
}
