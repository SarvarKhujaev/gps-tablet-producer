package com.ssd.mvd.gpstabletsservice.response;

import com.ssd.mvd.gpstabletsservice.payload.ReqLocationExchange;
import lombok.Data;
import reactor.core.publisher.Flux;

@Data
public class PatrulInfo { // it is used for response for PatrulController requests
    private String NSF;
    private Double distance; // the overall distance which patrul has moved out
    private Double fuelConsumption; // overall fuel consumption for the given distance
    private Flux< ReqLocationExchange > transactions; // the list of all points where the patrul has been

    public Double setDistance ( Double distance ) { return ( this.distance = distance ); }

    public PatrulInfo( Flux< ReqLocationExchange > history ) { this.setTransactions( history ); }

    public PatrulInfo ( String NSF ) { this.setNSF( NSF ); }
}
