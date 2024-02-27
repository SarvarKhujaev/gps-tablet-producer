package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForFindFace;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class LicensePlateNumber {
    private Double confidence;
    private String name;
    private Bbox bbox;
}