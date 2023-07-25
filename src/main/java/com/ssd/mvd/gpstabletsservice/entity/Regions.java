package com.ssd.mvd.gpstabletsservice.entity;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class Regions {
    private String message;
    private Boolean success;
    @JsonDeserialize
    private List< RegionData > resData;
}
