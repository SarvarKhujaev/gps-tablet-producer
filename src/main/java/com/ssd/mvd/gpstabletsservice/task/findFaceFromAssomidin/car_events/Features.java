package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.jackson.Jacksonized;

@lombok.Data
@Jacksonized
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class Features {
    @JsonDeserialize
    private Body body;

    @JsonDeserialize
    private Make make;

    @JsonDeserialize
    private Color color;

    @JsonDeserialize
    private Model model;

    @JsonDeserialize
    private LicensePlateNumber license_plate_number;

    @JsonDeserialize
    private LicensePlateRegion license_plate_region;

    @JsonDeserialize
    private LicensePlateCountry license_plate_country;
}