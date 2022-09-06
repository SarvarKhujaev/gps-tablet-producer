package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.extern.jackson.Jacksonized;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class DetectorParams {
	private String cam_id;
	private String detection_id;

	private Boolean end_of_track;
	private Integer track_duration_seconds;

	@JsonDeserialize
	private Track track;
}