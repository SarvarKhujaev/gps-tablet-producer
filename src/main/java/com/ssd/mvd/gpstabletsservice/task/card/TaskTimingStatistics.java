package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.constants.Status;
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
    private Long totalTimeConsumption; // общее время которое патрульный потратил чтобы дойти до пункта назначения

    private TaskTypes taskTypes;
    private TaskTypes taskTypesTable;
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
                .subscribe( patrul1 -> {
                    this.setPatrulStatus( patrul1.getStatus() );

                    this.setCarType( patrul1.getCarType() );
                    this.setCarNumber( patrul1.getCarNumber() );
                    this.setOrganName( patrul1.getOrganName() );
                    this.setTaskIdOfPatrul( patrul1.getTaskId() );
                    this.setFatherName( patrul1.getFatherName() );
                    this.setPoliceType( patrul1.getPoliceType() );
                    this.setDateOfBirth( patrul1.getDateOfBirth() );
                    this.setPhoneNumber( patrul1.getPhoneNumber() );
                    this.setPassportNumber( patrul1.getPassportNumber() );
                    this.setPatrulImageLink( patrul1.getPatrulImageLink() );
                    this.setSurnameNameFatherName( patrul1.getSurnameNameFatherName() );

                    this.setBatteryLevel( patrul1.getBatteryLevel() );
                    this.setLastActiveDate( patrul1.getLastActiveDate() );

                    this.setLatitude( patrul1.getLatitude() );
                    this.setLongitude( patrul1.getLongitude() );
                    this.setLatitudeOfTask( patrul1.getLatitudeOfTask() );
                    this.setLongitudeOfTask( patrul1.getLongitudeOfTask() ); } ); }
}