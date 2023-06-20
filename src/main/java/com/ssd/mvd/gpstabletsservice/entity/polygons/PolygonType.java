package com.ssd.mvd.gpstabletsservice.entity.polygons;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

import java.util.Optional;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class PolygonType {
    private UUID uuid;
    private String name;

    public UUID getUuid () { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }

    public PolygonType ( final Row row ) { Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setUuid( row.getUUID( "uuid" ) );
            this.setName( row.getString( "name" ) ); } ); }

    public PolygonType( final UDTValue row ) {
        this.setUuid( row.getUUID( "uuid" ) );
        this.setName( row.getString( "name" ) ); }
}
