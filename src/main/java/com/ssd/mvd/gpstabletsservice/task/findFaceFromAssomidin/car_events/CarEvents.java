package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import lombok.*;
import java.util.List;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.Camera;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.CameraGroup;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.MatchedListsItem;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarEvents  {
	private Camera camera;
	private Features features;
	private CameraGroup camera_group;
	private DetectorParams detector_params;

	private Boolean matched;
	private Boolean acknowledged;

	private String id;
	private String bs_type;
	private String fullframe;
	private String thumbnail;
	private String webhook_type;
	private String created_date;
	private String matched_object;
	private String acknowledged_date;
	private String event_model_class;
	private String acknowledged_reaction;

	private Double quality;
	private List< MatchedListsItem > matched_lists;

	private Integer confidence;
	private Integer frame_coords_top;
	private Integer frame_coords_left;
	private Integer frame_coords_right;
	private Integer frame_coords_bottom;
}