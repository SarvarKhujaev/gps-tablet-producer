package com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask;

import com.datastax.driver.core.Row;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.*;

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
    private String description; // info about incident

    private UUID uuid;
    private Status taskStatus; // might be just arrived or finished
    private Date incidentDate; // the date when the task was created

    public UUID getUuid() { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }

    private List< String > images;
    @JsonDeserialize
    private Map< UUID, Patrul > patruls = new HashMap<>();
    @JsonDeserialize
    private List< ReportForCard > reportForCards = new ArrayList<>();

    public SelfEmploymentTask( Row row ) {
        this.setLanOfPatrul( row.getDouble( "lanOfPatrul" ) );
        this.setLatOfPatrul( row.getDouble( "latOfPatrul" ) );
        this.setLanOfAccident( row.getDouble( "lanOfAccident" ) );
        this.setLatOfAccident( row.getDouble( "latOfAccident" ) );

        this.setTitle( row.getString( "title" ) );
        this.setAddress( row.getString( "address" ) );
        this.setDescription( row.getString( "description" ) );

        this.setUuid( row.getUUID( "uuid" ) );
        this.setIncidentDate( row.getTimestamp( "incidentDate" ) );
        this.setTaskStatus( Status.valueOf( row.getString( "taskStatus" ) ) );

        this.setImages( row.getList( "images", String.class ) );
        this.setPatruls( row.getMap( "patruls", UUID.class, Patrul.class ) );
        this.setReportForCards( row.getList( "reportForCards", ReportForCard.class ) ); }
}
