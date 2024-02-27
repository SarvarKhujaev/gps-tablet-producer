package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import java.util.Optional;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.UDTValue;

public final class PatrulAuthData {
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

    public static <T> PatrulAuthData generate ( final T object ) {
        return object instanceof Row
                ? new PatrulAuthData( (Row) object )
                : new PatrulAuthData( (UDTValue) object );
    }

    private PatrulAuthData ( final Row row ) {
        this.setLogin( row.getString( "login" ) );
        this.setPassword( row.getString( "password" ) );
    }

    private PatrulAuthData( final UDTValue udtValue ) {
        Optional.ofNullable( udtValue ).ifPresent( udtValue1 -> {
            this.setLogin( udtValue.getString( "login" ) );
            this.setPassword( udtValue.getString( "password" ) );
        } );
    }
}
