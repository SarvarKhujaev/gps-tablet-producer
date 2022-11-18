package com.ssd.mvd.gpstabletsservice.task.sos_task;

import lombok.extern.jackson.Jacksonized;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@lombok.Data
@Jacksonized
@NoArgsConstructor
@AllArgsConstructor
public class Data {
    private String road;
    private String city;
    private String county;
    private String country;
    private String postcode;
    private String residential;
    private String country_code;
}
