package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.task.card.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.Camera;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.CameraGroup;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.MatchedListsItem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class CarEvents  {
	private String id;
	private String bs_type;
	private String fullframe;
	private String thumbnail;
	private String webhook_type;
	private String created_date;
	private String video_archive;
	private String matched_object;
	private String acknowledged_by;
	private String acknowledged_date;
	private String event_model_class;
	private String acknowledged_reaction;

	@JsonDeserialize
	private Camera camera;
	@JsonDeserialize
	private Features features;
	@JsonDeserialize
	private CameraGroup camera_group;
	@JsonDeserialize
	private DetectorParams detector_params;
	@JsonDeserialize
	private List< MatchedListsItem > matched_lists;

	private Integer confidence;
	private Integer frame_coords_top;
	private Integer frame_coords_left;
	private Integer frame_coords_right;
	private Integer frame_coords_bottom;

	private Double quality;
	private Long matched_dossier;

	private Boolean matched;
	private Boolean acknowledged;

	private Status status;

	@com.fasterxml.jackson.databind.annotation.JsonDeserialize
	private Map< String, Patrul> patruls = new HashMap<>(); // the list of patruls who linked to this event
	@com.fasterxml.jackson.databind.annotation.JsonDeserialize
	private List<ReportForCard> reportForCardList = new ArrayList<>(); // the list of reports for the current card
	@com.fasterxml.jackson.databind.annotation.JsonDeserialize
	private Map< String, PatrulStatus> patrulStatuses = new HashMap<>(); // the final status with info the time and Statuses
}