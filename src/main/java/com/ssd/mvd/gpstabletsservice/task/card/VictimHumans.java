package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public final class VictimHumans {
    private String phone;
    private String lastName;
    private String firstName;
    private String middleName;
    private String dateOfBirth;

    private Integer victimId;
    private VictimAddress victimAddress;
}
