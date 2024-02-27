package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForFindFace;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class Track {
    private String first_timestamp;
    private String last_timestamp;
    private String id;
    private Car car;
}