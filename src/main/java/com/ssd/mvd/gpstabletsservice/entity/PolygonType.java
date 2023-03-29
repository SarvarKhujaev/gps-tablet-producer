package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class PolygonType {
    private UUID uuid;
    private String name;

    public UUID getUuid () { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }

    public PolygonType ( Row row ) {
        this.setUuid( row.getUUID( "uuid" ) );
        this.setName( row.getString( "name" ) ); }

    public PolygonType( UDTValue row ) {
        this.setUuid( row.getUUID( "uuid" ) );
        this.setName( row.getString( "name" ) ); }
}
