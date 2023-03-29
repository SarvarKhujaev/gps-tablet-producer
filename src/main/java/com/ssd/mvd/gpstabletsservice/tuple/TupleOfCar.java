package com.ssd.mvd.gpstabletsservice.tuple;

import com.datastax.driver.core.Row;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class TupleOfCar {
    private UUID uuid;
    private UUID uuidOfEscort; // UUID of the Escort which this car is linked to
    private UUID uuidOfPatrul; // UUID of the Escort which this car is linked to

    private String carModel;
    private String gosNumber;
    private String trackerId;
    private String nsfOfPatrul;
    private String simCardNumber;

    private Double latitude;
    private Double longitude;
    private Double averageFuelConsumption;

    public UUID getUuid () { return this.uuid != null ? uuid : ( this.uuid = UUID.randomUUID() ); }

    public TupleOfCar( Row row ) {
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
        this.setAverageFuelConsumption( row.getDouble( "averageFuelConsumption" ) ); }
}
