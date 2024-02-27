package com.ssd.mvd.gpstabletsservice.response;

import com.ssd.mvd.gpstabletsservice.entity.Data;

@lombok.Data
@lombok.Builder
public final class ApiResponseModel {
    private com.ssd.mvd.gpstabletsservice.entity.Data data;
    private Boolean success;
    private Status status;

    public static ApiResponseModel generate ( final Data data ) {
        return ApiResponseModel
                .builder()
                .data( data )
                .success( true )
                .build();
    }
}
