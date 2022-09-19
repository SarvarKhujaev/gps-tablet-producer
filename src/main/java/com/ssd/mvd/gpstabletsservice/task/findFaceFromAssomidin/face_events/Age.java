package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class Age implements Serializable {
    @SerializedName( "name" )
    private String name;

    @SerializedName( "confidence" )
    private Double confidence;
}