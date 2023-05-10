package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import java.util.*;
import lombok.extern.jackson.Jacksonized;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PatrulStatus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@lombok.Data
@Jacksonized
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class EventCar {
    private Status status = Status.CREATED;
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

    @JsonDeserialize
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

    private UUID uuid;

    public UUID getUUID () { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }

    @JsonDeserialize
    private Map< UUID, Patrul > patruls = new HashMap<>(); // the list of patruls who linked to this event
    @JsonDeserialize
    private List< ReportForCard > reportForCardList = new ArrayList<>(); // the list of reports for the current card
    @JsonDeserialize
    private Map< String, PatrulStatus > patrulStatuses = new HashMap<>(); // the final status with info the time and Statuses
}
