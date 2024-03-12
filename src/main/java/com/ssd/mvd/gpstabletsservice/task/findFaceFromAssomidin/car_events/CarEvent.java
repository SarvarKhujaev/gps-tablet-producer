package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events;

import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FIND_FACE_CAR;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
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
public final class CarEvent extends DataValidateInspector implements TaskCommonMethods< CarEvent > {
    public String getName() {
        return this.name;
    }

    public void setName ( final String name ) {
        this.name = name;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment ( final String comment ) {
        this.comment = comment;
    }

    public Double getConfidence() {
        return this.confidence;
    }

    public void setConfidence ( final Double confidence ) {
        this.confidence = confidence;
    }

    public String getDossier_photo() {
        return this.dossier_photo;
    }

    public void setDossier_photo ( final String dossier_photo ) {
        this.dossier_photo = dossier_photo;
    }

    public String getId() {
        return this.id;
    }

    public void setId ( final String id ) {
        this.id = id;
    }

    public String getThumbnail() {
        return this.thumbnail;
    }

    public void setThumbnail ( final String thumbnail ) {
        this.thumbnail = thumbnail;
    }

    public String getFullframe() {
        return this.fullframe;
    }

    public void setFullframe ( final String fullframe ) {
        this.fullframe = fullframe;
    }

    public String getCreated_date() {
        return this.created_date;
    }

    public void setCreated_date ( final String created_date ) {
        this.created_date = created_date;
    }

    public DataInfo getDataInfo() {
        return this.dataInfo;
    }

    public void setDataInfo ( final DataInfo dataInfo ) {
        this.dataInfo = dataInfo;
    }

    public CarTotalData getCarTotalData() {
        return this.carTotalData;
    }

    public void setCarTotalData(CarTotalData carTotalData) {
        this.carTotalData = carTotalData;
    }

    public List<VictimHumans> getVictimHumans() {
        return this.victimHumans;
    }

    public void setVictimHumans(List<VictimHumans> victimHumans) {
        this.victimHumans = victimHumans;
    }

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
    public TaskCommonParams getTaskCommonParams() {
        return this.taskCommonParams;
    }

    @Override
    public CarEvent update ( final ReportForCard reportForCard ) {
        this.getTaskCommonParams().getReportForCardList().add( reportForCard );
        return this;
    }

    private String name;
    private String comment;
    private Double confidence;
    private String dossier_photo;

    @JsonProperty( value = "id" )
    private String id;

    @JsonProperty( value = "thumbnail" )
    private String thumbnail;

    @JsonProperty( value = "fullframe" )
    private String fullframe;

    @JsonProperty( value = "created_date" )
    private String created_date;

    @JsonDeserialize
    private DataInfo dataInfo;

    private final TaskCommonParams taskCommonParams = TaskCommonParams.generate(
            FIND_FACE_CAR,
            this.getId()
    );

    @JsonDeserialize
    private CarTotalData carTotalData;

    @JsonDeserialize
    private List< VictimHumans > victimHumans;
}