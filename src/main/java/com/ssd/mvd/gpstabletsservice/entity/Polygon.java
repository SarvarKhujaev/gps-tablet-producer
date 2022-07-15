package com.ssd.mvd.gpstabletsservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Polygon {
    private UUID uuid;
    private UUID organ;

    private Long regionId;
    private Long mahallaId;
    private Long districtId; // tuman

    private String name;
    private PolygonType polygonType;

    private List< Patrul > patrulList; // the list of all Patruls who works at this polygon
    private List< PolygonEntity > latlngs;

    public UUID getUuid () { return this.uuid != null ? uuid : ( this.uuid = UUID.randomUUID() ); }
}
