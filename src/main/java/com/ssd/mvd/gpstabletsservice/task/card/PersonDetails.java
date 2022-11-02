package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.entity.TimeInspector;
import lombok.Data;

@Data
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

    public PersonDetails ( EventBody eventBody ) {
        this.setIp( eventBody.getCameraIp() );
        this.setConfidence( eventBody.getConfidence() );
        this.setDate( eventBody.getCreated_date() != null
                ? eventBody.getCreated_date().getTime() : null );
        this.setTime( eventBody.getCreated_date() != null
                ? eventBody.getCreated_date().getTime() : null );

        this.setThumbnail( eventBody.getThumbnail() );
        this.setCameraImage( eventBody.getFullframe() );
        this.setDossier_photo( eventBody.getMatched_dossier() );

        if ( eventBody.getPsychologyCard() != null ) {
            if ( eventBody.getPsychologyCard().getPinpp() != null )
                this.setFIO( eventBody.getPsychologyCard().getPinpp().getName() + " " +
                        eventBody.getPsychologyCard().getPinpp().getSurname() + " " +
                        eventBody.getPsychologyCard().getPinpp().getPatronym() );
            if ( eventBody.getPsychologyCard().getPapilonData() != null
                    && eventBody.getPsychologyCard().getPapilonData().size() > 0 )
                this.setPassportSeries( eventBody
                        .getPsychologyCard()
                        .getPapilonData()
                        .get( 0 )
                        .getPassport().split( " " )[0] ); } }

    public PersonDetails ( EventFace eventFace ) {
        this.setIp( eventFace.getCameraIp() );
        this.setConfidence( eventFace.getConfidence() );
        this.setDate( eventFace.getCreated_date() != null
                ? eventFace.getCreated_date().getTime() : null );
        this.setTime( eventFace.getCreated_date() != null
                ? eventFace.getCreated_date().getTime() : null );

        this.setThumbnail( eventFace.getThumbnail() );
        this.setCameraImage( eventFace.getFullframe() );
        this.setDossier_photo( eventFace.getMatched_dossier() );

        if ( eventFace.getPsychologyCard() != null ) {
            if ( eventFace.getPsychologyCard().getPinpp() != null )
                this.setFIO( eventFace.getPsychologyCard().getPinpp().getName() + " " +
                        eventFace.getPsychologyCard().getPinpp().getSurname() + " " +
                        eventFace.getPsychologyCard().getPinpp().getPatronym() );
            if ( eventFace.getPsychologyCard().getPapilonData() != null
                    && eventFace.getPsychologyCard().getPapilonData().size() > 0 )
                this.setPassportSeries( eventFace
                        .getPsychologyCard()
                        .getPapilonData()
                        .get( 0 )
                        .getPassport().split( " " )[0] ); } }

    public PersonDetails ( FaceEvent faceEvent ) {
        this.setConfidence( faceEvent.getConfidence() );
        this.setTime( faceEvent.getCreated_date() != null
                && !faceEvent.getCreated_date().equals( "null" )
                ? TimeInspector
                .getInspector()
                .getConvertTimeToLong()
                .apply( faceEvent.getCreated_date() ) : null );
        this.setIp( faceEvent.getDataInfo() != null
                && faceEvent.getDataInfo().getData() != null ?
                faceEvent.getDataInfo().getData().getIp() : null );

        this.setThumbnail( faceEvent.getThumbnail() );
        this.setCameraImage( faceEvent.getFullframe() );
        this.setDossier_photo( faceEvent.getDossier_photo() );

        if ( faceEvent.getPsychologyCard() != null ) {
            if ( faceEvent.getPsychologyCard().getPinpp() != null )
                this.setFIO( faceEvent.getPsychologyCard().getPinpp().getName() + " " +
                        faceEvent.getPsychologyCard().getPinpp().getSurname() + " " +
                        faceEvent.getPsychologyCard().getPinpp().getPatronym() );
            if ( faceEvent.getPsychologyCard().getPapilonData() != null
                    && faceEvent.getPsychologyCard().getPapilonData().size() > 0 )
                this.setPassportSeries( faceEvent
                        .getPsychologyCard()
                        .getPapilonData()
                        .get( 0 )
                        .getPassport().split( " " )[0] ); } }
}
