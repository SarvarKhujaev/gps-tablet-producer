package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import lombok.extern.jackson.Jacksonized;
import java.util.List;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class MatchedListsItem  {
    private String name;
    private String color;
    private String comment;
    private String created_date;
    private String car_threshold;
    private String modified_date;
    private String face_threshold;
    private String body_threshold;

    private Boolean active;
    private Boolean notify;
    private Boolean acknowledge;
    private Boolean ignore_events;

    @JsonDeserialize
    private Permissions permissions;

    private Integer id;
    @JsonDeserialize
    private List< Integer > camera_groups;
}