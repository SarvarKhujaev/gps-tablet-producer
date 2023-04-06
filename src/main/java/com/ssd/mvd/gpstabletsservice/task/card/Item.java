package com.ssd.mvd.gpstabletsservice.task.card;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class Item< T > {
    private String key;
    private T value;
}
