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

    public Points( final UDTValue value ) {
        this.setLat( value.getDouble( "lat" ) );
        this.setLng( value.getDouble( "lng" ) );

        this.setPointId( value.getUUID( "pointId" ) );
        this.setPointName( value.getString( "pointName" ) );
    }

    @Override
    public Points generate( final UDTValue udtValue ) {
        return new Points( udtValue );
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
