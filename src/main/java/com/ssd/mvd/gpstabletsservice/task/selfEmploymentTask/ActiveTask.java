package com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask;

import java.util.Map;
import java.util.Date;
import java.util.UUID;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.tuple.EscortTuple;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
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

    public ActiveTask ( final Card card ) {
        this.setLatitude( card.getLatitude() );
        this.setLongitude( card.getLongitude() );

        this.setCreatedDate( new Date() );
        this.setStatus( card.getStatus() );
        this.setPatrulList( card.getPatruls() );

        this.setAddress( card.getAddress() );
        this.setDescription( card.getFabula() );
        this.setType( TaskTypes.CARD_102.name() );
        this.setTaskId( card.getUUID().toString() );

        this.setRegion( card.getEventAddress().getSOblastiId() );
        this.setDistrict( card.getEventAddress().getSRegionId() );
        this.setCountryside( card.getEventAddress().getSMahallyaId() ); }

    public ActiveTask ( final Card card, final Status status ) {
        this.setPatrulStatus( status );
        this.setCreatedDate( new Date() );
        this.setStatus( card.getStatus() );

        this.setLatitude( card.getLatitude() );
        this.setLongitude( card.getLongitude() );

        this.setAddress( card.getAddress() );
        this.setDescription( card.getFabula() );
        this.setType( TaskTypes.CARD_102.name() );
        this.setTaskId( card.getUUID().toString() );

        this.setRegion( card.getEventAddress().getSOblastiId() );
        this.setDistrict( card.getEventAddress().getSRegionId() );
        this.setCountryside( card.getEventAddress().getSMahallyaId() ); }

    public ActiveTask ( final SelfEmploymentTask selfEmploymentTask ) {
        this.setCreatedDate( new Date() );
        this.setTitle( selfEmploymentTask.getTitle() );
        this.setType( TaskTypes.SELF_EMPLOYMENT.name() );
        this.setAddress( selfEmploymentTask.getAddress() );
        this.setStatus( selfEmploymentTask.getTaskStatus() );
        this.setPatrulList( selfEmploymentTask.getPatruls() );
        this.setTaskId( selfEmploymentTask.getUuid().toString() );
        this.setLatitude( selfEmploymentTask.getLatOfAccident() );
        this.setDescription( selfEmploymentTask.getDescription() );
        this.setLongitude( selfEmploymentTask.getLanOfAccident() ); }

    public ActiveTask ( final SelfEmploymentTask card, final Status status ) {
        this.setPatrulStatus( status );
        this.setTitle( card.getTitle() );
        this.setCreatedDate( new Date() );
        this.setAddress( card.getAddress() );
        this.setStatus( card.getTaskStatus() );
        this.setTaskId( card.getUuid().toString() );
        this.setLatitude( card.getLatOfAccident() );
        this.setDescription( card.getDescription() );
        this.setLongitude( card.getLanOfAccident() );
        this.setType( TaskTypes.SELF_EMPLOYMENT.name() ); }

    public ActiveTask ( final EventFace eventFace ) {
        this.setCreatedDate( new Date() );
        this.setStatus( eventFace.getStatus() );
        this.setLatitude( eventFace.getLatitude() );
        this.setPatrulList( eventFace.getPatruls() );
        this.setLongitude( eventFace.getLongitude() );
        this.setTaskId( eventFace.getUUID().toString() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() ); }

    public ActiveTask ( final EventFace eventFace, final Status status ) {
        this.setPatrulStatus( status );
        this.setCreatedDate( new Date() );
        this.setStatus( eventFace.getStatus() );
        this.setLatitude( eventFace.getLatitude() );
        this.setPatrulList( eventFace.getPatruls() );
        this.setLongitude( eventFace.getLongitude() );
        this.setTaskId( eventFace.getUUID().toString() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() ); }

    public ActiveTask ( final EventBody eventBody ) {
        this.setCreatedDate( new Date() );
        this.setStatus( eventBody.getStatus() );
        this.setLatitude( eventBody.getLatitude() );
        this.setPatrulList( eventBody.getPatruls() );
        this.setLongitude( eventBody.getLongitude() );
        this.setTaskId( eventBody.getUUID().toString() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() ); }

    public ActiveTask ( final EventBody eventBody, final Status status ) {
        this.setPatrulStatus( status );
        this.setCreatedDate( new Date() );
        this.setStatus( eventBody.getStatus() );
        this.setLatitude( eventBody.getLatitude() );
        this.setPatrulList( eventBody.getPatruls() );
        this.setLongitude( eventBody.getLongitude() );
        this.setTaskId( eventBody.getUUID().toString() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() ); }

    public ActiveTask ( final EventCar eventCar ) {
        this.setCreatedDate( new Date() );
        this.setStatus( eventCar.getStatus() );
        this.setLatitude( eventCar.getLatitude() );
        this.setPatrulList( eventCar.getPatruls() );
        this.setLongitude( eventCar.getLongitude() );
        this.setTaskId( eventCar.getUUID().toString() );
        this.setType( TaskTypes.FIND_FACE_CAR.name() ); }

    public ActiveTask ( final EventCar eventCar, final Status status ) {
        this.setPatrulStatus( status );
        this.setCreatedDate( new Date() );
        this.setStatus( eventCar.getStatus() );
        this.setLatitude( eventCar.getLatitude() );
        this.setPatrulList( eventCar.getPatruls() );
        this.setLongitude( eventCar.getLongitude() );
        this.setTaskId( eventCar.getUUID().toString() );
        this.setType( TaskTypes.FIND_FACE_CAR.name() ); }

    public ActiveTask ( final FaceEvent faceEvents ) {
        this.setCreatedDate( new Date() );
        this.setStatus( faceEvents.getStatus() );
        this.setPatrulList( faceEvents.getPatruls() );

        this.setTaskId( faceEvents.getUUID().toString() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() );

        if ( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( faceEvents.getDataInfo() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( faceEvents.getDataInfo().getData() ) ) {
            this.setRegion( faceEvents.getDataInfo().getData().getRegion() );
            this.setAddress( faceEvents.getDataInfo().getData().getAddress() );
            this.setLatitude( faceEvents.getDataInfo().getData().getLatitude() );
            this.setDistrict( faceEvents.getDataInfo().getData().getDistrict() );
            this.setLongitude( faceEvents.getDataInfo().getData().getLongitude() );
            this.setCountryside( faceEvents.getDataInfo().getData().getCountryside() ); } }

    public ActiveTask ( final FaceEvent faceEvent, final Status status ) {
        this.setPatrulStatus( status );
        this.setCreatedDate( new Date() );
        this.setStatus( faceEvent.getStatus() );

        this.setTaskId( faceEvent.getUUID().toString() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() );

        if ( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( faceEvent.getDataInfo() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( faceEvent.getDataInfo().getData() ) ) {
            this.setRegion( faceEvent.getDataInfo().getData().getRegion() );
            this.setAddress( faceEvent.getDataInfo().getData().getAddress() );
            this.setLatitude( faceEvent.getDataInfo().getData().getLatitude() );
            this.setDistrict( faceEvent.getDataInfo().getData().getDistrict() );
            this.setLongitude( faceEvent.getDataInfo().getData().getLongitude() );
            this.setCountryside( faceEvent.getDataInfo().getData().getCountryside() ); } }

    public ActiveTask ( final CarEvent carEvent ) {
        this.setCreatedDate( new Date() );
        this.setStatus( carEvent.getStatus() );
        this.setPatrulList( carEvent.getPatruls() );

        this.setType( TaskTypes.FIND_FACE_CAR.name() );
        this.setTaskId( carEvent.getUUID().toString() );

        if ( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvent.getDataInfo() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvent.getDataInfo().getData() ) ) {
            this.setRegion( carEvent.getDataInfo().getData().getRegion() );
            this.setAddress( carEvent.getDataInfo().getData().getAddress() );
            this.setLatitude( carEvent.getDataInfo().getData().getLatitude() );
            this.setDistrict( carEvent.getDataInfo().getData().getDistrict() );
            this.setLongitude( carEvent.getDataInfo().getData().getLongitude() );
            this.setCountryside( carEvent.getDataInfo().getData().getCountryside() ); } }

    public ActiveTask ( final CarEvent carEvent, final Status status ) {
        this.setPatrulStatus( status );
        this.setCreatedDate( new Date() );
        this.setStatus( carEvent.getStatus() );

        this.setType( TaskTypes.FIND_FACE_CAR.name() );
        this.setTaskId( carEvent.getUUID().toString() );

        if ( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvent.getDataInfo() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvent.getDataInfo().getData() ) ) {
            this.setRegion( carEvent.getDataInfo().getData().getRegion() );
            this.setAddress( carEvent.getDataInfo().getData().getAddress() );
            this.setLatitude( carEvent.getDataInfo().getData().getLatitude() );
            this.setDistrict( carEvent.getDataInfo().getData().getDistrict() );
            this.setLongitude( carEvent.getDataInfo().getData().getLongitude() );
            this.setCountryside( carEvent.getDataInfo().getData().getCountryside() ); } }

    public ActiveTask ( final EscortTuple escortTuple, final Status status ) {
        this.setPatrulStatus( status );
        this.setStatus( Status.CREATED );
        this.setCreatedDate( new Date() );
        this.setType( TaskTypes.ESCORT.name() );
        this.setTaskId( escortTuple.getUuid().toString() ); }
}
