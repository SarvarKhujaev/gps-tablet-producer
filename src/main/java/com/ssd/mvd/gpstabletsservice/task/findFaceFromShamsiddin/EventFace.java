package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;
import com.ssd.mvd.gpstabletsservice.entity.responseForAndroid.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.PsychologyCard;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PatrulStatus;

import static com.ssd.mvd.gpstabletsservice.constants.Status.FINISHED;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.*;

import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.constants.Status;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.*;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public final class EventFace {
    private Long age;
    private Status status = Status.CREATED;
    private Integer camera;
    private Boolean matched;
    private Date created_date;

    private Double latitude;
    private Double longitude;
    private Double confidence;

    private byte[] fullframebytes;
    private byte[] thumbnailbytes;

    private String id;
    private String address; // coming from front end
    private String cameraIp; // coming from front end
    private String fullframe;
    private String thumbnail;
    private String matched_dossier;

    @JsonDeserialize
    private PsychologyCard psychologyCard;

    @JsonDeserialize
    private Map< String, String > beard;
    @JsonDeserialize
    private Map< String, String > gender;
    @JsonDeserialize
    private Map< String, String > glasses;
    @JsonDeserialize
    private Map< String, String > medmask;

    private UUID uuid;

    public UUID getUUID () { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }

    public void update ( final Patrul patrul, final PatrulStatus patrulStatus ) { this.getPatrulStatuses().put( patrul.getPassportNumber(), patrulStatus ); }

    public EventFace update ( final ReportForCard reportForCard ) {
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
                        FIND_FACE_EVENT_FACE,
                        this.getPatruls() ) ); } }

    @JsonDeserialize
    private Map< UUID, Patrul> patruls = new HashMap<>(); // the list of patruls who linked to this event
    @JsonDeserialize
    private List< ReportForCard > reportForCardList = new ArrayList<>(); // the list of reports for the current card
    @JsonDeserialize
    private Map< String, PatrulStatus > patrulStatuses = new HashMap<>(); // the final status with info the time and Statuses
}
