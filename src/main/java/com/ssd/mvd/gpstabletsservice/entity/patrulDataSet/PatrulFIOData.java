package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import java.util.Optional;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;

import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;

public final class PatrulFIOData extends DataValidateInspector implements ObjectCommonMethods< PatrulFIOData > {
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
                .orElse( ( this.surnameNameFatherName = super.concatNames( this ) ) );
    }

    public void setSurnameNameFatherName (
            final String surnameNameFatherName
    ) {
        this.surnameNameFatherName = surnameNameFatherName;
    }

    private String name;
    private String surname;
    private String fatherName;
    private String surnameNameFatherName; // Ф.И.О

    private PatrulFIOData () {}

    public static PatrulFIOData empty() {
        return new PatrulFIOData();
    }

    @Override
    public PatrulFIOData generate( final Row row ) {
        this.setSurnameNameFatherName( row.getString( "surnameNameFatherName" ) );
        this.setFatherName( row.getString( "fatherName" ) );
        this.setSurname( row.getString( "surname" ) );
        this.setName( row.getString( "name" ) );

        return this;
    }

    @Override
    public PatrulFIOData generate( final UDTValue udtValue ) {
        this.setSurnameNameFatherName( udtValue.getString( "surnameNameFatherName" ) );
        this.setFatherName( udtValue.getString( "fatherName" ) );
        this.setSurname( udtValue.getString( "surname" ) );
        this.setName( udtValue.getString( "name" ) );

        return this;
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setString( "name", this.getName() )
                .setString( "surname", this.getSurname() )
                .setString( "fatherName", this.getFatherName() )
                .setString( "surnameNameFatherName", this.getSurnameNameFatherName() );
    }
}
