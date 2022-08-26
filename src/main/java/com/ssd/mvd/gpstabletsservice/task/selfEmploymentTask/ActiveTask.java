package com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask;

import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvents;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvents;

@Data
public class ActiveTask {
    private Double latitude;
    private Double longitude;

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
        this.setType( "card" );
        this.setStatus( card.getStatus() );
        this.setAddress( card.getAddress() );
        this.setLatitude( card.getLatitude() );
        this.setPatrulList( card.getPatruls() );
        this.setDescription( card.getFabula() );
        this.setLongitude( card.getLongitude() );
        this.setTaskId( card.getCardId().toString() );
        this.setCreatedDate( card.getCreated_date() );
        this.setPatrulStatus( this.getPatrulStatus() ); }

    public ActiveTask ( Card card, Status status ) {
        this.setType( "card" );
        this.setPatrulStatus( status );
        this.setStatus( card.getStatus() );
        this.setAddress( card.getAddress() );
        this.setLatitude( card.getLatitude() );
        this.setDescription( card.getFabula() );
        this.setLongitude( card.getLongitude() );
        this.setTaskId( card.getCardId().toString() );
        this.setCreatedDate( card.getCreated_date() ); }

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
        this.setCreatedDate( card.getIncidentDate() ); }

    public ActiveTask ( SelfEmploymentTask card, Status status ) {
        this.setPatrulStatus( status );
        this.setTitle( card.getTitle() );
        this.setType( "selfEmployment" );
        this.setAddress( card.getAddress() );
        this.setStatus( card.getTaskStatus() );
        this.setTaskId( card.getUuid().toString() );
        this.setLatitude( card.getLatOfAccident() );
        this.setDescription( card.getDescription() );
        this.setLongitude( card.getLanOfAccident() );
        this.setCreatedDate( card.getIncidentDate() ); }

    public ActiveTask( EventFace eventFace ) {
        this.setTaskId( eventFace.getId() );
        this.setStatus( eventFace.getStatus() );
        this.setPatrulList( eventFace.getPatruls() );
        this.setLatitude( eventFace.getLatitude() );
        this.setLongitude( eventFace.getLongitude() );
        this.setCreatedDate( eventFace.getCreated_date() );
        this.setType( TaskTypes.FIND_FACE_EVENT_FACE.name() ); }

    public ActiveTask ( EventFace eventFace, Status status ) {
        this.setPatrulStatus( status );
        this.setTaskId( eventFace.getId() );
        this.setStatus( eventFace.getStatus() );
        this.setLatitude( eventFace.getLatitude() );
        this.setPatrulList( eventFace.getPatruls() );
        this.setLongitude( eventFace.getLongitude() );
        this.setCreatedDate( eventFace.getCreated_date() );
        this.setType( TaskTypes.FIND_FACE_EVENT_FACE.name() ); }

    public ActiveTask( EventBody eventBody ) {
        this.setTaskId( eventBody.getId() );
        this.setStatus( eventBody.getStatus() );
        this.setPatrulList( eventBody.getPatruls() );
        this.setLatitude( eventBody.getLatitude() );
        this.setLongitude( eventBody.getLongitude() );
        this.setCreatedDate( eventBody.getCreated_date() );
        this.setType( TaskTypes.FIND_FACE_EVENT_BODY.name() ); }

    public ActiveTask ( EventBody eventBody, Status status ) {
        this.setPatrulStatus( status );
        this.setTaskId( eventBody.getId() );
        this.setStatus( eventBody.getStatus() );
        this.setLatitude( eventBody.getLatitude() );
        this.setPatrulList( eventBody.getPatruls() );
        this.setLongitude( eventBody.getLongitude() );
        this.setCreatedDate( eventBody.getCreated_date() );
        this.setType( TaskTypes.FIND_FACE_EVENT_BODY.name() ); }

    public ActiveTask ( EventCar eventCar ) {
        this.setTaskId( eventCar.getId() );
        this.setStatus( eventCar.getStatus() );
        this.setPatrulList( eventCar.getPatruls() );
        this.setLatitude( eventCar.getLatitude() );
        this.setLongitude( eventCar.getLongitude() );
        this.setCreatedDate( eventCar.getCreated_date() );
        this.setType( TaskTypes.FIND_FACE_EVENT_CAR.name() ); }

    public ActiveTask ( EventCar eventCar, Status status ) {
        this.setPatrulStatus( status );
        this.setTaskId( eventCar.getId() );
        this.setStatus( eventCar.getStatus() );
        this.setLatitude( eventCar.getLatitude() );
        this.setPatrulList( eventCar.getPatruls() );
        this.setLongitude( eventCar.getLongitude() );
        this.setCreatedDate( eventCar.getCreated_date() );
        this.setType( TaskTypes.FIND_FACE_EVENT_CAR.name() ); }

    public ActiveTask ( FaceEvents faceEvents ) {
        this.setTaskId( faceEvents.getId() );
        this.setStatus( faceEvents.getStatus() );
        this.setPatrulList( faceEvents.getPatruls() );
        this.setType( TaskTypes.FIND_FACE_EVENT_CAR.name() );
        this.setLatitude( faceEvents.getCamera().getLatitude() );
        this.setLongitude( faceEvents.getCamera().getLongitude() );
        try { this.setCreatedDate( new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse( faceEvents.getCreated_date() ) ); }
        catch ( ParseException e ) { this.setCreatedDate( new Date() ); } }

    public ActiveTask ( FaceEvents eventCar, Status status ) {
        this.setPatrulStatus( status );
        this.setTaskId( eventCar.getId() );
        this.setStatus( eventCar.getStatus() );
        this.setPatrulList( eventCar.getPatruls() );
        this.setType( TaskTypes.FIND_FACE_CAR.name() );
        this.setLatitude( eventCar.getCamera().getLatitude() );
        this.setLongitude( eventCar.getCamera().getLongitude() );
        try { this.setCreatedDate( new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse( eventCar.getCreated_date() ) ); }
        catch ( ParseException e ) { this.setCreatedDate( new Date() ); } }

    public ActiveTask ( CarEvents faceEvents ) {
        this.setTaskId( faceEvents.getId() );
        this.setStatus( faceEvents.getStatus() );
        this.setPatrulList( faceEvents.getPatruls() );
        this.setType( TaskTypes.FIND_FACE_EVENT_CAR.name() );
        this.setLatitude( faceEvents.getCamera().getLatitude() );
        this.setLongitude( faceEvents.getCamera().getLongitude() );
        try { this.setCreatedDate( new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse( faceEvents.getCreated_date() ) ); }
        catch ( ParseException e ) { this.setCreatedDate( new Date() ); } }

    public ActiveTask ( CarEvents eventCar, Status status ) {
        this.setPatrulStatus( status );
        this.setTaskId( eventCar.getId() );
        this.setStatus( eventCar.getStatus() );
        this.setPatrulList( eventCar.getPatruls() );
        this.setType( TaskTypes.FIND_FACE_CAR.name() );
        this.setLatitude( eventCar.getCamera().getLatitude() );
        this.setLongitude( eventCar.getCamera().getLongitude() );
        try { this.setCreatedDate( new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse( eventCar.getCreated_date() ) ); }
        catch ( ParseException e ) { this.setCreatedDate( new Date() ); } }
}
