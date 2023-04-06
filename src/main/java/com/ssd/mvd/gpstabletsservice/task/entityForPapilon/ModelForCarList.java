package com.ssd.mvd.gpstabletsservice.task.entityForPapilon;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ModelForCar;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.constants.ErrorResponse;

import lombok.extern.jackson.Jacksonized;
import java.util.List;

@lombok.Data
@Jacksonized
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class ModelForCarList {
    @JsonDeserialize
    private List< ModelForCar > modelForCarList;

    private ErrorResponse errorResponse;
}
