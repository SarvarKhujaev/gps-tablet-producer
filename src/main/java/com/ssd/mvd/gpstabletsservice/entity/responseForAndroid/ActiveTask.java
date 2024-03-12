package com.ssd.mvd.gpstabletsservice.entity.responseForAndroid;

import java.util.Map;
import java.util.Date;
import java.util.UUID;

import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.interfaces.TaskCommonMethods;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.DataInfo;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;

public final class ActiveTask extends DataValidateInspector {
    public double getLatitude() {
        return this.latitude;
    }

    public void setLatitude( final double latitude ) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void setLongitude( final double longitude ) {
        this.longitude = longitude;
    }

    public void setRegion( final int region ) {
        this.region = region;
    }

    public void setDistrict( final int district ) {
        this.district = district;
    }

    public void setCountryside( final int countryside ) {
        this.countryside = countryside;
    }

    public void setType( final String type ) {
        this.type = type;
    }

    public void setTitle( final String title ) {
        this.title = title;
    }

    public void setCardId( final String cardId ) {
        this.cardId = cardId;
    }

    public String getTaskId() {
        return this.taskId;
    }

    public void setTaskId( final String taskId ) {
        this.taskId = taskId;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress( final String address ) {
        this.address = address;
    }

    public void setDescription( final String description ) {
        this.description = description;
    }

    public Date getCreatedDate() {
        return this.createdDate;
    }

    public void setCreatedDate() {
        this.createdDate = super.newDate();
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus( final Status status ) {
        this.status = status;
    }

    public void setPatrulStatus( final Status patrulStatus ) {
        this.patrulStatus = patrulStatus;
    }

    public void setPatrulList( final Map< UUID, Patrul > patrulList ) {
        this.patrulList = patrulList;
    }

    private double latitude;
    private double longitude;

    private int region;
    private int district;
    private int countryside;

    private String type;
    private String title;
    private String cardId; // id from Asomiddin
    private String taskId; // from service itself
    private String address;
    private String description;

    private Date createdDate;

    private Status status;
    private Status patrulStatus;

    private Map< UUID, Patrul > patrulList;

    private void save ( final Card card ) {
        this.setAddress( card.getAddress() );
        this.setLatitude( card.getLatitude() );
        this.setDescription( card.getFabula() );
        this.setLongitude( card.getLongitude() );
        this.setRegion( card.getEventAddress().getSOblastiId() );
        this.setDistrict( card.getEventAddress().getSRegionId() );
        this.setCountryside( card.getEventAddress().getSMahallyaId() );
    }

    private void save ( final DataInfo dataInfo ) {
        if (  super.objectIsNotNull( dataInfo.getCadaster() ) ) {
            this.setRegion( dataInfo.getCadaster().getRegion() );
            this.setAddress( dataInfo.getCadaster().getAddress() );
            this.setLatitude( dataInfo.getCadaster().getLatitude() );
            this.setDistrict( dataInfo.getCadaster().getDistrict() );
            this.setLongitude( dataInfo.getCadaster().getLongitude() );
            this.setCountryside( dataInfo.getCadaster().getCountryside() );
        }
    }

    private void save ( final EventCar eventCar ) {
        this.setLongitude( eventCar.getDataInfo().getCadaster().getLongitude() );
        this.setLatitude( eventCar.getDataInfo().getCadaster().getLatitude() );
        this.setType( TaskTypes.FIND_FACE_CAR.name() );
        this.setAddress( null );
    }

    private void save ( final EventFace eventFace ) {
        this.setType( TaskTypes.FIND_FACE_PERSON.name() );
        this.setLongitude( eventFace.getLongitude() );
        this.setLatitude( eventFace.getLatitude() );
        this.setAddress( eventFace.getAddress() );
    }

    private void save ( final EventBody eventBody ) {
        this.setType( TaskTypes.FIND_FACE_PERSON.name() );
        this.setLongitude( eventBody.getLongitude() );
        this.setLatitude( eventBody.getLatitude() );
        this.setAddress( null );
    }

    private void save ( final SelfEmploymentTask selfEmploymentTask ) {
        this.setTitle( selfEmploymentTask.getTitle() );
        this.setAddress( selfEmploymentTask.getAddress() );
        this.setDescription( selfEmploymentTask.getDescription() );

        this.setLatitude( selfEmploymentTask.getLatOfAccident() );
        this.setLongitude( selfEmploymentTask.getLanOfAccident() );
    }

    private void save (
            final TaskCommonMethods taskCommonMethods
    ) {
        this.setStatus( taskCommonMethods.getTaskCommonParams().getStatus() );
        this.setTaskId( taskCommonMethods.getTaskCommonParams().getTaskId() );
        this.setPatrulList( taskCommonMethods.getTaskCommonParams().getPatruls() );
        this.setType( taskCommonMethods.getTaskCommonParams().getTaskTypes().name() );
        this.setCardId( taskCommonMethods.getTaskCommonParams().getUuid().toString() );

        switch ( taskCommonMethods.getTaskCommonParams().getTaskTypes() ) {
            case CARD_102 -> this.save( (Card) taskCommonMethods );

            case FIND_FACE_CAR -> this.save( ( (CarEvent) taskCommonMethods ).getDataInfo() );

            case FIND_FACE_PERSON -> this.save( ( (FaceEvent) taskCommonMethods ).getDataInfo() );

            case FIND_FACE_EVENT_CAR -> this.save( ( (EventCar) taskCommonMethods ) );

            case FIND_FACE_EVENT_FACE -> this.save( (EventFace) taskCommonMethods );

            case FIND_FACE_EVENT_BODY -> this.save( (EventBody) taskCommonMethods );

            case SELF_EMPLOYMENT -> this.save( (SelfEmploymentTask) taskCommonMethods );
        }
    }

    public static ActiveTask generate (
            final TaskCommonMethods taskCommonMethods
    ) {
        return new ActiveTask( taskCommonMethods );
    }

    private ActiveTask (
            final TaskCommonMethods taskCommonMethods
    ) {
        this.setCreatedDate();
        this.save( taskCommonMethods );
    }

    public static ActiveTask generate (
            final TaskCommonMethods taskCommonMethods,
            final Status patrulStatus
    ) {
        return new ActiveTask( taskCommonMethods, patrulStatus );
    }

    private ActiveTask (
            final TaskCommonMethods taskCommonMethods,
            final Status patrulStatus
    ) {
        this.setCreatedDate();
        this.save( taskCommonMethods );
        this.setPatrulStatus( patrulStatus );
    }
}
