package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Bbox {
    private Integer top;
    private Integer left;
    private Integer bottom;
    private Integer right;
}