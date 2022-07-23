package com.ssd.mvd.gpstabletsservice.task.card;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    private String road;
    private String city;
    private String county;
    private String country;
    private String postcode;
    private String country_code;
    private String neighbourhood;
}
