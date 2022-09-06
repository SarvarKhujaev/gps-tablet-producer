package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import lombok.extern.jackson.Jacksonized;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class LicensePlateNumber  {
	private Double confidence;
	private String name;
	private Bbox bbox;
}