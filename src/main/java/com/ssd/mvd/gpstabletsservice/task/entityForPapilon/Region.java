package com.ssd.mvd.gpstabletsservice.task.entityForPapilon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Region {
    private Long regionId;
    private Long mahallaId;
    private Long districtId; // tuman
    private String regionName;
    private String mahallaName;
    private String districtName;
}
