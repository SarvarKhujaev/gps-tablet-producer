package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForAddress;

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
public class ModelForAddress {
    private ErrorResponse errorResponse;
    private PermanentRegistration PermanentRegistration;
    private com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForPassport.RequestGuid RequestGuid;
    @JsonDeserialize
    private List< com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForAddress.TemproaryRegistration > TemproaryRegistration;
}