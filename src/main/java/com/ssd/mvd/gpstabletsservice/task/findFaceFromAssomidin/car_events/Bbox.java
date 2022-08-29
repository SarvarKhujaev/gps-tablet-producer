package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bbox implements Serializable {

    @SerializedName("bottom")
    private int bottom;

    @SerializedName("top")
    private int top;

    @SerializedName("right")
    private int right;

    @SerializedName("left")
    private int left;

}