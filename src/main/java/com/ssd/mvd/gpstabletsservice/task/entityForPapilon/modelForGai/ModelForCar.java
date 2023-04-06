package com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai;

import com.ssd.mvd.gpstabletsservice.constants.ErrorResponse;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class ModelForCar {
    private String Stir;
    private String Year;
    private String Pinpp;
    private String Model;
    private String Color;
    private String Kuzov;
    private String Power;
    private String Seats;
    private String Person;
    private String Engine;
    private String Stands;
    private String Address;
    private String FuelType;
    private String Additional;
    private String FullWeight;
    private String VehicleType;
    private String EmptyWeight;
    private String PlateNumber;
    private String Organization;
    private String RegistrationDate;
    private String TexPassportSerialNumber;

    private Tonirovka tonirovka;
    private Insurance insurance;
    private ErrorResponse errorResponse;
    private DoverennostList doverennostList;
}
