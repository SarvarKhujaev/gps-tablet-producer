package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.google.gson.annotations.SerializedName;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Last {
    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("bbox")
    private List<Integer> bbox;

    @SerializedName("quality")
    private Double quality;
}