package com.ssd.mvd.gpstabletsservice.task.entityForPapilon;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class PermanentRegistration {
    private String pAddress;
    private String pCadastre;
    private String pRegistrationDate;
    private com.ssd.mvd.gpstabletsservice.task.entityForPapilon.pRegion pRegion;
    private com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForAddress.pDistrict pDistrict;
}
