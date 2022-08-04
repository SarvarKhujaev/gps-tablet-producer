package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForAddress;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TemproaryRegistration {
    private String pAddress;
    private String pCadastre;
    private String pValidDate;
    private String pRegistrationDate;

    private pRegion pRegion;
    private pDistrict pDistrict;
}
