package com.ssd.mvd.gpstabletsservice.entity.polygons;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

import java.util.UUID;

public final class PolygonType extends DataValidateInspector implements ObjectCommonMethods {
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

    public static PolygonType empty () {
        return new PolygonType();
    }

    private PolygonType () {}

    public PolygonType ( final Row row ) {
        super.checkAndSetParams(
                row,
                row1 -> {
                    this.setUuid( row.getUUID( "uuid" ) );
                    this.setName( row.getString( "name" ) );
                }
        );
    }

    @Override
    public PolygonType generate( final UDTValue udtValue ) {
        super.checkAndSetParams(
                udtValue,
                udtValue1 -> {
                    this.setUuid( udtValue.getUUID( "uuid" ) );
                    this.setName( udtValue.getString( "name" ) );
                }
        );

        return this;
    }

    @Override
    public PolygonType generate( final Row row ) {
        super.checkAndSetParams(
                row,
                row1 -> {
                    this.setUuid( row.getUUID( "uuid" ) );
                    this.setName( row.getString( "name" ) );
                }
        );

        return this;
    }

    @Override
    public Object generate() {
        return new PolygonType();
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setUUID( "uuid", this.getUuid() )
                .setString( "name", this.getName() );
    }
}
