package com.ssd.mvd.gpstabletsservice.response;

@lombok.Data
@lombok.Builder
public final class ApiResponseModel {
    private com.ssd.mvd.gpstabletsservice.entity.Data data;
    private Boolean success;
    private Status status;
}
