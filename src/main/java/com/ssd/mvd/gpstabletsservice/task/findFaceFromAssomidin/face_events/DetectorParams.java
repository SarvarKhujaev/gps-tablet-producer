package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.extern.jackson.Jacksonized;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class DetectorParams {
    private String cam_id;
    private String detection_id;

    private Integer quality;
    private Integer track_duration_seconds;

    @JsonDeserialize
    private Track track;
    private Boolean end_of_track;
}