package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Statistic {
    private Integer job_starts;
    private Integer frame_width;
    private Integer faces_failed;
    private Integer faces_posted;
    private Integer frame_height;
    private Integer frames_dropped;
    private Integer processing_fps;
    private Integer faces_not_posted;
    private Integer frames_processed;
    private Integer processed_duration;
    private Integer decoding_soft_errors;
    private Integer last_stream_timestamp;
    private Integer frames_imotion_skipped;
}

