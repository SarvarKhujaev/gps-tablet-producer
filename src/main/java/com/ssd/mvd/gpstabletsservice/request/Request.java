package com.ssd.mvd.gpstabletsservice.request;

@lombok.Data
@lombok.Builder
public class Request< T, V > { // uses to get Patrul history
    private T object;
    private V subject;
    private String data;
    private String additional;
}