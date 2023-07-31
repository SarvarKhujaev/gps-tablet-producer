package com.ssd.mvd.gpstabletsservice.constants;

@lombok.Data
@lombok.Builder
public final class ErrorResponse {
    private String message;
    private Errors errors;
}
