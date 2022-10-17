package com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask;

import lombok.Data;
import java.util.Map;
import java.util.Date;
import java.util.UUID;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.tuple.EscortTuple;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;

@Data
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

    public ActiveTask ( Card card ) {
        this.setLatitude( card.getLatitude() );
        this.setLongitude( card.getLongitude() );

        this.setStatus( card.getStatus() );
        this.setPatrulList( card.getPatruls() );
        this.setCreatedDate( card.getCreated_date() );

        this.setAddress( card.getAddress() );
        this.setDescription( card.getFabula() );
        this.setType( TaskTypes.CARD_102.name() );
        this.setTaskId( card.getCardId().toString() );

        this.setRegion( card.getEventAddress().getSOblastiId() );
        this.setDistrict( card.getEventAddress().getSRegionId() );
        this.setCountryside( card.getEventAddress().getSMahallyaId() ); }

    public ActiveTask ( Card card, Status status ) {
        this.setPatrulStatus( status );
        this.setStatus( card.getStatus() );
        this.setCreatedDate( card.getCreated_date() );

        this.setLatitude( card.getLatitude() );
        this.setLongitude( card.getLongitude() );

        this.setAddress( card.getAddress() );
        this.setDescription( card.getFabula() );
        this.setType( TaskTypes.CARD_102.name() );
        this.setTaskId( card.getCardId().toString() );

        this.setRegion( card.getEventAddress().getSOblastiId() );
        this.setDistrict( card.getEventAddress().getSRegionId() );
        this.setCountryside( card.getEventAddress().getSMahallyaId() ); }

    public ActiveTask ( SelfEmploymentTask card ) {
        this.setTitle( card.getTitle() );
        this.setType( "selfEmployment" );
        this.setAddress( card.getAddress() );
        this.setStatus( card.getTaskStatus() );
        this.setPatrulList( card.getPatruls() );
        this.setTaskId( card.getUuid().toString() );
        this.setLatitude( card.getLatOfAccident() );
        this.setDescription( card.getDescription() );
        this.setLongitude( card.getLanOfAccident() );
        this.setCreatedDate( card.getIncidentDate() );
        this.setType( TaskTypes.SELF_EMPLOYMENT.name() ); }

    public ActiveTask ( SelfEmploymentTask card, Status status ) {
        this.setPatrulStatus( status );
        this.setTitle( card.getTitle() );
        this.setAddress( card.getAddress() );
        this.setStatus( card.getTaskStatus() );
        this.setTaskId( card.getUuid().toString() );
        this.setLatitude( card.getLatOfAccident() );
        this.setDescription( card.getDescription() );
        this.setLongitude( card.getLanOfAccident() );
        this.setCreatedDate( card.getIncidentDate() );
        this.setType( TaskTypes.SELF_EMPLOYMENT.name() ); }

    public ActiveTask( EventFace eventFace ) {
        this.setTaskId( eventFace.getId() );
        this.setStatus( eventFace.getStatus() );
        this.setLatitude( eventFace.getLatitude() );
        this.setPatrulList( eventFace.getPatruls() );
        this.setLongitude( eventFace.getLongitude() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() );
        this.setCreatedDate( eventFace.getCreated_date() ); }

    public ActiveTask ( EventFace eventFace, Status status ) {
        this.setPatrulStatus( status );
        this.setTaskId( eventFace.getId() );
        this.setStatus( eventFace.getStatus() );
        this.setLatitude( eventFace.getLatitude() );
        this.setPatrulList( eventFace.getPatruls() );
        this.setLongitude( eventFace.getLongitude() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() );
        this.setCreatedDate( eventFace.getCreated_date() ); }

    public ActiveTask( EventBody eventBody ) {
        this.setTaskId( eventBody.getId() );
        this.setStatus( eventBody.getStatus() );
        this.setLatitude( eventBody.getLatitude() );
        this.setPatrulList( eventBody.getPatruls() );
        this.setLongitude( eventBody.getLongitude() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() );
        this.setCreatedDate( eventBody.getCreated_date() ); }

    public ActiveTask ( EventBody eventBody, Status status ) {
        this.setPatrulStatus( status );
        this.setTaskId( eventBody.getId() );
        this.setStatus( eventBody.getStatus() );
        this.setLatitude( eventBody.getLatitude() );
        this.setPatrulList( eventBody.getPatruls() );
        this.setLongitude( eventBody.getLongitude() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() );
        this.setCreatedDate( eventBody.getCreated_date() ); }

    public ActiveTask ( EventCar eventCar ) {
        this.setTaskId( eventCar.getId() );
        this.setStatus( eventCar.getStatus() );
        this.setLatitude( eventCar.getLatitude() );
        this.setPatrulList( eventCar.getPatruls() );
        this.setLongitude( eventCar.getLongitude() );
        this.setType( TaskTypes.FIND_FACE_CAR.name() );
        this.setCreatedDate( eventCar.getCreated_date() ); }

    public ActiveTask ( EventCar eventCar, Status status ) {
        this.setPatrulStatus( status );
        this.setTaskId( eventCar.getId() );
        this.setStatus( eventCar.getStatus() );
        this.setLatitude( eventCar.getLatitude() );
        this.setPatrulList( eventCar.getPatruls() );
        this.setLongitude( eventCar.getLongitude() );
        this.setType( TaskTypes.FIND_FACE_CAR.name() );
        this.setCreatedDate( eventCar.getCreated_date() ); }

    public ActiveTask ( FaceEvent faceEvents ) {
        this.setCreatedDate( new Date() );
        this.setStatus( faceEvents.getStatus() );
        this.setPatrulList( faceEvents.getPatruls() );

        this.setTaskId( faceEvents.getId() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() );

        if ( faceEvents.getDataInfo() != null
                && faceEvents.getDataInfo().getData() != null ) {
            this.setRegion( faceEvents.getDataInfo().getData().getRegion() );
            this.setAddress( faceEvents.getDataInfo().getData().getAddress() );
            this.setLatitude( faceEvents.getDataInfo().getData().getLatitude() );
            this.setDistrict( faceEvents.getDataInfo().getData().getDistrict() );
            this.setLongitude( faceEvents.getDataInfo().getData().getLongitude() );
            this.setCountryside( faceEvents.getDataInfo().getData().getCountryside() ); } }

    public ActiveTask ( FaceEvent faceEvent, Status status ) {
        this.setPatrulStatus( status );
        this.setCreatedDate( new Date() );
        this.setStatus( faceEvent.getStatus() );

        this.setTaskId( faceEvent.getId() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() );

        if ( faceEvent.getDataInfo() != null
                && faceEvent.getDataInfo().getData() != null ) {
            this.setRegion( faceEvent.getDataInfo().getData().getRegion() );
            this.setAddress( faceEvent.getDataInfo().getData().getAddress() );
            this.setLatitude( faceEvent.getDataInfo().getData().getLatitude() );
            this.setDistrict( faceEvent.getDataInfo().getData().getDistrict() );
            this.setLongitude( faceEvent.getDataInfo().getData().getLongitude() );
            this.setCountryside( faceEvent.getDataInfo().getData().getCountryside() ); } }

    public ActiveTask ( CarEvent carEvent ) {
        this.setCreatedDate( new Date() );
        this.setStatus( carEvent.getStatus() );
        this.setPatrulList( carEvent.getPatruls() );

        this.setTaskId( carEvent.getId() );
        this.setType( TaskTypes.FIND_FACE_CAR.name() );

        if ( carEvent.getDataInfo() != null
                && carEvent.getDataInfo().getData() != null ) {
            this.setRegion( carEvent.getDataInfo().getData().getRegion() );
            this.setAddress( carEvent.getDataInfo().getData().getAddress() );
            this.setLatitude( carEvent.getDataInfo().getData().getLatitude() );
            this.setDistrict( carEvent.getDataInfo().getData().getDistrict() );
            this.setLongitude( carEvent.getDataInfo().getData().getLongitude() );
            this.setCountryside( carEvent.getDataInfo().getData().getCountryside() ); } }

    public ActiveTask ( CarEvent carEvent, Status status ) {
        this.setPatrulStatus( status );
        this.setCreatedDate( new Date() );
        this.setStatus( carEvent.getStatus() );

        this.setTaskId( carEvent.getId() );
        this.setType( TaskTypes.FIND_FACE_CAR.name() );

        if ( carEvent.getDataInfo() != null
                && carEvent.getDataInfo().getData() != null ) {
            this.setRegion( carEvent.getDataInfo().getData().getRegion() );
            this.setAddress( carEvent.getDataInfo().getData().getAddress() );
            this.setLatitude( carEvent.getDataInfo().getData().getLatitude() );
            this.setDistrict( carEvent.getDataInfo().getData().getDistrict() );
            this.setLongitude( carEvent.getDataInfo().getData().getLongitude() );
            this.setCountryside( carEvent.getDataInfo().getData().getCountryside() ); } }

    public ActiveTask( EscortTuple escortTuple, Status status ) {
        this.setPatrulStatus( status );
        this.setStatus( Status.CREATED );
        this.setType( TaskTypes.ESCORT.name() );
        this.setTaskId( escortTuple.getUuid().toString() ); }
}
