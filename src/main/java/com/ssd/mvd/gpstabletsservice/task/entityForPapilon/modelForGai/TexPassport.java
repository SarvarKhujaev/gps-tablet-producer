package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ssd.mvd.entity.modelForPassport.ModelForPassport;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TexPassport {
    private Double weight; // вес без груза
    private Double totalWeight; // общий вес

    private Integer enginePower;
    private Integer numberOfSeats; // количество сидений
    private Integer numberOfVerticalSeats; // количество стоячих мест

    private String STIR;
    private String address;
    private String fuelType; // ??
    private String carModel;
    private String carColor;
    private String engineNumber;
    private String organization;
    private String dateOfCreation; // the date when car was created
    private String dateOfReceiving; // дата выдачи машины
    private String texPassportSeries;
    @JsonDeserialize
    private ModelForPassport modelForPassport; // car owner
}
