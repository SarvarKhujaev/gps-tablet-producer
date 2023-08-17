package com.ssd.mvd.gpstabletsservice.task.sos_task;

import lombok.extern.jackson.Jacksonized;

@lombok.Data
@Jacksonized
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class Data {
    private String road;
    private String city;
    private String county;
    private String country;
    private String postcode;
    private String residential;
    private String country_code;
}
