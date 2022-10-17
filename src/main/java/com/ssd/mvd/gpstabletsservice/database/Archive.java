package com.ssd.mvd.gpstabletsservice.database;

import java.util.*;
import java.util.function.Function;

import lombok.Data;
import java.security.SecureRandom;
import java.util.function.Supplier;

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

    private final Supplier< ApiResponseModel > errorResponse = () -> ApiResponseModel
            .builder() // in case of wrong login
            .status( Status
                    .builder()
                    .message( "Server error" )
                    .code( 201 )
                    .build() )
            .success( false )
            .build();

    private final List< String > detailsList = List.of( "Ф.И.О", "", "ПОДРАЗДЕЛЕНИЕ", "ДАТА И ВРЕМЯ", "ID",
            "ШИРОТА", "ДОЛГОТА", "ВИД ПРОИСШЕСТВИЯ", "НАЧАЛО СОБЫТИЯ", "КОНЕЦ СОБЫТИЯ",
            "КОЛ.СТВО ПОСТРАДАВШИХ", "КОЛ.СТВО ПОГИБШИХ", "ФАБУЛА" );

    public static Archive getArchive () { return archive != null ? archive : ( archive = new Archive() ); }

    public String generateToken () {
        byte[] bytes = new byte[ 24 ];
        this.secureRandom.nextBytes( bytes );
        return this.encoder.encodeToString( bytes ); }

    // uses to link Card to current Patrul object, either additional Patrul in case of necessary
    public Mono< ApiResponseModel > save ( Patrul patrul, Card card ) { return this.getFunction()
            .apply( Map.of( "message", card + " was linked to: "
                    + TaskInspector
                    .getInstance()
                    .changeTaskStatus( patrul, ATTACHED, card )
                    .getName() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventCar eventCar ) { return this.getFunction()
            .apply( Map.of( "message", eventCar + " was linked to: "
                    + TaskInspector
                    .getInstance()
                    .changeTaskStatus( patrul, ATTACHED, eventCar )
                    .getName() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, CarEvent carEvent ) { return this.getFunction()
            .apply( Map.of( "message", carEvent + " was linked to: "
                    + TaskInspector
                    .getInstance()
                    .changeTaskStatus( patrul, ATTACHED, carEvent )
                    .getName() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventFace eventFace ) { return this.getFunction()
            .apply( Map.of( "message", eventFace + " was linked to: "
                    + TaskInspector
                    .getInstance()
                    .changeTaskStatus( patrul, ATTACHED, eventFace )
                    .getName() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventBody eventBody ) { return this.getFunction()
            .apply( Map.of( "message", eventBody + " was linked to: "
                    + TaskInspector
                    .getInstance()
                    .changeTaskStatus( patrul, ATTACHED, eventBody )
                    .getName() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, FaceEvent faceEvent ) { return this.getFunction()
            .apply( Map.of( "message", faceEvent + " was linked to: "
                    + TaskInspector
                    .getInstance()
                    .changeTaskStatus( patrul, ATTACHED, faceEvent ).getName() ) ); }

    public Mono< ApiResponseModel > save ( SelfEmploymentTask selfEmploymentTask, Patrul patrul ) {
        return this.getFunction()
                .apply( Map.of( "message", "SelfEmployment was linked to: "
                        + TaskInspector
                        .getInstance()
                        .changeTaskStatus( patrul, selfEmploymentTask.getTaskStatus(), selfEmploymentTask )
                        .getName() ) ); }
}
