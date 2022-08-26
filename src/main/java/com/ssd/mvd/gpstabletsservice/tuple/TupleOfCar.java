package com.ssd.mvd.gpstabletsservice.tuple;

import com.datastax.driver.core.Row;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TupleOfCar {
    private UUID uuid;
    private UUID uuidOfEscort; // UUID of the Escort which this car is linked to
    private UUID uuidOfPatrul; // UUID of the Escort which this car is linked to

    private String carModel;
    private String gosNumber;
    private String trackerId;
    private String nsfOfPatrul;
    private String simCardNumber;

    private Double latitude;
    private Double longitude;
    private Double averageFuelConsumption;
}
