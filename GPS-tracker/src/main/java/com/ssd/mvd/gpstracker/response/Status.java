package com.ssd.mvd.gpstracker.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Status {
    private long code;
    private String message;
}
