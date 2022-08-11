package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.PsychologyCard;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.card.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;

import lombok.extern.jackson.Jacksonized;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.util.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class EventFace {
    private Long age;
    private Status status;
    private Integer camera;
    private Boolean matched;
    private Date created_date;

    private Double latitude;
    private Double longitude;
    private Double confidence;

    private byte[] fullframebytes;
    private byte[] thumbnailbytes;

    private String id;
    private String address; // coming from front end
    private String cameraIp; // coming from front end
    private String fullframe;
    private String thumbnail;
    private String matched_dossier;

    @JsonDeserialize
    private PsychologyCard psychologyCard;

    @JsonDeserialize
    private Map< String, String > beard;
    @JsonDeserialize
    private Map< String, String > gender;
    @JsonDeserialize
    private Map< String, String > glasses;
    @JsonDeserialize
    private Map< String, String > medmask;

    @JsonDeserialize
    private Map< String, Patrul> patruls = new HashMap<>(); // the list of patruls who linked to this event
    @JsonDeserialize
    private List<ReportForCard> reportForCardList = new ArrayList<>(); // the list of reports for the current card
    @JsonDeserialize
    private Map< String, PatrulStatus> patrulStatuses = new HashMap<>(); // the final status with info the time and Statuses
}
