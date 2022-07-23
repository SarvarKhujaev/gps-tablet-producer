package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
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

    private Status status;
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

    @JsonDeserialize
    private List< Patrul > patruls; // the list of patruls who linked to this event
    @JsonDeserialize
    private List< VictimHumans > victimHumans;  // Jabirlanuchi inson
    @JsonDeserialize
    private List< ReportForCard > reportForCardList; // the list of reports for the current card
    @JsonDeserialize
    private Map< String, PatrulStatus > patrulStatuses; // the final status with info the time and Statuses
}