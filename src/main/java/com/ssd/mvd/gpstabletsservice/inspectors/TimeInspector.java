package com.ssd.mvd.gpstabletsservice.inspectors;

import java.util.Date;
import java.time.Instant;
import java.time.Duration;
import java.util.function.*;
import java.text.SimpleDateFormat;

@lombok.Data
public class TimeInspector {
    private Date date; // for comparing with current time
    private Long timestamp = 30L; // time interval of how much time has to be matched to set User like offline 30 mins by default
    private Long timestampForArchive = 15L;

    private Integer endTimeForEvening = 24;
    private Integer endTimeForMorning = 16;
    private Integer startTimeForEvening = 16;
    private Integer startTimeForMorning = 0;

    private final static TimeInspector inspector = new TimeInspector();

    public static TimeInspector getInspector () { return inspector; }

    private Date setDate () { return ( this.date = new Date() ); }

    private final Supplier< Date > getNewDate = Date::new;

    private final Predicate< Instant > checkDate = instant -> this.getEndTimeForEvening() >= this.setDate().getHours()
            && this.getDate().getHours() >= this.getStartTimeForMorning()
            ? ( this.getGetTimeDifference().apply( instant, 2 ) <= 10 )
            : ( this.getGetTimeDifference().apply( instant, 2 ) <= 7 );

    // for checking current time of task ending
    private final Function< String, Long > convertTimeToLong = time -> {
            try { return time != null && !time.contains( "null" )
                    ? new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" )
                    .parse( time )
                    .getTime()
                    : 0L; }
            catch ( final Exception e ) { return 0L; } };

    private final BiFunction< Instant, Integer, Long > getTimeDifference = ( instant, integer ) -> switch ( integer ) {
            case 1 -> Math.abs( Duration.between( Instant.now(), instant ).toHours() );
            case 2 -> Math.abs( Duration.between( Instant.now(), instant ).toMinutes() );
            default -> Math.abs( Duration.between( Instant.now(), instant ).toSeconds() ); };
}
