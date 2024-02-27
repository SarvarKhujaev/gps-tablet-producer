package com.ssd.mvd.gpstabletsservice.tuple;

import com.datastax.driver.core.Row;
import java.util.Optional;
import java.util.UUID;

public final class TupleOfCar {
    public void setUuid ( final UUID uuid ) {
        this.uuid = uuid;
    }

    public UUID getUuidOfEscort() {
        return this.uuidOfEscort;
    }

    public void setUuidOfEscort ( final UUID uuidOfEscort ) {
        this.uuidOfEscort = uuidOfEscort;
    }

    public UUID getUuidOfPatrul() {
        return this.uuidOfPatrul;
    }

    public void setUuidOfPatrul ( final UUID uuidOfPatrul ) {
        this.uuidOfPatrul = uuidOfPatrul;
    }

    public String getCarModel() {
        return this.carModel;
    }

    public void setCarModel ( final String carModel ) {
        this.carModel = carModel;
    }

    public String getGosNumber() {
        return this.gosNumber;
    }

    public void setGosNumber ( final String gosNumber ) {
        this.gosNumber = gosNumber;
    }

    public String getTrackerId() {
        return this.trackerId;
    }

    public void setTrackerId ( final String trackerId ) {
        this.trackerId = trackerId;
    }

    public String getNsfOfPatrul() {
        return this.nsfOfPatrul;
    }

    public void setNsfOfPatrul ( final String nsfOfPatrul ) {
        this.nsfOfPatrul = nsfOfPatrul;
    }

    public String getSimCardNumber() {
        return this.simCardNumber;
    }

    public void setSimCardNumber ( final String simCardNumber ) {
        this.simCardNumber = simCardNumber;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude ( final Double latitude ) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude ( final Double longitude ) {
        this.longitude = longitude;
    }

    public Double getAverageFuelConsumption() {
        return this.averageFuelConsumption;
    }

    public void setAverageFuelConsumption ( final Double averageFuelConsumption ) {
        this.averageFuelConsumption = averageFuelConsumption;
    }

    private UUID uuid;
    private UUID uuidOfEscort; // UUID of the Escort which this car is linked to
    private UUID uuidOfPatrul; // UUID of the Escort which this patrul is linked to

    private String carModel;
    private String gosNumber;
    private String trackerId;
    private String nsfOfPatrul;
    private String simCardNumber;

    private Double latitude;
    private Double longitude;
    private Double averageFuelConsumption;

    public UUID getUuid () { return this.uuid; }

    public void unlinkFromEscortCarAndPatrul () {
        this.setUuidOfEscort( null );
        this.setUuidOfPatrul( null );
    }

    public TupleOfCar( final Row row ) {
        Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setUuid( row.getUUID( "uuid" ) );
            this.setUuidOfEscort( row.getUUID( "uuidOfEscort" ) );
            this.setUuidOfPatrul( row.getUUID( "uuidOfPatrul" ) );

            this.setCarModel( row.getString( "carModel" ) );
            this.setGosNumber( row.getString( "gosNumber" ) );
            this.setTrackerId( row.getString( "trackerId" ) );
            this.setNsfOfPatrul( row.getString( "nsfOfPatrul" ) );
            this.setSimCardNumber( row.getString( "simCardNumber" ) );

            this.setLatitude( row.getDouble( "latitude" ) );
            this.setLongitude( row.getDouble( "longitude" ) );
            this.setAverageFuelConsumption( row.getDouble( "averageFuelConsumption" ) );
        } );
    }
}
