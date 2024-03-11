package com.ssd.mvd.gpstabletsservice.inspectors;

import java.util.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class TimeInspector extends StringOperations {
    protected static int DAY_IN_SECOND = 86400;

    private Date getDate() {
        return this.date;
    }

    private Calendar getCalendar() {
        return this.calendar;
    }

    private Date date; // for comparing with current time
    private final Calendar calendar = Calendar.getInstance();

    private static final byte endTimeForEvening = 24;
    private static final byte startTimeForMorning = 0;

    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern( "EEE MMM d H:mm:ss zzz yyyy", Locale.ENGLISH );

    private Date setDate () {
        return ( this.date = new Date() );
    }

    protected Date newDate () {
        return new Date();
    }

    protected Date convertDate( final String s ) {
        try {
            final List< String > words = super.convertArrayToList( s.split( " " ) );

            return new SimpleDateFormat(
                    words.get( 3 ).length() == 4
                            ? "EEE MMM dd yyyy kk:mm:ss"
                            : "EEE MMM dd kk:mm:ss", Locale.US )
                    .parse( words.get( 3 ).length() >= 4
                            ? String.join( " ",
                                    words.get( 0 ),
                                    words.get( 1 ),
                                    words.get( 2 ),
                                    words.get( 3 ),
                                    words.get( 4 ) )
                            : String.join( " ", words.get( 0 ), words.get( 1 ), words.get( 2 ), words.get( 3 ) ) );
        } catch ( final ParseException e ) {
            throw new RuntimeException( e );
        }
    }

    protected String convertDateToString ( final Date date ) {
        return ZonedDateTime.parse(
                date.toString(), this.formatter
        ).format( DateTimeFormatter.ISO_LOCAL_DATE );
    }

    protected boolean checkDate ( final Instant instant ) {
        return endTimeForEvening >= this.setDate().getHours()
                && this.getDate().getHours() >= startTimeForMorning
                ? ( this.getTimeDifference( instant, 2 ) <= 10 )
                : ( this.getTimeDifference( instant, 2 ) <= 7 );
    }

    // for checking current time of task ending
    protected long convertTimeToLong ( final String time ) {
        try {
            return time != null && !time.contains( "null" )
                    ? new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" )
                    .parse( time )
                    .getTime()
                    : 0L;
        } catch ( final Exception e ) {
            return 0L;
        }
    }

    // возвращает данные о дате о начале года или конце
    protected Date getYearStartOrEnd ( final boolean flag ) {
        if ( flag ) {
            this.getCalendar().set( Calendar.YEAR, Year.now().getValue() );
            this.getCalendar().set( Calendar.DAY_OF_YEAR, 1 );
        }

        else {
            calendar.set( Calendar.MONTH, 11 );
            calendar.set( Calendar.DAY_OF_MONTH, 31 );
        }

        return this.getCalendar().getTime();
    }

    protected Month getMonthName ( final Date date ) {
        return Month.of( date.getMonth() + 1 );
    }

    protected long getTimeDifference (
            final Instant instant,
            final int integer
    ) {
        return switch ( integer ) {
            case 1 -> Math.abs( Duration.between( Instant.now(), instant ).toHours() );
            case 2 -> Math.abs( Duration.between( Instant.now(), instant ).toMinutes() );
            default -> Math.abs( Duration.between( Instant.now(), instant ).toSeconds() );
        };
    }

    /*
    проверяем что патрульный добрался до пункта назначения менее чем за 24 часа
    */
    protected boolean checkPatrulCameInTime ( final Date date ) {
        return Math.abs( this.getTimeDifference( date.toInstant(), 1 ) ) >= 24;
    }
}
