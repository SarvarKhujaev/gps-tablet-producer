package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Features {
    private Body body;
    private Make make;
    private Model model;
    private Color color;
    private LicensePlateRegion license_plate_region;
    private LicensePlateNumber license_plate_number;
    private LicensePlateCountry license_plate_country;
}