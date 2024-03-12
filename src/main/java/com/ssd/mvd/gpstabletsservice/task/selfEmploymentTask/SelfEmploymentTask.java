package com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask;

import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.SELF_EMPLOYMENT;
import com.ssd.mvd.gpstabletsservice.interfaces.TaskCommonMethods;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskCommonParams;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.*;

public final class SelfEmploymentTask implements TaskCommonMethods< SelfEmploymentTask > {
    public double getLanOfPatrul() {
        return this.lanOfPatrul;
    }

    public double getLatOfPatrul() {
        return this.latOfPatrul;
    }

    public double getLanOfAccident() {
        return this.lanOfAccident;
    }

    public double getLatOfAccident() {
        return this.latOfAccident;
    }

    public String getTitle() {
        return this.title;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress( final String address ) {
        this.address = address;
    }

    public String getDescription() {
        return this.description;
    }

    public Date getIncidentDate() {
        return this.incidentDate;
    }

    public TaskCommonParams getTaskCommonParams() {
        return this.taskCommonParams;
    }

    private double lanOfPatrul; // in case if the accident is at Patrul place. then lan lat will be the same
    private double latOfPatrul;
    private double lanOfAccident;
    private double latOfAccident;

    private String title; // title of incident
    private String address; // the address of incident
    private String description; // info about incident

    private Date incidentDate; // the date when the task was created

    private final TaskCommonParams taskCommonParams = TaskCommonParams.generate(
            SELF_EMPLOYMENT,
            null
    );

    @JsonDeserialize
    private List< String > images;

    @Override
    public double getLatitude() {
        return this.getLatOfPatrul();
    }

    @Override
    public double getLongitude() {
        return this.getLanOfPatrul();
    }

    @Override
    public SelfEmploymentTask update ( final ReportForCard reportForCard ) {
        this.getTaskCommonParams().getReportForCardList().add( reportForCard );
        return this;
    }
}
