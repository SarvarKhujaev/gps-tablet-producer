package com.ssd.mvd.gpstabletsservice.task.card;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item< T > {
    private String key;
    private T value;
}
