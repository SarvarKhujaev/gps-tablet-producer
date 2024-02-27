package com.ssd.mvd.gpstabletsservice.task.sos_task;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.datastax.driver.core.Row;
import java.util.*;

public final class PatrulSos {
    private UUID uuid;

    public void setUuid( final UUID uuid ) {
        this.uuid = uuid;
    }

    public UUID getPatrulUUID() {
        return this.patrulUUID;
    }

    public void setPatrulUUID( final UUID patrulUUID ) {
        this.patrulUUID = patrulUUID;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress( final String address ) {
        this.address = address;
    }

    public Date getSosWasSendDate() {
        return this.sosWasSendDate;
    }

    public void setSosWasSendDate( final Date sosWasSendDate ) {
        this.sosWasSendDate = sosWasSendDate;
    }

    public Date getSosWasClosed() {
        return this.sosWasClosed;
    }

    public void setSosWasClosed( final Date sosWasClosed ) {
        this.sosWasClosed = sosWasClosed;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude( final double latitude ) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude( final double longitude ) {
        this.longitude = longitude;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus( final Status status ) {
        this.status = status;
    }

    public void setPatrulStatuses( final Map< UUID, String > patrulStatuses ) {
        this.patrulStatuses = patrulStatuses;
    }

    private UUID patrulUUID;

    private String address;

    private Date sosWasSendDate; // сохраняет время когда запрос был отправлен
    private Date sosWasClosed; // время когда сос был закрыт

    private double latitude;
    private double longitude;

    private Status status = Status.CREATED;

    public Map< UUID, String > getPatrulStatuses() {
        return this.patrulStatuses;
    }

    private Map< UUID, String > patrulStatuses;

    public UUID getUuid () {
        return this.uuid;
    }

    public PatrulSos () {}

    public PatrulSos ( final Row row ) {
        Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setUuid( row.getUUID( "uuid" ) );
            this.setPatrulUUID( row.getUUID( "patrulUUID" ) );

            this.setAddress( row.getString( "address" ) );

            this.setSosWasClosed( row.getTimestamp( "sosWasClosed" ) );
            this.setSosWasSendDate( row.getTimestamp( "sosWasSendDate" ) );

            this.setLatitude( row.getDouble( "latitude" ) );
            this.setLongitude( row.getDouble( "longitude" ) );

            this.setStatus( Status.valueOf( row.getString( "status" ) ) );
            this.setPatrulStatuses( row.getMap( "patrulStatuses", UUID.class, String.class ) );
        } );
    }
}
