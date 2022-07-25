package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectorParams {
    private Track track;
    private Boolean end_of_track;

    private String cam_id;
    private String detection_id;

    private Integer quality;
    private Integer track_duration_seconds;
}