package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StreamSettings {
    private String rot;
    private String ffmpeg_format;
    private String video_transform;
    private String stream_data_filter;
    private List< String > ffmpeg_params;

    private Detectors detectors;

    private Integer play_speed;
    private Integer router_timeout_ms;
    private Integer imotion_threshold;
    private Integer start_stream_timestamp;

    private Boolean disable_drops;
    private Boolean router_verify_ssl;
    private Boolean use_stream_timestamp;
}