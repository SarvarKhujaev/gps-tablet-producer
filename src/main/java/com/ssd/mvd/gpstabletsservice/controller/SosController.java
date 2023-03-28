package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.sos_task.SosRequest;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.task.sos_task.PatrulSos;
import com.ssd.mvd.gpstabletsservice.constants.Status;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Map;

@RestController
public class SosController extends LogInspector {
    @MessageMapping( value = "getAllSosEntities" )
    public Flux< PatrulSos > getAllSosEntities () { return CassandraDataControl
            .getInstance()
            .getGetAllEntities()
            .apply( CassandraTables.TABLETS, CassandraTables.PATRUL_SOS_TABLE )
            .filter( row -> Status.valueOf( row.getString( "status" ) ).compareTo( Status.FINISHED ) != 0 )
            .flatMap( row -> Mono.just( new PatrulSos( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging )
            .onErrorReturn( new PatrulSos() ); }

    // используется планшетом чтобы проверить не отправлял ли он СОС раньше
    @MessageMapping ( value = "checkSosStatus" )
    public Mono< ApiResponseModel > checkSosStatus ( String token ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getCheckSosTable()
                .test( CassandraDataControl
                        .getInstance()
                        .getDecode()
                        .apply( token ) )
                ? super.getFunction().apply(
                        Map.of( "message", "U did not send SOS signal",
                        "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                .builder()
                                .data( Status.IN_ACTIVE )
                                .build() ) )
                : super.getFunction().apply(
                        Map.of( "message", "U have SOS signal",
                        "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                .builder()
                                .data( Status.ACTIVE )
                                .build() ) ); }

    // возвращает список из сос сигналов которые еще не были закрыты и привязаны к данному патрульному
    @MessageMapping ( value = "getAllSosForCurrentPatrul" )
    public Mono< ApiResponseModel > getAllSosForCurrentPatrul ( String token ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getGetAllSosForCurrentPatrul()
                .apply( CassandraDataControl
                        .getInstance()
                        .getDecode()
                        .apply( token ) )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }

    // в случае возникновения какой - либо опасности, патрульный модет отправить сигнал СОС
    // метод перехватывает этот сигнал и вносит в базу и шлет оповещение на фронт
    @MessageMapping ( value = "saveSosFromPatrul" )
    public Mono< ApiResponseModel > saveSosFromPatrul ( PatrulSos patrulSos ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getSavePatrulSos()
                .apply( patrulSos )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "updatePatrulStatusInSosTable" )
    public Mono< ApiResponseModel > updatePatrulStatusInSosTable ( SosRequest sosRequest ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getUpdatePatrulStatusInSosTable()
                .apply( sosRequest )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }
}
