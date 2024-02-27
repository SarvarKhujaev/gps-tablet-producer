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
    private int region;

    @SerializedName( value = "findFaceId" )
    private int findFaceId;

    @SerializedName( value = "district" )
    private int district;

    @SerializedName( value = "countryside" )
    private int countryside;

    @SerializedName( value = "latitude" )
    private double latitude;

    @SerializedName( value = "longitude" )
    private double longitude;
}