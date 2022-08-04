package com.ssd.mvd.gpstabletsservice.task.entityForPapilon;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForAddress.ModelForAddress;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.util.List;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PsychologyCard {
    private Pinpp pinpp;
    private String personImage; // the image of the person

    private List< PapilonData > papilonData;
    private List< Violation > violationList;

    private ModelForCarList modelForCarList; // the list of all cars which belongs to this person
    private ModelForAddress modelForAddress;

    private com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForCadastr.Data modelForCadastr;
    private com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForPassport.Data modelForPassport;
}
