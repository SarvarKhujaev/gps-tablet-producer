package com.ssd.mvd.gpstracker.entity;

import lombok.Builder;

@lombok.Data
@Builder
public class Data<T, V> {
    private String type;
    private V subject;
    private T object;
}
