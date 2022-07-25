package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraGroup {
    private Boolean active;
    private Boolean deduplicate;

    private String name;
    private String comment;
    private String created_date;
    private String modified_date;

    private Labels labels;
    private Permissions permissions;

    private Integer id;
    private Integer deduplicate_delay;
}