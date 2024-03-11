package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.*;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.*;
import com.ssd.mvd.gpstabletsservice.inspectors.CollectionsInspector;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.*;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskCommonParams;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.card.Card;

import java.util.List;
import java.util.UUID;

public final class TaskDetails extends CollectionsInspector {
    public void setDate ( final String date ) {
        this.date = date;
    }

    public void setTitle ( final String title ) {
        this.title = title;
    }

    public void setFabula ( final String fabula ) {
        this.fabula = fabula;
    }

    public void setTimeWastedToArrive ( final long timeWastedToArrive ) {
        this.timeWastedToArrive = timeWastedToArrive;
    }

    public void setTotalTimeConsumption ( final long totalTimeConsumption ) {
        this.totalTimeConsumption = totalTimeConsumption;
    }

    public void setReportForCardList ( final ReportForCard reportForCardList ) {
        this.reportForCardList = reportForCardList;
    }

    public void setPositionInfoList ( final List< PositionInfo > positionInfoList ) {
        this.positionInfoList = positionInfoList;
    }

    private String date;
    private String title;
    private String fabula; // описания задачи

    private long timeWastedToArrive; // время которое патрульный потратил чтобы дойти до цели
    private long totalTimeConsumption; // общее потраченное время на задачу

    private ReportForCard reportForCardList; // рапорт конкретного патрульного
    private List< PositionInfo > positionInfoList; // история его маршрута до цели

    public static TaskDetails generate (
            final Object object,
            final UUID patrulUUID,
            final TaskTotalData taskTotalData,
            final TaskCommonParams taskCommonParams
    ) {
        return new TaskDetails(
                object,
                patrulUUID,
                taskTotalData,
                taskCommonParams );
    }

    private TaskDetails (
            final Object object,
            final UUID patrulUUID,
            final TaskTotalData taskTotalData,
            final TaskCommonParams taskCommonParams
    ) {
        this.setPositionInfoList( taskTotalData.getPositionInfoList() );
        this.setTimeWastedToArrive( taskTotalData.getTimeWastedToArrive() );
        this.setTotalTimeConsumption( taskTotalData.getTotalTimeConsumption() );

        /*
        находим рапорт касающиеся только этой задачи
        */
        super.analyze(
                taskCommonParams.getReportForCardList(),
                reportForCard -> {
                    if ( reportForCard.getUuidOfPatrul().compareTo( patrulUUID ) == 0 ) {
                        this.setReportForCardList( reportForCard );
                    }
                }
        );

        switch ( taskCommonParams.getTaskTypes() ) {
            case CARD_102 -> this.save( ( Card ) object );

            case FIND_FACE_CAR -> this.setDate( ( (CarEvent) object ).getCreated_date() );
            case FIND_FACE_PERSON -> this.setDate( ( (FaceEvent) object ).getCreated_date() );

            case FIND_FACE_EVENT_CAR -> this.setDate( ( (EventCar) object ).getCreated_date().toString() );
            case FIND_FACE_EVENT_BODY -> this.setDate( ( (EventBody) object ).getCreated_date().toString() );
            case FIND_FACE_EVENT_FACE -> this.setDate( ( (EventFace) object ).getCreated_date().toString() );
            default -> this.save( ( (SelfEmploymentTask) object ) );
        }

        super.close();
    }

    private void save ( final Card card ) {
        this.setTitle( card.getFabula() );
        this.setFabula( card.getFabula() );
        this.setDate( card.getCreated_date().toString() );
    }

    private void save (
            final SelfEmploymentTask selfEmploymentTask
    ) {
        this.setTitle( selfEmploymentTask.getTitle() );
        this.setFabula( selfEmploymentTask.getDescription() );
        this.setDate( selfEmploymentTask.getIncidentDate().toString() );
    }
}
