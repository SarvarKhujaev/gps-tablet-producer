package com.ssd.mvd.gpstabletsservice.task.card;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

@lombok.Data // used in case of historical request for some time duration
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class PositionInfo {
    private Double lat;
    private Double lng;

    public PositionInfo( Row row ) {
        if ( row != null ) {
            this.setLat( row.getDouble( "latitude" ) );
            this.setLng( row.getDouble( "longitude" ) ); } }

    public PositionInfo( UDTValue value ) {
        this.setLat( value.getDouble( "lat" ) );
        this.setLng( value.getDouble( "lng" ) ); }
}
