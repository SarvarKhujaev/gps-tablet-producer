package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Permissions {
    @JsonProperty("1")
    private String jsonMember1;
    @JsonProperty("2")
    private String jsonMember2;
    @JsonProperty("3")
    private String jsonMember3;
}