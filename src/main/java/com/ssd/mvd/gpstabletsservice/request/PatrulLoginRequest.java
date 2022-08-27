package com.ssd.mvd.gpstabletsservice.request;

import java.util.UUID;
import lombok.Data;

@Data
public class PatrulLoginRequest {
    private UUID passportSeries;

    private String simCardNumber;
    private String password;
    private String login;
}
