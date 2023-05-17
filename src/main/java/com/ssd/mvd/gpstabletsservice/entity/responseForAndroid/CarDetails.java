package com.ssd.mvd.gpstabletsservice.entity.responseForAndroid;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
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

    public CarDetails ( final EventCar eventCar ) {
        this.setIp( eventCar.getCameraIp() );
        this.setConfidence( eventCar.getConfidence() );

        this.setDate( DataValidateInspector
                .getInstance()
                .checkParam
                .test( eventCar.getCreated_date() )
                ? eventCar.getCreated_date().getTime()
                : null );

        this.setTime( DataValidateInspector
                .getInstance()
                .checkParam
                .test( eventCar.getCreated_date() )
                ? eventCar.getCreated_date().getTime()
                : null );

        this.setThumbnail( eventCar.getThumbnail() );
        this.setCameraImage( eventCar.getFullframe() );
        this.setDossier_photo( eventCar.getMatched_dossier() );

        if ( DataValidateInspector
                .getInstance()
                .checkParam
                .test( eventCar.getCarTotalData() )
                && DataValidateInspector
                .getInstance()
                .checkParam
                .test( eventCar.getCarTotalData().getModelForCar() ) ) {
            this.setCarNumber( DataValidateInspector
                    .getInstance()
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

            this.setCarData( DataValidateInspector
                    .getInstance()
                    .concatNames
                    .apply( eventCar
                            .getCarTotalData()
                            .getModelForCar(), 1 ) ); } }

    public CarDetails ( final CarEvent carEvent ) {
        this.setConfidence( carEvent.getConfidence() );

        if ( DataValidateInspector
                .getInstance()
                .checkParam
                .test( carEvent.getCreated_date() )
                && !carEvent.getCreated_date().equals( "null" ) )
            this.setTime( ( this.date = TimeInspector
                    .getInspector()
                    .getConvertTimeToLong()
                    .apply( carEvent.getCreated_date() ) ) );

        this.setIp( DataValidateInspector
                .getInstance()
                .checkParam
                .test( carEvent.getDataInfo() )
                && DataValidateInspector
                .getInstance()
                .checkParam
                .test( carEvent.getDataInfo().getData() )
                ? carEvent.getDataInfo().getData().getIp()
                : null );

        this.setThumbnail( carEvent.getThumbnail() ); // short version of the image from camera
        this.setCameraImage( carEvent.getFullframe() ); // original version of the image from camera
        this.setDossier_photo( carEvent.getDossier_photo() );

        if ( DataValidateInspector
                .getInstance()
                .checkParam
                .test( carEvent.getCarTotalData() )
                && DataValidateInspector
                .getInstance()
                .checkParam
                .test( carEvent
                        .getCarTotalData()
                        .getModelForCar() ) ) {
            this.setCarNumber( DataValidateInspector
                    .getInstance()
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

            this.setCarData( DataValidateInspector
                    .getInstance()
                    .concatNames
                    .apply( carEvent
                            .getCarTotalData()
                            .getModelForCar(), 1 ) ); } }
}
