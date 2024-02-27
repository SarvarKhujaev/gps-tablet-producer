package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;
import com.datastax.driver.core.Row;

import java.util.Optional;
import java.util.UUID;
import java.util.Date;

public final class TabletUsage extends TimeInspector {
    public Date getStartedToUse() {
        return this.startedToUse;
    }

    public void setStartedToUse ( final Date startedToUse ) {
        this.startedToUse = startedToUse;
    }

    public Date getLastActiveDate() {
        return this.lastActiveDate;
    }

    public void setLastActiveDate ( final Date lastActiveDate ) {
        this.lastActiveDate = lastActiveDate;
    }

    public UUID getUuidOfPatrul() {
        return this.uuidOfPatrul;
    }

    public void setUuidOfPatrul ( final UUID uuidOfPatrul ) {
        this.uuidOfPatrul = uuidOfPatrul;
    }

    public String getSimCardNumber() {
        return this.simCardNumber;
    }

    public void setSimCardNumber ( final String simCardNumber ) {
        this.simCardNumber = simCardNumber;
    }

    public long getTotalActivityTime() {
        return this.totalActivityTime;
    }

    public void setTotalActivityTime ( final long totalActivityTime ) {
        this.totalActivityTime = totalActivityTime;
    }

    private Date startedToUse = super.newDate();
    private Date lastActiveDate = super.newDate();

    private UUID uuidOfPatrul;
    private String simCardNumber; // unique identifier of Tablet
    private long totalActivityTime = 0L; // total time of usage in seconds

    public static <T> TabletUsage generate ( final T object ) {
        return object instanceof Patrul
                ? new TabletUsage( (Patrul) object )
                : new TabletUsage( (Row) object );
    }

    private TabletUsage ( final Row row ) {
        Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setStartedToUse( row.getTimestamp( "startedToUse" ) );
            this.setLastActiveDate( row.getTimestamp( "lastActiveDate" ) );

            this.setUuidOfPatrul( row.getUUID( "uuidOfPatrul" ) );
            this.setSimCardNumber( row.getString( "simCardNumber" ) );
            this.setTotalActivityTime( row.getLong( "totalActivityTime" ) );
        } );
    }

    public TabletUsage ( final Patrul patrul ) {
        this.setUuidOfPatrul( patrul.getUuid() );
        this.setSimCardNumber( patrul.getPatrulMobileAppInfo().getSimCardNumber() );
    }
}