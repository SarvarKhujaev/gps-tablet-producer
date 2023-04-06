package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForPassport;

import java.util.List;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class First {
    private List< Integer > bbox;
    private String timestamp;
    private Double quality;
}