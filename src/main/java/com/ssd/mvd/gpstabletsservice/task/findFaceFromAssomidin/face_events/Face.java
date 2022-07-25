package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Face {
    private String roi;
    private String trackerType;

    private Integer jpegQuality;
    private Integer filterMaxSize;
    private Integer filterMinSize;
    private Integer trackMissInterval;
    private Integer realtimePostInterval;
    private Integer trackMaxDurationFrames;

    private Object filterMinQuality;
    private Object trackOverlapThreshold;
    private Object trackDeepSortMatchingThreshold;

    private Boolean overallOnly;
    private Boolean fullframeUsePng;
    private Boolean fullframeCropRot;
    private Boolean trackSendHistory;
    private Boolean postBestTrackFrame;
    private Boolean postLastTrackFrame;
    private Boolean postFirstTrackFrame;
    private Boolean postBestTrackNormalize;
    private Boolean trackInterpolateBboxes;
    private Boolean realtimePostEveryInterval;
    private Boolean realtimePostFirstImmediately;
    private Boolean trackDeepSortFilterUnconfirmedTracks;
}