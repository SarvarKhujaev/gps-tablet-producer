package com.ssd.mvd.gpstabletsservice.task.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VictimHumans {
    private String phone;
    private String lastName;
    private Integer victimId;
    private String firstName;
    private String middleName;
    private String dateOfBirth;
    private VictimAddress victimAddress;
}
