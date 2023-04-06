package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForFindFace;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class DetectorParams {
    private Double track_duration_seconds;
    private Boolean endOf_track;
    private String detection_id;
    private String cam_id;
    private Track track;
}