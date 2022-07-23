package com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask;

import lombok.Data;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.constants.Status;

@Data
public class ActiveTask {
    private Double latitude;
    private Double longitude;

    private String type;
    private String address;
    private String description;

    private UUID uuid;
    private Long taskId;
    private Status status;
    private Date createdDate;

    private List< String > images;
    private List< Patrul > patrulList;

    public ActiveTask ( Card card ) {
        this.setType( "card" );
        this.setStatus( card.getStatus() );
        this.setTaskId( card.getCardId() );
        this.setAddress( card.getAddress() );
        this.setLatitude( card.getLatitude() );
        this.setPatrulList( card.getPatruls() );
        this.setDescription( card.getFabula() );
        this.setLongitude( card.getLongitude() );
        this.setCreatedDate( card.getCreated_date() ); }

    public ActiveTask ( SelfEmploymentTask card ) {
        this.setUuid( card.getUuid() );
        this.setType( "selfEmployment" );
        this.setAddress( card.getAddress() );
        this.setStatus( card.getTaskStatus() );
        this.setPatrulList( card.getPatruls() );
        this.setLatitude( card.getLatOfAccident() );
        this.setDescription( card.getDescription() );
        this.setLongitude( card.getLanOfAccident() );
        this.setCreatedDate( card.getIncidentDate() ); }
}
