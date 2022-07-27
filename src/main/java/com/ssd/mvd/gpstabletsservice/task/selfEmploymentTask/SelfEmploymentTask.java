package com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties ( ignoreUnknown = true )
public class SelfEmploymentTask {
    private Double lanOfPatrul; // in case if the accident is at Patrul place. then lan lat will be the same
    private Double latOfPatrul;
    private Double lanOfAccident;
    private Double latOfAccident;

    private String title; // title of incident
    private String address; // the address of incident
    private Status taskStatus; // might be just arrived or finished
    private String description; // info about incident

    private UUID uuid;
    private Date arrivedTime; // фиксировванное время когда он прибыл на дело
    private Date incidentDate; // the date when the task was created

    private List< String > images;
    @JsonDeserialize
    private Map< String, Patrul > patruls;
    @JsonDeserialize
    private List< ReportForCard > reportForCards;
}
