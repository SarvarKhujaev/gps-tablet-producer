package com.ssd.mvd.gpstabletsservice.entity.responseForAndroid;

import java.util.Map;
import java.util.Date;
import java.util.UUID;

import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.DataInfo;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;

@lombok.Data
public final class ActiveTask {
    private Double latitude;
    private Double longitude;

    private Integer region;
    private Integer district;
    private Integer countryside;

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

    private void save ( final DataInfo dataInfo ) {
        if (  DataValidateInspector
                .getInstance()
                .checkRequest
                .test( dataInfo, 9 ) ) {
            this.setRegion( dataInfo.getCadaster().getRegion() );
            this.setAddress( dataInfo.getCadaster().getAddress() );
            this.setLatitude( dataInfo.getCadaster().getLatitude() );
            this.setDistrict( dataInfo.getCadaster().getDistrict() );
            this.setLongitude( dataInfo.getCadaster().getLongitude() );
            this.setCountryside( dataInfo.getCadaster().getCountryside() ); } }

    private void save ( final String taskType,
                        final String address,
                        final Double latitude,
                        final Double longitude ) {
        this.setLongitude( longitude );
        this.setLatitude( latitude );
        this.setAddress( address );
        this.setType( taskType ); }

    private void save ( final Card card ) {
        this.setRegion( card.getEventAddress().getSOblastiId() );
        this.setDistrict( card.getEventAddress().getSRegionId() );
        this.setCountryside( card.getEventAddress().getSMahallyaId() ); }

    private ActiveTask save ( final String title,
                              final String address,
                              final String description,
                              final Double latitude,
                              final Double longitude ) {
        this.setDescription( description );
        this.setLongitude( longitude );
        this.setLatitude( latitude );
        this.setAddress( address );
        this.setTitle( title );
        return this; }

    public ActiveTask (
            final Object object,
            final String taskId,
            final String cardId,
            final Status status,
            final TaskTypes taskTypes,
            final Map< UUID, Patrul > patrulList ) {
        this.setCardId( cardId );
        this.setStatus( status );
        this.setTaskId( taskId );
        this.setType( taskTypes.name() );
        this.setPatrulList( patrulList );
        this.setCreatedDate( TimeInspector.getInspector().getGetNewDate().get() );

        switch ( taskTypes ) {
            case CARD_102 -> this.save( null,
                    ( (Card) object ).getAddress(),
                    ( (Card) object ).getFabula(),
                    ( (Card) object ).getLatitude(),
                    ( (Card) object ).getLongitude() )
                    .save( (Card) object );

            case FIND_FACE_CAR -> this.save( ( (CarEvent) object ).getDataInfo() );

            case FIND_FACE_PERSON -> this.save( ( (FaceEvent) object ).getDataInfo() );

            case FIND_FACE_EVENT_CAR -> this.save( TaskTypes.FIND_FACE_CAR.name(), null,
                    ( (EventCar) object ).getDataInfo().getCadaster().getLatitude(),
                    ( (EventCar) object ).getDataInfo().getCadaster().getLongitude() );

            case FIND_FACE_EVENT_FACE -> this.save( TaskTypes.FIND_FACE_PERSON.name(), null, ( (EventFace) object ).getLatitude(), ( (EventFace) object ).getLongitude() );

            case FIND_FACE_EVENT_BODY -> this.save( TaskTypes.FIND_FACE_PERSON.name(), null, ( (EventBody) object ).getLatitude(), ( (EventBody) object ).getLongitude() );

            case SELF_EMPLOYMENT -> this.save( ( (SelfEmploymentTask) object ).getTitle(),
                    ( (SelfEmploymentTask) object ).getAddress(),
                    ( (SelfEmploymentTask) object ).getDescription(),
                    ( (SelfEmploymentTask) object ).getLatOfAccident(),
                    ( (SelfEmploymentTask) object ).getLanOfAccident() ); } }

