package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.entity.PolygonEntity;
import com.datastax.driver.core.Row;

import java.util.List;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class PolygonForEscort {
    private UUID uuid;
    private String name;
    private UUID uuidOfEscort;

    private Integer totalTime;
    private Integer routeIndex;
    private Integer totalDistance;

    private List< Points > pointsList;
    private List< PolygonEntity > latlngs;

    public UUID getUuid () { return this.uuid != null ? uuid : ( this.uuid = UUID.randomUUID() ); }

    public PolygonForEscort ( final Row row ) {
        this.setUuid( row.getUUID( "uuid" ) );
        this.setName( row.getString( "name" ) );
        this.setUuidOfEscort( row.getUUID( "uuidOfEscort" ) );

        this.setTotalTime( row.getInt( "totalTime" ) );
        this.setRouteIndex( row.getInt( "routeIndex" ) );
        this.setTotalDistance( row.getInt( "totalDistance" ) );

        this.setPointsList( row.getList( "pointsList", Points.class ) );
        this.setLatlngs( row.getList( "latlngs", PolygonEntity.class ) ); }
}
