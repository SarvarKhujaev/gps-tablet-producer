package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.Row;
import java.util.List;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class AtlasLustra {
    private UUID uuid;

    private String lustraName;
    private String carGosNumber; // choosing from dictionary

    private List< CameraList > cameraLists; // Camera rtsp link with Pass&login

    public AtlasLustra( Row row ) {
        this.setUuid( row.getUUID("uuid") );
        this.setLustraName( row.getString("lustraName" ) );
        this.setCarGosNumber( row.getString("carGosNumber") );
        this.setCameraLists( row.getList( "cameraLists", CameraList.class ) ); }

    public UUID getUUID () { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }
}
