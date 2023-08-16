package com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import static com.ssd.mvd.gpstabletsservice.constants.Status.FINISHED;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.entity.responseForAndroid.ActiveTask;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FIND_FACE_EVENT_CAR;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public final class EventCar {
    private Status status = Status.CREATED;
    private Integer camera;
    private Boolean matched;
    private Date created_date;

    private String id;
    private String fullframe;
    private String thumbnail;
    private String dossier_photo;

    @JsonDeserialize
    private CarTotalData carTotalData;

    @JsonDeserialize
    private DataInfo dataInfo;

    private UUID uuid;

    public UUID getUUID () { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }

    public void update ( final Patrul patrul, final PatrulStatus patrulStatus ) { this.getPatrulStatuses().put( patrul.getPassportNumber(), patrulStatus ); }

    public EventCar update ( final ReportForCard reportForCard ) {
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
                        FIND_FACE_EVENT_CAR,
                        this.getPatruls() ) ); } }

    @JsonDeserialize
    private Map< UUID, Patrul > patruls = new HashMap<>(); // the list of patruls who linked to this event
    @JsonDeserialize
    private List< ReportForCard > reportForCardList = new ArrayList<>(); // the list of reports for the current card
    @JsonDeserialize
    private Map< String, PatrulStatus > patrulStatuses = new HashMap<>(); // the final status with info the time and Statuses
}
