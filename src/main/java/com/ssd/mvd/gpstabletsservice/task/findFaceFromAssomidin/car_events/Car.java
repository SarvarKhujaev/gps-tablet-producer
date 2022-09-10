package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.extern.jackson.Jacksonized;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class Car  {
	@JsonDeserialize
	@SerializedName("first")
	private First first;

	@JsonDeserialize
	@SerializedName("last")
	private Last last;

	@JsonDeserialize
	@SerializedName("best")
	private Best best;
}