package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.gson.annotations.SerializedName;
import lombok.extern.jackson.Jacksonized;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
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