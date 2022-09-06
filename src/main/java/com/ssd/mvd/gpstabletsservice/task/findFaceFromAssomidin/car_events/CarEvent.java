package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.card.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.task.card.VictimHumans;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import lombok.extern.jackson.Jacksonized;
import java.util.*;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class CarEvent {
    private String name;
    private String comment;
    private String dossier_photo;

    @JsonProperty( "id" )
    private String id;

    @JsonProperty( "acknowledged_date" )
    private String acknowledged_date;

    @JsonProperty("video_archive")
    private String video_archive;

    @JsonProperty("looks_like_confidence")
    private String looks_like_confidence;

    @JsonProperty("object_type")
    private String object_type;

    @JsonProperty("webhook_type")
    private String webhook_type;

    @JsonProperty("event_model_class")
    private String event_model_class;

    @JsonProperty( "acknowledged_by" )
    private String acknowledged_by;

    @JsonProperty("acknowledged_reaction")
    private String acknowledged_reaction;

    @JsonProperty("episode")
    private String episode;

    @JsonProperty("matched_object")
    private String matched_object;

    @JsonProperty( "thumbnail" )
    private String thumbnail;

    @JsonProperty( "fullframe" )
    private String fullframe;

    @JsonProperty("bs_type")
    private String bs_type;

    @JsonProperty("matched_cluster")
    private String matched_cluster;

    @JsonProperty("created_date")
    private String created_date;

    @JsonProperty("camera")
    private Integer camera_id;

    @JsonProperty("camera_group")
    private Integer camera_group;

    @JsonProperty("frame_coords_left")
    private Integer frame_coords_left;

    @JsonProperty("frame_coords_top")
    private Integer frame_coords_top;

    @JsonProperty("frame_coords_right")
    private Integer frame_coords_right;

    @JsonProperty("frame_coords_bottom")
    private Integer frame_coords_bottom;

    @JsonProperty("confidence")
    private Integer confidence;

    @JsonProperty("cluster_confidence")
    private Integer cluster_confidence;

    @JsonDeserialize
    @JsonProperty( "matched_lists" )
    private List< Integer > matched_lists;

    @JsonProperty( "matched_card" )
    private Long matched_dossier;

    @JsonProperty( "quality" )
    private Double quality;

    @JsonProperty( "matched" )
    private Boolean matched;

    @JsonProperty( "acknowledged" )
    private Boolean acknowledged;

    @JsonDeserialize
    private TintedGlass tintedGlass;

    @JsonDeserialize
    private DataInfo dataInfo;

    @JsonDeserialize
    @JsonProperty( "features" )
    private Features features;

    @JsonDeserialize
    @JsonProperty( "detector_params" )
    private DetectorParams detector_params;

    private Status status = Status.CREATED;
    @JsonDeserialize
    private CarTotalData carTotalData;

    @JsonDeserialize
    private Map<UUID, Patrul> patruls = new HashMap<>(); // the list of patruls who linked to this event
    @JsonDeserialize
    private List<VictimHumans> victimHumans = new ArrayList<>();  // Jabirlanuchi inson
    @JsonDeserialize
    private List<ReportForCard> reportForCardList = new ArrayList<>(); // the list of reports for the current card
    @JsonDeserialize
    private Map< String, PatrulStatus> patrulStatuses = new HashMap<>(); // the final status with info the time and Statuses
}