package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.extern.jackson.Jacksonized;
import java.util.List;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class Best  {
	@SerializedName("timestamp")
	private String timestamp;

	@SerializedName("bbox")
	private List<Integer> bbox;

	@SerializedName("quality")
	private Double quality;

	@SerializedName("full_frame")
	private String fullFrame;

	@SerializedName("normalized")
	private String normalized;
}