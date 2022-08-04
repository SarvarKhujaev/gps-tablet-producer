package com.ssd.mvd.gpstabletsservice.task.entityForPapilon;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermanentRegistration {
    private String pAddress;
    private String pCadastre;
    private String pRegistrationDate;
    private com.ssd.mvd.gpstabletsservice.task.entityForPapilon.pRegion pRegion;
    private com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForAddress.pDistrict pDistrict;
}
