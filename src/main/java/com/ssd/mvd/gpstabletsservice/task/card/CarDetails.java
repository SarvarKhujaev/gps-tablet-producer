package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;

import java.util.Arrays;
import lombok.Data;

@Data
public class CarDetails {
    private String ip;
    private String FIO;
    private String date;
    private String time;
    private String cameraImage; // фото человека с камеры
    private String originalImage; // фото человека с паспорта
    private String passportSeries;

    private String carData;
    private String carNumber;

    private Integer confidence;

    public CarDetails ( EventCar eventCar ) {
        this.setIp( eventCar.getCameraIp() );
        this.setDate( eventCar.getCreated_date().toString() );
        this.setConfidence( eventCar.getConfidence().intValue() );
        this.setTime( String.valueOf( eventCar.getCreated_date().getTime() ) );
        this.setCameraImage( Arrays.toString( eventCar.getFullframebytes() ) );
        if ( eventCar.getCarTotalData() != null
                && eventCar.getCarTotalData().getModelForCar() != null ) {
            this.setCarNumber( eventCar
                    .getCarTotalData()
                    .getModelForCar()
                    .getPlateNumber() );
            this.setCarData(
                    eventCar.getCarTotalData().getModelForCar().getVehicleType() + " " +
                            eventCar.getCarTotalData().getModelForCar().getModel() + " " +
                            eventCar.getCarTotalData().getModelForCar().getPlateNumber() ); } }

    public CarDetails ( CarEvent carEvent ) {
        this.setDate( carEvent.getCreated_date() );
        this.setConfidence( carEvent.getConfidence() );
        this.setCameraImage( carEvent.getFullframe() );
        this.setIp( carEvent.getDataInfo().getData().getIp() );
        this.setTime( String.valueOf( carEvent.getCreated_date() ) );
        if ( carEvent.getCarTotalData() != null
                && carEvent.getCarTotalData().getModelForCar() != null ) {
            this.setCarNumber( carEvent
                    .getCarTotalData()
                    .getModelForCar()
                    .getPlateNumber() );
            this.setCarData( carEvent.getCarTotalData().getModelForCar().getPlateNumber() + " " +
                    carEvent.getCarTotalData().getModelForCar().getPlateNumber() + " " +
                    carEvent.getCarTotalData().getModelForCar().getPlateNumber() ); } }

}
