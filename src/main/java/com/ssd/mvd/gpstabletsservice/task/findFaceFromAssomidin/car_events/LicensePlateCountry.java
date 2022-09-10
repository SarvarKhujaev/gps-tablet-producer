package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class LicensePlateCountry {
    private Double confidence;
    private String name;
}