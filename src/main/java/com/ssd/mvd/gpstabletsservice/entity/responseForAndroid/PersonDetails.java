package com.ssd.mvd.gpstabletsservice.entity.responseForAndroid;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;

public final class PersonDetails extends DataValidateInspector {
    public void setIp ( final String ip ) {
        this.ip = ip;
    }

    public void setFIO ( final String FIO ) {
        this.FIO = FIO;
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

    public void setPassportSeries ( final String passportSeries ) {
        this.passportSeries = passportSeries;
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
    private String FIO;
    private String thumbnail;
    private String cameraImage; // фото человека с камеры
    private String dossier_photo;
    private String passportSeries;

    private Long date;
    private Long time;
    private Double confidence;

    public static <T> PersonDetails from ( final T object ) {
        if ( object instanceof EventBody ) {
            return new PersonDetails(
                    (EventBody) object
            );
        } else if ( object instanceof EventFace ) {
            return new PersonDetails(
                    (EventFace) object
            );
        } else {
            return new PersonDetails(
                    (FaceEvent) object
            );
        }
    }

    private PersonDetails ( final EventBody eventBody ) {
        this.setIp( eventBody.getCameraIp() );
        this.setConfidence( eventBody.getConfidence() );

        this.setDate( super
                .objectIsNotNull( eventBody.getCreated_date() )
                ? eventBody.getCreated_date().getTime()
                : null );

        this.setTime( super
                .objectIsNotNull( eventBody.getCreated_date() )
                ? eventBody.getCreated_date().getTime()
                : null );

        this.setThumbnail( eventBody.getThumbnail() );
        this.setCameraImage( eventBody.getFullframe() );
        this.setDossier_photo( eventBody.getMatched_dossier() );

        if ( super
                .objectIsNotNull( eventBody.getPsychologyCard() ) ) {
            this.setFIO( super.isCollectionNotEmpty(
                    eventBody
                            .getPsychologyCard()
                            .getPapilonData() )
                    ? super.splitWordAndJoin(
                            eventBody
                                .getPsychologyCard()
                                .getPapilonData()
                                .get( 0 )
                                .getName() )
                    : null );

            this.setPassportSeries( super.isCollectionNotEmpty(
                    eventBody
                            .getPsychologyCard()
                            .getPapilonData() )
                    ? eventBody
                    .getPsychologyCard()
                    .getPapilonData()
                    .get( 0 )
                    .getPassport()
                    .split( " " )[0]
                    : null );
        }
    }

    private PersonDetails ( final EventFace eventFace ) {
        this.setIp( eventFace.getCameraIp() );
        this.setConfidence( eventFace.getConfidence() );

        this.setDate(
                super.objectIsNotNull( eventFace.getCreated_date() )
                        ? eventFace.getCreated_date().getTime()
                        : null );

        this.setTime(
                super.objectIsNotNull( eventFace.getCreated_date() )
                        ? eventFace.getCreated_date().getTime()
                        : null );

        this.setThumbnail( eventFace.getThumbnail() );
        this.setCameraImage( eventFace.getFullframe() );
        this.setDossier_photo( eventFace.getMatched_dossier() );

        if ( super.objectIsNotNull( eventFace.getPsychologyCard() ) ) {
            this.setFIO(
                    super.isCollectionNotEmpty(
                            eventFace
                                    .getPsychologyCard()
                                    .getPapilonData() )
                            ? super.splitWordAndJoin(
                                    eventFace
                                        .getPsychologyCard()
                                        .getPapilonData()
                                        .get( 0 )
                                        .getName() )
                            : null );

            this.setPassportSeries(
                    super.isCollectionNotEmpty(
                            eventFace
                                    .getPsychologyCard()
                                    .getPapilonData() )
                            ? eventFace
                                .getPsychologyCard()
                                .getPapilonData()
                                .get( 0 )
                                .getPassport()
                                .split( " " )[0]
                                : null );
        }
    }

    private PersonDetails ( final FaceEvent faceEvent ) {
        this.setConfidence( faceEvent.getConfidence() );
        this.setTime(
                super.objectIsNotNull( faceEvent.getCreated_date() )
                        && !faceEvent.getCreated_date().equals( "null" )
                        ? super.convertTimeToLong( faceEvent.getCreated_date() )
                        : null );

        this.setIp(
                super.objectIsNotNull( faceEvent.getDataInfo() )
                && super.objectIsNotNull( faceEvent.getDataInfo().getCadaster() )
                        ? faceEvent.getDataInfo().getCadaster().getIp()
                        : null );

        // в случае если псих портрет отсутствует то возмем отсюда
        this.setFIO( faceEvent.getComment() );
        this.setThumbnail( faceEvent.getThumbnail() );
        this.setCameraImage( faceEvent.getFullframe() );
        this.setDossier_photo( faceEvent.getDossier_photo() );

        if ( super.objectIsNotNull( faceEvent.getPsychologyCard() ) ) {
            this.setFIO( super.isCollectionNotEmpty( faceEvent.getPsychologyCard().getPapilonData() )
                    ? super.splitWordAndJoin(
                            faceEvent
                                .getPsychologyCard()
                                .getPapilonData()
                                .get( 0 )
                                .getName() )
                    : null );

            this.setPassportSeries(
                    super.isCollectionNotEmpty( faceEvent.getPsychologyCard().getPapilonData() )
                            ? faceEvent
                                .getPsychologyCard()
                                .getPapilonData()
                                .get( 0 )
                                .getPassport()
                                .split( " " )[0]
                                : null
            );
        }
    }
}
