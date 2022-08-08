package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.task.card.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.PsychologyCard;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class FaceEvents {
    private String id;
    private String name;
    private String comment;
    private String bs_type;
    private String thumbnail;
    private String fullframe;
    private String temperature;
    private String webhook_type;
    private String created_date;
    private String dossier_photo;
    private String matched_object;
    private String video_archive;
    private String acknowledged_by;
    private String event_model_class;
    private String acknowledged_date;
    private String acknowledged_reaction;

    private Camera camera;
    private Features features;
    private CameraGroup camera_group;
    private DetectorParams detector_params;
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

    @JsonDeserialize
    private PsychologyCard psychologyCard;

    private Integer episode;
    private Integer looks_like_confidence;

    private Status status;

    @JsonDeserialize
    private Map< String, Patrul> patruls = new HashMap<>(); // the list of patruls who linked to this event
    @JsonDeserialize
    private List<ReportForCard> reportForCardList = new ArrayList<>(); // the list of reports for the current card
    @JsonDeserialize
    private Map< String, PatrulStatus> patrulStatuses = new HashMap<>(); // the final status with info the time and Statuses
}