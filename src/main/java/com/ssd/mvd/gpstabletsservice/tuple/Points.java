package com.ssd.mvd.gpstabletsservice.tuple;

import com.datastax.driver.core.UDTValue;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.UUID;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Points {
    private Double lat;
    private Double lng;

    private UUID pointId;
    private String pointName;

    public Points( UDTValue value ) {
        this.setLat( value.getDouble( "lat" ) );
        this.setLng( value.getDouble( "lng" ) );

        this.setPointId( value.getUUID( "pointId" ) );
        this.setPointName( value.getString( "pointName" ) ); }
}
