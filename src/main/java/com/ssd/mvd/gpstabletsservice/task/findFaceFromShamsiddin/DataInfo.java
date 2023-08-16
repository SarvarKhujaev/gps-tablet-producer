package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import com.google.gson.annotations.SerializedName;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.FindFaceRegions;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public final class DataInfo {
    @JsonDeserialize
    @SerializedName( "cadaster" )
    private FindFaceRegions data;
}