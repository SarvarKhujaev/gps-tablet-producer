package com.ssd.mvd.gpstracker.entity;

import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class AtlasLustra {
    private UUID uuid;
    private String lustraName;
    private String carGosNumber; // choosing from dictionary
    private List< CameraList > cameraLists; // Camera rtsp link with Pass&login

    public UUID getUUID () { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }
}
