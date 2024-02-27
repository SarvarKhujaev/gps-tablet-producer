package com.ssd.mvd.gpstabletsservice.request;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.datastax.driver.core.Row;

public class AndroidVersionUpdate {
    public String getLink() {
        return this.link;
    }

    public void setLink ( final String link ) {
        this.link = link;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion ( final String version ) {
        this.version = version;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus ( final Status status ) {
        this.status = status;
    }

    private String link;
    private String version;
    private Status status;

    public static AndroidVersionUpdate generate ( final Row row, final Status status ) {
        return new AndroidVersionUpdate( row, status );
    }

    private AndroidVersionUpdate( final Row row, final Status status ) {
        this.setVersion( row.getString( "version" ) );
        this.setLink( row.getString( "link" ) );
        this.setStatus( status );
    }
}
