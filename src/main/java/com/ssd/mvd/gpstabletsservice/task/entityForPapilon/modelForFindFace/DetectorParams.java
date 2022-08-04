package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForFindFace;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetectorParams {
    private Double track_duration_seconds;
    private Boolean endOf_track;
    private String detection_id;
    private String cam_id;
    private Track track;
}