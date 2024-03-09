package com.ssd.mvd.gpstabletsservice.entity.polygons;

import com.datastax.driver.core.UDTValue;
import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;

public final class PolygonEntity implements ObjectCommonMethods< PolygonEntity > {
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

    public static PolygonEntity empty () {
        return new PolygonEntity();
    }

    private PolygonEntity () {}

    private PolygonEntity ( final UDTValue udtValue ) {
        this.setLat( udtValue.getDouble("lat" ) );
        this.setLng( udtValue.getDouble("lng" ) );
    }

    @Override
    public PolygonEntity generate ( final UDTValue udtValue ) {
        return new PolygonEntity( udtValue );
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setDouble("lat", this.getLat() )
                .setDouble("lng", this.getLng() );
    }
}
