package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.google.gson.annotations.SerializedName;
import lombok.extern.jackson.Jacksonized;
import java.util.List;

@lombok.Data
@Jacksonized
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class Best {
    @SerializedName("timestamp")
    private String timestamp;

    @JsonDeserialize
    @SerializedName("bbox")
    private List< Integer > bbox;

    @JsonDeserialize
    @SerializedName("quality")
    private Object quality;

    @SerializedName("full_frame")
    private String fullFrame;

    @SerializedName("normalized")
    private String normalized;
}