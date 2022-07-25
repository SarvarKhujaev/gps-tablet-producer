package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicensePlateNumber  {
	private Integer confidence;
	private String name;
	private Bbox bbox;
}