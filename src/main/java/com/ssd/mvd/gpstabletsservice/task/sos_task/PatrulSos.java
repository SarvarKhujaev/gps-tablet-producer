package com.ssd.mvd.gpstabletsservice.task.sos_task;

import com.datastax.driver.core.Row;
import java.util.Date;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatrulSos {
    private String address;
    private UUID patrulUUID;
    private Date sosWasSendDate; // созраняет время когда запрос был отправлен

    private Double latitude;
    private Double longitude;

    public PatrulSos ( Row row ) {
        this.setPatrulUUID( row.getUUID( "patrulUUID" ) );
        this.setSosWasSendDate( row.getTimestamp( "sosWasSendDate" ) );

        this.setLatitude( row.getDouble( "latitude" ) );
        this.setLongitude( row.getDouble( "longitude" ) ); }
}
