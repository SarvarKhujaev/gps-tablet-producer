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

    public PositionInfo ( final Row row ) {
        super.checkAndSetParams(
                row,
                row1 -> {
                    this.setLat( row.getDouble( "latitude" ) );
                    this.setLng( row.getDouble( "longitude" ) );
                }
        );
    }

    public PositionInfo ( final UDTValue value ) {
        this.setLat( value.getDouble( "lat" ) );
        this.setLng( value.getDouble( "lng" ) );
    }

    @Override
    public PositionInfo generate( final UDTValue udtValue ) {
        return new PositionInfo( udtValue );
    }

    @Override
    public PositionInfo generate( final Row row ) {
        return new PositionInfo( row );
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setDouble ( "lat", this.getLat() )
                .setDouble ( "lng", this.getLng() );
    }
}
