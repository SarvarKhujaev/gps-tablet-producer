package com.ssd.mvd.gpstabletsservice.entity;

import lombok.Builder;

@lombok.Data
@Builder
public class Data< T, V > {
    private String type;
    private V subject;
    private T data;
}
