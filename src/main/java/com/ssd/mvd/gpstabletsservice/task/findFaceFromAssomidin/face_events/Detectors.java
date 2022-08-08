package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.Body;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.Car;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Detectors {
    private Face face;
    private Body body;
    private Car car;
}