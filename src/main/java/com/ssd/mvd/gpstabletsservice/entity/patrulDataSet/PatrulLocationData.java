package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import java.util.Optional;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.DataInfo;

public final class PatrulLocationData {
    public Double getDistance() {
        return this.distance;
    }

    public void setDistance( final Double distance ) {
        this.distance = distance;
    }

    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude( final Double latitude ) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude( final Double longitude ) {
        this.longitude = longitude;
    }

    public Double getLatitudeOfTask() {
        return this.latitudeOfTask;
    }

    public void setLatitudeOfTask( final Double latitudeOfTask ) {
        this.latitudeOfTask = latitudeOfTask;
    }

    public Double getLongitudeOfTask() {
        return this.longitudeOfTask;
    }

    public void setLongitudeOfTask( final Double longitudeOfTask ) {
        this.longitudeOfTask = longitudeOfTask;
    }

    private double distance;
    // текущее местоположение патрульного по Х
    private double latitude;
    // текущее местоположение патрульного по Y
    private double longitude;
    // локация заданной задачи по Х
    private double latitudeOfTask;
    // локация заданной задачи по Y
    private double longitudeOfTask;

    public void changeLocationFromCadastre ( final DataInfo dataInfo ) {
        if (
                DataValidateInspector
                        .getInstance()
                        .objectIsNotNull( dataInfo )
                        && DataValidateInspector
                        .getInstance()
                        .objectIsNotNull( dataInfo.getCadaster() ) ) {
            this.setLatitudeOfTask( dataInfo.getCadaster().getLatitude() );
            this.setLongitudeOfTask( dataInfo.getCadaster().getLongitude() );
        }
    }

    public static PatrulLocationData empty () {
        return new PatrulLocationData();
    }

    private PatrulLocationData () {}

    public static <T> PatrulLocationData generate ( final T object ) {
        return object instanceof Row
                ? new PatrulLocationData( (Row) object )
                : new PatrulLocationData( (UDTValue) object );
    }

    private PatrulLocationData ( final Row row ) {
        this.setDistance( row.getDouble( "distance" ) );
        this.setLatitude( row.getDouble( "latitude" ) );
        this.setLongitude( row.getDouble( "longitude" ) );
        this.setLatitudeOfTask( row.getDouble( "latitudeOfTask" ) );
        this.setLongitudeOfTask( row.getDouble( "longitudeOfTask" ) );
    }

    private PatrulLocationData( final UDTValue udtValue ) {
        Optional.ofNullable( udtValue ).ifPresent( udtValue1 -> {
            this.setDistance( udtValue.getDouble( "distance" ) );
            this.setLatitude( udtValue.getDouble( "latitude" ) );
            this.setLongitude( udtValue.getDouble( "longitude" ) );
            this.setLatitudeOfTask( udtValue.getDouble( "latitudeOfTask" ) );
            this.setLongitudeOfTask( udtValue.getDouble( "longitudeOfTask" ) );
        } );
    }
}
