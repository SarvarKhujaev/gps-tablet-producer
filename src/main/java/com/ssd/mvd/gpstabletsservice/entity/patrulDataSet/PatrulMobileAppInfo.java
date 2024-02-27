package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;
import java.util.Optional;

public final class PatrulMobileAppInfo {
    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    public void setPhoneNumber ( final String phoneNumber ) {
        this.phoneNumber = phoneNumber;
    }

    public String getSimCardNumber() {
        return this.simCardNumber;
    }

    public void setSimCardNumber ( final String simCardNumber ) {
        this.simCardNumber = simCardNumber;
    }

    public byte getBatteryLevel() {
        return this.batteryLevel;
    }

    public void setBatteryLevel ( final byte batteryLevel ) {
        this.batteryLevel = batteryLevel;
    }

    private String phoneNumber;
    private String simCardNumber;
    private byte batteryLevel;

    public static PatrulMobileAppInfo empty() {
        return new PatrulMobileAppInfo();
    }

    private PatrulMobileAppInfo () {
        this.setPhoneNumber( "" );
        this.setSimCardNumber( "" );
        this.setBatteryLevel( (byte) 0 );
    }

    public static <T> PatrulMobileAppInfo generate ( final T object ) {
        return object instanceof Row
                ? new PatrulMobileAppInfo( (Row) object )
                : new PatrulMobileAppInfo( (UDTValue) object );
    }

    private PatrulMobileAppInfo ( final Row row ) {
        this.setBatteryLevel( row.getByte( "batteryLevel" ) );
        this.setPhoneNumber( row.getString( "phoneNumber" ) );
        this.setSimCardNumber( row.getString( "simCardNumber" ) );
    }

    private PatrulMobileAppInfo( final UDTValue udtValue ) {
        Optional.ofNullable( udtValue ).ifPresent( udtValue1 -> {
            this.setBatteryLevel( udtValue.getByte( "batteryLevel" ) );
            this.setPhoneNumber( udtValue.getString( "phoneNumber" ) );
            this.setSimCardNumber( udtValue.getString( "simCardNumber" ) );
        } );
    }
}
