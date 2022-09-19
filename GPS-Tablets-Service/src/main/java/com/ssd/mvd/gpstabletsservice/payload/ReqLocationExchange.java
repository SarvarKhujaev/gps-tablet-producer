package com.ssd.mvd.gpstabletsservice.payload;

import com.datastax.driver.core.Row;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReqLocationExchange {
    private String date;
    private Double lan;
    private Double lat;

    public ReqLocationExchange ( Row row ) {
        this.setLat( row.getDouble( "latitude" ) );
        this.setLan( row.getDouble( "longitude" ) );
        this.setDate( String.valueOf( row.getTimestamp( "date" ) ) ); }
}
