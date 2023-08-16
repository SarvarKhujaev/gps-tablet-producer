package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public final class FindFaceRegions {
    @SerializedName( "address" )
    private String address;

    @SerializedName( "ip" )
    private String ip;

    private String camera_name;

    @SerializedName( "region" )
    private Integer region;

    @SerializedName( "findFaceId" )
    private Integer findFaceId;

    @SerializedName( "district" )
    private Integer district;

    @SerializedName( "countryside" )
    private Integer countryside;

    @SerializedName( "latitude" )
    private Double latitude;

    @SerializedName( "longitude" )
    private Double longitude;
}