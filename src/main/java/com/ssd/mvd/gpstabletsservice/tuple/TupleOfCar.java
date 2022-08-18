package com.ssd.mvd.gpstabletsservice.tuple;

import com.datastax.driver.core.Row;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TupleOfCar {
    private String carModel;
    private String gosNumber;
    private String trackerId;
    private String nsfOfPatrul;
    private String simCardNumber;
    private String passportSeries; // adding after admin chose all cars and wishes to link them to patruls

    private Double latitude;
    private Double longitude;
    private Double averageFuelConsumption;

    public TupleOfCar ( Row row ) {
        this.setCarModel( row.getString( "carModel" ) );
        this.setNsfOfPatrul( row.getString( "patrul" ) );
        this.setGosNumber( row.getString( "gosNumber" ) );
        this.setTrackerId( row.getString( "trackerId" ) );
        this.setSimCardNumber( row.getString( "simCardNumber" ) );
        this.setPassportSeries( row.getString( "passportNumber" ) );
        this.setAverageFuelConsumption( row.getDouble( "averageFuelConsumption" ) ); }
}
