package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class Statistic  {
	private Integer jobStarts;
	private Integer frameWidth;
	private Integer facesFailed;
	private Integer facesPosted;
	private Integer frameHeight;
	private Integer processingFps;
	private Integer framesDropped;
	private Integer facesNotPosted;
	private Integer framesProcessed;
	private Integer processedDuration;
	private Integer decodingSoftErrors;
	private Integer lastStreamTimestamp;
	private Integer framesImotionSkipped;
}