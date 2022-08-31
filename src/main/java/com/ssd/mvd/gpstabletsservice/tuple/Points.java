package com.ssd.mvd.gpstabletsservice.tuple;

import com.datastax.driver.core.UDTValue;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Points {
    private Double lat;
    private Double lng;

    private Integer pointId;
    private String pointName;

    public Points( UDTValue value ) {
        this.setLat( value.getDouble( "lat" ) );
        this.setLng( value.getDouble( "lng" ) );

        this.setPointId( value.getInt( "pointId" ) );
        this.setPointName( value.getString( "pointName" ) ); }
}
