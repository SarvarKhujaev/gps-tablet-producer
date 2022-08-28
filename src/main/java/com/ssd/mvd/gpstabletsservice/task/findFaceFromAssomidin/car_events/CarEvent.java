package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class CarEvent {

    @JsonProperty("episode")
    private String episode;

    @JsonProperty("matched_object")
    private String matched_object;

    @JsonProperty("matched_card")
    private Long matched_dossier;

    @JsonProperty("matched_cluster")
    private String matched_cluster;

    @JsonProperty("created_date")
    private String created_date;

    @JsonProperty("camera")
    private Integer camera_id;

    @JsonProperty("camera_group")
    private Integer camera_group;

    @JsonProperty("thumbnail")
    private String thumbnail;

    @JsonProperty("fullframe")
    private String fullframe;

    @JsonProperty("bs_type")
    private String bs_type;

    @JsonProperty("frame_coords_left")
    private Integer frame_coords_left;

    @JsonProperty("frame_coords_top")
    private Integer frame_coords_top;

    @JsonProperty("frame_coords_right")
    private Integer frame_coords_right;

    @JsonProperty("frame_coords_bottom")
    private Integer frame_coords_bottom;

    @JsonProperty("matched")
    private Boolean matched;

    @JsonProperty("matched_lists")
    private List<Integer> matched_lists;

    @JsonProperty("confidence")
    private Integer confidence;

    @JsonProperty("cluster_confidence")
    private Integer cluster_confidence;

    @JsonProperty("quality")
    private Double quality;

//    private TintedGlass tintedGlass;

    @JsonProperty("detector_params")
    private DetectorParams detector_params;

    @JsonProperty("acknowledged_date")
    private String acknowledged_date;

    @JsonProperty("acknowledged_by")
    private String acknowledged_by;

    @JsonProperty("acknowledged_reaction")
    private String acknowledged_reaction;

    @JsonProperty("acknowledged")
    private Boolean acknowledged;

    @JsonProperty("video_archive")
    private String video_archive;

    @JsonProperty("id")
    private String id;

    @JsonProperty("features")
    private Features features;

    @JsonProperty("looks_like_confidence")
    private String looks_like_confidence;

    @JsonProperty("object_type")
    private String object_type;

    @JsonProperty("webhook_type")
    private String webhook_type;

    @JsonProperty("event_model_class")
    private String event_model_class;
}