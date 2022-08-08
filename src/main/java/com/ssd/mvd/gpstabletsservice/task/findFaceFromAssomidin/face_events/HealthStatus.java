package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class HealthStatus  {
	private String msg;
	private String code;
	private String status;
	private String code_desc;

	private Boolean enabled;
	private Statistic statistic;
}