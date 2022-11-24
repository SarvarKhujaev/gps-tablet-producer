package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.*;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.*;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.*;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskDetails {
    private String date;
    private String title;
    private String fabula;

    private Long timeWastedToArrive;
    private Long totalTimeConsumption;

    private ReportForCard reportForCardList;
    private List< PositionInfo > positionInfoList;

    public TaskDetails ( Card card, UUID patrulUUID ) {
        if ( card != null && patrulUUID != null ) {
            this.setTitle( card.getFabula() );
            this.setFabula( card.getFabula() );
            this.setDate( card.getCreated_date().toString() );
            CassandraDataControlForTasks
                    .getInstance()
                    .getGetPositionInfoList()
                    .apply( card.getCardId().toString(), patrulUUID )
                    .subscribe( taskTotalData -> {
                        this.setPositionInfoList( taskTotalData.getPositionInfoList() );
                        this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
                        this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() ); } );

            card.getReportForCardList()
                    .parallelStream()
                    .parallel()
                    .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                    .forEach( this::setReportForCardList ); } }

    public TaskDetails( CarEvent carEvent, UUID patrulUUID ) {
        this.setDate( carEvent.getCreated_date() );
        CassandraDataControlForTasks
                .getInstance()
                .getGetPositionInfoList()
                .apply( carEvent.getId(), patrulUUID )
                .subscribe( taskTotalData -> {
                    this.setPositionInfoList( taskTotalData.getPositionInfoList() );
                    this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
                    this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() ); } );
        carEvent.getReportForCardList()
                .parallelStream()
                .parallel()
                .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                .forEach( this::setReportForCardList ); }

    public TaskDetails ( EventCar eventCar, UUID patrulUUID ) {
        if ( eventCar != null && patrulUUID != null ) {
            this.setDate( eventCar.getCreated_date().toString() );
            CassandraDataControlForTasks
                    .getInstance()
                    .getGetPositionInfoList()
                    .apply( eventCar.getId(), patrulUUID )
                    .subscribe( taskTotalData -> {
                        this.setPositionInfoList( taskTotalData.getPositionInfoList() );
                        this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
                        this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() ); } );
            eventCar.getReportForCardList()
                    .parallelStream()
                    .parallel()
                    .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                    .forEach( this::setReportForCardList ); } }

    public TaskDetails ( EventBody eventBody, UUID patrulUUID ) {
        if ( eventBody != null && patrulUUID != null ) {
            this.setDate( eventBody.getCreated_date().toString() );
            CassandraDataControlForTasks
                    .getInstance()
                    .getGetPositionInfoList()
                    .apply( eventBody.getId(), patrulUUID )
                    .subscribe( taskTotalData -> {
                        this.setPositionInfoList( taskTotalData.getPositionInfoList() );
                        this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
                        this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() ); } );
            eventBody.getReportForCardList()
                    .parallelStream()
                    .parallel()
                    .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                    .forEach( this::setReportForCardList ); } }

    public TaskDetails ( EventFace eventFace, UUID patrulUUID ) {
        this.setDate( eventFace.getCreated_date().toString() );
        CassandraDataControlForTasks
                .getInstance()
                .getGetPositionInfoList()
                .apply( eventFace.getId(), patrulUUID )
                .subscribe( taskTotalData -> {
                    this.setPositionInfoList( taskTotalData.getPositionInfoList() );
                    this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
                    this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() ); } );
        eventFace.getReportForCardList()
                .parallelStream()
                .parallel()
                .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                .forEach( this::setReportForCardList ); }

    public TaskDetails ( FaceEvent faceEvent, UUID patrulUUID ) {
        this.setDate( faceEvent.getCreated_date() );
        CassandraDataControlForTasks
                .getInstance()
                .getGetPositionInfoList()
                .apply( faceEvent.getId(), patrulUUID )
                .subscribe( taskTotalData -> {
                    this.setPositionInfoList( taskTotalData.getPositionInfoList() );
                    this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
                    this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() ); } );
        faceEvent.getReportForCardList()
                .parallelStream()
                .parallel()
                .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                .forEach( this::setReportForCardList ); }

    public TaskDetails ( SelfEmploymentTask selfEmploymentTask, UUID patrulUUID ) {
        this.setTitle( selfEmploymentTask.getTitle() );
        this.setFabula( selfEmploymentTask.getDescription() );
        this.setDate( selfEmploymentTask.getIncidentDate().toString() );
        CassandraDataControlForTasks
                .getInstance()
                .getGetPositionInfoList()
                .apply( selfEmploymentTask.getUuid().toString(), patrulUUID )
                .subscribe( taskTotalData -> {
                    this.setPositionInfoList( taskTotalData.getPositionInfoList() );
                    this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
                    this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() ); } );
        selfEmploymentTask.getReportForCards()
                .parallelStream()
                .parallel()
                .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                .forEach( this::setReportForCardList ); }
}
