package com.ssd.mvd.gpstabletsservice.inspectors;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.*;
import java.util.function.*;

import java.time.*;
import java.time.format.DateTimeFormatter;

@lombok.Data
public final class TimeInspector {
    private Date date; // for comparing with current time
    private final Calendar calendar = Calendar.getInstance();

    private Long timestamp = 30L; // time interval of how much time has to be matched to set User like offline 30 mins by default
    private Long timestampForArchive = 15L;

    private Integer endTimeForEvening = 24;
    private Integer endTimeForMorning = 16;
    private Integer startTimeForEvening = 16;
    private Integer startTimeForMorning = 0;

    private final static TimeInspector inspector = new TimeInspector();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern( "EEE MMM d H:mm:ss zzz yyyy", Locale.ENGLISH );

    public static TimeInspector getInspector () { return inspector; }

    private Date setDate () { return ( this.date = new Date() ); }

    private final Supplier< Date > getNewDate = Date::new;

    private final Function< String, Date > convertDate = s -> {
            try {
                final List< String > words = Arrays.asList( s.split( " " ) );
                return new SimpleDateFormat( words.get( 3 ).length() == 4 ? "EEE MMM dd yyyy kk:mm:ss" : "EEE MMM dd kk:mm:ss", Locale.US )
                    .parse( words.get( 3 ).length() >= 4
                            ? words.get( 0 ) + " " + words.get( 1 ) + " " + words.get( 2 ) + " " + words.get( 3 ) + " " + words.get( 4 )
                            : words.get( 0 ) + " " + words.get( 1 ) + " " + words.get( 2 ) + " " + words.get( 3 ) );
            } catch ( final ParseException e ) { throw new RuntimeException(e); } };

    private final Function< Date, String > convertDateToString = s -> ZonedDateTime.parse( s.toString(), this.formatter ).format( DateTimeFormatter.ISO_LOCAL_DATE );

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

    // возвращает данные о дате о начале года или конце
    private final Function< Boolean, Date > getYearStartOrEnd = flag -> {
            if ( flag ) {
                this.getCalendar().set( Calendar.YEAR, Year.now().getValue() );
                this.getCalendar().set( Calendar.DAY_OF_YEAR, 1 ); }
            else {
                calendar.set( Calendar.MONTH, 11 );
                calendar.set( Calendar.DAY_OF_MONTH, 31 ); }
            return this.getCalendar().getTime(); };

    private final Function< Date, Month > getMonthName = date1 -> Month.of( date1.getMonth() + 1 );

    private final BiFunction< Instant, Integer, Long > getTimeDifference = ( instant, integer ) -> switch ( integer ) {
            case 1 -> Math.abs( Duration.between( Instant.now(), instant ).toHours() );
            case 2 -> Math.abs( Duration.between( Instant.now(), instant ).toMinutes() );
            default -> Math.abs( Duration.between( Instant.now(), instant ).toSeconds() ); };
}
