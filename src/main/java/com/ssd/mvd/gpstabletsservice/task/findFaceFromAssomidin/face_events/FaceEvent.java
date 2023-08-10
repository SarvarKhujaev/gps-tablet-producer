package com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.DataInfo;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;
import com.ssd.mvd.gpstabletsservice.entity.responseForAndroid.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.PsychologyCard;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PatrulStatus;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.CARD_102;
import static com.ssd.mvd.gpstabletsservice.constants.Status.FINISHED;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.card.VictimHumans;
import com.ssd.mvd.gpstabletsservice.constants.Status;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.*;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public final class FaceEvent {
    private String name;
    private String comment; // Ф.И.О
    private String dossier_photo;

    @JsonProperty( "video_archive" )
    private String video_archive;

    @JsonProperty( "id" )
    private String id;

    @JsonProperty("matched_object")
    private String matched_object;

    @JsonProperty("matched_cluster")
    private String matched_cluster;

    @JsonProperty( "temperature" )
    private String temperature;

    @JsonProperty( "created_date" )
    private String created_date;

    @JsonProperty( "thumbnail" )
    private String thumbnail;

    @JsonProperty( "fullframe" )
    private String fullframe;

    @JsonProperty( "bs_type" )
    private String bs_type;

    @JsonProperty( "quality" )
    private String quality;

    @JsonProperty( "acknowledged_date" )
    private String acknowledged_date;

    @JsonProperty( "acknowledged_by" )
    private String acknowledged_by;

    @JsonProperty( "acknowledged_reaction" )
    private String acknowledged_reaction;

    @JsonProperty( "looks_like_confidence" )
    private String looksLikeConfidence;

    @JsonProperty( "object_type" )
    private String object_type;

    @JsonProperty( "webhook_type" )
    private String webhook_type;

    @JsonProperty( "event_model_class" )
    private String event_model_class;

    @JsonProperty( "matched_card" )
    private Long matched_dossier;

    @JsonProperty( "camera" )
    private Integer camera_id;

    @JsonProperty( "camera_group" )
    private Integer camera_group;

    @JsonProperty( "episode" )
    private Integer episode;

    @JsonProperty( "frame_coords_left" )
    private Integer frame_coords_left;

    @JsonProperty("frame_coords_top")
    private Integer frame_coords_top;

    @JsonProperty("frame_coords_right")
    private Integer frame_coords_right;

    @JsonProperty("frame_coords_bottom")
    private Integer frame_coords_bottom;

    @JsonProperty("confidence")
    private Double confidence;

    @JsonProperty("cluster_confidence")
    private Integer cluster_confidence;

    @JsonDeserialize
    @JsonProperty("matched_lists")
    private List< Integer > matched_lists;

    @JsonProperty( "matched" )
    private Boolean matched;

    @JsonProperty( "acknowledged" )
    private Boolean acknowledged;

    @JsonDeserialize
    private DataInfo dataInfo;

    @JsonDeserialize
    @JsonProperty( "detector_params" )
    private DetectorParams detector_params;

    @JsonDeserialize
    @JsonProperty( "features" )
    private Features features;

    private Status status = Status.CREATED;

    private UUID uuid;

    public UUID getUUID () { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }

    public void update ( final Patrul patrul, final PatrulStatus patrulStatus ) { this.getPatrulStatuses().put( patrul.getPassportNumber(), patrulStatus ); }

    public FaceEvent update ( final ReportForCard reportForCard ) {
        this.getReportForCardList().add( reportForCard );
        return this; }

    public void update ( final Patrul patrul ) { this.getPatruls().put( patrul.getUuid(), patrul ); }

    public void remove ( final Patrul patrul ) {
        this.getPatruls().remove( CassandraDataControlForTasks
                .getInstance()
                .getDeleteRowFromTaskTimingTable()
                .apply( patrul ) ); }

    public void update () { if ( this.getPatruls().size() == this.getReportForCardList().size() ) { // в случае если количество патрульных равно количеству рапортов, то значит что таск закрыт
        this.setStatus( FINISHED );
        CassandraDataControlForTasks
                .getInstance()
                .getDeleteActiveTask()
                .accept( this.getUUID().toString() );
        if ( !this.getPatruls().isEmpty() ) KafkaDataControl // если таск закончен без удаления всех патрульных, то есть удачно завершен
                .getInstance()
                .getWriteActiveTaskToKafka()
                .accept( new ActiveTask(
                        this,
                        this.getUUID().toString(),
                        this.getId(),
                        this.getStatus(),
                        CARD_102,
                        this.getPatruls() ) ); } }

    @JsonDeserialize
    private PsychologyCard psychologyCard;

    @JsonDeserialize
    private Map< UUID, Patrul > patruls = new HashMap<>(); // the list of patruls who linked to this event
    @JsonDeserialize
    private List< VictimHumans > victimHumans = new ArrayList<>();  // Jabirlanuchi inson
    @JsonDeserialize
    private List< ReportForCard > reportForCardList = new ArrayList<>(); // the list of reports for the current card
    @JsonDeserialize
    private Map< String, PatrulStatus > patrulStatuses = new HashMap<>(); // the final status with info the time and Statuses
}