package com.ssd.mvd.gpstabletsservice.entity;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Notification< T > {
    private String title; // description of Patrul action
    private Integer index; // unique index number
    private Patrul patrul;
    private Boolean status; // shows this notification was read or not by default not read
    private Date notificationWasCreated; // the date when this current notification was created

    private T object;
}
