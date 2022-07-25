package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Statistic  {
	private Integer jobStarts;
	private Integer frameWidth;
	private Integer facesFailed;
	private Integer frameHeight;
	private Integer facesPosted;
	private Integer processingFps;
	private Integer framesDropped;
	private Integer facesNotPosted;
	private Integer framesProcessed;
	private Integer processedDuration;
	private Integer decodingSoftErrors;
	private Integer lastStreamTimestamp;
	private Integer framesImotionSkipped;
}