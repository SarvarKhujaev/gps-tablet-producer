package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.google.gson.annotations.SerializedName;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car  {
	@SerializedName("first")
	private First first;

	@SerializedName("last")
	private Last last;

	@SerializedName("best")
	private Best best;
}