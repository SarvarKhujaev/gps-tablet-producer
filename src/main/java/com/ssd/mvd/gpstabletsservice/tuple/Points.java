package com.ssd.mvd.gpstabletsservice.tuple;

import java.util.UUID;
import com.datastax.driver.core.UDTValue;
import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;

public final class Points implements ObjectCommonMethods< Points > {
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

    public static Points empty () {
        return new Points();
    }

    private Points () {}

    @Override
    public Points generate( final UDTValue udtValue ) {
        this.setPointName( udtValue.getString( "pointName" ) );
        this.setPointId( udtValue.getUUID( "pointId" ) );

        this.setLat( udtValue.getDouble( "lat" ) );
        this.setLng( udtValue.getDouble( "lng" ) );

        return this;
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setDouble("lat", this.getLat() )
                .setDouble("lng", this.getLng() )
                .setUUID( "pointId", this.getPointId() )
                .setString( "pointName", this.getPointName() );
    }
}
