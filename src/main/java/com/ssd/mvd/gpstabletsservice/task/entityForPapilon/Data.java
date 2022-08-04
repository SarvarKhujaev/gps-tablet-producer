package com.ssd.mvd.gpstabletsservice.task.entityForPapilon;

@lombok.Data
public class Data<T, V> {
    private String type;
    private V subject;
    private T data;
}
