package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class Features {
    @JsonDeserialize
    private Beard beard;
    @JsonDeserialize
    private Gender gender;
    @JsonDeserialize
    private Medmask medmask;
    @JsonDeserialize
    private Glasses glasses;
    @JsonDeserialize
    private Emotions emotions;

    private Integer age;
    private String liveness;
}