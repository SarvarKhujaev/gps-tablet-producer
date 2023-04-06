package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.constants.ErrorResponse;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class Insurance {
    private String DateBegin;
    private String DateValid;
    private String InsuranceSerialNumber;

    @JsonDeserialize
    private ErrorResponse errorResponse;
}
