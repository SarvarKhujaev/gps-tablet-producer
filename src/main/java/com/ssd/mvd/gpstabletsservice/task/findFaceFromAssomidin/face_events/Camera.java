package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.extern.jackson.Jacksonized;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class Camera {
    private Integer group;
    private Integer car_count;
    private Integer face_count;
    private Integer body_count;
    @JsonProperty("id")
    private Integer camera_id;

    private String url;
    private String name;
    private String comment;
    private String azimuth;
    private String onvif_epr;
    private String screenshot;
    private String created_date;
    private String onvif_camera;
    private String modified_date;
    private String car_threshold;
    private String face_threshold;
    private String body_threshold;
    private String external_detector_token;

    private Boolean active;
    private Boolean single_pass;
    private Boolean external_detector;

    @JsonDeserialize
    private HealthStatus health_status;
    @JsonDeserialize
    private StreamSettings stream_settings;

    private Double latitude;
    private Double longitude;
}