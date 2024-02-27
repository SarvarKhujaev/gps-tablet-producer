package com.ssd.mvd.gpstabletsservice.entity.responseForAndroid;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;

public final class CarDetails extends DataValidateInspector {
    public void setIp ( final String ip ) {
        this.ip = ip;
    }

    public void setCarData ( final String carData ) {
        this.carData = carData;
    }

    public void setCarNumber ( final String carNumber ) {
        this.carNumber = carNumber;
    }

    public void setThumbnail ( final String thumbnail ) {
        this.thumbnail = thumbnail;
    }

    public void setCameraImage ( final String cameraImage ) {
        this.cameraImage = cameraImage;
    }

    public void setDossier_photo ( final String dossier_photo ) {
        this.dossier_photo = dossier_photo;
    }

    public void setDate ( final Long date ) {
        this.date = date;
    }

    public void setTime ( final Long time ) {
        this.time = time;
    }

    public Double getConfidence() {
        return this.confidence;
    }

    public void setConfidence ( final Double confidence ) {
        this.confidence = confidence;
    }

    private String ip;
    private String carData;
    private String carNumber;
    private String thumbnail;
    private String cameraImage;
    private String dossier_photo;

    private Long date;
    private Long time;
    private Double confidence;

    public static <T> CarDetails from ( final T event ) {
        if ( event instanceof EventCar ) {
            return new CarDetails( (EventCar) event );
        } else {
            return new CarDetails( (CarEvent) event );
        }
    }

    private CarDetails ( final EventCar eventCar ) {
        this.setConfidence( eventCar.getConfidence() );
        this.setIp( eventCar.getDataInfo().getCadaster().getIp() );

        this.setDate( super.objectIsNotNull( eventCar.getCreated_date() )
                ? eventCar.getCreated_date().getTime()
                : null );

        this.setTime( super.objectIsNotNull( eventCar.getCreated_date() )
                ? eventCar.getCreated_date().getTime()
                : null );

        this.setThumbnail( eventCar.getThumbnail() );
        this.setCameraImage( eventCar.getFullframe() );
        this.setDossier_photo( eventCar.getDossier_photo() );

        if ( super.objectIsNotNull( eventCar.getCarTotalData() )
                && super.objectIsNotNull( eventCar.getCarTotalData().getModelForCar() ) ) {
            this.setCarNumber( super.objectIsNotNull( eventCar
                            .getCarTotalData()
                            .getModelForCar()
                            .getPlateNumber() )
                    ? eventCar
                            .getCarTotalData()
                            .getModelForCar()
                            .getPlateNumber()
                    : eventCar.getCarTotalData().getGosNumber() );

            this.setCarData( super.concatNames( eventCar.getCarTotalData().getModelForCar() ) );
        }
    }

    private CarDetails ( final CarEvent carEvent ) {
        this.setConfidence( carEvent.getConfidence() );

        if ( super.objectIsNotNull( carEvent.getCreated_date() )
                && !carEvent.getCreated_date().equals( "null" ) )
            this.setTime( ( this.date = super.convertTimeToLong( carEvent.getCreated_date() ) ) );

        this.setIp( super.objectIsNotNull( carEvent.getDataInfo() )
                && super.objectIsNotNull( carEvent.getDataInfo().getCadaster() )
                ? carEvent.getDataInfo().getCadaster().getIp()
                : null );

        this.setThumbnail( carEvent.getThumbnail() ); // short version of the image from camera
        this.setCameraImage( carEvent.getFullframe() ); // original version of the image from camera
        this.setDossier_photo( carEvent.getDossier_photo() );

        if ( super.objectIsNotNull( carEvent.getCarTotalData() )
                && super.objectIsNotNull( carEvent.getCarTotalData().getModelForCar() ) ) {
            this.setCarNumber( super.objectIsNotNull( carEvent
                            .getCarTotalData()
                            .getModelForCar()
                            .getPlateNumber() )
                    ? carEvent
                            .getCarTotalData()
                            .getModelForCar()
                            .getPlateNumber()
                    : carEvent.getCarTotalData().getGosNumber() );

            this.setCarData( super.concatNames( carEvent.getCarTotalData().getModelForCar() ) );
        }
    }
}
