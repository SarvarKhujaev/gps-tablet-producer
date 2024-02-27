package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.entity.polygons.PolygonEntity;
import com.datastax.driver.core.Row;

import java.util.Optional;
import java.util.List;
import java.util.UUID;

public final class PolygonForEscort {
    public void setUuid( final UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName( final String name ) {
        this.name = name;
    }

    public UUID getUuidOfEscort() {
        return this.uuidOfEscort;
    }

    public void setUuidOfEscort( final UUID uuidOfEscort ) {
        this.uuidOfEscort = uuidOfEscort;
    }

    public short getTotalTime() {
        return this.totalTime;
    }

    public void setTotalTime( final short totalTime ) {
        this.totalTime = totalTime;
    }

    public short getRouteIndex() {
        return this.routeIndex;
    }

    public void setRouteIndex( final short routeIndex ) {
        this.routeIndex = routeIndex;
    }

    public short getTotalDistance() {
        return this.totalDistance;
    }

    public void setTotalDistance( final short totalDistance ) {
        this.totalDistance = totalDistance;
    }

    public List< Points > getPointsList() {
        return this.pointsList;
    }

    public void setPointsList( final List< Points > pointsList ) {
        this.pointsList = pointsList;
    }

    public List< PolygonEntity > getLatlngs() {
        return this.latlngs;
    }

    public void setLatlngs( final List< PolygonEntity > latlngs ) {
        this.latlngs = latlngs;
    }

    private UUID uuid;
    private String name;
    private UUID uuidOfEscort;

    private short totalTime;
    private short routeIndex;
    private short totalDistance;

    private List< Points > pointsList;
    private List< PolygonEntity > latlngs;

    public UUID getUuid () {
        return this.uuid;
    }

    public PolygonForEscort ( final Row row ) {
        Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setUuid( row.getUUID( "uuid" ) );
            this.setName( row.getString( "name" ) );
            this.setUuidOfEscort( row.getUUID( "uuidOfEscort" ) );

            this.setTotalTime( row.getByte( "totalTime" ) );
            this.setRouteIndex( row.getByte( "routeIndex" ) );
            this.setTotalDistance( row.getByte( "totalDistance" ) );

            this.setPointsList( row.getList( "pointsList", Points.class ) );
            this.setLatlngs( row.getList( "latlngs", PolygonEntity.class ) );
        } );
    }
}
