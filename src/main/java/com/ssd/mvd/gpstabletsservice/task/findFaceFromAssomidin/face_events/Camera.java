package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Camera {
    private Double latitude;
    private Double longitude;

    private String url;
    private String name;
    private String comment;
    private String screenshot;
    private String created_date;
    private String modified_date;

    private HealthStatus health_status;
    private StreamSettings stream_settings;

    private Integer group;
    @JsonProperty("id")
    private Integer camera_id;
    private Integer car_count;
    private Integer body_count;
    private Integer face_count;

    private Boolean active;
    private Boolean single_pass;
    private Boolean external_detector;
}