package com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask;

import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SelfEmploymentTask {
    private Double lanOfPatrul; // in case if the accident is at Patrul place. then lan lat will be the same
    private Double latOfPatrul;
    private Double lanOfAccident;
    private Double latOfAccident;

    private String title; // title of incident
    private String address; // the address of incident
    private String taskStatus; // might be just arrived or finished
    private String description; // info about incident

    private UUID uuid;
    private Date incidentDate; // the date when the task was created
    private Date arrivedTime; // фиксировванное время когда он прибыл на дело

    private List< String > images;
    private List< String > patruls;
    private List< ReportForCard > reportForCards;

    public void clear() {
        this.setUuid( null );
        this.getImages().clear();
        this.getPatruls().clear();
        this.getReportForCards().clear(); }
}
