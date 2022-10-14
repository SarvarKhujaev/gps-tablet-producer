package com.ssd.mvd.gpstabletsservice.database;

import java.util.*;
import java.util.function.Function;

import lombok.Data;
import java.security.SecureRandom;
import reactor.core.publisher.Mono;

import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.response.Status;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;

@Data
public class Archive {
    public Boolean flag = true;
    private static Archive archive = new Archive();
    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder();
    private final Function< Map< String, ? >, Mono< ApiResponseModel > > function =
            map -> Mono.just( ApiResponseModel
            .builder() // in case of wrong login
            .status( Status
                    .builder()
                    .message( map.get( "message" ).toString() )
                    .code( map.containsKey( "code" ) ?
                            Long.parseLong( map.get( "code" ).toString() ) : 200 )
                    .build() )
            .data( map.containsKey( "data" ) ?
                    (com.ssd.mvd.gpstabletsservice.entity.Data) map.get( "data" )
                    : com.ssd.mvd.gpstabletsservice.entity.Data.builder().build() )
            .success( !map.containsKey( "success" ) )
            .build() );

    private final List< String > detailsList = List.of( "Ф.И.О", "", "ПОДРАЗДЕЛЕНИЕ", "ДАТА И ВРЕМЯ", "ID",
            "ШИРОТА", "ДОЛГОТА", "ВИД ПРОИСШЕСТВИЯ", "НАЧАЛО СОБЫТИЯ", "КОНЕЦ СОБЫТИЯ",
            "КОЛ.СТВО ПОСТРАДАВШИХ", "КОЛ.СТВО ПОГИБШИХ", "ФАБУЛА" );

    public static Archive getArchive () { return archive != null ? archive : ( archive = new Archive() ); }

    public String generateToken () {
        byte[] bytes = new byte[ 24 ];
        this.secureRandom.nextBytes( bytes );
        return this.encoder.encodeToString( bytes ); }

    public Mono< ApiResponseModel > getResponse ( String message,
                                                  Long code,
                                                  Boolean success,
                                                  com.ssd.mvd.gpstabletsservice.entity.Data data ) {
        return Mono.just( ApiResponseModel
                .builder() // in case of wrong login
                .status( Status
                        .builder()
                        .code( code )
                        .message( message )
                        .build() )
                        .data( data != null ? data : com.ssd.mvd.gpstabletsservice.entity.Data.builder().build() )
                .success( success )
                .build() ); }

    // uses to link Card to current Patrul object, either additional Patrul in case of necessary
    public Mono< ApiResponseModel > save ( Patrul patrul, Card card ) { return this.getResponse(
            card + " was linked to: "
                    + TaskInspector
                    .getInstance()
                    .changeTaskStatus( patrul, ATTACHED, card )
                    .getName(),
            200L,
            true,
            null ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventCar eventCar ) { return this.getResponse(
            eventCar + " was linked to: " + TaskInspector
                    .getInstance()
                    .changeTaskStatus( patrul, ATTACHED, eventCar )
                    .getName(),
            200L,
            true,
            null ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, CarEvent carEvent ) { return this.getResponse(
            carEvent + " was linked to: " + TaskInspector
                    .getInstance()
                    .changeTaskStatus( patrul, ATTACHED, carEvent )
                    .getName(),
            200L,
            true,
            null ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventFace eventFace ) { return this.getResponse(
            eventFace + " was linked to: " + TaskInspector
                    .getInstance()
                    .changeTaskStatus( patrul, ATTACHED, eventFace )
                    .getName(),
            200L,
            true,
            null ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventBody eventBody ) { return this.getResponse(
            eventBody + " was linked to: " + TaskInspector
                    .getInstance()
                    .changeTaskStatus( patrul, ATTACHED, eventBody )
                    .getName(),
            200L,
            true,
            null ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, FaceEvent faceEvent ) { return this.getResponse(
            faceEvent + " was linked to: " + TaskInspector
            .getInstance()
            .changeTaskStatus( patrul, ATTACHED, faceEvent ).getName(),
            200L,
            true,
            null ); }

    public Mono< ApiResponseModel > save ( SelfEmploymentTask selfEmploymentTask, Patrul patrul ) {
        return this.getResponse(
                "SelfEmployment was linked to: "
                        + TaskInspector
                        .getInstance()
                        .changeTaskStatus( patrul, selfEmploymentTask.getTaskStatus(), selfEmploymentTask )
                        .getName(),
                200L,
                true,
                null ); }
}
