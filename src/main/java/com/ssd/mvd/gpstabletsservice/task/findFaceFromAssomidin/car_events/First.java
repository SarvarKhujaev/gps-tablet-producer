package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class First  {
	private List<Integer> bbox;
	private String timestamp;
	private Integer quality;
}