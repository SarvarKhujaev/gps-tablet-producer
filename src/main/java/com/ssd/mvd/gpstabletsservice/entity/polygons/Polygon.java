package com.ssd.mvd.gpstabletsservice.entity.polygons;

import com.datastax.driver.core.Row;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public final class Polygon {
    public void setUuid( final UUID uuid ) {
        this.uuid = uuid;
    }

    public UUID  getOrgan () {
        return this.organ;
    }

    public void setOrgan( final UUID organ ) {
        this.organ = organ;
    }

    public Long  getRegionId () {
        return this.regionId;
    }

    public void setRegionId( final Long regionId ) {
        this.regionId = regionId;
    }

    public Long  getMahallaId () {
        return this.mahallaId;
    }

    public void setMahallaId( final Long mahallaId ) {
        this.mahallaId = mahallaId;
    }

    public Long  getDistrictId () {
        return this.districtId;
    }

    public void setDistrictId( final Long districtId ) {
        this.districtId = districtId;
    }

    public String  getName () {
        return this.name;
    }

    public void setName( final String name ) {
        this.name = name;
    }

    public String  getColor () {
        return this.color;
    }

    public void setColor( final String color ) {
        this.color = color;
    }

    public PolygonType getPolygonType() {
        return this.polygonType;
    }

    public void setPolygonType( final PolygonType polygonType ) {
        this.polygonType = polygonType;
    }

    public List< UUID > getPatrulList() {
        return this.patrulList;
    }

    public void setPatrulList( final List<UUID> patrulList ) {
        this.patrulList = patrulList;
    }

    public List< PolygonEntity > getLatlngs() {
        return this.latlngs;
    }

    public void setLatlngs( final List<PolygonEntity> latlngs) {
        this.latlngs = latlngs;
    }

    public UUID uuid;
    public UUID organ;

    public long regionId;
    public long mahallaId;
    public long districtId; // tuman

    public String name;
    public String color;

    private PolygonType polygonType;

    public List< UUID > patrulList; // the list of all Patruls who works at this polygon
    public List< PolygonEntity > latlngs;

    public Polygon ( final Row row ) {
        Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setUuid( row.getUUID( "uuid" ) );
            this.setOrgan( row.getUUID( "organ" ) );

            this.setRegionId( row.getLong( "regionId" ) );
            this.setMahallaId( row.getLong( "mahallaId" ) );
            this.setDistrictId( row.getLong( "districtId" ) );

            this.setName( row.getString( "name" ) );
            this.setColor( row.getString( "color" ) );

            this.setPatrulList( row.getList( "patrulList", UUID.class ) );
            this.setLatlngs( row.getList( "latlngs", PolygonEntity.class ) );
            this.setPolygonType( row.get( "polygonType", PolygonType.class ) );
        } );
    }

    public UUID getUuid () {
        return this.uuid;
    }
}
