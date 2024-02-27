package com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer;

import static com.ssd.mvd.gpstabletsservice.constants.Status.IN_TIME;
import static com.ssd.mvd.gpstabletsservice.constants.Status.LATE;

import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.*;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.datastax.driver.core.Row;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@lombok.Data
public final class TaskTimingStatistics { // показывает все таски со временем их выполнения и деталями
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
    private byte batteryLevel;

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

    private void save ( final Patrul patrul ) {
        this.save( patrul.getPatrulCarInfo() );
        this.save( patrul.getPatrulFIOData() );
        this.save( patrul.getPatrulTaskInfo() );
        this.save( patrul.getPatrulLocationData() );
        this.save( patrul.getPatrulMobileAppInfo() );

        this.setOrganName( patrul.getOrganName() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setDateOfBirth( patrul.getDateOfBirth() );
        this.setPassportNumber( patrul.getPassportNumber() );
        this.setPatrulImageLink( patrul.getPatrulImageLink() );

        this.setLastActiveDate( patrul.getPatrulDateData().getLastActiveDate() );
    }

    private void save ( final PatrulCarInfo patrulCarInfo ) {
        this.setCarNumber( patrulCarInfo.getCarNumber() );
        this.setCarType( patrulCarInfo.getCarType() );
    }

    private void save ( final PatrulFIOData patrulFIOData ) {
        this.setSurnameNameFatherName( patrulFIOData.getSurnameNameFatherName() );
        this.setFatherName( patrulFIOData.getFatherName() );
    }

    private void save ( final PatrulTaskInfo patrulTaskInfo ) {
        this.setTaskIdOfPatrul( patrulTaskInfo.getTaskId() );
        this.setPatrulStatus( patrulTaskInfo.getStatus() );
    }

    private void save ( final PatrulLocationData patrulLocationData ) {
        this.setLongitudeOfTask( patrulLocationData.getLongitudeOfTask() );
        this.setLatitudeOfTask( patrulLocationData.getLatitudeOfTask() );
        this.setLongitude( patrulLocationData.getLongitude() );
        this.setLatitude( patrulLocationData.getLatitude() );
    }

    private void save ( final PatrulMobileAppInfo patrulMobileAppInfo ) {
        this.setBatteryLevel( patrulMobileAppInfo.getBatteryLevel() );
        this.setPhoneNumber( patrulMobileAppInfo.getPhoneNumber() );
    }

    public static TaskTimingStatistics generate ( final Row row, final Patrul patrul ) {
        return new TaskTimingStatistics( row, patrul );
    }

    public static TaskTimingStatistics generate (
            final Patrul patrul,
            final TaskTypes taskTypes,
            final PatrulTimeConsumedToArriveToTaskLocation patrulStatus,
            final List< PositionInfo > positionInfo ) {
        return new TaskTimingStatistics(
                patrul,
                taskTypes,
                patrulStatus,
                positionInfo
        );
    }

    private TaskTimingStatistics ( final Row row, final Patrul patrul ) {
        this.save( patrul );
        this.setInTime( row.getBool( "inTime" ) );
        this.setTaskId( row.getString( "taskId" ) );
        this.setPatrulUUID( row.getUUID( "patrulUUID" ) );
        this.setDateOfComing( row.getTimestamp( "dateOfComing" ) );
        this.setStatus( Status.valueOf( row.getString( "status" ) ) );
        this.setTimeWastedToArrive( row.getLong( "timeWastedToArrive" ) );
        this.setTotalTimeConsumption( row.getLong( "totalTimeConsumption" ) );
        this.setTaskTypes( TaskTypes.valueOf( row.getString("taskTypes" ) ) );
        this.setPositionInfoList( row.getList( "positionInfoList", PositionInfo.class ) );
    }

    private TaskTimingStatistics (
            final Patrul patrul,
            final TaskTypes taskTypes,
            final PatrulTimeConsumedToArriveToTaskLocation patrulStatus,
            final List< PositionInfo > positionInfo ) {
        this.save( patrul );
        this.setTaskTypes( taskTypes );
        this.setDateOfComing( new Date() );
        this.setTotalTimeConsumption( 0L );
        this.setPatrulUUID( patrul.getUuid() );
        this.setPositionInfoList( positionInfo );
        this.setInTime( patrulStatus.getInTime() );
        this.setTaskId( patrul.getPatrulTaskInfo().getTaskId() );
        this.setStatus( patrulStatus.getInTime() ? IN_TIME : LATE );
        this.setTimeWastedToArrive( patrulStatus.getTotalTimeConsumption() );
    }
}
