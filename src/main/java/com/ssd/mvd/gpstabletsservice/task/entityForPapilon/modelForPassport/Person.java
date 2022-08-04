package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForPassport;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    private Sex Sex;
    private String Pinpp;
    private String pCitizen;
    private String NameLatin;
    private String DateBirth;
    private String BirthPlace;
    private String SurnameLatin;
    private String PatronymLatin;
}
