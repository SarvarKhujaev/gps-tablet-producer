package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.PsychologyCard;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.card.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.extern.jackson.Jacksonized;
import lombok.*;

import java.util.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class FaceEvents {
    private String id;
    private String name;
    private String bs_type;
    private String comment;
    private String thumbnail;
    private String fullframe;
    private String temperature;
    private String created_date;
    private String webhook_type;
    private String video_archive;
    private String dossier_photo;
    private String matched_object;
    private String acknowledged_by;
    private String acknowledged_date;
    private String event_model_class;
    private String acknowledged_reaction;

    private Integer episode;
    private Integer confidence;
    private Integer frame_coords_top;
    private Integer frame_coords_left;
    private Integer frame_coords_right;
    private Integer frame_coords_bottom;
    private Integer looks_like_confidence;

    private Double quality;
    private Long matched_dossier;

    private Boolean matched;
    private Boolean acknowledged;

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

    private Status status = Status.CREATED;
    @JsonDeserialize
    private PsychologyCard psychologyCard;

    @com.fasterxml.jackson.databind.annotation.JsonDeserialize
    private Map< UUID, Patrul > patruls = new HashMap<>(); // the list of patruls who linked to this event
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize
    private List<ReportForCard> reportForCardList = new ArrayList<>(); // the list of reports for the current card
    @com.fasterxml.jackson.databind.annotation.JsonDeserialize
    private Map< String, PatrulStatus> patrulStatuses = new HashMap<>(); // the final status with info the time and Statuses
}