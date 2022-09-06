package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import lombok.*;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class LicensePlateCountry {
    private Double confidence;
    private String name;
}