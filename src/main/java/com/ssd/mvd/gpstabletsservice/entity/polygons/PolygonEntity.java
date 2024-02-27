package com.ssd.mvd.gpstabletsservice.entity.polygons;

import com.datastax.driver.core.UDTValue;

public final class PolygonEntity {
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

    public static PolygonEntity generate ( final UDTValue udtValue ) {
        return new PolygonEntity( udtValue );
    }

    private PolygonEntity ( final UDTValue udtValue ) {
        this.setLat( udtValue.getDouble("lat" ) );
        this.setLng( udtValue.getDouble("lng" ) );
    }
}
