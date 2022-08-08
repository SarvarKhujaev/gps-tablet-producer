package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Car  {
	private Integer jpeg_quality;
	private Integer filter_max_size;
	private Integer filter_min_size;
	private Integer track_miss_interval;
	private Integer realtime_post_interval;
	private Integer track_max_duration_frames;

	private Boolean overall_only;
	private Boolean fullframe_use_png;
	private Boolean fullframe_crop_rot;
	private Boolean track_send_history;
	private Boolean post_best_track_frame;
	private Boolean post_last_track_frame;
	private Boolean post_first_track_frame;
	private Boolean track_interpolate_bboxes;
	private Boolean post_best_track_normalize;
	private Boolean realtime_post_every_interval;
	private Boolean realtime_post_first_immediately;
	private Boolean track_deep_sort_filter_unconfirmed_tracks;

	private String roi;
	private String tracker_type;

	private Double filter_min_quality;
	private Double track_overlap_threshold;
	private Double track_deep_sort_matching_threshold;
}