package com.ssd.mvd.gpstabletsservice.entity.responseForAndroid;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;

@lombok.Data
public final class CarDetails {
    private String ip;
    private String carData;
    private String carNumber;
    private String thumbnail;
    private String cameraImage;
    private String dossier_photo;

    private Long date;
    private Long time;
    private Double confidence;

    public CarDetails ( final EventCar eventCar, final DataValidateInspector dataValidateInspector ) {
        this.setIp( eventCar.getCameraIp() );
        this.setConfidence( eventCar.getConfidence() );

        this.setDate( dataValidateInspector
                .checkParam
                .test( eventCar.getCreated_date() )
                ? eventCar.getCreated_date().getTime()
                : null );

        this.setTime( dataValidateInspector
                .checkParam
                .test( eventCar.getCreated_date() )
                ? eventCar.getCreated_date().getTime()
                : null );

        this.setThumbnail( eventCar.getThumbnail() );
        this.setCameraImage( eventCar.getFullframe() );
        this.setDossier_photo( eventCar.getMatched_dossier() );

        if ( dataValidateInspector
                .checkParam
                .test( eventCar.getCarTotalData() )
                && dataValidateInspector
                .checkParam
                .test( eventCar.getCarTotalData().getModelForCar() ) ) {
            this.setCarNumber( dataValidateInspector
                    .checkParam
                    .test( eventCar
                            .getCarTotalData()
                            .getModelForCar()
                            .getPlateNumber() )
                    ? eventCar
                            .getCarTotalData()
                            .getModelForCar()
                            .getPlateNumber()
                    : eventCar
                    .getCarTotalData()
                    .getGosNumber() );

            this.setCarData( dataValidateInspector
                    .concatNames
                    .apply( eventCar
                            .getCarTotalData()
                            .getModelForCar(), 1 ) ); } }

    public CarDetails ( final CarEvent carEvent, final DataValidateInspector dataValidateInspector ) {
        this.setConfidence( 100.0 );

        if ( dataValidateInspector
                .checkParam
                .test( carEvent.getCreated_date() )
                && !carEvent.getCreated_date().equals( "null" ) )
            this.setTime( ( this.date = TimeInspector
                    .getInspector()
                    .getConvertTimeToLong()
                    .apply( carEvent.getCreated_date() ) ) );

        this.setIp( dataValidateInspector
                .checkParam
                .test( carEvent.getDataInfo() )
                && dataValidateInspector
                .checkParam
                .test( carEvent.getDataInfo().getCadaster() )
                ? carEvent.getDataInfo().getCadaster().getIp()
                : null );

        this.setThumbnail( carEvent.getThumbnail() ); // short version of the image from camera
        this.setCameraImage( carEvent.getFullframe() ); // original version of the image from camera
        this.setDossier_photo( carEvent.getDossier_photo() );

        if ( dataValidateInspector
                .checkParam
                .test( carEvent.getCarTotalData() )
                && dataValidateInspector
                .checkParam
                .test( carEvent
                        .getCarTotalData()
                        .getModelForCar() ) ) {
            this.setCarNumber( dataValidateInspector
                    .checkParam
                    .test( carEvent
                            .getCarTotalData()
                            .getModelForCar()
                            .getPlateNumber() )
                    ? carEvent
                            .getCarTotalData()
                            .getModelForCar()
                            .getPlateNumber()
                    : carEvent
                    .getCarTotalData()
                    .getGosNumber() );

            this.setCarData( dataValidateInspector
                    .concatNames
                    .apply( carEvent
                            .getCarTotalData()
                            .getModelForCar(), 1 ) ); } }
}
