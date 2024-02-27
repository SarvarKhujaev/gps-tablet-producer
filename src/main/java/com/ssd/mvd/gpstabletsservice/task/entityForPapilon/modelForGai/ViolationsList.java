package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.constants.ErrorResponse;
import java.util.List;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class ViolationsList {
    private ErrorResponse errorResponse;
    @JsonDeserialize
    private List< ViolationsInformation > violationsInformationsList;
}
