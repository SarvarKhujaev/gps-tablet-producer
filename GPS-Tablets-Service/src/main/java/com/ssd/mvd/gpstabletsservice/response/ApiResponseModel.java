package com.ssd.mvd.gpstabletsservice.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ApiResponseModel {
    private com.ssd.mvd.gpstabletsservice.entity.Data data;
    private Boolean success;
    private Status status;
}
