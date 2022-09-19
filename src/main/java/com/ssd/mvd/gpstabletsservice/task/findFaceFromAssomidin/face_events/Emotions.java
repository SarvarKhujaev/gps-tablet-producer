package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class Emotions {
    private Double confidence;
    private String name;
}