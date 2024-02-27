package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForFindFace;

import java.util.List;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class PreferenceItem {
    private Double quality;
    private Double confidence;

    private Boolean matched;
    private Boolean acknowledged;

    private Features features;
    private DetectorParams detector_params;

    private List< Integer > video_archive;
    private List< Integer > matched_lists;

    private String id;
    private String bs_type;
    private String thumbnail;
    private String fullframe;
    private String looks_like;
    private String created_date;
    private String webhook_type;
    private String matched_object;
    private String event_model_class;
    private String acknowledged_date;
    private String acknowledged_reaction;

    private Integer episode;
    private Integer camera;
    private Integer camera_group;
    private Integer acknowledged_by;
    private Integer matched_dossier;
    private Integer frame_coords_top;
    private Integer frame_coords_left;
    private Integer frame_coords_right;
    private Integer frame_coords_bottom;
}