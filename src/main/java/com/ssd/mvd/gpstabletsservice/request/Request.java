package com.ssd.mvd.gpstabletsservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Request< T, V > { // uses to get Patrul history
    private T object;
    private V subject;
    private String data;
    private String additional;
}