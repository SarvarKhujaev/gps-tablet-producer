package com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask;

import java.util.Date;
import java.util.List;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import lombok.Data;

@Data
public class ActiveTask<T> {
    private Double latitude;
    private Double longitude;

    private String type;
    private String address;
    private String description;

    private T taskId;
    private Status status;
    private Date createdDate;

    private List< String > images;

    public ActiveTask ( Card card ) {
        this.setType( "card" );
        this.setStatus( card.getStatus() );
        this.setTaskId( (T) card.getCardId() );
        this.setLatitude( card.getLatitude() );
        this.setDescription( card.getFabula() );
        this.setLongitude( card.getLongitude() );
        this.setCreatedDate( card.getDateCreateCard() );
        this.setAddress( card.getEventAddress().getFlat() + " " + card.getEventAddress().getHouse() + " " + card.getEventAddress().getStreet() ); }

    public ActiveTask ( SelfEmploymentTask card ) {
        this.setType( "selfEmployment" );
        this.setTaskId( (T) card.getUuid() );
        this.setAddress( card.getAddress() );
        this.setStatus( card.getTaskStatus() );
        this.setLatitude( card.getLatOfAccident() );
        this.setDescription( card.getDescription() );
        this.setLongitude( card.getLanOfAccident() );
        this.setCreatedDate( card.getIncidentDate() ); }
}
