package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.task.card.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.Camera;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.CameraGroup;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.MatchedListsItem;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.extern.jackson.Jacksonized;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class CarEvents  {
	private String id;
	private String bs_type;
	private String fullframe;
	private String thumbnail;
	private String created_date;
	private String webhook_type;
	private String video_archive;
	private String matched_object;
	private String acknowledged_by;
	private String acknowledged_date;
	private String event_model_class;
	private String acknowledged_reaction;

	private Camera camera;
	private Features features;
	private CameraGroup camera_group;
	private DetectorParams detector_params;
	private List< MatchedListsItem > matched_lists;

	private Boolean matched;
	private Boolean acknowledged;

	private Integer confidence;
	private Integer frame_coords_top;
	private Integer frame_coords_left;
	private Integer frame_coords_right;
	private Integer frame_coords_bottom;

	private Status status;
	private Double quality;
	private Long matched_dossier;

	@JsonDeserialize
	private Map< String, Patrul> patruls = new HashMap<>(); // the list of patruls who linked to this event
	@JsonDeserialize
	private List<ReportForCard> reportForCardList = new ArrayList<>(); // the list of reports for the current card
	@JsonDeserialize
	private Map< String, PatrulStatus> patrulStatuses = new HashMap<>(); // the final status with info the time and Statuses
}