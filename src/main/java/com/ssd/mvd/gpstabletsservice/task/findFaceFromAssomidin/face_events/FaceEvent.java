package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.DataInfo;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FIND_FACE_PERSON;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.PsychologyCard;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.interfaces.TaskCommonMethods;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskCommonParams;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.card.VictimHumans;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties( ignoreUnknown = true )
public final class FaceEvent extends DataValidateInspector implements TaskCommonMethods< FaceEvent > {
    public String getName() {
        return this.name;
    }

    public void setName( final String name ) {
        this.name = name;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment( final String comment ) {
        this.comment = comment;
    }

    public String getDossier_photo() {
        return this.dossier_photo;
    }

    public void setDossier_photo( final String dossier_photo ) {
        this.dossier_photo = dossier_photo;
    }

    public double getConfidence() {
        return this.confidence;
    }

    public void setConfidence( final double confidence ) {
        this.confidence = confidence;
    }

    public String getId() {
        return this.id;
    }

    public void setId( final String id ) {
        this.id = id;
    }

    public String getCreated_date() {
        return this.created_date;
    }

    public void setCreated_date( final String created_date ) {
        this.created_date = created_date;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail( final String thumbnail ) {
        this.thumbnail = thumbnail;
    }

    public String getFullframe() {
        return this.fullframe;
    }

    public void setFullframe( final String fullframe ) {
        this.fullframe = fullframe;
    }

    public DataInfo getDataInfo() {
        return this.dataInfo;
    }

    public void setDataInfo( final DataInfo dataInfo ) {
        this.dataInfo = dataInfo;
    }

    public PsychologyCard getPsychologyCard() {
        return this.psychologyCard;
    }

    public void setPsychologyCard( final PsychologyCard psychologyCard ) {
        this.psychologyCard = psychologyCard;
    }

    public List<VictimHumans> getVictimHumans() {
        return this.victimHumans;
    }

    public void setVictimHumans( final List< VictimHumans > victimHumans ) {
        this.victimHumans = victimHumans;
    }

    private String name;
    private String comment; // Ф.И.О
    private String dossier_photo;

    private double confidence;

    @JsonProperty( "id" )
    private String id;

    @JsonProperty( "created_date" )
    private String created_date;

    @JsonProperty( "thumbnail" )
    private String thumbnail;

    @JsonProperty( "fullframe" )
    private String fullframe;

    @JsonDeserialize
    private DataInfo dataInfo;

    private final TaskCommonParams taskCommonParams = TaskCommonParams.generate(
            FIND_FACE_PERSON,
            this.getId()
    );

    @JsonDeserialize
    private PsychologyCard psychologyCard;

    @JsonDeserialize
    private List<VictimHumans> victimHumans;

    @Override
    public double getLatitude() {
        return super.objectIsNotNull( this.getDataInfo().getCadaster() )
                ? this.getDataInfo().getCadaster().getLatitude()
                : 0;
    }

    @Override
    public double getLongitude() {
        return super.objectIsNotNull( this.getDataInfo().getCadaster() )
                ? this.getDataInfo().getCadaster().getLongitude()
                : 0;
    }

    @Override
    public FaceEvent update ( final ReportForCard reportForCard ) {
        this.getTaskCommonParams().getReportForCardList().add( reportForCard );
        return this;
    }

    @Override
    public TaskCommonParams getTaskCommonParams() {
        return this.taskCommonParams;
    }
}