package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.interfaces.TaskCommonMethods;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskCommonParams;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FIND_FACE_EVENT_CAR;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.DataInfo;

@JsonIgnoreProperties( ignoreUnknown = true )
public final class EventCar extends DataValidateInspector implements TaskCommonMethods< EventCar > {
    public boolean isMatched() {
        return this.matched;
    }

    public void setMatched ( final boolean matched ) {
        this.matched = matched;
    }

    public double getConfidence() {
        return this.confidence;
    }

    public void setConfidence ( final double confidence ) {
        this.confidence = confidence;
    }

    public Date getCreated_date() {
        return this.created_date;
    }

    public void setCreated_date ( final Date created_date ) {
        this.created_date = created_date;
    }

    public Status getStatus() {
        return this.status;
    }

    public void setStatus ( final Status status ) {
        this.status = status;
    }

    public String getId() {
        return this.id;
    }

    public void setId ( final String id ) {
        this.id = id;
    }

    public String getFullframe() {
        return this.fullframe;
    }

    public void setFullframe ( final String fullframe ) {
        this.fullframe = fullframe;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail ( final String thumbnail ) {
        this.thumbnail = thumbnail;
    }

    public String getDossier_photo() {
        return this.dossier_photo;
    }

    public void setDossier_photo ( final String dossier_photo ) {
        this.dossier_photo = dossier_photo;
    }

    public CarTotalData getCarTotalData() {
        return this.carTotalData;
    }

    public void setCarTotalData ( final CarTotalData carTotalData ) {
        this.carTotalData = carTotalData;
    }

    public DataInfo getDataInfo() {
        return this.dataInfo;
    }

    public void setDataInfo ( final DataInfo dataInfo ) {
        this.dataInfo = dataInfo;
    }

    @Override
    public TaskCommonParams getTaskCommonParams() {
        return this.taskCommonParams;
    }

    private boolean matched;
    private double confidence;

    private Date created_date;
    private Status status = Status.CREATED;

    private String id;
    private String fullframe;
    private String thumbnail;
    private String dossier_photo;

    @JsonDeserialize
    private CarTotalData carTotalData;

    @JsonDeserialize
    private DataInfo dataInfo;

    private final TaskCommonParams taskCommonParams = TaskCommonParams.generate(
            FIND_FACE_EVENT_CAR,
            this.getId()
    );

    @Override
    public double getLatitude() {
        return super.objectIsNotNull( this.getDataInfo() )
                ? this.getDataInfo().getCadaster().getLatitude()
                : 0;
    }

    @Override
    public double getLongitude() {
        return super.objectIsNotNull( this.getDataInfo() )
                ? this.getDataInfo().getCadaster().getLongitude()
                : 0;
    }

    @Override
    public EventCar update ( final ReportForCard reportForCard ) {
        this.getTaskCommonParams().getReportForCardList().add( reportForCard );
        return this;
    }
}
