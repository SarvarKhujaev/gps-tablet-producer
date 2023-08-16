package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public final class FindFaceRegions {
    @SerializedName( value = "address" )
    private String address;

    @SerializedName( value = "ip" )
    private String ip;

    private String camera_name;

    @SerializedName( value = "region" )
    private Integer region;

    @SerializedName( value = "findFaceId" )
    private Integer findFaceId;

    @SerializedName( value = "district" )
    private Integer district;

    @SerializedName( value = "countryside" )
    private Integer countryside;

    @SerializedName( value = "latitude" )
    private Double latitude;

    @SerializedName( value = "longitude" )
    private Double longitude;
}