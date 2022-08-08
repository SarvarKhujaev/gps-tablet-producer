package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraGroup {
    private Integer id;
    private Integer deduplicate_delay;

    private String name;
    private String comment;
    private String created_date;
    private String modified_date;
    private String car_threshold;
    private String face_threshold;
    private String body_threshold;

    private Boolean active;
    private Boolean deduplicate;

    private Labels labels;
    private Permissions permissions;
}