package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;
import java.util.Optional;

public final class PositionInfo {
    public double getLat() {
        return this.lat;
    }

    public void setLat ( final double lat ) {
        this.lat = lat;
    }

    public double getLng() {
        return this.lng;
    }

    public void setLng ( final double lng ) {
        this.lng = lng;
    }

    private double lat;
    private double lng;

    public PositionInfo ( final Row row ) {
        Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setLat( row.getDouble( "latitude" ) );
            this.setLng( row.getDouble( "longitude" ) );
        } );
    }

    public PositionInfo ( final UDTValue value ) {
        this.setLat( value.getDouble( "lat" ) );
        this.setLng( value.getDouble( "lng" ) );
    }
}
