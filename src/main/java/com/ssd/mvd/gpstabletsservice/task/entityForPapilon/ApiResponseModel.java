package com.ssd.mvd.gpstabletsservice.task.entityForPapilon;

import lombok.Builder;

@Builder
@lombok.Data
public class ApiResponseModel {
    private Boolean success;
    private Status status;
    private Data data;
}
