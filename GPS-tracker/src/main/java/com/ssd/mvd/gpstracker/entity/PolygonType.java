package com.ssd.mvd.gpstracker.entity;

import lombok.Data;

import java.util.UUID;

@Data
public class PolygonType {
    private UUID uuid;
    private String name;

    public UUID getUuid () { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }
}
