package com.ssd.mvd.gpstracker.task.card;

import com.ssd.mvd.gpstracker.entity.Patrul;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card { // заполняется оператором
    private Boolean hostpitalApplication; // показывает где был сделан звонок на улице или уже в больнице

    private String fabula; // description
    private String status = "new"; // Tayinlangan, Yangi by default, Bajarilgan
    private String userFullName; // имя, отчество и фамилия того кто звонил

    private Double latitude; // местонахождение того кто звонил
    private Double longitude;

    private Long gomNum; // ??
    private Long deadQuanity; // количество погибших
    private Long traumaQuanity; // уровень нанесенного уровня

    private Date eventEnd; // начало звонка
    private Date eventStart; // конец звонка
    private Date dateCreateCard; // дата создания бланка

    private Integer cardId; // создается со стороны оператора
    private Integer branchId; // ???
    private Integer firstOfAll; // ??
    private Integer seventFromsAddId; // ???
    private Integer initSeventFormsId; // ???

    private EventHuman eventHuman; // нужно уточнить на счет Нападавшего
    private EventAddress eventAddress; // адрес происшествия
    private List< VictimHumans > victimHumans; // список пострадавщих

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
