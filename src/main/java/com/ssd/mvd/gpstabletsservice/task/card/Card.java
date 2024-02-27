package com.ssd.mvd.gpstabletsservice.task.card;

import java.util.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import com.ssd.mvd.gpstabletsservice.inspectors.TaskOperations;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskCommonParams;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.CARD_102;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties ( ignoreUnknown = true )
public final class Card extends TaskOperations {
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

    private final TaskCommonParams taskCommonParams = TaskCommonParams.generate(
            CARD_102,
            this.getCardId().toString()
    );

    public Card update ( final ReportForCard reportForCard ) {
        this.getTaskCommonParams().getReportForCardList().add( reportForCard );
        return this;
    }

    @JsonDeserialize
    private List< VictimHumans > victimHumans;
}