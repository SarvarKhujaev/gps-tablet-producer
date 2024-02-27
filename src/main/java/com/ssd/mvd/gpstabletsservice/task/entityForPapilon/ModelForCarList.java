package com.ssd.mvd.gpstabletsservice.task.entityForPapilon;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ModelForCar;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.gpstabletsservice.constants.ErrorResponse;
import java.util.List;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class ModelForCarList {
    @JsonDeserialize
    private List< ModelForCar > modelForCarList;

    private ErrorResponse errorResponse;
}
