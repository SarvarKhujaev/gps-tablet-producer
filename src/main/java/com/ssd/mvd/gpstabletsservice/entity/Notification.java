package com.ssd.mvd.gpstabletsservice.entity;

import java.util.Date;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Notification {
    private String type; // might be from 102 or Camera
    private String title; // description of Patrul action
    private String address;
    private String passportSeries;

    private Double latitudeOfTask;
    private Double longitudeOfTask;

    private Long id; // id of any task
    private Date notificationWasCreated; // the date when this current notification was created
}
