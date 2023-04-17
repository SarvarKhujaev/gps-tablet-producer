package com.ssd.mvd.gpstabletsservice.entity;

import java.util.Date;
import java.util.UUID;
import com.datastax.driver.core.Row;

import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.Errors;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.*;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;

@lombok.Data
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
    private Status taskStatus;

    private Boolean wasRead;
    private TaskTypes taskTypes;
    private Date notificationWasCreated; // the date when this current notification was created

    public Notification ( final Row row ) {
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
        this.setTaskStatus( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( row.getString( "taskStatus" ) )
                ? Status.valueOf( row.getString( "taskStatus" ) ) : Status.CREATED );
        this.setTaskTypes( TaskTypes.valueOf( row.getString( "taskTypes" ) ) );
        this.setNotificationWasCreated( row.getTimestamp( "notificationWasCreated" ) ); }

    public Notification ( final Patrul patrul,
                          final Card card,
                          final String text,
                          final Status status ) {
        this.setTitle( text );
        this.setStatus( status );
        this.setType( CARD_102.name() );
        this.setUuid( UUID.randomUUID() );
        this.setTaskStatus( card.getStatus() );
        this.setId( card.getUUID().toString() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setLatitudeOfTask( card.getLatitude() );
        this.setNotificationWasCreated( new Date() );
        this.setLongitudeOfTask( card.getLongitude() );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setAddress( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( card.getAddress() )
                ? card.getAddress()
                : Errors.DATA_NOT_FOUND.name() ); }

    public Notification ( final  Patrul patrul,
                          final EventCar eventCar,
                          final String text,
                          final Status status ) {
        this.setTitle( text );
        this.setStatus( status );
        this.setUuid( UUID.randomUUID() );
        this.setTaskStatus( eventCar.getStatus() );
        this.setType( FIND_FACE_EVENT_CAR.name() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setId( eventCar.getUUID().toString() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setLatitudeOfTask( eventCar.getLatitude() );
        this.setNotificationWasCreated( new Date() );
        this.setLongitudeOfTask( eventCar.getLongitude() );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setAddress( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( eventCar.getAddress() )
                ? eventCar.getAddress()
                : Errors.DATA_NOT_FOUND.name() ); }

    public Notification ( final  Patrul patrul,
                          final EventFace eventFace,
                          final String text,
                          final Status status ) {
        this.setTitle( text );
        this.setStatus( status );
        this.setUuid( UUID.randomUUID() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setTaskStatus( eventFace.getStatus() );
        this.setType( FIND_FACE_EVENT_FACE.name() );
        this.setId( eventFace.getUUID().toString() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setNotificationWasCreated( new Date() );
        this.setLatitudeOfTask( eventFace.getLatitude() );
        this.setLongitudeOfTask( eventFace.getLongitude() );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setAddress( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( eventFace.getAddress() )
                ? eventFace.getAddress()
                : Errors.DATA_NOT_FOUND.name() ); }

    public Notification ( final  Patrul patrul,
                          final EventBody eventBody,
                          final String text,
                          final Status status ) {
        this.setTitle( text );
        this.setStatus( status );
        this.setUuid( UUID.randomUUID() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setTaskStatus( eventBody.getStatus() );
        this.setType( FIND_FACE_EVENT_BODY.name() );
        this.setId( eventBody.getUUID().toString() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setNotificationWasCreated( new Date() );
        this.setLatitudeOfTask( eventBody.getLatitude() );
        this.setLongitudeOfTask( eventBody.getLongitude() );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setAddress( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( eventBody.getAddress() )
                ? eventBody.getAddress()
                : Errors.DATA_NOT_FOUND.name() ); }

    public Notification ( final  Patrul patrul,
                          final CarEvent carEvents,
                          final String text,
                          final Status status ) {
        this.setTitle( text );
        this.setStatus( status );
        this.setUuid( UUID.randomUUID() );
        this.setType( FIND_FACE_CAR.name() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setTaskStatus( carEvents.getStatus() );
        this.setId( carEvents.getUUID().toString() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setLatitudeOfTask( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvents.getDataInfo() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvents.getDataInfo().getData() )
                ? carEvents.getDataInfo().getData().getLatitude() : null );
        this.setNotificationWasCreated( new Date() );
        this.setLongitudeOfTask( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvents.getDataInfo() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvents.getDataInfo().getData() )
                ? carEvents.getDataInfo().getData().getLongitude() : null );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setAddress( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvents.getDataInfo() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvents.getDataInfo().getData() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvents.getDataInfo().getData().getAddress() )
                ? carEvents.getDataInfo().getData().getAddress()
                : Errors.DATA_NOT_FOUND.name() ); }

    public Notification ( final  Patrul patrul,
                          final FaceEvent faceEvent,
                          final String text,
                          final Status status ) {
        this.setTitle( text );
        this.setStatus( status );
        this.setUuid( UUID.randomUUID() );
        this.setType( FIND_FACE_PERSON.name() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setTaskStatus( faceEvent.getStatus() );
        this.setId( faceEvent.getUUID().toString() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setLatitudeOfTask( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( faceEvent.getDataInfo() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( faceEvent.getDataInfo().getData() )
                ? faceEvent.getDataInfo().getData().getLatitude() : null );
        this.setNotificationWasCreated( new Date() );
        this.setLongitudeOfTask( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( faceEvent.getDataInfo() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( faceEvent.getDataInfo().getData() )
                ? faceEvent.getDataInfo().getData().getLongitude() : null );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setAddress( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( faceEvent.getDataInfo() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( faceEvent.getDataInfo().getData() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( faceEvent.getDataInfo().getData().getAddress() )
                ? faceEvent.getDataInfo().getData().getAddress()
                : Errors.DATA_NOT_FOUND.name() ); }

    public Notification ( final  Patrul patrul,
                          final SelfEmploymentTask selfEmploymentTask,
                          final String text,
                          final Status status ) {
        this.setTitle( text );
        this.setStatus( status );
        this.setUuid( UUID.randomUUID() );
        this.setType( SELF_EMPLOYMENT.name() );
        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setNotificationWasCreated( new Date() );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setId( selfEmploymentTask.getUuid().toString() );
        this.setTaskStatus( selfEmploymentTask.getTaskStatus() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );
        this.setLatitudeOfTask( selfEmploymentTask.getLatOfAccident() );
        this.setLongitudeOfTask( selfEmploymentTask.getLanOfAccident() );
        this.setAddress( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( selfEmploymentTask.getAddress() )
                ? selfEmploymentTask.getAddress()
                : Errors.DATA_NOT_FOUND.name() ); }
}
