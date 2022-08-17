package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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