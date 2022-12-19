package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.constants.ErrorResponse;

import lombok.extern.jackson.Jacksonized;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import lombok.Data;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class ViolationsList {
    private ErrorResponse errorResponse;
    @JsonDeserialize
    private List< ViolationsInformation > violationsInformationsList;
}
