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
    private List< PolygonEntity > latlngs;

    public PolygonForEscort( Row row ) {
        this.setUuid( row.getUUID( "id" ) );
        this.setName( row.getString( "name" ) );
        this.setLatlngs( row.getList( "latlngs", PolygonEntity.class ) ); }
}
