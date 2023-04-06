package com.ssd.mvd.gpstabletsservice.task.entityForPapilon;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class Region {
    private Long regionId;
    private Long mahallaId;
    private Long districtId; // tuman
    private String regionName;
    private String mahallaName;
    private String districtName;
}
