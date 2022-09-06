package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.google.gson.annotations.SerializedName;
import lombok.extern.jackson.Jacksonized;
import java.util.List;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class First {
    @SerializedName("timestamp")
    private String timestamp;

    @SerializedName("bbox")
    private List< Integer > bbox;

    @SerializedName("quality")
    private Double quality;
}