package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.Row;
import java.util.List;
import java.util.UUID;

public final class AtlasLustra {
    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid ( final UUID uuid ) {
        this.uuid = uuid;
    }

    public String getLustraName() {
        return this.lustraName;
    }

    public void setLustraName ( final String lustraName ) {
        this.lustraName = lustraName;
    }

    public String getCarGosNumber() {
        return this.carGosNumber;
    }

    public void setCarGosNumber ( final String carGosNumber ) {
        this.carGosNumber = carGosNumber;
    }

    public List<CameraList> getCameraLists() {
        return this.cameraLists;
    }

    public void setCameraLists ( final List< CameraList > cameraLists ) {
        this.cameraLists = cameraLists;
    }

    private UUID uuid;
    private String lustraName;
    private String carGosNumber; // choosing from dictionary

    private List< CameraList > cameraLists; // Camera rtsp link with Pass&login

    public AtlasLustra ( final Row row ) {
        this.setUuid( row.getUUID("uuid") );
        this.setLustraName( row.getString("lustraName" ) );
        this.setCarGosNumber( row.getString("carGosNumber") );
        this.setCameraLists( row.getList( "cameraLists", CameraList.class ) );
    }
}
