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
public class ActiveTask {
    private Double latitude;
    private Double longitude;

    private Integer region;
    private Integer district;
    private Integer countryside;

    private String type;
    private String title;
    private String taskId;
    private String address;
    private String description;

    private Date createdDate;

    private Status status;
    private Status patrulStatus;

    private Map< UUID, Patrul > patrulList;

    private void save ( final DataInfo dataInfo ) {
        if (  DataValidateInspector
                .getInstance()
                .getCheckRequest()
                .test( dataInfo, 9 ) ) {
            this.setRegion( dataInfo.getData().getRegion() );
            this.setAddress( dataInfo.getData().getAddress() );
            this.setLatitude( dataInfo.getData().getLatitude() );
            this.setDistrict( dataInfo.getData().getDistrict() );
            this.setLongitude( dataInfo.getData().getLongitude() );
            this.setCountryside( dataInfo.getData().getCountryside() ); } }

    public ActiveTask (
            final Object object,
            final String taskId,
            final Status status,
            final TaskTypes taskTypes,
            final Map< UUID, Patrul > patrulList ) {
        this.setStatus( status );
        this.setTaskId( taskId );
        this.setType( taskTypes.name() );
        this.setPatrulList( patrulList );
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

            case FIND_FACE_EVENT_CAR -> {
                this.setType( TaskTypes.FIND_FACE_CAR.name() );
                this.setLatitude( ( (EventCar) object ).getLatitude() );
                this.setLongitude( ( (EventCar) object ).getLongitude() ); }

            case FIND_FACE_EVENT_FACE -> {
                this.setType( TaskTypes.FIND_FACE_PERSON.name() );
                this.setLatitude( ( (EventFace) object ).getLatitude() );
                this.setLongitude( ( (EventFace) object ).getLongitude() ); }

            case FIND_FACE_EVENT_BODY -> {
                this.setType( TaskTypes.FIND_FACE_PERSON.name() );
                this.setLatitude( ( (EventBody) object ).getLatitude() );
                this.setLongitude( ( (EventBody) object ).getLongitude() ); }

            case SELF_EMPLOYMENT -> {
                this.setTitle( ( (SelfEmploymentTask) object ).getTitle() );
                this.setAddress( ( (SelfEmploymentTask) object ).getAddress() );
                this.setLatitude( ( (SelfEmploymentTask) object ).getLatOfAccident() );
                this.setDescription( ( (SelfEmploymentTask) object ).getDescription() );
                this.setLongitude( ( (SelfEmploymentTask) object ).getLanOfAccident() ); } } }

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

            case FIND_FACE_EVENT_CAR -> {
                this.setType( TaskTypes.FIND_FACE_CAR.name() );
                this.setAddress( ( (EventCar) object ).getAddress() );
                this.setLatitude( ( (EventCar) object ).getLatitude() );
                this.setLongitude( ( (EventCar) object ).getLongitude() ); }

            case FIND_FACE_EVENT_FACE -> {
                this.setType( TaskTypes.FIND_FACE_PERSON.name() );
                this.setAddress( ( (EventFace) object ).getAddress() );
                this.setLatitude( ( (EventFace) object ).getLatitude() );
                this.setLongitude( ( (EventFace) object ).getLongitude() ); }

            case FIND_FACE_EVENT_BODY -> {
                this.setType( TaskTypes.FIND_FACE_PERSON.name() );
                this.setAddress( ( (EventBody) object ).getAddress() );
                this.setLatitude( ( (EventBody) object ).getLatitude() );
                this.setLongitude( ( (EventBody) object ).getLongitude() ); }

            case SELF_EMPLOYMENT -> {
                this.setTitle( ( (SelfEmploymentTask) object ).getTitle() );
                this.setAddress( ( (SelfEmploymentTask) object ).getAddress() );
                this.setLatitude( ( (SelfEmploymentTask) object ).getLatOfAccident() );
                this.setDescription( ( (SelfEmploymentTask) object ).getDescription() );
                this.setLongitude( ( (SelfEmploymentTask) object ).getLanOfAccident() ); } } }
}
