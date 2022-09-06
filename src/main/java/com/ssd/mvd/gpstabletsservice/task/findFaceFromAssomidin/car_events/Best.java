package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.annotations.SerializedName;
import lombok.extern.jackson.Jacksonized;
import java.util.List;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
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