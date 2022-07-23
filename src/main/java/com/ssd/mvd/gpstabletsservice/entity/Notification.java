package com.ssd.mvd.gpstabletsservice.entity;

import com.ssd.mvd.gpstabletsservice.task.card.Address;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Data
@Builder
public class Notification {
    private String type; // might be from 102 or Camera
    private String title; // description of Patrul action
    private String passportSeries;

    private Address address;
    private Double latitudeOfTask;
    private Double longitudeOfTask;

    private Long id; // id of any task
    private Date notificationWasCreated; // the date when this current notification was created
}
