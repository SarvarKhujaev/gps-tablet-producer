package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForAddress;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.jackson.Jacksonized;

@lombok.Data
@Jacksonized
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
@JsonIgnoreProperties( ignoreUnknown = true )
public class PermanentRegistration {
    private pRegion pregion;

    private String paddress;
    private String pcadastre;
    private String pregistrationDate;
}
