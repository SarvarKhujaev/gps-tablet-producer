package com.ssd.mvd.gpstabletsservice.constants;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private String message;
    private Errors errors;
}
