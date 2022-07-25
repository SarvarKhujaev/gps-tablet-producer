package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Track {
    private Face face;
    private String id;
    private String lastTimestamp;
    private String firstTimestamp;
}