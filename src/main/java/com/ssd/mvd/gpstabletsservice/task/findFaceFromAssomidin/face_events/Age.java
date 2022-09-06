package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.google.gson.annotations.SerializedName;
import lombok.extern.jackson.Jacksonized;

import java.io.Serializable;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class Age implements Serializable {
    @SerializedName( "name" )
    private String name;

    @SerializedName( "confidence" )
    private Integer confidence;
}