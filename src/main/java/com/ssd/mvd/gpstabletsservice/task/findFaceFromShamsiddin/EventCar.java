package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.task.card.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;

import lombok.Data;
import java.util.Map;
import java.util.List;
import java.util.Date;

@Data
public class EventCar {
    private Status status;
    private Integer camera;
    private Boolean matched;
    private Date created_date;

    private Double latitude;
    private Double longitude;
    private Double confidence;

    private String id;
    private String address; // coming from front end
    private String cameraIp; // coming from front end
    private String fullframe;
    private String thumbnail;
    private String matched_dossier;

    private byte[] fullframebytes;
    private byte[] thumbnailbytes;

    private CarTotalData carTotalData;

    @JsonDeserialize
    private Map< String, String > body;
    @JsonDeserialize
    private Map< String, String > make;
    @JsonDeserialize
    private Map< String, String > color;
    @JsonDeserialize
    private Map< String, String > model;
    @JsonDeserialize
    private Map< String, String > license_plate_number;
    @JsonDeserialize
    private Map< String, String > license_plate_region;
    @JsonDeserialize
    private Map< String, String > license_plate_country;

    @JsonDeserialize
    private Map< String, Patrul> patruls; // the list of patruls who linked to this event
    @JsonDeserialize
    private List<ReportForCard> reportForCardList; // the list of reports for the current card
    @JsonDeserialize
    private Map< String, PatrulStatus> patrulStatuses; // the final status with info the time and Statuses
}
