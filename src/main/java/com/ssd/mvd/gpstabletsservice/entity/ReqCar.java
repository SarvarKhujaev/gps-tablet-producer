package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.Row;
import java.util.Optional;
import java.util.UUID;

public final class ReqCar {
    public UUID getPatrulId() {
        return this.patrulId;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid ( final UUID uuid ) {
        this.uuid = uuid;
    }

    public UUID getLustraId() {
        return this.lustraId;
    }

    public void setLustraId ( final UUID lustraId ) {
        this.lustraId = lustraId;
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

    public String getVehicleType() {
        return this.vehicleType;
    }

    public void setVehicleType ( final String vehicleType ) {
        this.vehicleType = vehicleType;
    }

    public String getCarImageLink() {
        return this.carImageLink;
    }

    public void setCarImageLink ( final String carImageLink ) {
        this.carImageLink = carImageLink;
    }

    public String getPatrulPassportSeries() {
        return this.patrulPassportSeries;
    }

    public void setPatrulPassportSeries ( final String patrulPassportSeries ) {
        this.patrulPassportSeries = patrulPassportSeries;
    }

    public Integer getSideNumber() {
        return this.sideNumber;
    }

    public void setSideNumber ( final Integer sideNumber ) {
        this.sideNumber = sideNumber;
    }

    public Integer getSimCardNumber() {
        return this.simCardNumber;
    }

    public void setSimCardNumber ( final Integer simCardNumber ) {
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

    public Double getAverageFuelSize() {
        return this.averageFuelSize;
    }

    public void setAverageFuelSize ( final Double averageFuelSize ) {
        this.averageFuelSize = averageFuelSize;
    }

    public Double getAverageFuelConsumption() {
        return this.averageFuelConsumption;
    }

    public void setAverageFuelConsumption ( final Double averageFuelConsumption ) {
        this.averageFuelConsumption = averageFuelConsumption;
    }

    private UUID uuid;
    private UUID lustraId;

    private UUID patrulId;

    private String gosNumber;
    private String trackerId;
    private String vehicleType;
    private String carImageLink;
    private String patrulPassportSeries;

    private Integer sideNumber; // бортовой номер
    private Integer simCardNumber;

    private Double latitude;
    private Double longitude;
    private Double averageFuelSize; // средний расход топлива по документам
    private Double averageFuelConsumption = 0.0; // средний расход топлива исходя из стиля вождения водителя

    public ReqCar ( final Row row ) {
        Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setUuid( row.getUUID( "uuid" ) );
            this.setLustraId( row.getUUID( "lustraId" ) );

            this.setGosNumber( row.getString( "gosNumber" ) );
            this.setTrackerId( row.getString( "trackerId" ) );
            this.setVehicleType( row.getString( "vehicleType" ) );
            this.setCarImageLink( row.getString( "carImageLink" ) );
            this.setPatrulPassportSeries( row.getString( "patrulPassportSeries" ) );

            this.setSideNumber( row.getInt( "sideNumber" ) );
            this.setSimCardNumber( row.getInt( "simCardNumber" ) );

            this.setLatitude( row.getDouble( "latitude" ) );
            this.setLongitude( row.getDouble( "longitude" ) );
            this.setAverageFuelSize( row.getDouble( "averageFuelSize" ) );
            this.setAverageFuelConsumption( row.getDouble( "averageFuelConsumption" ) );
        } );
    }
}
