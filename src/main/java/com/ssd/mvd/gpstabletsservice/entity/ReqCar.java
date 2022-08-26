package com.ssd.mvd.gpstabletsservice.entity;

import java.util.UUID;

import com.datastax.driver.core.Row;
import lombok.Data;

@Data
public class ReqCar {
    private UUID uuid;
    private UUID lustraId;

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

    public ReqCar ( Row row ) {
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
        this.setAverageFuelConsumption( row.getDouble( "averageFuelConsumption" ) ); }
}
