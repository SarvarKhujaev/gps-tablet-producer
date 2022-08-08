package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Track {
    private String firstTimestamp;
    private String lastTimestamp;
    private String id;
    private Face face;
}