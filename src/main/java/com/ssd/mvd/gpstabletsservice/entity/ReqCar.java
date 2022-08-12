package com.ssd.mvd.gpstabletsservice.entity;

import java.util.UUID;
import lombok.Data;

@Data
public class ReqCar {
    private UUID uuid;
    private UUID lustraId;

    private String gosNumber;
    private String trackerId;
    private String vehicleType;
    private String carImageLink;
    private String patrulPassportSeries;

    private Integer sideNumber; // бортовой номер
    private Integer simCardNumber;

    private Double averageFuelSize; // средний расход топлива по документам
    private Double averageFuelConsumption = 0.0; // средний расход топлива исходя из стиля вождения водителя
}
