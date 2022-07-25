package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchedListsItem  {

    private String name;
    private String color;
    private String comment;
    private String created_date;
    private String modified_date;

    private Integer id;
    private Permissions permissions;
    private List< Integer > camera_groups;

    private Boolean active;
    private Boolean notify;
    private Boolean acknowledge;
    private Boolean ignore_events;
}