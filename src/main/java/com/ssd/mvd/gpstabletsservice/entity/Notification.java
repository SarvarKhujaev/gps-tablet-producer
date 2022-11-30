package com.ssd.mvd.gpstabletsservice.entity;

import lombok.Data;
import java.util.Date;
import java.util.UUID;
import com.datastax.driver.core.Row;

import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.*;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;

@Data
public class Notification {
    private String id; // id of any task
    private String type; // might be from 102 or Camera
    private String title; // description of Patrul action
    private String address;
    private String carNumber;
    private String policeType;
    private String nsfOfPatrul;
    private String passportSeries;

    private Double latitudeOfTask;
    private Double longitudeOfTask;

    private UUID uuid;
    private Status status;
    private Boolean wasRead;
    private TaskTypes taskTypes;
    private Date notificationWasCreated; // the date when this current notification was created

    public Notification ( Row row ) {
        this.setId( row.getString( "id" ) );
        this.setType( row.getString( "type" ) );
        this.setTitle( row.getString( "title" ) );
        this.setAddress( row.getString( "address" ) );
        this.setCarNumber( row.getString( "carNumber" ) );
        this.setPoliceType( row.getString( "policeType" ) );
        this.setNsfOfPatrul( row.getString( "nsfOfPatrul" ) );
        this.setPassportSeries( row.getString( "passportSeries" ) );

        this.setLatitudeOfTask( row.getDouble( "latitudeOfTask" ) );
        this.setLongitudeOfTask( row.getDouble( "longitudeOfTask" ) );

        this.setUuid( row.getUUID( "uuid" ) );
        this.setWasRead( row.getBool( "wasRead" ) );
        this.setStatus( Status.valueOf( row.getString( "status" ) ) );
        this.setTaskTypes( TaskTypes.valueOf( row.getString( "taskTypes" ) ) );
        this.setNotificationWasCreated( row.getTimestamp( "notificationWasCreated" ) ); }

    public Notification ( Patrul patrul, Card card, String text ) {
        this.setTitle( text );
        this.setType( CARD_102.name() );
        this.setUuid( UUID.randomUUID() );
        this.setStatus( patrul.getStatus() );
        this.setId( card.getCardId().toString() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setLatitudeOfTask( card.getLatitude() );
        this.setNotificationWasCreated( new Date() );
        this.setLongitudeOfTask( card.getLongitude() );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setAddress( card.getAddress() != null ? card.getAddress() : "unknown" ); }

    public Notification ( Patrul patrul, EventCar eventCar, String text ) {
        this.setTitle( text );
        this.setId( eventCar.getId() );
        this.setUuid( UUID.randomUUID() );
        this.setStatus( patrul.getStatus() );
        this.setType( FIND_FACE_EVENT_CAR.name() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setLatitudeOfTask( eventCar.getLatitude() );
        this.setNotificationWasCreated( new Date() );
        this.setLongitudeOfTask( eventCar.getLongitude() );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setAddress( eventCar.getAddress() != null ? eventCar.getAddress() : "unknown" ); }

    public Notification ( Patrul patrul, EventFace eventFace, String text ) {
        this.setTitle( text );
        this.setId( eventFace.getId() );
        this.setUuid( UUID.randomUUID() );
        this.setStatus( patrul.getStatus() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setType( FIND_FACE_EVENT_FACE.name() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setNotificationWasCreated( new Date() );
        this.setLatitudeOfTask( eventFace.getLatitude() );
        this.setLongitudeOfTask( eventFace.getLongitude() );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setAddress( eventFace.getAddress() != null ? eventFace.getAddress() : "unknown" ); }

    public Notification ( Patrul patrul, EventBody eventBody, String text ) {
        this.setTitle( text );
        this.setId( eventBody.getId() );
        this.setUuid( UUID.randomUUID() );
        this.setStatus( patrul.getStatus() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setType( FIND_FACE_EVENT_BODY.name() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setNotificationWasCreated( new Date() );
        this.setLatitudeOfTask( eventBody.getLatitude() );
        this.setLongitudeOfTask( eventBody.getLongitude() );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setAddress( eventBody.getAddress() != null ? eventBody.getAddress() : "unknown" ); }

    public Notification ( Patrul patrul, CarEvent carEvents, String text ) {
        this.setTitle( text );
        this.setId( carEvents.getId() );
        this.setUuid( UUID.randomUUID() );
        this.setStatus( patrul.getStatus() );
        this.setType( FIND_FACE_CAR.name() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setLatitudeOfTask( carEvents.getDataInfo() != null
                && carEvents.getDataInfo().getData() != null ?
                carEvents.getDataInfo().getData().getLatitude() : null );
        this.setNotificationWasCreated( new Date() );
        this.setLongitudeOfTask( carEvents.getDataInfo() != null
                && carEvents.getDataInfo().getData() != null ?
                carEvents.getDataInfo().getData().getLongitude() : null );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setAddress( carEvents.getDataInfo() != null
                && carEvents.getDataInfo().getData() != null
                && carEvents.getDataInfo().getData().getAddress() != null ?
                carEvents.getDataInfo().getData().getAddress() : "unknown" ); }

    public Notification ( Patrul patrul, FaceEvent faceEvent, String text ) {
        this.setTitle( text );
        this.setId( faceEvent.getId() );
        this.setUuid( UUID.randomUUID() );
        this.setStatus( patrul.getStatus() );
        this.setType( FIND_FACE_PERSON.name() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setLatitudeOfTask( faceEvent.getDataInfo() != null
                && faceEvent.getDataInfo().getData() != null ?
                faceEvent.getDataInfo().getData().getLatitude() : null );
        this.setNotificationWasCreated( new Date() );
        this.setLongitudeOfTask( faceEvent.getDataInfo() != null
                && faceEvent.getDataInfo().getData() != null ?
                faceEvent.getDataInfo().getData().getLongitude() : null );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setAddress( faceEvent.getDataInfo() != null
                && faceEvent.getDataInfo().getData() != null
                && faceEvent.getDataInfo().getData().getAddress() != null ?
                faceEvent.getDataInfo().getData().getAddress() : "unknown" ); }

    public Notification ( Patrul patrul, SelfEmploymentTask selfEmploymentTask, String text ) {
        this.setTitle( text );
        this.setUuid( UUID.randomUUID() );
        this.setStatus( patrul.getStatus() );
        this.setType( SELF_EMPLOYMENT.name() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setNotificationWasCreated( new Date() );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setId( selfEmploymentTask.getUuid().toString() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setLatitudeOfTask( selfEmploymentTask.getLatOfAccident() );
        this.setLongitudeOfTask( selfEmploymentTask.getLanOfAccident() );
        this.setAddress( selfEmploymentTask.getAddress() != null ? selfEmploymentTask.getAddress() : "unknown" ); }
}
