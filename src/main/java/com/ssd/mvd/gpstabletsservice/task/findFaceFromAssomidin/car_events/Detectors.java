package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.Face;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Detectors  {
	private Car car;
	private Face face;
	private Body body;
}