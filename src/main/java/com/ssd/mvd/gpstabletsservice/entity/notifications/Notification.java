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

    private void save ( final DataInfo dataInfo,
                        final Status status,
                        final String taskId,
                        final DataValidateInspector dataValidateInspector ) {
        this.setId( taskId );
        this.setTaskStatus( status );
        if (  dataValidateInspector.checkRequest.test( dataInfo, 9 ) ) {
            this.setLongitudeOfTask( dataInfo.getCadaster().getLongitude() );
            this.setLatitudeOfTask( dataInfo.getCadaster().getLatitude() );
            this.setAddress( dataInfo.getCadaster().getAddress() ); } }

    private void save( final Status status,
                       final String taskId,
                       final Double latitude,
                       final Double longitude,
                       final String address ) {
        this.setLongitudeOfTask( longitude );
        this.setLatitudeOfTask( latitude );
        this.setTaskStatus( status );
        this.setAddress( address );
        this.setId( taskId ); }

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
                          final TaskTypes taskTypes,
                          final DataValidateInspector dataValidateInspector ) {
        this.setStatus( status );
        this.setType( taskTypes.name() );
        this.setTitle( text + new Date() );

        this.setUuid( UUID.randomUUID() );
        this.setNotificationWasCreated( new Date() );

        this.setCarNumber( patrul.getCarNumber() );
        this.setTaskTypes( patrul.getTaskTypes() );
        this.setPoliceType( patrul.getPoliceType() );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setNsfOfPatrul( patrul.getSurnameNameFatherName() );

        switch ( taskTypes ) {
            case CARD_102 -> this.save( ( (Card) task ).getStatus(),
                    ( (Card) task ).getUUID().toString(),
                    ( (Card) task ).getLatitude(),
                    ( (Card) task ).getLongitude(),
                    dataValidateInspector.checkParam.test( ( (Card) task ).getAddress() )
                            ? ( (Card) task ).getAddress()
                            : Errors.DATA_NOT_FOUND.name() );

            case FIND_FACE_EVENT_CAR -> this.save( ( (EventCar) task ).getStatus(),
                    ( (EventCar) task ).getUUID().toString(),
                    ( (EventCar) task ).getLatitude(),
                    ( (EventCar) task ).getLongitude(),
                    dataValidateInspector.checkParam.test( ( (EventCar) task ).getAddress() )
                            ? ( (EventCar) task ).getAddress()
                            : Errors.DATA_NOT_FOUND.name() );

            case FIND_FACE_EVENT_FACE -> this.save( ( (EventFace) task ).getStatus(),
                    ( (EventFace) task ).getUUID().toString(),
                    ( (EventFace) task ).getLatitude(),
                    ( (EventFace) task ).getLongitude(),
                    dataValidateInspector.checkParam.test( ( (EventFace) task ).getAddress() )
                            ? ( (EventFace) task ).getAddress()
                            : Errors.DATA_NOT_FOUND.name() );

            case FIND_FACE_EVENT_BODY -> this.save( ( (EventBody) task ).getStatus(),
                    ( (EventBody) task ).getUUID().toString(),
                    ( (EventBody) task ).getLatitude(),
                    ( (EventBody) task ).getLongitude(),
                    dataValidateInspector.checkParam.test( ( (EventBody) task ).getAddress() )
                            ? ( (EventFace) task ).getAddress()
                            : Errors.DATA_NOT_FOUND.name() );

            case FIND_FACE_CAR -> this.save( ( (CarEvent) task ).getDataInfo(),
                    ( (CarEvent) task ).getStatus(),
                    ( (CarEvent) task ).getUUID().toString(),
                    dataValidateInspector );

            case FIND_FACE_PERSON -> this.save( ( (FaceEvent) task ).getDataInfo(),
                    ( (FaceEvent) task ).getStatus(),
                    ( (FaceEvent) task ).getUUID().toString(),
                    dataValidateInspector );

            default -> this.save( ( (SelfEmploymentTask) task ).getTaskStatus(),
                    ( (SelfEmploymentTask) task ).getUuid().toString(),
                    ( (SelfEmploymentTask) task ).getLatOfAccident(),
                    ( (SelfEmploymentTask) task ).getLanOfAccident(),
                    dataValidateInspector.checkParam.test( ( (SelfEmploymentTask) task ).getAddress() )
                            ? ( (SelfEmploymentTask) task ).getAddress()
                            : Errors.DATA_NOT_FOUND.name() ); } }
}
