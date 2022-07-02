package com.ssd.mvd.gpstabletsservice.request;

import lombok.Data;

@Data
public class PatrulLoginRequest {
    private String passportSeries;
    private String simCardNumber;
    private String password;
}
