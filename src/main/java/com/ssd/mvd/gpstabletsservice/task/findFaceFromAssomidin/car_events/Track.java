package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Track  {
	@JsonDeserialize
	private Car car;
	private String id;
	private String last_timestamp;
	private String first_timestamp;
}