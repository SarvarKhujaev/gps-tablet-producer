package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.annotations.SerializedName;
import lombok.extern.jackson.Jacksonized;
import java.util.List;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class Last {
    @SerializedName( "timestamp" )
    private String timestamp;

    @JsonDeserialize
    @SerializedName( "bbox" )
    private List< Integer > bbox;

    @SerializedName( "quality" )
    private Double quality;

}