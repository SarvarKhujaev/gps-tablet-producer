package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import lombok.Data;

@Data
public class CarDetails {
    private String ip;
    private String date;
    private String time;
    private String carData;
    private String carNumber;
    private String thumbnail;
    private String cameraImage;
    private String dossier_photo;

    private Double confidence;

    public CarDetails ( EventCar eventCar ) {
        this.setIp( eventCar.getCameraIp() );
        this.setConfidence( eventCar.getConfidence() );
        this.setDate( eventCar.getCreated_date().toString() );
        this.setTime( String.valueOf( eventCar.getCreated_date().getTime() ) );

        this.setThumbnail( eventCar.getThumbnail() );
        this.setCameraImage( eventCar.getFullframe() );
        this.setDossier_photo( eventCar.getMatched_dossier() );

        if ( eventCar.getCarTotalData() != null
                && eventCar.getCarTotalData().getModelForCar() != null ) {
            this.setCarNumber( eventCar
                    .getCarTotalData()
                    .getModelForCar()
                    .getPlateNumber() != null ?
                    eventCar
                            .getCarTotalData()
                            .getModelForCar()
                            .getPlateNumber() : eventCar
                    .getCarTotalData()
                    .getGosNumber() );
            this.setCarData( eventCar.getCarTotalData().getModelForCar().getModel() + " " +
                    eventCar.getCarTotalData().getModelForCar().getVehicleType() + " " +
                    eventCar.getCarTotalData().getModelForCar().getColor() ); } }

    public CarDetails ( CarEvent carEvent ) {
        this.setDate( carEvent.getCreated_date() );
        this.setConfidence( carEvent.getConfidence() );
        this.setTime( String.valueOf( carEvent.getCreated_date() ) );
        this.setIp( carEvent.getDataInfo() != null
                && carEvent.getDataInfo().getData() != null ?
                carEvent.getDataInfo().getData().getIp() : null );

        this.setThumbnail( carEvent.getThumbnail() ); // short version of the image from camera
        this.setCameraImage( carEvent.getFullframe() ); // original version of the image from camera
        this.setDossier_photo( carEvent.getDossier_photo() );

        if ( carEvent.getCarTotalData() != null
                && carEvent.getCarTotalData().getModelForCar() != null ) {
            this.setCarNumber( carEvent
                    .getCarTotalData()
                    .getModelForCar()
                    .getPlateNumber() != null ?
                    carEvent
                            .getCarTotalData()
                            .getModelForCar()
                            .getPlateNumber() : carEvent
                    .getCarTotalData()
                    .getGosNumber() );
            this.setCarData( carEvent.getCarTotalData().getModelForCar().getModel() + " " +
                    carEvent.getCarTotalData().getModelForCar().getVehicleType() + " " +
                    carEvent.getCarTotalData().getModelForCar().getColor() ); } }

}
