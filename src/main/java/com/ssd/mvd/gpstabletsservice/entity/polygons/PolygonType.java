package com.ssd.mvd.gpstabletsservice.entity.polygons;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

import java.util.Optional;
import java.util.UUID;

public final class PolygonType {
    public UUID getUuid () {
        return this.uuid;
    }

    public void setUuid ( final UUID uuid ) {
        this.uuid = uuid;
    }

    public String getName() {
        return this.name;
    }

    public void setName ( final String name ) {
        this.name = name;
    }

    private UUID uuid;

    private String name;

    public PolygonType ( final Row row ) {
        Optional.ofNullable( row ).ifPresent(
                row1 -> {
                    this.setUuid( row.getUUID( "uuid" ) );
                    this.setName( row.getString( "name" ) );
                }
        );
    }

    public PolygonType( final UDTValue row ) {
        this.setUuid( row.getUUID( "uuid" ) );
        this.setName( row.getString( "name" ) );
    }
}
