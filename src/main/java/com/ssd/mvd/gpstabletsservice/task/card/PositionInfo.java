package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

@lombok.Data // used in case of historical request for some time duration
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class PositionInfo {
    private Double lat;
    private Double lng;

    public PositionInfo ( final Row row ) {
        if ( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( row ) ) {
            this.setLat( row.getDouble( "latitude" ) );
            this.setLng( row.getDouble( "longitude" ) ); } }

    public PositionInfo ( final UDTValue value ) {
        this.setLat( value.getDouble( "lat" ) );
        this.setLng( value.getDouble( "lng" ) ); }
}
