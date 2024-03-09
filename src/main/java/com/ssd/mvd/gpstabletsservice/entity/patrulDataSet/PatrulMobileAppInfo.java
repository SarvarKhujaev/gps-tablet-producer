package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

public final class PatrulMobileAppInfo extends DataValidateInspector implements ObjectCommonMethods< PatrulMobileAppInfo > {
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

    public PatrulMobileAppInfo setInitialValues () {
        this.setBatteryLevel( (byte) 0 );
        this.setSimCardNumber( "" );
        this.setPhoneNumber( "" );

        return this;
    }

    public static PatrulMobileAppInfo empty() {
        return new PatrulMobileAppInfo();
    }

    private PatrulMobileAppInfo () {}

    @Override
    public PatrulMobileAppInfo generate( final Row row ) {
        this.setSimCardNumber( row.getString( "simCardNumber" ) );
        this.setBatteryLevel( row.getByte( "batteryLevel" ) );
        this.setPhoneNumber( row.getString( "phoneNumber" ) );

        return this;
    }

    @Override
    public PatrulMobileAppInfo generate( final UDTValue udtValue ) {
        super.checkAndSetParams(
                udtValue,
                udtValue1 -> {
                    this.setBatteryLevel( udtValue.getByte( "batteryLevel" ) );
                    this.setPhoneNumber( udtValue.getString( "phoneNumber" ) );
                    this.setSimCardNumber( udtValue.getString( "simCardNumber" ) );
                }
        );

        return this;
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setString( "phoneNumber", this.getPhoneNumber() )
                .setString( "simCardNumber", this.getSimCardNumber() )
                .setByte( "batteryLevel", this.getBatteryLevel() );
    }
}
