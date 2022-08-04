package com.ssd.mvd.gpstabletsservice.task.entityForPapilon;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PermanentRegistration {
    private String pAddress;
    private String pCadastre;
    private String pRegistrationDate;
    private com.ssd.mvd.entity.pRegion pRegion;
    private com.ssd.mvd.entity.modelForAddress.pDistrict pDistrict;
}
