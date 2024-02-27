package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;
import java.util.Optional;

public final class PatrulCarInfo {
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

    private PatrulCarInfo () {
        this.setCarType( "" );
        this.setCarNumber( "" );
    }

    public static <T> PatrulCarInfo generate ( final T object ) {
        return object instanceof Row
                ? new PatrulCarInfo( (Row) object )
                : new PatrulCarInfo( (UDTValue) object );
    }

    private PatrulCarInfo ( final Row row ) {
        this.setCarType( row.getString( "carType" ) );
        this.setCarNumber( row.getString( "carNumber" ) );
    }

    private PatrulCarInfo( final UDTValue udtValue ) {
        Optional.ofNullable( udtValue ).ifPresent( udtValue1 -> {
            this.setCarType( udtValue.getString( "carType" ) );
            this.setCarNumber( udtValue.getString( "carNumber" ) );
        } );
    }
}
