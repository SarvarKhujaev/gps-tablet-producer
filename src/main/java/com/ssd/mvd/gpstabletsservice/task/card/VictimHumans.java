package com.ssd.mvd.gpstabletsservice.task.card;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VictimHumans {
    private Integer victimId;
    private String lastName;
    private String firstName;
    private String middleName;
    private String dateOfBirth;
    private String phone;
    private VictimAddress victimAddress;
}
