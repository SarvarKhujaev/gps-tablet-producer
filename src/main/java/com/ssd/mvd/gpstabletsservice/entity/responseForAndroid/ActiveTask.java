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

    public ActiveTask ( final Object object, final TaskTypes taskTypes ) {
        this.setCreatedDate( TimeInspector
                .getInspector()
                .getGetNewDate()
                .get() );

        switch ( taskTypes ) {
            case CARD_102 -> {
                this.setType( taskTypes.name() );
                this.setLatitude( ( (Card) object ).getLatitude() );
                this.setLongitude( ( (Card) object ).getLongitude() );

                this.setStatus( ( (Card) object ).getStatus() );
                this.setPatrulList( ( (Card) object ).getPatruls() );

                this.setAddress( ( (Card) object ).getAddress() );
                this.setDescription( ( (Card) object ).getFabula() );
                this.setTaskId( ( (Card) object ).getUUID().toString() );

                this.setRegion( ( (Card) object ).getEventAddress().getSOblastiId() );
                this.setDistrict( ( (Card) object ).getEventAddress().getSRegionId() );
                this.setCountryside( ( (Card) object ).getEventAddress().getSMahallyaId() ); }

            case FIND_FACE_CAR -> {
                this.setType( taskTypes.name() );
                this.setStatus( ( (CarEvent) object ).getStatus() );
                this.setPatrulList( ( (CarEvent) object ).getPatruls() );
                this.setTaskId( ( (CarEvent) object ).getUUID().toString() );

                if ( DataValidateInspector
                        .getInstance()
                        .getCheckParam()
                        .test( ( (CarEvent) object ).getDataInfo() )
                        && DataValidateInspector
                        .getInstance()
                        .getCheckParam()
                        .test( ( (CarEvent) object ).getDataInfo().getData() ) ) {
                    this.setRegion( ( (CarEvent) object ).getDataInfo().getData().getRegion() );
                    this.setAddress( ( (CarEvent) object ).getDataInfo().getData().getAddress() );
                    this.setLatitude( ( (CarEvent) object ).getDataInfo().getData().getLatitude() );
                    this.setDistrict( ( (CarEvent) object ).getDataInfo().getData().getDistrict() );
                    this.setLongitude( ( (CarEvent) object ).getDataInfo().getData().getLongitude() );
                    this.setCountryside( ( (CarEvent) object ).getDataInfo().getData().getCountryside() ); } }
            case FIND_FACE_PERSON -> {
                this.setType( taskTypes.name() );
                this.setStatus( ( (FaceEvent) object ).getStatus() );
                this.setPatrulList( ( (FaceEvent) object ).getPatruls() );
                this.setTaskId( ( (FaceEvent) object ).getUUID().toString() );

                if ( DataValidateInspector
                        .getInstance()
                        .getCheckParam()
                        .test( ( (FaceEvent) object ).getDataInfo() )
                        && DataValidateInspector
                        .getInstance()
                        .getCheckParam()
                        .test( ( (FaceEvent) object ).getDataInfo().getData() ) ) {
                    this.setRegion( ( (FaceEvent) object ).getDataInfo().getData().getRegion() );
                    this.setAddress( ( (FaceEvent) object ).getDataInfo().getData().getAddress() );
                    this.setLatitude( ( (FaceEvent) object ).getDataInfo().getData().getLatitude() );
                    this.setDistrict( ( (FaceEvent) object ).getDataInfo().getData().getDistrict() );
                    this.setLongitude( ( (FaceEvent) object ).getDataInfo().getData().getLongitude() );
                    this.setCountryside( ( (FaceEvent) object ).getDataInfo().getData().getCountryside() ); } }

            case FIND_FACE_EVENT_CAR -> {
                this.setType( TaskTypes.FIND_FACE_CAR.name() );
                this.setStatus( ( (EventCar) object ).getStatus() );
                this.setLatitude( ( (EventCar) object ).getLatitude() );
                this.setPatrulList( ( (EventCar) object ).getPatruls() );
                this.setLongitude( ( (EventCar) object ).getLongitude() );
                this.setTaskId( ( (EventCar) object ).getUUID().toString() ); }

            case FIND_FACE_EVENT_FACE -> {
                this.setType( TaskTypes.FIND_FACE_PERSON.name() );
                this.setStatus( ( (EventFace) object ).getStatus() );
                this.setLatitude( ( (EventFace) object ).getLatitude() );
                this.setPatrulList( ( (EventFace) object ).getPatruls() );
                this.setLongitude( ( (EventFace) object ).getLongitude() );
                this.setTaskId( ( (EventFace) object ).getUUID().toString() ); }

            case FIND_FACE_EVENT_BODY -> {
                this.setType( TaskTypes.FIND_FACE_PERSON.name() );
                this.setStatus( ( (EventBody) object ).getStatus() );
                this.setLatitude( ( (EventBody) object ).getLatitude() );
                this.setPatrulList( ( (EventBody) object ).getPatruls() );
                this.setLongitude( ( (EventBody) object ).getLongitude() );
                this.setTaskId( ( (EventBody) object ).getUUID().toString() ); }

            case SELF_EMPLOYMENT -> {
                this.setType( taskTypes.name() );
                this.setTitle( ( (SelfEmploymentTask) object ).getTitle() );
                this.setAddress( ( (SelfEmploymentTask) object ).getAddress() );
                this.setStatus( ( (SelfEmploymentTask) object ).getTaskStatus() );
                this.setPatrulList( ( (SelfEmploymentTask) object ).getPatruls() );
                this.setTaskId( ( (SelfEmploymentTask) object ).getUuid().toString() );
                this.setLatitude( ( (SelfEmploymentTask) object ).getLatOfAccident() );
                this.setDescription( ( (SelfEmploymentTask) object ).getDescription() );
                this.setLongitude( ( (SelfEmploymentTask) object ).getLanOfAccident() ); } } }

    public ActiveTask ( final Object object, final TaskTypes taskTypes, final Status status ) {
        this.setCreatedDate( TimeInspector
                .getInspector()
                .getGetNewDate()
                .get() );

        switch ( taskTypes ) {
            case CARD_102 -> {
                this.setPatrulStatus( status );
                this.setType( taskTypes.name() );
                this.setStatus( ( (Card) object ).getStatus() );

                this.setLatitude( ( (Card) object ).getLatitude() );
                this.setLongitude( ( (Card) object ).getLongitude() );

                this.setAddress( ( (Card) object ).getAddress() );
                this.setDescription( ( (Card) object ).getFabula() );
                this.setTaskId( ( (Card) object ).getUUID().toString() );

                this.setRegion( ( (Card) object ).getEventAddress().getSOblastiId() );
                this.setDistrict( ( (Card) object ).getEventAddress().getSRegionId() );
                this.setCountryside( ( (Card) object ).getEventAddress().getSMahallyaId() ); }

            case FIND_FACE_CAR -> {
                this.setPatrulStatus( status );
                this.setType( taskTypes.name() );

                this.setStatus( ( (CarEvent) object ).getStatus() );
                this.setTaskId( ( (CarEvent) object ).getUUID().toString() );

                if ( DataValidateInspector
                        .getInstance()
                        .getCheckParam()
                        .test( ( (CarEvent) object ).getDataInfo() )
                        && DataValidateInspector
                        .getInstance()
                        .getCheckParam()
                        .test( ( (CarEvent) object ).getDataInfo().getData() ) ) {
                    this.setRegion( ( (CarEvent) object ).getDataInfo().getData().getRegion() );
                    this.setAddress( ( (CarEvent) object ).getDataInfo().getData().getAddress() );
                    this.setLatitude( ( (CarEvent) object ).getDataInfo().getData().getLatitude() );
                    this.setDistrict( ( (CarEvent) object ).getDataInfo().getData().getDistrict() );
                    this.setLongitude( ( (CarEvent) object ).getDataInfo().getData().getLongitude() );
                    this.setCountryside( ( (CarEvent) object ).getDataInfo().getData().getCountryside() ); } }

            case FIND_FACE_PERSON -> {
                this.setPatrulStatus( status );
                this.setType( taskTypes.name() );

                this.setStatus( ( (FaceEvent) object ).getStatus() );
                this.setTaskId( ( (FaceEvent) object ).getUUID().toString() );

                if ( DataValidateInspector
                        .getInstance()
                        .getCheckParam()
                        .test( ( (FaceEvent) object ).getDataInfo() )
                        && DataValidateInspector
                        .getInstance()
                        .getCheckParam()
                        .test( ( (FaceEvent) object ).getDataInfo().getData() ) ) {
                    this.setRegion( ( (FaceEvent) object ).getDataInfo().getData().getRegion() );
                    this.setAddress( ( (FaceEvent) object ).getDataInfo().getData().getAddress() );
                    this.setLatitude( ( (FaceEvent) object ).getDataInfo().getData().getLatitude() );
                    this.setDistrict( ( (FaceEvent) object ).getDataInfo().getData().getDistrict() );
                    this.setLongitude( ( (FaceEvent) object ).getDataInfo().getData().getLongitude() );
                    this.setCountryside( ( (FaceEvent) object ).getDataInfo().getData().getCountryside() ); } }

            case FIND_FACE_EVENT_CAR -> {
                this.setPatrulStatus( status );
                this.setType( TaskTypes.FIND_FACE_CAR.name() );
                this.setStatus( ( (EventCar) object ).getStatus() );
                this.setLatitude( ( (EventCar) object ).getLatitude() );
                this.setPatrulList( ( (EventCar) object ).getPatruls() );
                this.setLongitude( ( (EventCar) object ).getLongitude() );
                this.setTaskId( ( (EventCar) object ).getUUID().toString() ); }

            case FIND_FACE_EVENT_FACE -> {
                this.setPatrulStatus( status );
                this.setType( TaskTypes.FIND_FACE_PERSON.name() );
                this.setStatus( ( (EventFace) object ).getStatus() );
                this.setLatitude( ( (EventFace) object ).getLatitude() );
                this.setPatrulList( ( (EventFace) object ).getPatruls() );
                this.setLongitude( ( (EventFace) object ).getLongitude() );
                this.setTaskId( ( (EventFace) object ).getUUID().toString() ); }

            case FIND_FACE_EVENT_BODY -> {
                this.setPatrulStatus( status );
                this.setType( TaskTypes.FIND_FACE_PERSON.name() );
                this.setStatus( ( (EventBody) object ).getStatus() );
                this.setLatitude( ( (EventBody) object ).getLatitude() );
                this.setPatrulList( ( (EventBody) object ).getPatruls() );
                this.setLongitude( ( (EventBody) object ).getLongitude() );
                this.setTaskId( ( (EventBody) object ).getUUID().toString() ); }

            case SELF_EMPLOYMENT -> {
                this.setPatrulStatus( status );
                this.setType( taskTypes.name() );

                this.setTitle( ( (SelfEmploymentTask) object ).getTitle() );
                this.setAddress( ( (SelfEmploymentTask) object ).getAddress() );
                this.setStatus( ( (SelfEmploymentTask) object ).getTaskStatus() );
                this.setTaskId( ( (SelfEmploymentTask) object ).getUuid().toString() );
                this.setLatitude( ( (SelfEmploymentTask) object ).getLatOfAccident() );
                this.setDescription( ( (SelfEmploymentTask) object ).getDescription() );
                this.setLongitude( ( (SelfEmploymentTask) object ).getLanOfAccident() ); } } }
}
