package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.entity.PolygonEntity;
import com.datastax.driver.core.Row;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolygonForEscort {
    private UUID uuid;
    private String name;

    private Integer totalTime;
    private Integer totalDistance;

    private List< Points > pointsList;
    private List< PolygonEntity > latlngs;

    public UUID getUuid () { return this.uuid != null ? uuid : ( this.uuid = UUID.randomUUID() ); }

    public PolygonForEscort ( Row row ) {
        this.setUuid( row.getUUID( "uuid" ) );
        this.setName( row.getString( "name" ) );

        this.setTotalTime( row.getInt( "totalTime" ) );
        this.setTotalDistance( row.getInt( "totalDistance" ) );

        this.setPointsList( row.getList( "pointsList", Points.class ) );
        this.setLatlngs( row.getList( "latlngs", PolygonEntity.class ) );
    }
}
