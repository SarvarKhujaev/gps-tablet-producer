package com.ssd.mvd.gpstabletsservice.task.entityForPapilon;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForAddress.ModelForAddress;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PsychologyCard {
    @JsonDeserialize
    private Pinpp pinpp;
    private String personImage; // the image of the person

    @JsonDeserialize
    private List< PapilonData > papilonData;
    @JsonDeserialize
    private List< Violation > violationList;

    @JsonDeserialize
    private ModelForCarList modelForCarList; // the list of all cars which belongs to this person
    @JsonDeserialize
    private ModelForAddress modelForAddress;

    @JsonDeserialize
    private com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForCadastr.Data modelForCadastr;
    @JsonDeserialize
    private com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForPassport.Data modelForPassport;
}
