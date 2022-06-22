package com.ssd.mvd.gpstracker.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ApiResponseModel {
    private com.ssd.mvd.gpstracker.entity.Data data;
    private Boolean success;
    private Status status;
}
