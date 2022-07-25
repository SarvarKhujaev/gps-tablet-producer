package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import lombok.Data;
import java.util.Map;
import java.util.Date;

@Data
public class EventCar {
    private Integer camera;
    private Boolean matched;
    private Date created_date;
    private Double confidence;

    private String id;
    private String fullframe;
    private String thumbnail;
    private String matched_dossier;

    private byte[] fullframebytes;
    private byte[] thumbnailbytes;

    private Map< String, String > body;
    private Map< String, String > make;
    private Map< String, String > color;
    private Map< String, String > model;
    private Map< String, String > license_plate_number;
    private Map< String, String > license_plate_region;
    private Map< String, String > license_plate_country;
}
