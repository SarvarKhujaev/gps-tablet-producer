package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectorParams {
    private Track track;
    private Boolean end_of_track;
    private Integer track_duration_seconds;

    private String cam_id;
    private String detection_id;
}