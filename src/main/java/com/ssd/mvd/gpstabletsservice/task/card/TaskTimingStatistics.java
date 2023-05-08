package com.ssd.mvd.gpstabletsservice.task.card;

import static com.ssd.mvd.gpstabletsservice.constants.Status.IN_TIME;
import static com.ssd.mvd.gpstabletsservice.constants.Status.LATE;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.datastax.driver.core.Row;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@lombok.Data
public class TaskTimingStatistics { // показывает все таски со временем их выполнения и деталями
    private String carType; // модель машины
    private String organName;
    private String carNumber;
    private String fatherName;
    private String policeType; // choosing from dictionary
    private String phoneNumber;
    private String dateOfBirth;
    private String taskIdOfPatrul;
    private String passportNumber;
    private String patrulImageLink;
    private String surnameNameFatherName; // Ф.И.О

    private Double latitude; // the current location of the user
    private Double longitude; // the current location of the user
    private Double latitudeOfTask;
    private Double longitudeOfTask;

    private Status patrulStatus; // статус самого патрульного
    private Date lastActiveDate; // shows when user was online lastly
    private Integer batteryLevel;

    // параметры самого класса
    private Status status; // показывает пришел ли патрульный во время или нет
    private String taskId;
    private Boolean inTime;
    private UUID patrulUUID;
    private Date dateOfComing; // показывает время когда патрульный пришел в точку назначения

    private Long timeWastedToArrive; // общее время которое патрульный потратил чтобы дойти до пункта назначения
    private Long totalTimeConsumption; // общее время которое патрульный потратил чтобы дойти до пункта назначения

    private TaskTypes taskTypes;
    private List< PositionInfo > positionInfoList;

    public TaskTimingStatistics ( final Row row, final Patrul patrul ) {
            this.setInTime( row.getBool( "inTime" ) );
            this.setTaskId( row.getString( "taskId" ) );
            this.setPatrulUUID( row.getUUID( "patrulUUID" ) );
            this.setDateOfComing( row.getTimestamp( "dateOfComing" ) );
            this.setStatus( Status.valueOf( row.getString( "status" ) ) );
            this.setTimeWastedToArrive( row.getLong( "timeWastedToArrive" ) );
            this.setTotalTimeConsumption( row.getLong( "totalTimeConsumption" ) );
            this.setTaskTypes( TaskTypes.valueOf( row.getString("taskTypes" ) ) );
            this.setPositionInfoList( row.getList( "positionInfoList", PositionInfo.class ) );

            this.setPatrulStatus( patrul.getStatus() );

            this.setCarType( patrul.getCarType() );
            this.setCarNumber( patrul.getCarNumber() );
            this.setOrganName( patrul.getOrganName() );
            this.setTaskIdOfPatrul( patrul.getTaskId() );
            this.setFatherName( patrul.getFatherName() );
            this.setPoliceType( patrul.getPoliceType() );
            this.setDateOfBirth( patrul.getDateOfBirth() );
            this.setPhoneNumber( patrul.getPhoneNumber() );
            this.setPassportNumber( patrul.getPassportNumber() );
            this.setPatrulImageLink( patrul.getPatrulImageLink() );
            this.setSurnameNameFatherName( patrul.getSurnameNameFatherName() );

            this.setBatteryLevel( patrul.getBatteryLevel() );
            this.setLastActiveDate( patrul.getLastActiveDate() );

            this.setLatitude( patrul.getLatitude() );
            this.setLongitude( patrul.getLongitude() );
            this.setLatitudeOfTask( patrul.getLatitudeOfTask() );
            this.setLongitudeOfTask( patrul.getLongitudeOfTask() ); }

    public TaskTimingStatistics (
            final Patrul patrul,
            final String taskId,
            final TaskTypes taskTypes,
            final PatrulStatus patrulStatus,
            final List< PositionInfo > positionInfo ) {
        this.setTaskId( taskId );
        this.setTaskTypes( taskTypes );
        this.setDateOfComing( new Date() );
        this.setTotalTimeConsumption( 0L );
        this.setPatrulUUID( patrul.getUuid() );
        this.setPositionInfoList( positionInfo );
        this.setInTime( patrulStatus.getInTime() );
        this.setStatus( patrulStatus.getInTime() ? IN_TIME : LATE );
        this.setTimeWastedToArrive( patrulStatus.getTotalTimeConsumption() ); }
}
