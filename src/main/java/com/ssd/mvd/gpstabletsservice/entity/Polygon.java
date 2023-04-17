package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.Row;
import java.util.List;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class Polygon {
    public UUID uuid;
    public UUID organ;

    public Long regionId;
    public Long mahallaId;
    public Long districtId; // tuman

    public String name;
    public String color;

    private PolygonType polygonType;

    public List< UUID > patrulList; // the list of all Patruls who works at this polygon
    public List< PolygonEntity > latlngs;

    public Polygon ( final Row row ) {
        this.setUuid( row.getUUID( "uuid" ) );
        this.setOrgan( row.getUUID( "organ" ) );

        this.setRegionId( row.getLong( "regionId" ) );
        this.setMahallaId( row.getLong( "mahallaId" ) );
        this.setDistrictId( row.getLong( "districtId" ) );

        this.setName( row.getString( "name" ) );
        this.setColor( row.getString( "color" ) );

        this.setPatrulList( row.getList( "patrulList", UUID.class ) );
        this.setLatlngs( row.getList( "latlngs", PolygonEntity.class ) );
        this.setPolygonType( row.get( "polygonType", PolygonType.class ) ); }

    public UUID getUuid () { return this.uuid != null ? uuid : ( this.uuid = UUID.randomUUID() ); }
}
