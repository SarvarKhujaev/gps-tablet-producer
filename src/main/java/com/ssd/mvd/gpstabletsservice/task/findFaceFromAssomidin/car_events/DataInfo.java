package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public final class DataInfo {
    @SerializedName( "code" )
    private int code;

    @SerializedName( "message" )
    private String message;

    @JsonDeserialize
    @SerializedName( "cadaster" )
    private FindFaceRegions cadaster;
}