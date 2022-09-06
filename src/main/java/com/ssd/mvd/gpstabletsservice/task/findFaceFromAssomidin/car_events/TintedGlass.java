package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TintedGlass {
    @JsonProperty("TintinType")
    private String TintinType;

    @JsonProperty("DateBegin")
    private String DateBegin;

    @JsonProperty("DateValid")
    private String DateValid;
}
