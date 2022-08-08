package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Features {
    private Beard beard;
    private Gender gender;
    private Glasses glasses;
    private Medmask medmask;
    private Emotions emotions;

    private Integer age;
    private String liveness;
}