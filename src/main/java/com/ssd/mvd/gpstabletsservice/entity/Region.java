package com.ssd.mvd.gpstabletsservice.entity;

import lombok.Data;

@Data
public class Region {
    private Long regionId;
    private Long mahallaId;
    private Long districtId; // tuman

    private String regionName;
    private String mahallaName;
    private String districtName;
}
