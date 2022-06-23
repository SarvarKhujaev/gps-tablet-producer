package com.ssd.mvd.gpstabletsservice.task.card;

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
    private String dateOfBirth;

    private Integer humanId;
    private Integer hospital;
    private Integer hospitaldept;
    private Integer treatmentkind;
    private Integer professionidcaller;

    private HumanAddress humanAddress;
}
