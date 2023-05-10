package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.*;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.*;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.*;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.card.Card;

import java.util.List;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class TaskDetails {
    private String date;
    private String title;
    private String fabula;

    private Long timeWastedToArrive;
    private Long totalTimeConsumption;

    private ReportForCard reportForCardList;
    private List< PositionInfo > positionInfoList;

    public TaskDetails ( final Card card, final UUID patrulUUID, final TaskTotalData taskTotalData ) {
        if ( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( card )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( patrulUUID ) ) {
            this.setTitle( card.getFabula() );
            this.setFabula( card.getFabula() );
            this.setDate( card.getCreated_date().toString() );
            this.setPositionInfoList( taskTotalData.getPositionInfoList() );
            this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
            this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() );

            card.getReportForCardList()
                    .parallelStream()
                    .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                    .forEach( this::setReportForCardList ); } }

    public TaskDetails( final CarEvent carEvent, final UUID patrulUUID, final TaskTotalData taskTotalData ) {
        this.setDate( carEvent.getCreated_date() );
        this.setPositionInfoList( taskTotalData.getPositionInfoList() );
        this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
        this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() );
        carEvent.getReportForCardList()
                .parallelStream()
                .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                .forEach( this::setReportForCardList ); }

    public TaskDetails ( final EventCar eventCar, final UUID patrulUUID, final TaskTotalData taskTotalData ) {
        if ( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( eventCar )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( patrulUUID ) ) {
            this.setDate( eventCar.getCreated_date().toString() );
            this.setPositionInfoList( taskTotalData.getPositionInfoList() );
            this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
            this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() );
            eventCar.getReportForCardList()
                    .parallelStream()
                    .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                    .forEach( this::setReportForCardList ); } }

    public TaskDetails ( final EventBody eventBody, final UUID patrulUUID, final TaskTotalData taskTotalData ) {
        if ( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( eventBody )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( patrulUUID ) ) {
            this.setDate( eventBody.getCreated_date().toString() );
            this.setPositionInfoList( taskTotalData.getPositionInfoList() );
            this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
            this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() );
            eventBody.getReportForCardList()
                    .parallelStream()
                    .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                    .forEach( this::setReportForCardList ); } }

    public TaskDetails ( final EventFace eventFace, final UUID patrulUUID, final TaskTotalData taskTotalData ) {
        this.setDate( eventFace.getCreated_date().toString() );
        this.setPositionInfoList( taskTotalData.getPositionInfoList() );
        this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
        this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() );
        eventFace.getReportForCardList()
                .parallelStream()
                .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                .forEach( this::setReportForCardList ); }

    public TaskDetails ( final FaceEvent faceEvent, final UUID patrulUUID, final TaskTotalData taskTotalData ) {
        this.setDate( faceEvent.getCreated_date() );
        this.setPositionInfoList( taskTotalData.getPositionInfoList() );
        this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
        this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() );
        faceEvent.getReportForCardList()
                .parallelStream()
                .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                .forEach( this::setReportForCardList ); }

    public TaskDetails ( final SelfEmploymentTask selfEmploymentTask, final UUID patrulUUID, final TaskTotalData taskTotalData ) {
        this.setTitle( selfEmploymentTask.getTitle() );
        this.setFabula( selfEmploymentTask.getDescription() );
        this.setDate( selfEmploymentTask.getIncidentDate().toString() );
        this.setPositionInfoList( taskTotalData.getPositionInfoList() );
        this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
        this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() );
        selfEmploymentTask.getReportForCards()
                .parallelStream()
                .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                .forEach( this::setReportForCardList ); }
}
