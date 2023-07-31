package com.ssd.mvd.gpstabletsservice.entity.notifications;

import java.util.Date;
import java.util.UUID;
import com.datastax.driver.core.Row;

import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.Errors;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.DataInfo;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;

@lombok.Data
public final class Notification {
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

    private void save ( final DataInfo dataInfo ) {
        if (  DataValidateInspector
                .getInstance()
                .checkRequest
                .test( dataInfo, 9 ) ) {
            this.setLongitudeOfTask( dataInfo.getCadaster().getLongitude() );
            this.setLatitudeOfTask( dataInfo.getCadaster().getLatitude() );
            this.setAddress( dataInfo.getCadaster().getAddress() ); } }

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
                .checkParam
                .test( row.getString( "taskStatus" ) )
                ? Status.valueOf( row.getString( "taskStatus" ) )
                : Status.CREATED );
        this.setTaskTypes( TaskTypes.valueOf( row.getString( "taskTypes" ) ) );
        this.setNotificationWasCreated( row.getTimestamp( "notificationWasCreated" ) ); }

    public Notification ( final Patrul patrul,
                          final Status status,
                          final Object task,
                          final String text,
                          final TaskTypes taskTypes ) {
        this.setTitle( text );
        this.setStatus( status );
        this.setType( taskTypes.name() );

        this.setUuid( UUID.randomUUID() );
        this.setNotificationWasCreated( new Date() );

        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );

        switch ( taskTypes ) {
            case CARD_102 -> {
                this.setTaskStatus( ( (Card) task ).getStatus() );
                this.setId( ( (Card) task ).getUUID().toString() );
                this.setLatitudeOfTask( ( (Card) task ).getLatitude() );
                this.setLongitudeOfTask( ( (Card) task ).getLongitude() );
                this.setAddress( DataValidateInspector
                        .getInstance()
                        .checkParam
                        .test( ( (Card) task ).getAddress() )
                        ? ( (Card) task ).getAddress()
                        : Errors.DATA_NOT_FOUND.name() ); }

            case FIND_FACE_EVENT_CAR -> {
                this.setTaskStatus( ( (EventCar) task ).getStatus() );
                this.setId( ( (EventCar) task ).getUUID().toString() );
                this.setLatitudeOfTask( ( (EventCar) task ).getLatitude() );
                this.setLongitudeOfTask( ( (EventCar) task ).getLongitude() );
                this.setAddress( DataValidateInspector
                        .getInstance()
                        .checkParam
                        .test( ( (EventCar) task ).getAddress() )
                        ? ( (EventCar) task ).getAddress()
                        : Errors.DATA_NOT_FOUND.name() ); }
            case FIND_FACE_EVENT_FACE -> {
                this.setTaskStatus( ( (EventFace) task ).getStatus() );
                this.setId( ( (EventFace) task ).getUUID().toString() );
                this.setLatitudeOfTask( ( (EventFace) task ).getLatitude() );
                this.setLongitudeOfTask( ( (EventFace) task ).getLongitude() );
                this.setAddress( DataValidateInspector
                        .getInstance()
                        .checkParam
                        .test( ( (EventFace) task ).getAddress() )
                        ? ( (EventFace) task ).getAddress()
                        : Errors.DATA_NOT_FOUND.name() ); }
            case FIND_FACE_EVENT_BODY -> {
                this.setTaskStatus( ( (EventBody) task ).getStatus() );
                this.setId( ( (EventBody) task ).getUUID().toString() );
                this.setLatitudeOfTask( ( (EventBody) task ).getLatitude() );
                this.setLongitudeOfTask( ( (EventBody) task ).getLongitude() );
                this.setAddress( DataValidateInspector
                        .getInstance()
                        .checkParam
                        .test( ( (EventBody) task ).getAddress() )
                        ? ( (EventBody) task ).getAddress()
                        : Errors.DATA_NOT_FOUND.name() ); }

            case FIND_FACE_CAR -> {
                this.save( ( (CarEvent) task ).getDataInfo() );
                this.setTaskStatus( ( (CarEvent) task ).getStatus() );
                this.setId( ( (CarEvent) task ).getUUID().toString() ); }
            case FIND_FACE_PERSON -> {
                this.save( ( (FaceEvent) task ).getDataInfo() );
                this.setTaskStatus( ( (FaceEvent) task ).getStatus() );
                this.setId( ( (FaceEvent) task ).getUUID().toString() ); }

            default -> {
                this.setId( ( (SelfEmploymentTask) task ).getUuid().toString() );
                this.setTaskStatus( ( (SelfEmploymentTask) task ).getTaskStatus() );
                this.setLatitudeOfTask( ( (SelfEmploymentTask) task ).getLatOfAccident() );
                this.setLongitudeOfTask( ( (SelfEmploymentTask) task ).getLanOfAccident() );

                this.setAddress( DataValidateInspector
                        .getInstance()
                        .checkParam
                        .test( ( (SelfEmploymentTask) task ).getAddress() )
                        ? ( (SelfEmploymentTask) task ).getAddress()
                        : Errors.DATA_NOT_FOUND.name() ); } } }
}
