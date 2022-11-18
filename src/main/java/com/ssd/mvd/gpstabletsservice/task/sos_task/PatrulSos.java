package com.ssd.mvd.gpstabletsservice.task.sos_task;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.datastax.driver.core.Row;

import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.UUID;
import java.util.Map;

@Data
@NoArgsConstructor
public class PatrulSos {
    private UUID uuid;
    private UUID patrulUUID;

    private String address;

    private Date sosWasSendDate; // созраняет время когда запрос был отправлен
    private Date sosWasClosed; // время когда сос был отменен

    private Double latitude;
    private Double longitude;

    private Status status = Status.CREATED;
    private Map< UUID, String > patrulStatuses;

    public UUID getUuid () { return uuid != null ? uuid : ( uuid = UUID.randomUUID() ); }

    public PatrulSos ( Row row ) {
        this.setUuid( row.getUUID( "uuid" ) );
        this.setPatrulUUID( row.getUUID( "patrulUUID" ) );

        this.setAddress( row.getString( "address" ) );

        this.setSosWasClosed( row.getTimestamp( "sosWasClosed" ) );
        this.setSosWasSendDate( row.getTimestamp( "sosWasSendDate" ) );

        this.setLatitude( row.getDouble( "latitude" ) );
        this.setLongitude( row.getDouble( "longitude" ) );

        this.setStatus( Status.valueOf( row.getString( "status" ) ) );
        this.setPatrulStatuses( row.getMap( "patrulStatuses", UUID.class, String.class ) ); }
}
