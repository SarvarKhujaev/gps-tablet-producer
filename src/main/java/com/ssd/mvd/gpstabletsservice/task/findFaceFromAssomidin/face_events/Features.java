package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Features {
    private Integer age;
    private Beard beard;
    private Gender gender;
    private Medmask medmask;
    private Glasses glasses;
    private Emotions emotions;
}