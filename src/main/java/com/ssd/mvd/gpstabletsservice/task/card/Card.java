package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {
    private String fabula;   //????
    private String userFio;//Ariza berivchining F.I.SH
    private Long id;
    private Long gomNum;  //??
    private Long firstOfAll;  //??

    private Integer branchId;   //???
    private Integer sEventFormsAddId;  //??
    private Integer initSeventFormsId;  //??

    private Double latitude;   // Hodisa bo'lgan joy
    private Double longitude;   // Hodisa bo'lgan joy

    private Status status;
    private Boolean hospitalApplication;   // Ariza shifoxonadan kelgan-kelmaganligi

    private Date created_date;   // Qachon yaratilgani
    private Date eventStart;  // Yaratilish vaqt
    private Date eventEnd;   // Tugallangan vaqt

    private Long traumaQuantity;   //Jarohatlanganlar soni
    private Long deadQuantity;   //O'lganlar soni

    @JsonDeserialize
    private EventAddress eventAddress;   //Voqea manzili
    @JsonDeserialize
    private EventHuman eventHuman;   // Aybdor inson

    @JsonDeserialize
    private List< Patrul > patruls;
    @JsonDeserialize
    private List< VictimHumans > victimHumans;  // Jabirlanuchi inson
    @JsonDeserialize
    private List< ReportForCard > reportForCardList;
    @JsonDeserialize
    private Map< String, PatrulStatus > patrulStatuses;
}