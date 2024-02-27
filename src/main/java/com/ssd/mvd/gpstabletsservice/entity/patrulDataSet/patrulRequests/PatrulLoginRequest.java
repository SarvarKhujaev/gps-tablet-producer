package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests;

@lombok.Data
public final class PatrulLoginRequest {
    private String simCardNumber;
    private String password;
    private String login;
}
