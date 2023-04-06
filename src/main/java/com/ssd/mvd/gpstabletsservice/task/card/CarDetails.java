package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;

@lombok.Data
public class CarDetails {
    private String ip;
    private String carData;
    private String carNumber;
    private String thumbnail;
    private String cameraImage;
    private String dossier_photo;

    private Long date;
    private Long time;
    private Double confidence;

    public CarDetails ( EventCar eventCar ) {
        this.setIp( eventCar.getCameraIp() );
        this.setConfidence( eventCar.getConfidence() );
        this.setDate( eventCar.getCreated_date() != null
                ? eventCar.getCreated_date().getTime() : null );
        this.setTime( eventCar.getCreated_date() != null
                ? eventCar.getCreated_date().getTime() : null );

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
        this.setConfidence( carEvent.getConfidence() );

        if ( carEvent.getCreated_date() != null
                && !carEvent.getCreated_date().equals( "null" ) )
            this.setTime( ( this.date = TimeInspector
                    .getInspector()
                    .getConvertTimeToLong()
                    .apply( carEvent.getCreated_date() ) ) );

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
