package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForAddress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermanentRegistration {
    private pRegion pRegion;

    private String pAddress;
    private String pCadastre;
    private String pRegistrationDate;
}
