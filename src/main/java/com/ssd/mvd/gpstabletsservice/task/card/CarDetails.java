package com.ssd.mvd.gpstabletsservice.task.card;

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
                .getCheckParam()
                .test( eventCar.getCreated_date() )
                ? eventCar.getCreated_date().getTime()
                : null );

        this.setTime( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( eventCar.getCreated_date() )
                ? eventCar.getCreated_date().getTime()
                : null );

        this.setThumbnail( eventCar.getThumbnail() );
        this.setCameraImage( eventCar.getFullframe() );
        this.setDossier_photo( eventCar.getMatched_dossier() );

        if ( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( eventCar.getCarTotalData() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( eventCar.getCarTotalData().getModelForCar() ) ) {
            this.setCarNumber( DataValidateInspector
                    .getInstance()
                    .getCheckParam()
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
                    .getConcatNames()
                    .apply( eventCar
                            .getCarTotalData()
                            .getModelForCar(), 1 ) ); } }

    public CarDetails ( final CarEvent carEvent ) {
        this.setConfidence( carEvent.getConfidence() );

        if ( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvent.getCreated_date() )
                && !carEvent.getCreated_date().equals( "null" ) )
            this.setTime( ( this.date = TimeInspector
                    .getInspector()
                    .getConvertTimeToLong()
                    .apply( carEvent.getCreated_date() ) ) );

        this.setIp( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvent.getDataInfo() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvent.getDataInfo().getData() )
                ? carEvent.getDataInfo().getData().getIp()
                : null );

        this.setThumbnail( carEvent.getThumbnail() ); // short version of the image from camera
        this.setCameraImage( carEvent.getFullframe() ); // original version of the image from camera
        this.setDossier_photo( carEvent.getDossier_photo() );

        if ( DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvent.getCarTotalData() )
                && DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( carEvent
                        .getCarTotalData()
                        .getModelForCar() ) ) {
            this.setCarNumber( DataValidateInspector
                    .getInstance()
                    .getCheckParam()
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
                    .getConcatNames()
                    .apply( carEvent
                            .getCarTotalData()
                            .getModelForCar(), 1 ) ); } }
}
