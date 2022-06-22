package com.ssd.mvd.gpstracker.task.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VictimHumans {
    private String phone; // номер пострадавшего
    private String lastName; // фамилия
    private String firstName; // имя
    private String middleName; // отчество

    private Integer victimId; // дается со стороны оператора
    private Date dateOfBirth; // год рождения
    private VictimAddress victimAddress; // адрес пострадавшего
}
