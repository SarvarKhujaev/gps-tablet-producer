package com.ssd.mvd.gpstabletsservice.task.entityForPapilon;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Status {
    private long code;
    private String message;
}
