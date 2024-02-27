package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import java.util.Optional;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;

public final class PatrulFIOData {
    public String getName() {
        return this.name;
    }

    public void setName( final String name ) {
        this.name = name;
    }

    public String getSurname() {
        return this.surname;
    }

    public void setSurname( final String surname ) {
        this.surname = surname;
    }

    public String getFatherName() {
        return this.fatherName;
    }

    public void setFatherName( final String fatherName ) {
        this.fatherName = fatherName;
    }

    public String getSurnameNameFatherName () {
        return Optional.ofNullable( this.surnameNameFatherName )
                .filter( s -> this.surnameNameFatherName.contains("NULL") )
                .orElse( ( this.surnameNameFatherName = DataValidateInspector
                        .getInstance()
                        .concatNames( this ) ) );
    }

    public void setSurnameNameFatherName ( final String surnameNameFatherName ) {
        this.surnameNameFatherName = surnameNameFatherName;
    }

    private String name;
    private String surname;
    private String fatherName;
    private String surnameNameFatherName; // Ф.И.О

    public static <T> PatrulFIOData generate ( final T object ) {
        return object instanceof Row
                ? new PatrulFIOData( (Row) object )
                : new PatrulFIOData( (UDTValue) object );
    }

    private PatrulFIOData ( final Row row ) {
        this.setName( row.getString( "name" ) );
        this.setSurname( row.getString( "surname" ) );
        this.setFatherName( row.getString( "fatherName" ) );
        this.setSurnameNameFatherName( row.getString( "surnameNameFatherName" ) );
    }

    private PatrulFIOData( final UDTValue udtValue ) {
        this.setName( udtValue.getString( "name" ) );
        this.setSurname( udtValue.getString( "surname" ) );
        this.setFatherName( udtValue.getString( "fatherName" ) );
        this.setSurnameNameFatherName( udtValue.getString( "surnameNameFatherName" ) );
    }
}
