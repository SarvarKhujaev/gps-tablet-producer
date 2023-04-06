package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.annotation.JsonProperty;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import java.util.*;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class Card {
    @JsonProperty( "CardId" )
    private Long cardId;
    private Long gomNum;  //??
    @JsonProperty("FirstOfAll")
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
    @JsonProperty( "dateCreateCard" )
    private Date created_date;   // Qachon yaratilgani

    @JsonDeserialize
    private EventAddress eventAddress;   //Voqea manzili
    @JsonDeserialize
    private EventHuman eventHuman;   // Aybdor inson

    private Status status = Status.CREATED;
    private UUID uuid;

    public UUID getUUID () { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }

    @JsonDeserialize
    private Map< UUID, Patrul > patruls = new HashMap<>(); // the list of patruls who linked to this event
    @JsonDeserialize
    private List< VictimHumans > victimHumans = new ArrayList<>();  // Jabirlanuchi inson
    @JsonDeserialize
    private List< ReportForCard > reportForCardList = new ArrayList<>(); // the list of reports for the current card
    @JsonDeserialize
    private Map< String, PatrulStatus > patrulStatuses = new HashMap<>(); // the final status with info the time and Statuses
}