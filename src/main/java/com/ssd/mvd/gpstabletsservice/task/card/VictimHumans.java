package com.ssd.mvd.gpstabletsservice.task.card;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.jackson.Jacksonized;
import lombok.*;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class VictimHumans {
    private String phone;
    private String lastName;
    private String firstName;
    private String middleName;
    private String dateOfBirth;

    private Integer victimId;
    private VictimAddress victimAddress;
}
