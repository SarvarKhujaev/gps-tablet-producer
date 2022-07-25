package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Last {
    private Integer quality;
    private String timestamp;
    private List< Integer > bbox;
}