package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;

import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;

public final class PositionInfo extends DataValidateInspector implements ObjectCommonMethods< PositionInfo > {
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

    public static PositionInfo empty () {
        return new PositionInfo();
    }

    private PositionInfo () {}

    @Override
    public PositionInfo generate( final Row row ) {
        super.checkAndSetParams(
                row,
                row1 -> {
                    this.setLat( row.getDouble( "latitude" ) );
                    this.setLng( row.getDouble( "longitude" ) );
                }
        );

        return this;
    }

    @Override
    public PositionInfo generate( final UDTValue udtValue ) {
        this.setLat( udtValue.getDouble( "lat" ) );
        this.setLng( udtValue.getDouble( "lng" ) );

        return this;
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setDouble ( "lat", this.getLat() )
                .setDouble ( "lng", this.getLng() );
    }
}
