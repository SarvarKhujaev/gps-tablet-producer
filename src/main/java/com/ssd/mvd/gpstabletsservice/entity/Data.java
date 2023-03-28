package com.ssd.mvd.gpstabletsservice.entity;

@lombok.Data
@lombok.Builder
public class Data< T, V > {
    private String type;
    private V subject;
    private T data;
}
