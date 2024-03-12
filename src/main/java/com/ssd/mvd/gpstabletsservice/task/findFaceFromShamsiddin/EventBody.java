package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FIND_FACE_EVENT_BODY;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.PsychologyCard;
import com.ssd.mvd.gpstabletsservice.interfaces.TaskCommonMethods;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskCommonParams;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.*;

@JsonIgnoreProperties( ignoreUnknown = true )
public final class EventBody implements TaskCommonMethods< EventBody > {
    public int getCamera() {
        return this.camera;
    }

    public void setCamera ( final int camera ) {
        this.camera = camera;
    }

    public boolean isMatched() {
        return this.matched;
    }

    public void setMatched ( final boolean matched ) {
        this.matched = matched;
    }

    public Date getCreated_date() {
        return this.created_date;
    }

    public void setCreated_date ( final Date created_date ) {
        this.created_date = created_date;
    }

    public void setLatitude ( final double latitude ) {
        this.latitude = latitude;
    }

    public void setLongitude ( final double longitude ) {
        this.longitude = longitude;
    }

    public double getConfidence() {
        return this.confidence;
    }

    public void setConfidence ( final double confidence ) {
        this.confidence = confidence;
    }

    public String getId() {
        return this.id;
    }

    public void setId ( final String id ) {
        this.id = id;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress ( final String address ) {
        this.address = address;
    }

    public String getCameraIp() {
        return this.cameraIp;
    }

    public void setCameraIp ( final String cameraIp ) {
        this.cameraIp = cameraIp;
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

    public String getMatched_dossier() {
        return this.matched_dossier;
    }

    public void setMatched_dossier ( final String matched_dossier ) {
        this.matched_dossier = matched_dossier;
    }

    public PsychologyCard getPsychologyCard() {
        return this.psychologyCard;
    }

    public void setPsychologyCard ( final PsychologyCard psychologyCard ) {
        this.psychologyCard = psychologyCard;
    }

    private int camera;
    private boolean matched;
    private Date created_date;

    private double latitude;
    private double longitude;
    private double confidence;

    private String id;
    private String address; // coming from front end
    private String cameraIp; // coming from front end
    private String fullframe;
    private String thumbnail;
    private String matched_dossier;

    @JsonDeserialize
    private PsychologyCard psychologyCard;

    private final TaskCommonParams taskCommonParams = TaskCommonParams.generate(
            FIND_FACE_EVENT_BODY,
            this.getId()
    );

    @Override
    public double getLatitude() {
        return this.latitude;
    }

    @Override
    public double getLongitude() {
        return this.longitude;
    }

    @Override
    public TaskCommonParams getTaskCommonParams() {
        return this.taskCommonParams;
    }

    @Override
    public EventBody update ( final ReportForCard reportForCard ) {
        this.getTaskCommonParams().getReportForCardList().add( reportForCard );
        return this;
    }
}
