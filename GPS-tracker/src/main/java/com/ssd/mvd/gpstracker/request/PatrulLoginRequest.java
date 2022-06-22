package com.ssd.mvd.gpstracker.request;

import lombok.Data;

@Data
public class PatrulLoginRequest {
    private String passportSeries;
    private String password;
}
