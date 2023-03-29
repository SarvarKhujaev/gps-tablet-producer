package com.ssd.mvd.gpstabletsservice.request;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.datastax.driver.core.Row;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class AndroidVersionUpdate {
    private String link;
    private String version;
    private Status status;

    public AndroidVersionUpdate( Row row, Status status ) {
        this.setStatus( status );
        this.setLink( row.getString( "link" ) );
        this.setVersion( row.getString( "version" ) ); }
}
