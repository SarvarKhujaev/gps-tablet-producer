package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.google.gson.annotations.SerializedName;
import lombok.extern.jackson.Jacksonized;
import lombok.*;

import java.io.Serializable;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class DetectorParams implements Serializable {

    @SerializedName("quality")
    private String quality;

    @SerializedName("liveness_score")
    private String livenessScore;

    @SerializedName("track_duration_seconds")
    private Integer trackDurationSeconds;

    @SerializedName("cam_id")
    private String camId;

    @SerializedName("end_of_track")
    private Boolean endOfTrack;

    @JsonDeserialize
    @SerializedName("track")
    private Track track;

    @SerializedName("detection_id")
    private String detectionId;
}