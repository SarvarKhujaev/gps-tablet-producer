package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;

import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;

public final class PatrulCarInfo extends DataValidateInspector implements ObjectCommonMethods< PatrulCarInfo > {
    public String getCarType() {
        return this.carType;
    }

    public void setCarType( final String carType ) {
        this.carType = carType;
    }

    public String getCarNumber() {
        return this.carNumber;
    }

    public void setCarNumber( final String carNumber ) {
        this.carNumber = carNumber;
    }

    private String carType; // модель машины
    private String carNumber;

    public static PatrulCarInfo empty () {
        return new PatrulCarInfo();
    }

    private PatrulCarInfo () {}

    private PatrulCarInfo ( final Row row ) {
        this.setCarType( row.getString( "carType" ) );
        this.setCarNumber( row.getString( "carNumber" ) );
    }

    private PatrulCarInfo( final UDTValue udtValue ) {
        super.checkAndSetParams(
                udtValue,
                udtValue1 -> {
                    this.setCarType( udtValue.getString( "carType" ) );
                    this.setCarNumber( udtValue.getString( "carNumber" ) );
                }
        );
    }

    @Override
    public PatrulCarInfo generate( final Row row ) {
        this.setCarType( row.getString( "carType" ) );
        this.setCarNumber( row.getString( "carNumber" ) );
        return this;
    }

    @Override
    public PatrulCarInfo generate( final UDTValue udtValue ) {
        super.checkAndSetParams(
                udtValue,
                udtValue1 -> {
                    this.setCarType( udtValue.getString( "carType" ) );
                    this.setCarNumber( udtValue.getString( "carNumber" ) );
                }
        );
        return this;
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setString( "carType", this.getCarType() )
                .setString( "carNumber", this.getCarNumber() );
    }
}
