package com.ssd.mvd.gpstabletsservice.task.card;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import static com.ssd.mvd.gpstabletsservice.constants.Status.FINISHED;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.CARD_102;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.entity.responseForAndroid.ActiveTask;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties ( ignoreUnknown = true )
public final class Card {
    @JsonProperty( value = "id" )
    private Long cardId;
    private Long gomNum;  //??
    @JsonProperty( value = "FirstOfAll" )
    private Long firstOfAll;  //??
    private Long deadQuantity;   //O'lganlar soni
    private Long traumaQuantity;   //Jarohatlanganlar soni

    private Integer branchId;   //???
    private Integer sEventFormsAddId;  //??
    private Integer initSeventFormsId;  //??

    private String fabula;   //????
    private String address;
    private String userFio; //Ariza berivchining F.I.SH

    private Double latitude;   // Hodisa bo'lgan joy
    private Double longitude;   // Hodisa bo'lgan joy
    private Boolean hospitalApplication;   // Ariza shifoxonadan kelgan-kelmaganligi

    private Date eventEnd;   // Tugallangan vaqt
    private Date eventStart;  // Yaratilish vaqt
    @JsonProperty( value = "dateCreateCard" )
    private Date created_date;   // Qachon yaratilgani

    @JsonDeserialize
    private EventAddress eventAddress;   //Voqea manzili
    @JsonDeserialize
    private EventHuman eventHuman;   // Aybdor inson

    private Status status = Status.CREATED;
    private UUID uuid;

    public UUID getUUID () { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }

    public void update ( final Patrul patrul, final PatrulStatus patrulStatus ) { this.getPatrulStatuses().put( patrul.getPassportNumber(), patrulStatus ); }

    public Card update ( final ReportForCard reportForCard ) {
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
                            this.getCardId().toString(),
                            this.getStatus(),
                            CARD_102,
                            this.getPatruls() ) ); } }

    @JsonDeserialize
    private Map< UUID, Patrul > patruls = new HashMap<>(); // the list of patruls who linked to this event
    @JsonDeserialize
    private List< VictimHumans > victimHumans = new ArrayList<>();  // Jabirlanuchi inson
    @JsonDeserialize
    private List< ReportForCard > reportForCardList = new ArrayList<>(); // the list of reports for the current card
    @JsonDeserialize
    private Map< String, PatrulStatus> patrulStatuses = new HashMap<>(); // the final status with info the time and Statuses
}