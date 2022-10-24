package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.datastax.driver.core.Row;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskTimingStatistics< T > { // показывает все таски со временем их выполнения и деталями
    private String taskId;
    private Boolean inTime;
    private UUID patrulUUID;
    private Date dateOfComing; // показывает время когда патрульный пришел в точку назначения
    private Long totalTimeConsumption; // общее время которое патрульный потратил чтобы дойти до пункта назначения

    private T task;
    private Patrul patrul;
    private Status status; // показывает пришел ли патрульный во время или нет
    private TaskTypes taskTypes;
    private List< PositionInfo > positionInfoList;

    public TaskTimingStatistics( Row row ) {
        this.setInTime( row.getBool( "inTime" ) );
        this.setTaskId( row.getString( "taskId" ) );
        this.setPatrulUUID( row.getUUID( "patrulUUID" ) );
        this.setDateOfComing( row.getTimestamp( "dateOfComing" ) );
        this.setStatus( Status.valueOf( row.getString( "status" ) ) );
        this.setTotalTimeConsumption( row.getLong( "totalTimeConsumption" ) );
        this.setTaskTypes( TaskTypes.valueOf( row.getString("taskTypes" ) ) );
        this.setPositionInfoList( row.getList( "positionInfoList", PositionInfo.class ) );
        CassandraDataControl
                .getInstance()
                .getGetPatrulByUUID()
                .apply( this.getPatrulUUID() )
                .subscribe( this::setPatrul );
        switch ( this.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCard102()
                    .apply( this.getTaskId() )
                    .subscribe( card -> this.setTask( (T) card ) );

            case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetSelfEmploymentTask()
                    .apply( UUID.fromString( this.getTaskId() ) )
                    .subscribe( selfEmploymentTask -> this.setTask( (T) selfEmploymentTask ) );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCarEvents()
                    .apply( this.getTaskId() )
                    .subscribe( carEvent -> this.setTask( (T) carEvent ) );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetFaceEvents()
                    .apply( this.getTaskId() )
                    .subscribe( faceEvent -> this.setTask( (T) faceEvent ) );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventCar()
                    .apply( this.getTaskId() )
                    .subscribe( eventFace -> this.setTask( (T) eventFace ) );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventBody()
                    .apply( this.getTaskId() )
                    .subscribe( eventBody -> this.setTask( (T) eventBody ) );

            default -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventFace()
                    .apply( this.getTaskId() )
                    .subscribe( eventFace -> this.setTask( (T) eventFace ) ); } }
}
