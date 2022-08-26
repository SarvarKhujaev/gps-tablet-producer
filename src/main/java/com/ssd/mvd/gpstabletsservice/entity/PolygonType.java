package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.Row;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.UUID;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolygonType {
    private UUID uuid;
    private String name;

    public UUID getUuid () { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }

    public PolygonType ( Row row ) {
        this.setUuid( row.getUUID( "uuid" ) );
        this.setName( row.getString( "name" ) ); }
}
