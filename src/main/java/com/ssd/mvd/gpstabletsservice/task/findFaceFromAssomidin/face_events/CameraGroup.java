package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import lombok.extern.jackson.Jacksonized;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
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

    @JsonDeserialize
    private Labels labels;
    @JsonDeserialize
    private Permissions permissions;
}