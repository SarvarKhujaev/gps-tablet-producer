package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.PsychologyCard;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.card.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;

import java.util.List;
import java.util.Date;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventBody {
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

    private PsychologyCard psychologyCard;

    @JsonDeserialize
    private Map< String, String > headwear;
    @JsonDeserialize
    private Map< String, String > top_color;
    @JsonDeserialize
    private Map< String, String > bottom_color;
    @JsonDeserialize
    private Map< String, String > lower_clothes;
    @JsonDeserialize
    private Map< String, String > upper_clothes;
    @JsonDeserialize
    private Map< String, String > detailed_upper_clothes;

    @JsonDeserialize
    private Map< String, Patrul > patruls; // the list of patruls who linked to this event
    @JsonDeserialize
    private List< ReportForCard > reportForCardList; // the list of reports for the current card
    @JsonDeserialize
    private Map< String, PatrulStatus > patrulStatuses; // the final status with info the time and Statuses

}
