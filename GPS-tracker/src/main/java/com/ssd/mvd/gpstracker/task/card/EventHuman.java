package com.ssd.mvd.gpstracker.task.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventHuman {
    private String phone;
    private String checkin;
    private String lastName;
    private String firstName;
    private String middleName;

    private Integer humanId;
    private Integer hospital;
    private Integer hospitalDept;
    private Integer treatmentkind;
    private Integer professionidcaller;

    private Date dateOfBirth;
    private HumanAddress humanAddress;
}
