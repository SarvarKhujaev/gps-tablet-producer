package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.extern.jackson.Jacksonized;

@lombok.Data
@Jacksonized
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class Face {
    @JsonDeserialize
    @SerializedName( "first" )
    private First first;

    @JsonDeserialize
    @SerializedName( "last" )
    private Last last;

    @JsonDeserialize
    @SerializedName( "best" )
    private Best best;
}