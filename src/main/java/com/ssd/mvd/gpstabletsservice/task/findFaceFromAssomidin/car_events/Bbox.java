package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bbox {
    private Integer top;
    private Integer left;
    private Integer right;
    private Integer bottom;
}