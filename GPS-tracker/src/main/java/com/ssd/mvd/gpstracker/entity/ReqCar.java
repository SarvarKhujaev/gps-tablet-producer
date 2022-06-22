package com.ssd.mvd.gpstracker.entity;

import lombok.Data;

import java.util.UUID;

@Data
public class ReqCar {
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

    public void saveAverageFuelConsumption ( Double distance ) { this.averageFuelConsumption = distance / this.getAverageFuelSize(); }
}
