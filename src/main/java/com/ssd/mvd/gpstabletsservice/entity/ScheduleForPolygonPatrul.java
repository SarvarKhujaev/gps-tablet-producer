package com.ssd.mvd.gpstabletsservice.entity;

import lombok.Data;

import java.util.List;

@Data
public class ScheduleForPolygonPatrul {
    private List< String > passportSeries;
    private String uuid;
    private String from; //  время с которого начинается дежурство
    private String to;
}