package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class ViolationsList {
    @JsonDeserialize
    private List< ViolationsInformation > violationsInformationsList;
}
