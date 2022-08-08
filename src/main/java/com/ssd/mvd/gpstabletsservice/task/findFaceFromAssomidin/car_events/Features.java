package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import lombok.*;
import lombok.extern.jackson.Jacksonized;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class Features {
    @JsonDeserialize
    private Body body;
    @JsonDeserialize
    private Make make;
    @JsonDeserialize
    private Model model;
    @JsonDeserialize
    private Color color;
    @JsonDeserialize
    private LicensePlateRegion license_plate_region;
    @JsonDeserialize
    private LicensePlateNumber license_plate_number;
    @JsonDeserialize
    private LicensePlateCountry license_plate_country;
}