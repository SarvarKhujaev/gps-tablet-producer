package com.ssd.mvd.gpstabletsservice.entity;

import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import lombok.Data;

@Data
public class TimeInspector {
    private Date date; // for comparing with current time
    private Long timestamp = 30L; // time interval of how much time has to be matched to set User like offline 30 mins by default
    private Long timestampForArchive = 15L;

    private Integer endTimeForEvening = 24;
    private Integer endTimeForMorning = 16;
    private Integer startTimeForEvening = 16;
    private Integer startTimeForMorning = 0;

    private static TimeInspector inspector = new TimeInspector();

    public static TimeInspector getInspector () { return inspector != null ? inspector : ( inspector = new TimeInspector() ); }

    private Date setDate () { return ( this.date = new Date() ); }

    // for checking current time of task ending
    public Boolean checkDate ( Instant instant ) { return TimeInspector.getInspector().getEndTimeForEvening() >= this.setDate().getHours()
            && this.date.getHours() >= TimeInspector.getInspector().getStartTimeForMorning() ?
            ( this.getTimeDifference( instant ) <= 10 ) : ( this.getTimeDifference( instant ) <= 7 ); }

    public Long convertTimeToLong ( String time ) {
        try { return time != null && !time.contains( "null" ) ?
                new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" )
                .parse( time )
                .getTime() : 0L;
        } catch ( Exception e ) { return 0L; } }

    public Long getTimeDifference ( Instant instant ) { return Duration.between( Instant.now(), instant ).toMinutes(); } // for comparing time difference between instance and current time

    public Long getTimeDifferenceInSEconds ( Instant instant ) { return Duration.between( Instant.now(), instant ).toSeconds(); } // for comparing time difference between instance and current time
}
