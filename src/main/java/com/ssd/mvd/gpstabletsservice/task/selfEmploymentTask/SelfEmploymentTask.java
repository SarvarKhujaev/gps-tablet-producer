package com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask;

import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.constants.Status;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.*;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class SelfEmploymentTask {
    private Double lanOfPatrul; // in case if the accident is at Patrul place. then lan lat will be the same
    private Double latOfPatrul;
    private Double lanOfAccident;
    private Double latOfAccident;

    private String title; // title of incident
    private String address; // the address of incident
    private String description; // info about incident

    private UUID uuid;
    private Status taskStatus; // might be just arrived or attached
    private Date incidentDate; // the date when the task was created

    public UUID getUuid() { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }

    @JsonDeserialize
    private List< String > images;
    @JsonDeserialize
    private List< ReportForCard > reportForCards = new ArrayList<>();

    @JsonDeserialize
    private Map< UUID, Patrul > patruls = new HashMap<>();
    @JsonDeserialize
    private Map< String, PatrulStatus > patrulStatuses = new HashMap<>(); // the final status with info the time and Statuses
}
