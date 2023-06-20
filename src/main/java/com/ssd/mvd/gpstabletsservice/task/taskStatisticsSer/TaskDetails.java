package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.*;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.*;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.*;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.task.card.Card;

import java.util.List;
import java.util.UUID;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class TaskDetails {
    private String date;
    private String title;
    private String fabula; // описания задачи

    private Long timeWastedToArrive; // время которое патрульный потратил чтобы дойти до цели
    private Long totalTimeConsumption; // общее потраченное время на задачу

    private ReportForCard reportForCardList; // рапорт конкретного патрульного
    private List< PositionInfo > positionInfoList; // история его маршрута до цели

    public TaskDetails ( final Object object,
                         final UUID patrulUUID,
                         final TaskTypes taskTypes,
                         final TaskTotalData taskTotalData,
                         final List< ReportForCard > reportForCardList ) {
        this.setPositionInfoList( taskTotalData.getPositionInfoList() );
        this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
        this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() );
        reportForCardList
                .parallelStream()
                .filter( reportForCard -> reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 )
                .forEach( this::setReportForCardList );

        switch ( taskTypes ) {
            case CARD_102 -> {
                this.setTitle( ( (Card) object ).getFabula() );
                this.setFabula( ( (Card) object ).getFabula() );
                this.setDate( ( (Card) object ).getCreated_date().toString() ); }
            case FIND_FACE_CAR -> this.setDate( ( (CarEvent) object ).getCreated_date() );
            case FIND_FACE_PERSON -> this.setDate( ( (FaceEvent) object ).getCreated_date() );

            case FIND_FACE_EVENT_CAR -> this.setDate( ( (EventCar) object ).getCreated_date().toString() );
            case FIND_FACE_EVENT_BODY -> this.setDate( ( (EventBody) object ).getCreated_date().toString() );
            case FIND_FACE_EVENT_FACE -> this.setDate( ( (EventFace) object ).getCreated_date().toString() );
            default -> {
                this.setTitle( ( (SelfEmploymentTask) object ).getTitle() );
                this.setFabula( ( (SelfEmploymentTask) object ).getDescription() );
                this.setDate( ( (SelfEmploymentTask) object ).getIncidentDate().toString() ); } } }
}
