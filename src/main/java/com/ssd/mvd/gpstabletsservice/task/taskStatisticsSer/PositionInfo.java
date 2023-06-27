package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;
import java.util.Optional;

@lombok.Data // used in case of historical request for some time duration
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class PositionInfo {
    private Double lat;
    private Double lng;

    public PositionInfo ( final Row row ) { Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setLat( row.getDouble( "latitude" ) );
            this.setLng( row.getDouble( "longitude" ) ); } ); }

    public PositionInfo ( final UDTValue value ) {
        this.setLat( value.getDouble( "lat" ) );
        this.setLng( value.getDouble( "lng" ) ); }
}
