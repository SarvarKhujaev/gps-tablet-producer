package com.ssd.mvd.gpstabletsservice.task.sos_task;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.datastax.driver.core.Row;
import java.util.*;

@lombok.Data
@lombok.NoArgsConstructor
public final class PatrulSos {
    private UUID uuid;
    private UUID patrulUUID;

    private String address;

    private Date sosWasSendDate; // сохраняет время когда запрос был отправлен
    private Date sosWasClosed; // время когда сос был закрыт

    private Double latitude;
    private Double longitude;

    private Status status = Status.CREATED;

    public Map< UUID, String > getPatrulStatuses() { return patrulStatuses != null ? patrulStatuses : new HashMap<>(); }

    private Map< UUID, String > patrulStatuses;

    public UUID getUuid () { return uuid != null ? uuid : ( uuid = UUID.randomUUID() ); }

    public PatrulSos ( final Row row ) { Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setUuid( row.getUUID( "uuid" ) );
            this.setPatrulUUID( row.getUUID( "patrulUUID" ) );

            this.setAddress( row.getString( "address" ) );

            this.setSosWasClosed( row.getTimestamp( "sosWasClosed" ) );
            this.setSosWasSendDate( row.getTimestamp( "sosWasSendDate" ) );

            this.setLatitude( row.getDouble( "latitude" ) );
            this.setLongitude( row.getDouble( "longitude" ) );

            this.setStatus( Status.valueOf( row.getString( "status" ) ) );
            this.setPatrulStatuses( row.getMap( "patrulStatuses", UUID.class, String.class ) ); } ); }
}
