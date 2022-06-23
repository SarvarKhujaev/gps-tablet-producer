package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;
import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card { // заполняется оператором
    private String fabula;   //????
    private String userFio;  //Ariza berivchining F.I.SH

    private Double latitude;   // Hodisa bo'lgan joy
    private Double longitude;   // Hodisa bo'lgan joy

    private Boolean hospitalApplication;   // Ariza shifoxonadan kelgan-kelmaganligi

    private Integer branchId;   //???
    private Integer sEventFormsAddId;  //??
    private Integer initSeventFormsId;  //??

    private Date eventEnd;   // Tugallangan vaqt
    private Date eventStart;  // Yaratilish vaqt
    private Date dateCreateCard;   // Qachon yaratilgani

    private Long gomNum;  //??
    private Long cardId;
    private Long firstOfAll;  //??
    private Long deadQuantity;   //O'lganlar soni
    private Long traumaQuantity;   //Jarohatlanganlar soni

    private EventHuman eventHuman;   // Aybdor inson
    private EventAddress eventAddress;   //Voqea manzili
    private List< VictimHumans > victimHumans;  // Jabirlanuchi inson

    private UUID uuid = null;

    public UUID getUuid () { return this.uuid != null ? this.uuid : ( this.uuid = UUID.randomUUID() ); }

    private List< Patrul > patruls = new ArrayList<>(); // link to list of Patruls who is gonna deal with this Card
    private List< ReportForCard > reportForCards = new ArrayList<>();
    private Map< String, PatrulStatus > patrulStatuses = new HashMap<>(); // ths list of all patruls Time consumption

    public void clear () { // cleaning the hall memory
        // it is necessary to add request to Asomiddin service to send final object of Card with status
        this.getVictimHumans().forEach( victimHumans1 -> victimHumans1.setVictimAddress( null ) );
        this.getEventHuman().setHumanAddress( null );
        this.setVictimHumans( null );
        this.setEventAddress( null );
        this.setEventHuman( null ); }
}
