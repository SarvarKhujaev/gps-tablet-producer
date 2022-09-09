package com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask;

import lombok.Data;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import java.text.ParseException;
import java.text.SimpleDateFormat;

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

    private List< String > images;
    private Map< UUID, Patrul > patrulList;

    public ActiveTask ( Card card ) {
        this.setStatus( card.getStatus() );
        this.setPatrulList( card.getPatruls() );
        this.setCreatedDate( card.getCreated_date() );

        this.setRegion( card.getEventAddress().getSOblastiId() );
        this.setDistrict( card.getEventAddress().getSRegionId() );
        this.setCountryside( card.getEventAddress().getSMahallyaId() );

        this.setAddress( card.getAddress() );
        this.setDescription( card.getFabula() );
        this.setType( TaskTypes.CARD_102.name() );
        this.setTaskId( card.getCardId().toString() );

        this.setLatitude( card.getLatitude() );
        this.setLongitude( card.getLongitude() );
    }

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
        this.setCreatedDate( eventFace.getCreated_date() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() ); }

    public ActiveTask( EventBody eventBody ) {
        this.setTaskId( eventBody.getId() );
        this.setStatus( eventBody.getStatus() );
        this.setPatrulList( eventBody.getPatruls() );
        this.setLatitude( eventBody.getLatitude() );
        this.setLongitude( eventBody.getLongitude() );
        this.setCreatedDate( eventBody.getCreated_date() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() ); }

    public ActiveTask ( EventBody eventBody, Status status ) {
        this.setPatrulStatus( status );
        this.setTaskId( eventBody.getId() );
        this.setStatus( eventBody.getStatus() );
        this.setLatitude( eventBody.getLatitude() );
        this.setPatrulList( eventBody.getPatruls() );
        this.setLongitude( eventBody.getLongitude() );
        this.setCreatedDate( eventBody.getCreated_date() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() ); }

    public ActiveTask ( EventCar eventCar ) {
        this.setTaskId( eventCar.getId() );
        this.setStatus( eventCar.getStatus() );
        this.setLatitude( eventCar.getLatitude() );
        this.setPatrulList( eventCar.getPatruls() );
        this.setLongitude( eventCar.getLongitude() );
        this.setCreatedDate( eventCar.getCreated_date() );
        this.setType( TaskTypes.FIND_FACE_CAR.name() ); }

    public ActiveTask ( EventCar eventCar, Status status ) {
        this.setPatrulStatus( status );
        this.setTaskId( eventCar.getId() );
        this.setStatus( eventCar.getStatus() );
        this.setLatitude( eventCar.getLatitude() );
        this.setPatrulList( eventCar.getPatruls() );
        this.setLongitude( eventCar.getLongitude() );
        this.setCreatedDate( eventCar.getCreated_date() );
        this.setType( TaskTypes.FIND_FACE_CAR.name() ); }

    public ActiveTask ( FaceEvent faceEvents ) {
        this.setStatus( faceEvents.getStatus() );
        this.setPatrulList( faceEvents.getPatruls() );

        this.setRegion( faceEvents.getDataInfo().getData().getRegion() );
        this.setDistrict( faceEvents.getDataInfo().getData().getDistrict() );
        this.setCountryside( faceEvents.getDataInfo().getData().getCountryside() );

        this.setTaskId( faceEvents.getId() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() );
        this.setAddress( faceEvents.getDataInfo().getData().getAddress() );

        this.setLatitude( faceEvents.getDataInfo().getData().getLatitude() );
        this.setLongitude( faceEvents.getDataInfo().getData().getLongitude() );

        try { this.setCreatedDate( new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse( faceEvents.getCreated_date() ) ); }
        catch ( ParseException e ) { this.setCreatedDate( new Date() ); } }

    public ActiveTask ( FaceEvent faceEvent, Status status ) {
        this.setPatrulStatus( status );
        this.setStatus( faceEvent.getStatus() );

        this.setRegion( faceEvent.getDataInfo().getData().getRegion() );
        this.setDistrict( faceEvent.getDataInfo().getData().getDistrict() );
        this.setCountryside( faceEvent.getDataInfo().getData().getCountryside() );

        this.setTaskId( faceEvent.getId() );
        this.setType( TaskTypes.FIND_FACE_PERSON.name() );
        this.setAddress( faceEvent.getDataInfo().getData().getAddress() );

        this.setLatitude( faceEvent.getDataInfo().getData().getLatitude() );
        this.setLongitude( faceEvent.getDataInfo().getData().getLongitude() );

        try { this.setCreatedDate( new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse( faceEvent.getCreated_date() ) ); }
        catch ( ParseException e ) { this.setCreatedDate( new Date() ); } }

    public ActiveTask ( CarEvent carEvent ) {
        this.setStatus( carEvent.getStatus() );
        this.setPatrulList( carEvent.getPatruls() );

        this.setRegion( carEvent.getDataInfo().getData().getRegion() );
        this.setDistrict( carEvent.getDataInfo().getData().getDistrict() );
        this.setCountryside( carEvent.getDataInfo().getData().getCountryside() );

        this.setTaskId( carEvent.getId() );
        this.setType( TaskTypes.FIND_FACE_CAR.name() );
        this.setAddress( carEvent.getDataInfo().getData().getAddress() );

        this.setLatitude( carEvent.getDataInfo().getData().getLatitude() );
        this.setLongitude( carEvent.getDataInfo().getData().getLongitude() );

        try { this.setCreatedDate( new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse( carEvent.getCreated_date() ) ); }
        catch ( ParseException e ) { this.setCreatedDate( new Date() ); } }

    public ActiveTask ( CarEvent carEvent, Status status ) {
        this.setPatrulStatus( status );
        this.setStatus( carEvent.getStatus() );

        this.setRegion( carEvent.getDataInfo().getData().getRegion() );
        this.setDistrict( carEvent.getDataInfo().getData().getDistrict() );
        this.setCountryside( carEvent.getDataInfo().getData().getCountryside() );

        this.setTaskId( carEvent.getId() );
        this.setType( TaskTypes.FIND_FACE_CAR.name() );
        this.setAddress( carEvent.getDataInfo().getData().getAddress() );

        this.setLatitude( carEvent.getDataInfo().getData().getLatitude() );
        this.setLongitude( carEvent.getDataInfo().getData().getLongitude() );

        try { this.setCreatedDate( new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse( carEvent.getCreated_date() ) ); }
        catch ( ParseException e ) { this.setCreatedDate( new Date() ); } }

    public ActiveTask( EscortTuple escortTuple, Status status ) {
        this.setPatrulStatus( status );
        this.setStatus( Status.CREATED );
        this.setType( TaskTypes.ESCORT.name() );
        this.setTaskId( escortTuple.getUuid().toString() ); }
}
