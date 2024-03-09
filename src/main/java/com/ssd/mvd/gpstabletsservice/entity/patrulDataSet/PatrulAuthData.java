package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;

import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;

public final class PatrulAuthData extends DataValidateInspector implements ObjectCommonMethods< PatrulAuthData > {
    public String getLogin() {
        return this.login;
    }

    public void setLogin( final String login ) {
        this.login = login;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword( final String password ) {
        this.password = password;
    }

    private String login;
    private String password;

    public void setInitialPasswordAndLogin ( final Patrul patrul ) {
        if ( this.getLogin() == null ) {
            this.setLogin( patrul.getPassportNumber() );
        }
        if ( this.getPassword() == null ) {
            this.setPassword( patrul.getPassportNumber() );
        }
    }

    public static PatrulAuthData empty () {
        return new PatrulAuthData();
    }

    private PatrulAuthData () {}

    @Override
    public PatrulAuthData generate( final Row row ) {
        this.setPassword( row.getString( "password" ) );
        this.setLogin( row.getString( "login" ) );

        return this;
    }

    @Override
    public PatrulAuthData generate( final UDTValue udtValue ) {
        super.checkAndSetParams(
                udtValue,
                udtValue1 -> {
                    this.setLogin( udtValue.getString( "login" ) );
                    this.setPassword( udtValue.getString( "password" ) );
                }
        );

        return this;
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setString( "login", this.getLogin() )
                .setString( "password", this.getPassword() );
    }
}
