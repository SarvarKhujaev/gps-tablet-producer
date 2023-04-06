package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class TintedGlass {
    @JsonProperty("TintinType")
    private String TintinType;

    @JsonProperty("DateBegin")
    private String DateBegin;

    @JsonProperty("DateValid")
    private String DateValid;
}
