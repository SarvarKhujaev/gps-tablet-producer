package com.ssd.mvd.gpstabletsservice.entity;

import java.util.UUID;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;
import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;

public final class PoliceType implements ObjectCommonMethods< PoliceType > {
    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid ( final UUID uuid ) {
        this.uuid = uuid;
    }

    public String getIcon() {
        return this.icon;
    }

    public void setIcon ( final String icon ) {
        this.icon = icon;
    }

    public String getIcon2() {
        return this.icon2;
    }

    public void setIcon2 ( final String icon2 ) {
        this.icon2 = icon2;
    }

    public String getPoliceType() {
        return this.policeType;
    }

    public void setPoliceType ( final String policeType ) {
        this.policeType = policeType;
    }

    private UUID uuid;
    private String icon;
    private String icon2;
    private String policeType;

    public static PoliceType empty () {
        return new PoliceType();
    }

    private PoliceType () {}

    @Override
    public PoliceType generate( final Row row ) {
        this.setPoliceType( row.getString( "PoliceType" ) );
        this.setIcon2( row.getString( "icon2" ) );
        this.setIcon( row.getString( "icon" ) );
        this.setUuid( row.getUUID( "uuid" ) );

        return this;
    }

    @Override
    public PoliceType generate( final UDTValue udtValue ) {
        this.setPoliceType( udtValue.getString( "PoliceType" ) );
        this.setIcon2( udtValue.getString( "icon2" ) );
        this.setIcon( udtValue.getString( "icon" ) );
        this.setUuid( udtValue.getUUID( "uuid" ) );

        return this;
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setUUID( "uuid", this.getUuid() )
                .setString( "icon", this.getIcon() )
                .setString( "icon2", this.getIcon2() )
                .setString( "policeType", this.getPoliceType() );
    }
}
