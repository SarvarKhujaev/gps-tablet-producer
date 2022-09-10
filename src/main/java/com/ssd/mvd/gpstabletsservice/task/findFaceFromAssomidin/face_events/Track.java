package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.google.gson.annotations.SerializedName;
import lombok.extern.jackson.Jacksonized;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class Track {
	@SerializedName( "id" )
	private String id;

	@SerializedName( "first_timestamp" )
	private String firstTimestamp;

	@SerializedName( "last_timestamp" )
	private String lastTimestamp;

	@JsonDeserialize
	@SerializedName( "face" )
	private Face face;
}