package com.ssd.mvd.gpstabletsservice.entity;

import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import com.ssd.mvd.gpstabletsservice.task.card.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.database.Archive;

import java.time.Duration;
import lombok.Data;
import java.util.*;

@Data
public class Patrul {
    private Date taskDate; // for registration of exact time when patrul started to deal with task
    private Date lastActiveDate; // shows when user was online lastly
    private Date startedToWorkDate; // the time
    private Date dateOfRegistration;

    private Double distance;
    private Double latitude; // the current location of the user
    private Double longitude; // the current location of the user
    private Double latitudeOfTask;
    private Double longitudeOfTask;

    private UUID uuid;
    private UUID organ; // choosing from dictionary
    private UUID selfEmploymentId;

    private Long card;
    private Long regionId;
    private Long mahallaId;
    private Long districtId; // choosing from dictionary
    private Long totalActivityTime;

    private Boolean inPolygon = false;

    private String name;
    private String rank;
    private String email;
    private String token;
    private String surname;
    private String password;
    private String carType; // модель машины
    private String carNumber;
    private String policeType; // choosing from dictionary
    private String fatherName;
    private String dateOfBirth;
    private String phoneNumber;
    private String findFaceTask;
    private String simCardNumber;
    private String passportNumber;
    private String patrulImageLink;
    private String surnameNameFatherName; // Ф.И.О

    private Status status; // busy, free by default, available or not available
    private Status taskStatus; // ths status of the Card or SelfEmployment

    private Map< String, String > listOfTasks = new HashMap<>(); // the list which will store ids of all tasks which have been completed by Patrul

    public String getSurnameNameFatherName () { return this.surnameNameFatherName != null ? this.surnameNameFatherName
            : ( this.surnameNameFatherName = this.getName() + " " + this.getSurname() + " " + this.getFatherName() ); }

    public Patrul changeTaskStatus ( Status status ) {
        switch ( ( this.taskStatus = status ) ) {
            case ATTACHED -> this.setStatus( Status.BUSY );
            case ACCEPTED -> {
                this.setStatus( Status.ACCEPTED );
                this.setTaskDate( new Date() ); // fixing time when patrul started this task
                if ( this.getCard() != null ) RedisDataControl.getRedis().getCard( this.getCard() ).subscribe( card1 -> {
                    card1.getPatruls().put( this.getPassportNumber(), this );
                    RedisDataControl.getRedis().update( card1 ); } );
            } case FINISHED -> {
                this.setStatus( Status.FREE );
                if ( this.getCard() != null ) {
                    RedisDataControl.getRedis().getCard( this.getCard() ).subscribe( card1 -> {
                        card1.setStatus( Status.FINISHED );
                        card1.getPatruls().put( this.getPassportNumber(), this );
                        card1.getPatrulStatuses().get( this.getPassportNumber() )
                                .setTotalTimeConsumption( TimeInspector.getInspector().getTimeDifference( this.getTaskDate().toInstant() ) );
                        RedisDataControl.getRedis().update( card1 ); } );
                    this.getListOfTasks().putIfAbsent( this.getCard().toString(), "card" );
                    this.setTaskDate( null );
                    this.setCard( null );
                } else { this.getListOfTasks().putIfAbsent( this.getSelfEmploymentId().toString(), "selfEmployment" );
                    this.setSelfEmploymentId( null );
                    this.setTaskDate( null ); }
            } case ARRIVED -> {
                this.setStatus( Status.ARRIVED );
                if ( this.getCard() != null ) RedisDataControl.getRedis().getCard( this.getCard() ).subscribe( card1 -> {
                    card1.getPatruls().put( this.getPassportNumber(), this );
                    card1.getPatrulStatuses().putIfAbsent( this.getPassportNumber(), PatrulStatus.builder()
                            .patrul( this )
                            .inTime( this.check() )
                            .totalTimeConsumption( TimeInspector.getInspector().getTimeDifference( this.getTaskDate().toInstant() ) )
                            .build() );
                    RedisDataControl.getRedis().update( card1 ); } );
                else Archive.getAchieve().get( this.getSelfEmploymentId() ).subscribe( selfEmploymentTask -> {
                    selfEmploymentTask.setArrivedTime( new Date() );
                    selfEmploymentTask.setTaskStatus( com.ssd.mvd.gpstabletsservice.constants.Status.ARRIVED ); } ); }
        } return this; }

    private Boolean check () { return switch ( this.getPoliceType() ) {
            case "TTG", "PI" -> Duration.between( new Date().toInstant(), this.getTaskDate().toInstant() ).toMinutes() <= 30;
            default -> TimeInspector.getInspector().checkDate( this.getTaskDate().toInstant() ); }; }
}
