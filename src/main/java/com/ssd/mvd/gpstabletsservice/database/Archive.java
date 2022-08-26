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
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvents;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvents;

@Data
public class Archive {
    public Boolean flag = true;
    private static Archive archive = new Archive();
    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder();

    private final List< String > detailsList = List.of( "Ф.И.О", "", "ПОДРАЗДЕЛЕНИЕ", "ДАТА И ВРЕМЯ", "ID",
            "ШИРОТА", "ДОЛГОТА", "ВИД ПРОИСШЕСТВИЯ", "НАЧАЛО СОБЫТИЯ", "КОНЕЦ СОБЫТИЯ",
            "КОЛ.СТВО ПОСТРАДАВШИХ", "КОЛ.СТВО ПОШИБЩИХ", "ФАБУЛА" );

    public static Archive getAchieve () { return archive != null ? archive : ( archive = new Archive() ); }

    // link new Patrul to existing SelfEmployment object
    public Mono< ApiResponseModel > save ( UUID uuid, Patrul patrul ) { return CassandraDataControlForTasks
            .getInstance()
            .getSelfEmploymentTask( uuid )
            .flatMap( selfEmploymentTask -> {
                selfEmploymentTask.getPatruls().put( patrul.getUuid(),
                        TaskInspector.getInstance().changeTaskStatus(
                                patrul,
                                ATTACHED,
                                selfEmploymentTask ) );
                RedisDataControl.getRedis()
                        .addValue( selfEmploymentTask.getUuid().toString(),
                                new ActiveTask( selfEmploymentTask ) )
                        .subscribe();
                CassandraDataControlForTasks
                        .getInstance()
                        .addValue( selfEmploymentTask );
                return RedisDataControl.getRedis().update( patrul ); } ); }

    // taking off some Patrul from current Card
    public Mono< ApiResponseModel > removePatrulFromSelfEmployment ( UUID uuid, Patrul patrul ) { return CassandraDataControlForTasks
            .getInstance()
            .getSelfEmploymentTask( uuid )
                .flatMap( selfEmploymentTask -> {
                    TaskInspector.getInstance().changeTaskStatus( patrul, CANCEL, selfEmploymentTask );
                    CassandraDataControlForTasks
                            .getInstance()
                            .addValue( selfEmploymentTask );
                    return RedisDataControl.getRedis().update( patrul ); } ); }

    public Mono< ApiResponseModel > addNewPatrulToCard ( Long cardId, Patrul patrul ) { return RedisDataControl.getRedis().getCard( cardId )
            .flatMap( card -> {
                TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, card );
                return Mono.just( ApiResponseModel.builder().success( true )
                                .status( Status.builder()
                                        .message( patrul.getName() + " linked to " + card.getCardId() )
                                        .code( 200 )
                                        .build() )
                        .build() ); } ); }

    public Mono< ApiResponseModel > removePatrulFromCard ( Long cardId, Patrul patrul ) { return RedisDataControl.getRedis()
            .getCard( cardId )
            .flatMap( card -> {
                TaskInspector.getInstance().changeTaskStatus( patrul, CANCEL, card );
                return Mono.just( ApiResponseModel.builder()
                        .success( true )
                        .status( Status.builder()
                                .message( patrul.getName() + " removed from: " + card.getCardId() )
                                .code( 200 ).build() )
                        .build() ); } ); }

    // uses to link Card to current Patrul object, either additional Patrul in case of necessary
    public Mono< ApiResponseModel > save ( Patrul patrul, Card card ) {
        TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, card );
        return RedisDataControl.getRedis().update( patrul )
                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder().success( true )
                        .status( Status.builder()
                                .message( card + " was linked to: " + patrul.getName() ).build() ).build() ) ); }

    public Mono< ApiResponseModel > save ( SelfEmploymentTask selfEmploymentTask, Patrul patrul ) {
            TaskInspector.getInstance().changeTaskStatus( patrul, selfEmploymentTask.getTaskStatus(), selfEmploymentTask );
            RedisDataControl.getRedis()
                    .addValue( selfEmploymentTask.getUuid().toString(),
                            new ActiveTask( selfEmploymentTask ) );
            return RedisDataControl.getRedis()
                    .update( patrul )
                    .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                            .status( Status.builder()
                                    .message( "SelfEmployment was saved" )
                                    .code( 200 )
                                    .build() )
                            .success( CassandraDataControlForTasks
                                    .getInstance()
                                    .addValue( selfEmploymentTask ) )
                            .build() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventFace card ) {
        TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, card );
        return RedisDataControl.getRedis().update( patrul )
                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                        .success( true )
                        .status( Status.builder()
                                .message( card + " was linked to: " + patrul.getName() )
                                .build() )
                        .build() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventBody card ) {
        TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, card );
        return RedisDataControl.getRedis().update( patrul )
                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                        .success( true )
                        .status( Status.builder()
                                .message( card + " was linked to: " + patrul.getName() )
                                .build() )
                        .build() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, EventCar card ) {
        TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, card );
        return RedisDataControl.getRedis().update( patrul )
                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                        .success( true )
                        .status( Status.builder()
                                .message( card + " was linked to: " + patrul.getName() )
                                .build() )
                        .build() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, FaceEvents card ) {
        TaskInspector.getInstance()
                .changeTaskStatus( patrul, ATTACHED, card );
        return RedisDataControl.getRedis().update( patrul )
                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                        .success( true )
                        .status( Status.builder()
                                .message( card + " was linked to: " + patrul.getName() )
                                .build() )
                        .build() ) ); }

    public Mono< ApiResponseModel > save ( Patrul patrul, CarEvents card ) {
        TaskInspector.getInstance().changeTaskStatus( patrul, ATTACHED, card );
        return RedisDataControl.getRedis().update( patrul )
                .flatMap( apiResponseModel -> Mono.just( ApiResponseModel.builder()
                        .success( true )
                        .status( Status.builder()
                                .message( card + " was linked to: " + patrul.getName()  )
                                .build() )
                        .build() ) ); }

    public String generateToken () {
        byte[] bytes = new byte[ 24 ];
        this.secureRandom.nextBytes( bytes );
        return this.encoder.encodeToString( bytes ); }
}
