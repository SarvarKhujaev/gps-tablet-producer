package com.ssd.mvd.gpstabletsservice.task.sos_task;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.extern.jackson.Jacksonized;
import java.util.List;

@lombok.Data
@Jacksonized
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class Address {
    private Long osm_id;
    private Long place_id;

    private Double lat;
    private Double lon;
    private Double importance;

    private String type;
    private String licence;
    private String osm_type;
    private String display_name;

    @JsonDeserialize
    private List< String > boundingbox;
    @JsonDeserialize
    private com.ssd.mvd.gpstabletsservice.task.sos_task.Data address;
}
