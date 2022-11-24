package com.ssd.mvd.gpstabletsservice.task.card;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data // used in case of historical request for some time duration
@NoArgsConstructor
@AllArgsConstructor
public class PositionInfo {
    private Double lat;
    private Double lng;

    public PositionInfo( Row row ) {
        this.setLat( row.getDouble( "latitude" ) );
        this.setLng( row.getDouble( "longitude" ) ); }

    public PositionInfo( UDTValue value ) {
        this.setLat( value.getDouble( "lat" ) );
        this.setLng( value.getDouble( "lng" ) ); }
}
