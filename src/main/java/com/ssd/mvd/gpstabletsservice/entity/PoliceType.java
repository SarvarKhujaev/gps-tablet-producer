package com.ssd.mvd.gpstabletsservice.entity;

import java.util.UUID;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;

public final class PoliceType {
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

    public PoliceType ( final Row value ) {
        this.setUuid( value.getUUID( "uuid" ) );
        this.setIcon( value.getString( "icon" ) );
        this.setIcon2( value.getString( "icon2" ) );
        this.setPoliceType( value.getString( "PoliceType" ) );
    }

    public PoliceType ( final UDTValue value ) {
        this.setUuid( value.getUUID( "uuid" ) );
        this.setIcon( value.getString( "icon" ) );
        this.setIcon2( value.getString( "icon2" ) );
        this.setPoliceType( value.getString( "PoliceType" ) );
    }
}
