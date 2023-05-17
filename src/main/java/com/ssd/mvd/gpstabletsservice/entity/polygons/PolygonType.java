package com.ssd.mvd.gpstabletsservice.entity.polygons;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
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

    public PolygonType ( final Row row ) {
        if ( DataValidateInspector
                .getInstance()
                .checkParam
                .test( row ) ) {
            this.setUuid( row.getUUID( "uuid" ) );
            this.setName( row.getString( "name" ) ); } }

    public PolygonType( final UDTValue row ) {
        this.setUuid( row.getUUID( "uuid" ) );
        this.setName( row.getString( "name" ) ); }
}
