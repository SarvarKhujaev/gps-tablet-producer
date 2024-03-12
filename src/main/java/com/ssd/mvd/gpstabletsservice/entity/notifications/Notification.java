package com.ssd.mvd.gpstabletsservice.entity.notifications;

import java.util.Date;
import java.util.UUID;

import com.datastax.driver.core.Row;
import java.util.function.BiFunction;

import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.Errors;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.interfaces.TaskCommonMethods;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.DataInfo;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;

public final class Notification extends DataValidateInspector {
    public String getId() {
        return this.id;
    }

    public void setId ( final String id ) {
        this.id = id;
    }

    public String getType() {
        return this.type;
    }

    public void setType ( final String type ) {
        this.type = type;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle ( final String title ) {
        this.title = title;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress ( final String address ) {
        this.address = address;
    }

    public String getCarNumber() {
        return this.carNumber;
    }

    public void setCarNumber ( final String carNumber ) {
        this.carNumber = carNumber;
    }

    public String getPoliceType() {
        return this.policeType;
    }

    public void setPoliceType ( final String policeType ) {
        this.policeType = policeType;
    }

    public String getNsfOfPatrul() {
        return this.nsfOfPatrul;
    }

    public void setNsfOfPatrul ( final String nsfOfPatrul ) {
        this.nsfOfPatrul = nsfOfPatrul;
    }

    public String getPassportSeries() {
        return this.passportSeries;
    }

    public void setPassportSeries ( final String passportSeries ) {
        this.passportSeries = passportSeries;
    }

    public void setLatitudeOfTask ( final Double latitudeOfTask ) {
        this.latitudeOfTask = latitudeOfTask;
    }

    public double getLongitudeOfTask() {
        return this.longitudeOfTask;
    }

    public void setLongitudeOfTask ( final double longitudeOfTask ) {
        this.longitudeOfTask = longitudeOfTask;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public void setUuid ( final UUID uuid ) {
        this.uuid = uuid;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus ( final Status status ) {
        this.status = status;
    }

    public Status getTaskStatus() {
        return this.taskStatus;
    }

    public void setTaskStatus ( final Status taskStatus ) {
        this.taskStatus = taskStatus;
    }

    public void setWasRead ( final boolean wasRead ) {
        this.wasRead = wasRead;
    }

    public TaskTypes getTaskTypes() {
        return this.taskTypes;
    }

    public void setTaskTypes ( final TaskTypes taskTypes ) {
        this.taskTypes = taskTypes;
    }

    public Date getNotificationWasCreated() {
        return this.notificationWasCreated;
    }

    public void setNotificationWasCreated ( final Date notificationWasCreated ) {
        this.notificationWasCreated = notificationWasCreated;
    }

    // id задачи
    private String id;
    // тип задачи
    private String type;
    // оглавление уведомления
    private String title;
    private String address;
    private String carNumber;
    private String policeType;
    private String nsfOfPatrul;
    private String passportSeries;

    private double latitudeOfTask;
    private double longitudeOfTask;

    private UUID uuid;

    private Status status;
    private Status taskStatus;

    // показывает прочитано ли уведомление
    private boolean wasRead;
    private TaskTypes taskTypes;
    // дата создания уведомления
    private Date notificationWasCreated;

    /*
    в зависимости от статуса генерирует различное сообщения для уведомления
    */
    private final BiFunction< Status, Patrul, String > generateAndSaveMessage = ( status, patrul ) ->
        switch ( status ) {
            case ACCEPTED -> String.join(
                    " ",
                    patrul.getPatrulFIOData().getName(),
                            status.name(),
                            "his task:",
                            patrul.getPatrulTaskInfo().getTaskId(),
                            patrul.getPatrulTaskInfo().getTaskTypes().name(),
                            "at:",
                    super.newDate().toString()
            );

            case ARRIVED -> String.join(
                    " ",
                    patrul.getPatrulFIOData().getName(),
                            status.name(),
                            patrul.getPatrulTaskInfo().getTaskTypes().name(),
                            "task location at:",
                    super.newDate().toString()
            );

            case ATTACHED -> String.join(
                    " ",
                    patrul.getPatrulFIOData().getName(),
                            "got new task:",
                            patrul.getPatrulTaskInfo().getTaskId(),
                            patrul.getPatrulTaskInfo().getTaskTypes().name()
            );

            case FINISHED -> String.join(
                    " ",
                    patrul.getPatrulFIOData().getName(),
                    "completed his task at:",
                    super.newDate().toString()
            );

            default -> String.join(
                    " ",
                    patrul.getPatrulFIOData().getName(),
                    "has been canceled from task at:",
                    super.newDate().toString()
            );
    };

    private void save ( final DataInfo dataInfo ) {
        if (  super.objectIsNotNull( dataInfo.getCadaster() ) ) {
            this.setLongitudeOfTask( dataInfo.getCadaster().getLongitude() );
            this.setLatitudeOfTask( dataInfo.getCadaster().getLatitude() );
            this.setAddress( dataInfo.getCadaster().getAddress() );
        }
    }

    private void save ( final Patrul patrul ) {
        this.setPoliceType( patrul.getPoliceType() );
        this.setPassportSeries( patrul.getPassportNumber() );
        this.setCarNumber( patrul.getPatrulCarInfo().getCarNumber() );
        this.setTaskTypes( patrul.getPatrulTaskInfo().getTaskTypes() );
        this.setNsfOfPatrul( patrul.getPatrulFIOData().getSurnameNameFatherName() );
    }

    private void save ( final Card card ) {
        this.setLatitudeOfTask( card.getLatitude() );
        this.setLongitudeOfTask( card.getLongitude() );

        this.setTaskStatus( card.getTaskCommonParams().getStatus() );
        this.setId( card.getTaskCommonParams().getUuid().toString() );

        this.setAddress(
                super.objectIsNotNull( card.getAddress() )
                ? card.getAddress()
                : Errors.DATA_NOT_FOUND.name() );
    }

    private void save ( final EventCar eventCar ) {
        this.setTaskStatus( eventCar.getTaskCommonParams().getStatus() );
        this.setId( eventCar.getTaskCommonParams().getUuid().toString() );

        this.setLatitudeOfTask( eventCar.getDataInfo().getCadaster().getLatitude() );
        this.setLongitudeOfTask( eventCar.getDataInfo().getCadaster().getLongitude() );
        this.setAddress(
                super.objectIsNotNull( eventCar.getDataInfo().getCadaster().getAddress() )
                ? eventCar.getDataInfo().getCadaster().getAddress()
                : Errors.DATA_NOT_FOUND.name() );
    }

    private void save ( final EventFace eventFace ) {
        this.setLatitudeOfTask( eventFace.getLatitude() );
        this.setLongitudeOfTask( eventFace.getLongitude() );

        this.setTaskStatus( eventFace.getTaskCommonParams().getStatus() );
        this.setId( eventFace.getTaskCommonParams().getUuid().toString() );

        this.setAddress(
                super.objectIsNotNull( eventFace.getAddress() )
                ? eventFace.getAddress()
                : Errors.DATA_NOT_FOUND.name() );
    }

    private void save ( final EventBody eventBody ) {
        this.setLatitudeOfTask( eventBody.getLatitude() );
        this.setLongitudeOfTask( eventBody.getLongitude() );

        this.setTaskStatus( eventBody.getTaskCommonParams().getStatus() );
        this.setId( eventBody.getTaskCommonParams().getUuid().toString() );

        this.setAddress(
                super.objectIsNotNull( eventBody.getAddress() )
                ? eventBody.getAddress()
                : Errors.DATA_NOT_FOUND.name() );
    }

    private void save ( final CarEvent carEvent ) {
        this.save( carEvent.getDataInfo() );
        this.setTaskStatus( carEvent.getTaskCommonParams().getStatus() );
        this.setId( carEvent.getTaskCommonParams().getUuid().toString() );
    }

    private void save ( final FaceEvent faceEvent ) {
        this.save( faceEvent.getDataInfo() );
        this.setTaskStatus( faceEvent.getTaskCommonParams().getStatus() );
        this.setId( faceEvent.getTaskCommonParams().getUuid().toString() );
    }

    private void save ( final SelfEmploymentTask selfEmploymentTask ) {
        this.setLatitudeOfTask( selfEmploymentTask.getLatOfAccident() );
        this.setLongitudeOfTask( selfEmploymentTask.getLanOfAccident() );

        this.setTaskStatus( selfEmploymentTask.getTaskCommonParams().getStatus() );
        this.setId( selfEmploymentTask.getTaskCommonParams().getUuid().toString() );

        this.setAddress(
                super.objectIsNotNull( selfEmploymentTask.getAddress() )
                        ? selfEmploymentTask.getAddress()
                        : Errors.DATA_NOT_FOUND.name()
        );
    }

    public static Notification generate (
            final Patrul patrul,
            final Status status,
            final TaskCommonMethods taskCommonMethods
    ) {
        return new Notification(
                patrul,
                status,
                taskCommonMethods
        );
    }

    public static Notification generate ( final Row row ) {
        return new Notification( row );
    }

    private Notification ( final Row row ) {
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
        this.setTaskStatus( super.objectIsNotNull( row.getString( "taskStatus" ) )
                ? Status.valueOf( row.getString( "taskStatus" ) )
                : Status.CREATED );
        this.setTaskTypes( TaskTypes.valueOf( row.getString( "taskTypes" ) ) );
        this.setNotificationWasCreated( row.getTimestamp( "notificationWasCreated" ) );
    }

    private Notification (
            final Patrul patrul,
            final Status status,
            final TaskCommonMethods taskCommonMethods
    ) {
        // сохраняем данные патрульного
        this.save( patrul );
        this.setStatus( status );
        this.setType( taskCommonMethods.getTaskCommonParams().getTaskTypes().name() );

        // составляем сообщение для уведомления
        this.setTitle( this.generateAndSaveMessage.apply( status, patrul ) );

        switch ( taskTypes ) {
            case CARD_102 -> this.save( ( Card ) taskCommonMethods );

            case FIND_FACE_EVENT_CAR -> this.save( (EventCar) taskCommonMethods );

            case FIND_FACE_EVENT_FACE -> this.save( (EventFace) taskCommonMethods );

            case FIND_FACE_EVENT_BODY -> this.save( (EventBody) taskCommonMethods );

            case FIND_FACE_CAR -> this.save( (CarEvent) taskCommonMethods );

            case FIND_FACE_PERSON -> this.save( (FaceEvent) taskCommonMethods );

            default -> this.save( (SelfEmploymentTask) taskCommonMethods );
        }
    }
}
