package com.ssd.mvd.gpstabletsservice.task.card;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import lombok.Data;

@Data
public class PersonDetails {
    private String ip;
    private String FIO;
    private String date;
    private String time;
    private String cameraImage; // фото человека с камеры
    private String originalImage; // фото человека с паспорта
    private String passportSeries;

    private Integer confidence;

    public PersonDetails ( EventBody eventBody ) {
        this.setIp( eventBody.getCameraIp() );
        this.setDate( eventBody.getCreated_date().toString() );
        this.setTime( eventBody.getCreated_date().toString() );
        this.setConfidence( eventBody.getConfidence().intValue() );
        if ( eventBody.getPsychologyCard() != null
                && eventBody.getPsychologyCard().getPapilonData() != null
                && eventBody.getPsychologyCard().getPapilonData().size() > 0 ) {
            this.setCameraImage( eventBody.getPsychologyCard().getPersonImage() );
            this.setOriginalImage( eventBody.getPsychologyCard().getPapilonData().get( 0 ).getPhoto() );
            this.setPassportSeries( eventBody
                    .getPsychologyCard()
                    .getPapilonData()
                    .get( 0 )
                    .getPassport() );
            if ( eventBody.getPsychologyCard().getPinpp() != null )
                this.setFIO( eventBody.getPsychologyCard().getPinpp().getName() + " " +
                        eventBody.getPsychologyCard().getPinpp().getSurname() + " " +
                        eventBody.getPsychologyCard().getPinpp().getPatronym() ); } }

    public PersonDetails ( EventFace eventFace ) {
        this.setIp( eventFace.getCameraIp() );
        this.setDate( eventFace.getCreated_date().toString() );
        this.setTime( eventFace.getCreated_date().toString() );
        this.setConfidence( eventFace.getConfidence().intValue() );
        if ( eventFace.getPsychologyCard() != null
                && eventFace.getPsychologyCard().getPapilonData() != null
                && eventFace.getPsychologyCard().getPapilonData().size() > 0 ) {
            this.setCameraImage( eventFace.getPsychologyCard().getPersonImage() );
            this.setOriginalImage( eventFace.getPsychologyCard().getPapilonData().get( 0 ).getPhoto() );
            this.setPassportSeries( eventFace
                    .getPsychologyCard()
                    .getPapilonData()
                    .get( 0 )
                    .getPassport() );
            if ( eventFace.getPsychologyCard().getPinpp() != null )
                this.setFIO( eventFace.getPsychologyCard().getPinpp().getName() + " " +
                        eventFace.getPsychologyCard().getPinpp().getSurname() + " " +
                        eventFace.getPsychologyCard().getPinpp().getPatronym() ); } }

    public PersonDetails ( FaceEvent faceEvent ) {
        this.setDate( faceEvent.getCreated_date() );
        this.setTime( faceEvent.getCreated_date() );
        this.setConfidence( faceEvent.getConfidence() );
        this.setIp( faceEvent.getDataInfo().getData().getIp() );
        if ( faceEvent.getPsychologyCard() != null
                && faceEvent.getPsychologyCard().getPapilonData() != null
                && faceEvent.getPsychologyCard().getPapilonData().size() > 0 ) {
            this.setCameraImage( faceEvent.getPsychologyCard().getPersonImage() );
            this.setOriginalImage( faceEvent.getPsychologyCard().getPapilonData().get( 0 ).getPhoto() );
            this.setPassportSeries( faceEvent
                    .getPsychologyCard()
                    .getPapilonData()
                    .get( 0 )
                    .getPassport() );
            if ( faceEvent.getPsychologyCard().getPinpp() != null )
                this.setFIO( faceEvent.getPsychologyCard().getPinpp().getName() + " " +
                        faceEvent.getPsychologyCard().getPinpp().getSurname() + " " +
                        faceEvent.getPsychologyCard().getPinpp().getPatronym() ); } }
}
