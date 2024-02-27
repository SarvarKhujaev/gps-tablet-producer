package com.ssd.mvd.gpstabletsservice.tuple;

import com.datastax.driver.core.UDTValue;
import java.util.UUID;

public final class Points {
    public double getLat() {
        return this.lat;
    }

    public void setLat( final double lat ) {
        this.lat = lat;
    }

    public double getLng() {
        return this.lng;
    }

    public void setLng( final double lng ) {
        this.lng = lng;
    }

    public UUID getPointId() {
        return this.pointId;
    }

    public void setPointId( final UUID pointId ) {
        this.pointId = pointId;
    }

    public String getPointName() {
        return this.pointName;
    }

    public void setPointName( final String pointName ) {
        this.pointName = pointName;
    }

    private double lat;
    private double lng;

    private UUID pointId;
    private String pointName;

    public Points( final UDTValue value ) {
        this.setLat( value.getDouble( "lat" ) );
        this.setLng( value.getDouble( "lng" ) );

        this.setPointId( value.getUUID( "pointId" ) );
        this.setPointName( value.getString( "pointName" ) );
    }
}
