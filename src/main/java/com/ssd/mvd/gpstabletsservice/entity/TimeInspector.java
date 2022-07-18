package com.ssd.mvd.gpstabletsservice.entity;

import lombok.Data;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Data
public class TimeInspector {
    private Date date; // for comparing with current time
    private Long timestamp = 30L; // time interval of how much time has to be matched to set User like offline 30 mins by default
    private Long timestampForArchive = 60L;

    private Integer endTimeForEvening;
    private Integer endTimeForMorning;
    private Integer startTimeForEvening;
    private Integer startTimeForMorning;

    private static TimeInspector inspector = new TimeInspector();

    private Date setDate () { return ( this.date = new Date() ); }

    public Long getTimeDifference ( Instant instant ) { return Duration.between( Instant.now(), instant ).toMinutes(); } // for comparing time difference between instance and current time

    public static TimeInspector getInspector () { return inspector != null ? inspector : ( inspector = new TimeInspector() ); }

    // for checking current time of task ending
    public Boolean checkDate ( Instant instant ) { return TimeInspector.getInspector().getEndTimeForEvening() >= this.setDate().getHours() && this.date.getHours() >= TimeInspector.getInspector().getStartTimeForMorning() ?
                ( this.getTimeDifference( instant ) <= 10 ) : ( this.getTimeDifference( instant ) <= 7 ); }

    public boolean compareTime ( Instant instant ) { return Math.abs( Duration.between( Instant.now(), instant ).getSeconds() ) < this.getTimestamp(); }
}
