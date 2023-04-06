package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForCadastr;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.extern.jackson.Jacksonized;
import java.util.List;

@lombok.Data
@Jacksonized
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class Data {
    @JsonDeserialize
    private List< Person > PermanentRegistration;
    @JsonDeserialize
    private List< TemproaryRegistration > TemproaryRegistration;
}
