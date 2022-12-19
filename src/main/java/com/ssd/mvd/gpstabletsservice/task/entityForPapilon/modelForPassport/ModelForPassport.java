package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForPassport;

import com.ssd.mvd.gpstabletsservice.constants.ErrorResponse;
import lombok.Data;

@Data
public class ModelForPassport {
    private ErrorResponse errorResponse;
    private com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForPassport.Data Data;
}
