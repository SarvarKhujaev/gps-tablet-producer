package com.ssd.mvd.gpstabletsservice.entity;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class RegionData {
    private Long id;
    private String name;
    private Double latitude;
    private Double longitude;
    private Boolean hasPolygon;
}
