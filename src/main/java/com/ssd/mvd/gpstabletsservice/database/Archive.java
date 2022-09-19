package com.ssd.mvd.gpstabletsservice.database;

import lombok.Data;
import java.util.*;
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

    private final List< String > detailsList = List.of( "Ф.И.О", "", "ПОДРАЗДЕЛЕНИЕ", "ДАТА И ВРЕМЯ", "ID",
            "ШИРОТА", "ДОЛГОТА", "ВИД ПРОИСШЕСТВИЯ", "НАЧАЛО СОБЫТИЯ", "КОНЕЦ СОБЫТИЯ",
            "КОЛ.СТВО ПОСТРАДАВШИХ", "КОЛ.СТВО ПОГИБШИХ", "ФАБУЛА" );

    public static Archive getAchieve () { return archive != null ? archive : ( archive = new Archive() ); }

    public String generateToken () {
        byte[] bytes = new byte[ 24 ];
        this.secureRandom.nextBytes( bytes );
        return this.encoder.encodeToString( bytes ); }

    // uses to link Card to current Patrul object, either additional Patrul in case of necessary
    public Mono< ApiResponseModel > save ( Patrul patrul, Card card ) {
        return Mono.just( ApiResponseModel
                .builder()
                .success( true )
                .status( Status.builder()
                        .message( card + " was linked to: "
                                + TaskInspector
                                .getInstance()
                                .changeTaskStatus( patrul, ATTACHED, card )
                                .getName() ).build() ).build() ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventCar card ) {
        return Mono.just( ApiResponseModel
                .builder()
                .success( true )
                .status( Status
                        .builder()
                        .message( card + " was linked to: " + TaskInspector
                                .getInstance()
                                .changeTaskStatus( patrul, ATTACHED, card )
                                .getName() )
                        .build() )
                .build() ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventFace card ) {
        return Mono.just( ApiResponseModel
                .builder()
                .success( true )
                .status( Status
                        .builder()
                        .message( card + " was linked to: " + TaskInspector
                                .getInstance()
                                .changeTaskStatus( patrul, ATTACHED, card )
                                .getName() )
                        .build() )
                .build() ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, CarEvent card ) {
        return Mono.just( ApiResponseModel
                .builder()
                .success( true )
                .status( Status
                        .builder()
                        .message( card + " was linked to: " + TaskInspector
                                .getInstance()
                                .changeTaskStatus( patrul, ATTACHED, card )
                                .getName() )
                        .build() )
                .build() ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventBody card ) {
        return Mono.just( ApiResponseModel
                .builder()
                .success( true )
                .status( Status
                        .builder()
                        .message( card + " was linked to: " + TaskInspector
                                .getInstance()
                                .changeTaskStatus( patrul, ATTACHED, card )
                                .getName() )
                        .build() )
                .build() ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, FaceEvent card ) {
        return Mono.just( ApiResponseModel
                .builder()
                .success( true )
                .status( Status
                        .builder()
                        .message( card + " was linked to: " + TaskInspector
                                .getInstance()
                                .changeTaskStatus( patrul, ATTACHED, card ).getName() )
                        .build() )
                .build() ); }

    public Mono< ApiResponseModel > save ( SelfEmploymentTask selfEmploymentTask, Patrul patrul ) {
        TaskInspector
                .getInstance()
                .changeTaskStatus( patrul, selfEmploymentTask.getTaskStatus(), selfEmploymentTask );
        return Mono.just( ApiResponseModel.builder()
                .status( Status.builder()
                        .message( "SelfEmployment was saved" )
                        .code( 200 )
                        .build() )
                .success( true )
                .build() ); }
}
