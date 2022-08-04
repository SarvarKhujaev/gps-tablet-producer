package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForPassport;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class Data {
    private Person Person;
    private Document Document;
    private RequestGuid RequestGuid;
}
