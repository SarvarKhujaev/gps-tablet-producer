package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForCadastr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    private String pPsp;
    private String pPerson;
    private pStatus pStatus;
    private String pCitizen;
    private String pDateBirth;
    private String pRegistrationDate;
}
