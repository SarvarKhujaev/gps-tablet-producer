package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.jackson.Jacksonized;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class Track {
	@JsonDeserialize
	private Car car;

	private String first_timestamp;
	private String last_timestamp;
	private String id;
}