    public ActiveTask (
            final Status patrulStatus,
            final Object object,
            final String taskId,
            final String cardId,
            final Status status,
            final TaskTypes taskTypes,
            final Map< UUID, Patrul > patrulList ) {
        this.setCardId( cardId );
        this.setStatus( status );
        this.setTaskId( taskId );
        this.setType( taskTypes.name() );
        this.setPatrulList( patrulList );
        this.setPatrulStatus( patrulStatus );
        this.setCreatedDate( TimeInspector.getInspector().getGetNewDate().get() );

        switch ( taskTypes ) {
            case CARD_102 -> {
                this.setLatitude( ( (Card) object ).getLatitude() );
                this.setLongitude( ( (Card) object ).getLongitude() );

                this.setAddress( ( (Card) object ).getAddress() );
                this.setDescription( ( (Card) object ).getFabula() );

                this.setRegion( ( (Card) object ).getEventAddress().getSOblastiId() );
                this.setDistrict( ( (Card) object ).getEventAddress().getSRegionId() );
                this.setCountryside( ( (Card) object ).getEventAddress().getSMahallyaId() ); }

            case FIND_FACE_CAR -> this.save( ( (CarEvent) object ).getDataInfo() );

            case FIND_FACE_PERSON -> this.save( ( (FaceEvent) object ).getDataInfo() );

            case FIND_FACE_EVENT_CAR -> this.save( TaskTypes.FIND_FACE_CAR.name(), null, ( (EventCar) object ).getDataInfo().getCadaster().getLatitude(), ( (EventCar) object ).getDataInfo().getCadaster().getLongitude() );

            case FIND_FACE_EVENT_FACE -> this.save( TaskTypes.FIND_FACE_PERSON.name(), null, ( (EventFace) object ).getLatitude(), ( (EventFace) object ).getLongitude() );

            case FIND_FACE_EVENT_BODY -> this.save( TaskTypes.FIND_FACE_PERSON.name(), null, ( (EventBody) object ).getLatitude(), ( (EventBody) object ).getLongitude() );

            case SELF_EMPLOYMENT -> this.save( ( (SelfEmploymentTask) object ).getTitle(),
                    ( (SelfEmploymentTask) object ).getAddress(),
                    ( (SelfEmploymentTask) object ).getDescription(),
                    ( (SelfEmploymentTask) object ).getLatOfAccident(),
                    ( (SelfEmploymentTask) object ).getLanOfAccident() ); } }

    public ActiveTask ( final Object object,
                        final TaskTypes taskTypes,
                        final Status status,
                        final Status taskStatus,
                        final String taskId ) {
        this.setTaskId( taskId );
        this.setStatus( taskStatus );
        this.setPatrulStatus( status );
        this.setType( taskTypes.name() );
        this.setCreatedDate( TimeInspector.getInspector().getGetNewDate().get() );

        switch ( taskTypes ) {
            case CARD_102 -> this.save( null,
                            ( (Card) object ).getAddress(),
                            ( (Card) object ).getFabula(),
                            ( (Card) object ).getLatitude(),
                            ( (Card) object ).getLongitude() )
                    .save( (Card) object );

            case FIND_FACE_CAR -> this.save( ( (CarEvent) object ).getDataInfo() );
            case FIND_FACE_PERSON -> this.save( ( (FaceEvent) object ).getDataInfo() );

            case FIND_FACE_EVENT_CAR -> this.save( TaskTypes.FIND_FACE_CAR.name(),
                    ( (EventCar) object ).getDataInfo().getCadaster().getAddress(),
                    ( (EventCar) object ).getDataInfo().getCadaster().getLatitude(),
                    ( (EventCar) object ).getDataInfo().getCadaster().getLongitude() );

            case FIND_FACE_EVENT_FACE -> this.save( TaskTypes.FIND_FACE_PERSON.name(),
                    ( (EventFace) object ).getAddress(),
                    ( (EventFace) object ).getLatitude(),
                    ( (EventFace) object ).getLongitude() );

            case FIND_FACE_EVENT_BODY -> this.save( TaskTypes.FIND_FACE_PERSON.name(),
                    ( (EventBody) object ).getAddress(),
                    ( (EventBody) object ).getLatitude(),
                    ( (EventBody) object ).getLongitude() );

            case SELF_EMPLOYMENT -> this.save( ( (SelfEmploymentTask) object ).getTitle(),
                    ( (SelfEmploymentTask) object ).getAddress(),
                    ( (SelfEmploymentTask) object ).getDescription(),
                    ( (SelfEmploymentTask) object ).getLatOfAccident(),
                    ( (SelfEmploymentTask) object ).getLanOfAccident() ); } }
}
