package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.google.gson.annotations.SerializedName;
import lombok.*;

import java.util.List;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Best {

    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("bbox")
    private List< Integer > bbox;

    @SerializedName("quality")
    private Object quality;

    @SerializedName("full_frame")
    private String fullFrame;

    @SerializedName("normalized")
    private String normalized;
}