package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.Row;
import java.util.Date;
import java.util.UUID;

@lombok.Data
public class TabletUsage {
    private Date startedToUse = new Date();
    private Date lastActiveDate = new Date();

    private UUID uuidOfPatrul;
    private String simCardNumber; // unique identifier of Tablet
    private Long totalActivityTime = 0L; // total time of usage in seconds

    public TabletUsage ( final Row row ) {
        this.setStartedToUse( row.getTimestamp( "startedToUse" ) );
        this.setLastActiveDate( row.getTimestamp( "lastActiveDate" ) );

        this.setUuidOfPatrul( row.getUUID( "uuidOfPatrul" ) );
        this.setSimCardNumber( row.getString( "simCardNumber" ) );
        this.setTotalActivityTime( row.getLong( "totalActivityTime" ) ); }

    public TabletUsage ( final Patrul patrul ) {
        this.setUuidOfPatrul( patrul.getUuid() );
        this.setSimCardNumber( patrul.getSimCardNumber() ); }
}