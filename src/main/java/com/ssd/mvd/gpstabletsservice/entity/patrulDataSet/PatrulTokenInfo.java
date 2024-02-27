package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;
import java.util.Optional;

public final class PatrulTokenInfo {
    public String getSpecialToken() {
        return this.specialToken;
    }

    public void setSpecialToken( final String specialToken ) {
        this.specialToken = specialToken;
    }

    public String getTokenForLogin() {
        return this.tokenForLogin;
    }

    public void setTokenForLogin( final String tokenForLogin) {
        this.tokenForLogin = tokenForLogin;
    }

    private String specialToken;
    private String tokenForLogin;

    public static PatrulTokenInfo empty () {
        return new PatrulTokenInfo();
    }

    private PatrulTokenInfo () {
        this.setSpecialToken( "" );
        this.setTokenForLogin( "" );
    }

    public static <T> PatrulTokenInfo generate ( final T object ) {
        return object instanceof Row
                ? new PatrulTokenInfo( (Row) object )
                : new PatrulTokenInfo( (UDTValue) object );
    }

    private PatrulTokenInfo ( final Row row ) {
        this.setSpecialToken( row.getString( "specialToken" ) );
        this.setTokenForLogin( row.getString( "tokenForLogin" ) );
    }

    private PatrulTokenInfo( final UDTValue udtValue ) {
        Optional.ofNullable( udtValue ).ifPresent( udtValue1 -> {
            this.setSpecialToken( udtValue.getString( "specialToken" ) );
            this.setTokenForLogin( udtValue.getString( "tokenForLogin" ) );
        } );
    }
}
