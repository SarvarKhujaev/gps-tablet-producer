package com.ssd.mvd.gpstabletsservice.entity;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import com.ssd.mvd.gpstabletsservice.task.card.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.task.card.Card;

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

    private UUID organ; // choosing from dictionary
    private UUID selfEmploymentId;

    private Long card;
    private Long regionId;
    private Long mahallaId;
    private Long districtId; // choosing from dictionary
    private Long totalActivityTime;

    private Boolean inPolygon;

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

    private Map< String, String > listOfTasks = new HashMap<>(); // the list which will store ids of all tasks which have been completed by Patrul

    public String getSurnameNameFatherName () { return this.surnameNameFatherName != null ? this.surnameNameFatherName
            : ( this.surnameNameFatherName = this.getName() + " " + this.getSurname() + " " + this.getFatherName() ); }

    public Patrul changeTaskStatus ( Status status, Card card ) {
        switch ( ( this.status = status ) ) {
            case CANCEL -> {
                this.setCard( null );
                this.setStatus( Status.FREE );
                card.getPatruls().remove( this.getPassportNumber() ); }
            case ATTACHED -> {
                this.setCard( card.getCardId() ); // saving card id into patrul object
                this.setLatitudeOfTask( card.getLatitude() );
                this.setLongitudeOfTask( card.getLongitude() ); }
            case ACCEPTED -> this.setTaskDate( new Date() ); // fixing time when patrul started this task
            case FINISHED -> {
                card.getPatrulStatuses().get( this.getPassportNumber() )
                        .setTotalTimeConsumption( TimeInspector.getInspector().getTimeDifference( this.getTaskDate().toInstant() ) );
                this.getListOfTasks().putIfAbsent( this.getCard().toString(), "card" );
                this.setStatus( Status.FREE );
                this.setTaskDate( null );
                this.setCard( null );
            } case ARRIVED -> card.getPatrulStatuses().putIfAbsent( this.getPassportNumber(), PatrulStatus.builder()
                    .patrul( this )
                    .inTime( this.check() )
                    .totalTimeConsumption( TimeInspector.getInspector().getTimeDifference( this.getTaskDate().toInstant() ) ).build() );
        } card.getPatruls().put( this.getPassportNumber(), this );
        RedisDataControl.getRedis().addValue( card.getCardId().toString(), new ActiveTask( card ) );
        RedisDataControl.getRedis().update( card );
        return this; }

    public Patrul changeTaskStatus ( Status status, SelfEmploymentTask selfEmploymentTask ) {
        switch ( ( this.status = status ) ) {
            case CANCEL -> {
                this.setStatus( Status.FREE );
                this.setSelfEmploymentId( null );
                selfEmploymentTask.getPatruls().remove( this.getPassportNumber() ); }
            case ATTACHED -> {
                this.setSelfEmploymentId( selfEmploymentTask.getUuid() ); // saving card id into patrul object
                this.setLatitudeOfTask( selfEmploymentTask.getLatOfAccident() );
                this.setLongitudeOfTask( selfEmploymentTask.getLanOfAccident() );
                selfEmploymentTask.getPatruls().put( this.getPassportNumber(), this ); }
            case ACCEPTED, ARRIVED -> {
                this.setTaskDate( new Date() ); // fixing time when patrul started this task
                this.setSelfEmploymentId( selfEmploymentTask.getUuid() );
                selfEmploymentTask.getPatruls().put( this.getPassportNumber(), this ); }
            case FINISHED -> {
                this.getListOfTasks().putIfAbsent( this.getSelfEmploymentId().toString(), "selfEmployment" );
                selfEmploymentTask.getPatruls().put( this.getPassportNumber(), this );
                this.setSelfEmploymentId( null );
                this.setStatus( Status.FREE );
                this.setTaskDate( null ); }
        } RedisDataControl.getRedis().addValue( selfEmploymentTask.getUuid().toString(), new ActiveTask( selfEmploymentTask ) );
        return this; }

    private Boolean check () { return switch ( this.getPoliceType() ) {
            case "TTG", "PI" -> Duration.between( new Date().toInstant(), this.getTaskDate().toInstant() ).toMinutes() <= 30;
            default -> TimeInspector.getInspector().checkDate( this.getTaskDate().toInstant() ); }; }
}
