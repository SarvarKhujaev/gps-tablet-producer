package com.ssd.mvd.gpstabletsservice.entity;

import java.util.Date;
import java.time.Instant;
import java.time.Duration;
import java.text.SimpleDateFormat;

import lombok.Data;
import java.util.function.Function;
import java.util.function.Predicate;

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

    private final Predicate< Instant > checkDate = instant -> this.getEndTimeForEvening() >= this.setDate().getHours()
            && this.getDate().getHours() >= this.getStartTimeForMorning() ?
            ( this.getGetTimeDifference()
                    .apply( instant ) <= 10 ) : ( this.getGetTimeDifference().apply( instant ) <= 7 );

    // for checking current time of task ending
    private final Function< String, Long > convertTimeToLong = time -> {
        try { return time != null && !time.contains( "null" ) ?
                new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" )
                        .parse( time )
                        .getTime() : 0L;
        } catch ( Exception e ) { return 0L; } };

    private final Function< Instant, Long > getTimeDifference = instant -> Math.abs( Duration.between( Instant.now(), instant ).toMinutes() );

    private final Function< Instant, Long > getTimeDifferenceInHours = instant -> Math.abs( Duration.between( Instant.now(), instant ).toHours() );

    private final Function< Instant, Long > getTimeDifferenceInSeconds = instant -> Math.abs( Duration.between( Instant.now(), instant ).toSeconds() );
}
