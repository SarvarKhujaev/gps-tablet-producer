package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.*;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.*;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.*;

import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetails {
    private String date;
    private String title;
    private String fabula;

    private ReportForCard reportForCardList;
    private List< PositionInfo > positionInfoList;

    public TaskDetails ( Card card, UUID patrulUUID ) {
        if ( card != null && patrulUUID != null ) {
            this.setTitle( card.getFabula() );
            this.setFabula( card.getFabula() );
            this.setDate( card.getCreated_date().toString() );
            CassandraDataControlForTasks
                    .getInstance()
                    .getPositionInfoList( card.getCardId().toString() )
                    .subscribe( this::setPositionInfoList );

            card.getReportForCardList()
                    .stream()
                    .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                    .forEach( this::setReportForCardList ); } }

    public TaskDetails( CarEvent carEvent, UUID patrulUUID ) {
        this.setDate( carEvent.getCreated_date() );
        CassandraDataControlForTasks
                .getInstance()
                .getPositionInfoList( carEvent.getId()  )
                .subscribe( this::setPositionInfoList );
        carEvent.getReportForCardList()
                .stream()
                .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                .forEach( this::setReportForCardList ); }

    public TaskDetails ( EventCar eventCar, UUID patrulUUID ) {
        if ( eventCar != null && patrulUUID != null ) {
            this.setDate( eventCar.getCreated_date().toString() );
            CassandraDataControlForTasks
                    .getInstance()
                    .getPositionInfoList( eventCar.getId() )
                    .subscribe( this::setPositionInfoList );
            eventCar.getReportForCardList()
                    .stream()
                    .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                    .forEach( this::setReportForCardList ); } }

    public TaskDetails ( EventBody eventBody, UUID patrulUUID ) {
        if ( eventBody != null && patrulUUID != null ) {
            this.setDate( eventBody.getCreated_date().toString() );
            CassandraDataControlForTasks
                    .getInstance()
                    .getPositionInfoList( eventBody.getId() )
                    .subscribe( this::setPositionInfoList );
            eventBody.getReportForCardList()
                    .stream()
                    .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                    .forEach( this::setReportForCardList ); } }

    public TaskDetails ( EventFace eventFace, UUID patrulUUID ) {
        this.setDate( eventFace.getCreated_date().toString() );
        CassandraDataControlForTasks
                .getInstance()
                .getPositionInfoList( eventFace.getId() )
                .subscribe( this::setPositionInfoList );
        eventFace.getReportForCardList()
                .stream()
                .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                .forEach( this::setReportForCardList ); }

    public TaskDetails ( FaceEvent faceEvent, UUID patrulUUID ) {
        this.setDate( faceEvent.getCreated_date() );
        CassandraDataControlForTasks
                .getInstance()
                .getPositionInfoList( faceEvent.getId() )
                .subscribe( this::setPositionInfoList );
        faceEvent.getReportForCardList()
                .stream()
                .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                .forEach( this::setReportForCardList ); }

    public TaskDetails ( SelfEmploymentTask selfEmploymentTask, UUID patrulUUID ) {
        this.setTitle( selfEmploymentTask.getTitle() );
        this.setFabula( selfEmploymentTask.getDescription() );
        this.setDate( selfEmploymentTask.getIncidentDate().toString() );
        CassandraDataControlForTasks
                .getInstance()
                .getPositionInfoList( selfEmploymentTask.getUuid().toString() )
                .subscribe( this::setPositionInfoList );
        selfEmploymentTask.getReportForCards()
                .stream()
                .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                .forEach( this::setReportForCardList ); }
}
