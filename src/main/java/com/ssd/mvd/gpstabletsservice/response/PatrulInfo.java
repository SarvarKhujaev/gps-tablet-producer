package com.ssd.mvd.gpstabletsservice.response;

import lombok.Data;

@Data
public class PatrulInfo { // it is used for response for PatrulController requests
    private String NSF;
    private Double distance; // the overall distance which patrul has moved out
    private Double fuelConsumption; // overall fuel consumption for the given distance

    public PatrulInfo ( String NSF ) { this.setNSF( NSF ); }
}
