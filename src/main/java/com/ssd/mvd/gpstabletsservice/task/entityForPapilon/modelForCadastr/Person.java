package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForCadastr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.jackson.Jacksonized;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties ( ignoreUnknown = true )
public class Person {
    private String pPsp;
    private String pPerson;
    private pStatus pStatus;
    private String pCitizen;
    private String pDateBirth;
    private String pRegistrationDate;
}
