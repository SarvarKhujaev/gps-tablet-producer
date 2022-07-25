package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Best  {
	private String fullFrame;
	private String timestamp;
	private String normalized;

	private Integer quality;
	private List< Integer > bbox;
}