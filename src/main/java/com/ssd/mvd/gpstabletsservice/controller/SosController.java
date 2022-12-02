package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.sos_task.SosRequest;
import com.ssd.mvd.gpstabletsservice.task.sos_task.PatrulSos;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.database.Archive;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import lombok.extern.slf4j.Slf4j;
import java.util.Map;

@Slf4j
@RestController
public class SosController {
    @MessageMapping( value = "getAllSosEntities" )
    public Flux< PatrulSos > getAllSosEntities () { return CassandraDataControlForTasks
            .getInstance()
            .getGetAllSos()
            .get()
            .onErrorContinue( ( throwable, o ) -> log.error(
                    "Error: " + throwable.getMessage()
                            + " Reason: " + o ) )
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
                ? Archive
                .getArchive()
                .getFunction()
                .apply( Map.of(
                        "message", "U did not send SOS signal",
                        "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                .builder()
                                .data( Status.IN_ACTIVE )
                                .build() ) )
                : Archive
                .getArchive()
                .getFunction()
                .apply( Map.of(
                        "message", "U have SOS signal",
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
                .onErrorContinue( ( throwable, o ) -> log.error(
                        "Error: " + throwable.getMessage()
                                + " Reason: " + o ) )
                .onErrorReturn( Archive.getArchive().getErrorResponse().get() ); }

    // в случае возникновения какой - либо опасности, патрульный модет отправить сигнал СОС
    // метод перехватывает этот сигнал и вносит в базу и шлет оповещение на фронт
    @MessageMapping ( value = "saveSosFromPatrul" )
    public Mono< ApiResponseModel > saveSosFromPatrul ( PatrulSos patrulSos ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getSavePatrulSos()
                .apply( patrulSos )
                .onErrorContinue( ( throwable, o ) -> log.error(
                        "Error: " + throwable.getMessage()
                                + " Reason: " + o ) )
                .onErrorReturn( Archive
                        .getArchive()
                        .getErrorResponse()
                        .get() ); }

    @MessageMapping ( value = "updatePatrulStatusInSosTable" )
    public Mono< ApiResponseModel > updatePatrulStatusInSosTable ( SosRequest sosRequest ) {
        return CassandraDataControlForTasks
                .getInstance()
                .getUpdatePatrulStatusInSosTable()
                .apply( sosRequest )
                .onErrorContinue( ( throwable, o ) -> log.error(
                        "Error: " + throwable.getMessage()
                                + " Reason: " + o ) )
                .onErrorReturn( Archive
                        .getArchive()
                        .getErrorResponse()
                        .get() ); }
}
