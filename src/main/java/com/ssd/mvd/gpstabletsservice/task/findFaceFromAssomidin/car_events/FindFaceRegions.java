package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class FindFaceRegions {
    @SerializedName( "address" )
    private String address;

    @SerializedName( "ip" )
    private String ip;

    @SerializedName( "objectId" )
    private String objectId;

    @SerializedName( "type" )
    private String type;

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