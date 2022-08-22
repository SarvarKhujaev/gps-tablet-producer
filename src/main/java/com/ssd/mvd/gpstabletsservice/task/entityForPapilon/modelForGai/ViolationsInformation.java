package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ViolationsInformation {
    private Integer decreeStatus;
    private Integer amount;

    private String decreeSerialNumber;
    private String violation;
    private String division;
    private String payDate;
    private String address;
    private String article;
    private String owner;
    private String model;
    private String bill;
}
