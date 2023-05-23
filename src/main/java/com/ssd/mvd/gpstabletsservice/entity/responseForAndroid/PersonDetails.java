package com.ssd.mvd.gpstabletsservice.entity.responseForAndroid;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;

@lombok.Data
public class PersonDetails {
    private String ip;
    private String FIO;
    private String thumbnail;
    private String cameraImage; // фото человека с камеры
    private String dossier_photo;
    private String passportSeries;

    private Long date;
    private Long time;
    private Double confidence;

    public PersonDetails ( final EventBody eventBody ) {
        this.setIp( eventBody.getCameraIp() );
        this.setConfidence( eventBody.getConfidence() );

        this.setDate( DataValidateInspector
                .getInstance()
                .checkParam
                .test( eventBody.getCreated_date() )
                ? eventBody.getCreated_date().getTime()
                : null );

        this.setTime( DataValidateInspector
                .getInstance()
                .checkParam
                .test( eventBody.getCreated_date() )
                ? eventBody.getCreated_date().getTime()
                : null );

        this.setThumbnail( eventBody.getThumbnail() );
        this.setCameraImage( eventBody.getFullframe() );
        this.setDossier_photo( eventBody.getMatched_dossier() );

        if ( DataValidateInspector
                .getInstance()
                .checkParam
                .test( eventBody.getPsychologyCard() ) ) {
            this.setFIO( DataValidateInspector
                    .getInstance()
                    .checkParam
                    .test( eventBody
                            .getPsychologyCard()
                            .getPinpp() )
                    ? DataValidateInspector
                    .getInstance()
                    .concatNames
                    .apply( eventBody.getPsychologyCard().getPinpp(), 0 )
                    : null );
            this.setPassportSeries( DataValidateInspector
                    .getInstance()
                    .checkRequest
                    .test( eventBody
                            .getPsychologyCard()
                            .getPapilonData(), 6 )
                    ? eventBody
                    .getPsychologyCard()
                    .getPapilonData()
                    .get( 0 )
                    .getPassport()
                    .split( " " )[0]
                    : null ); } }

    public PersonDetails ( final EventFace eventFace ) {
        this.setIp( eventFace.getCameraIp() );
        this.setConfidence( eventFace.getConfidence() );

        this.setDate( DataValidateInspector
                .getInstance()
                .checkParam
                .test( eventFace.getCreated_date() )
                ? eventFace.getCreated_date().getTime()
                : null );

        this.setTime( DataValidateInspector
                .getInstance()
                .checkParam
                .test( eventFace.getCreated_date() )
                ? eventFace.getCreated_date().getTime()
                : null );

        this.setThumbnail( eventFace.getThumbnail() );
        this.setCameraImage( eventFace.getFullframe() );
        this.setDossier_photo( eventFace.getMatched_dossier() );

        if ( DataValidateInspector
                .getInstance()
                .checkParam
                .test( eventFace.getPsychologyCard() ) ) {
            this.setFIO( DataValidateInspector
                    .getInstance()
                    .checkParam
                    .test( eventFace
                            .getPsychologyCard()
                            .getPinpp() )
                    ? DataValidateInspector
                    .getInstance()
                    .concatNames
                    .apply( eventFace.getPsychologyCard().getPinpp(), 0 )
                    : null );

            this.setPassportSeries( DataValidateInspector
                    .getInstance()
                    .checkRequest
                    .test( eventFace
                            .getPsychologyCard()
                            .getPapilonData(), 6 )
                    ? eventFace
                    .getPsychologyCard()
                    .getPapilonData()
                    .get( 0 )
                    .getPassport()
                    .split( " " )[0]
                    : null ); } }

    public PersonDetails ( final FaceEvent faceEvent ) {
        this.setConfidence( faceEvent.getConfidence() );
        this.setTime( DataValidateInspector
                .getInstance()
                .checkParam
                .test( faceEvent.getCreated_date() )
                && !faceEvent.getCreated_date().equals( "null" )
                ? TimeInspector
                .getInspector()
                .getConvertTimeToLong()
                .apply( faceEvent.getCreated_date() )
                : null );

        this.setIp( DataValidateInspector
                .getInstance()
                .checkParam
                .test( faceEvent.getDataInfo() )
                && DataValidateInspector
                .getInstance()
                .checkParam
                .test( faceEvent.getDataInfo().getData() )
                ? faceEvent.getDataInfo().getData().getIp()
                : null );

        // в случае если псих портрет отсутствует то возмем отсюда
        this.setFIO( faceEvent.getComment() );
        this.setThumbnail( faceEvent.getThumbnail() );
        this.setCameraImage( faceEvent.getFullframe() );
        this.setDossier_photo( faceEvent.getDossier_photo() );

        if ( DataValidateInspector
                .getInstance()
                .checkParam
                .test( faceEvent.getPsychologyCard() ) ) {
            this.setFIO( DataValidateInspector
                    .getInstance()
                    .checkParam
                    .test( faceEvent.getPsychologyCard().getPinpp() )
                    ? DataValidateInspector
                    .getInstance()
                    .concatNames
                    .apply( faceEvent.getPsychologyCard().getPinpp(), 0 )
                    : null );

            this.setPassportSeries( DataValidateInspector
                    .getInstance()
                    .checkRequest
                    .test( faceEvent.getPsychologyCard().getPapilonData(), 6 )
                    ? faceEvent
                    .getPsychologyCard()
                    .getPapilonData()
                    .get( 0 )
                    .getPassport()
                    .split( " " )[0]
                    : null ); } }
}