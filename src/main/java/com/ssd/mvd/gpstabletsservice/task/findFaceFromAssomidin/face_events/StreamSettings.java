package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonDeserialize;
import lombok.extern.jackson.Jacksonized;
import java.util.List;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class StreamSettings {
    @JsonDeserialize
    private Detectors detectors;

    private Boolean disable_drops;
    private Boolean router_verify_ssl;
    private Boolean use_stream_timestamp;

    private String rot;
    private String ffmpeg_format;
    private String video_transform;
    private String stream_data_filter;
    @JsonDeserialize
    private List< String > ffmpeg_params;

    private Integer play_speed;
    private Integer imotion_threshold;
    private Integer router_timeout_ms;
    private Integer start_stream_timestamp;
}