package com.ssd.mvd.gpstabletsservice.controller;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.Comparator;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.database.SerDes;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.task.card.CardRequest;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.request.PatrulLoginRequest;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.PatrulActivityStatistics;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.handler.annotation.MessageMapping;

@Slf4j
@RestController
public class PatrulController {
    @MessageMapping ( value = "ping" )
    public Mono< Boolean > ping () { return Mono.just( true ); }

    @MessageMapping ( value = "ARRIVED" )
    public Mono< ApiResponseModel > arrived ( String token ) { return CassandraDataControl
            .getInstance()
            .arrived( token )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "ACCEPTED" )
    public Mono< ApiResponseModel > accepted ( String token ) { return CassandraDataControl
            .getInstance()
            .accepted( token )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "SET_IN_PAUSE" )
    public Mono< ApiResponseModel > setInPause ( String token ) { return CassandraDataControl
            .getInstance()
            .setInPause( token )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "START_TO_WORK" )
    public Mono< ApiResponseModel > starToWork ( String token ) { return CassandraDataControl
            .getInstance()
            .startToWork( token )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "getTaskDetails" )
    public Mono< ApiResponseModel > getTaskDetails ( Data data ) { return TaskInspector
            .getInstance()
            .getTaskDetails( SerDes
                    .getSerDes()
                    .deserialize( data.getData() ) )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "LOGOUT" ) // used to Log out from current Account
    public Mono< ApiResponseModel > patrulLogout ( String token ) { return CassandraDataControl
            .getInstance()
            .logout( token )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "RETURNED_TO_WORK" )
    public Mono< ApiResponseModel > setInActive ( String token ) { return CassandraDataControl
            .getInstance()
            .backToWork( token )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "STOP_TO_WORK" )
    public Mono< ApiResponseModel > finishWorkOfPatrul ( String token ) { return CassandraDataControl
            .getInstance()
            .stopToWork( token )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "LOGIN" ) // for checking login data of Patrul with his Login and password
    public Mono< ApiResponseModel > patrulLogin ( PatrulLoginRequest patrulLoginRequest ) { return CassandraDataControl
            .getInstance()
            .login( patrulLoginRequest )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping( value = "getAllUsersList" ) // returns the list of all created Users
    public Flux< Patrul > getAllUsersList () { return CassandraDataControl
            .getInstance()
            .getGetPatrul()
            .get()
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "getPatrulByPortion" ) // searching Patruls by their partion name
    public Flux< Patrul > getPatrulByPortion ( String name ) { return CassandraDataControl
            .getInstance()
            .getGetPatrul()
            .get()
            .filter( patrul -> patrul
                    .getSurnameNameFatherName()
                    .contains( name ) )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping( value = "addUser" ) // adding new user
    public Mono< ApiResponseModel > addUser ( Patrul patrul ) {
        UnirestController
                .getInstance()
                .addUser( patrul );
        patrul.setSpecialToken( null );
        return CassandraDataControl
                .getInstance()
                .addValue( patrul )
                .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                        error.getMessage(), object ) ) )
                .onErrorReturn( Archive
                        .getArchive()
                        .getErrorResponse()
                        .get() ); }

    @MessageMapping ( value = "findTheClosestPatruls" )
    public Flux< Patrul > findTheClosestPatruls ( Point point ) {
        if ( point.getLatitude() == null && point.getLongitude() == null ) return Flux.empty();
        return CassandraDataControl
                .getInstance()
                .getFindTheClosestPatruls()
                .apply( point )
                .sort( Comparator.comparing( Patrul::getDistance ) )
                .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                        error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "checkToken" )
    public Mono< ApiResponseModel > checkToken ( String token ) { return CassandraDataControl
            .getInstance()
            .checkToken( token )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "getPatrulDataByToken" )
    public Mono< ApiResponseModel > getPatrulDataByToken ( String token ) { return CassandraDataControl
            .getInstance()
            .checkToken( token )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "updatePatrul" )
    public Mono< ApiResponseModel > updatePatrul ( Patrul patrul ) {
        UnirestController
                .getInstance()
                .updateUser( patrul );
        patrul.setSpecialToken( null );
        return CassandraDataControl
                .getInstance()
                .update( patrul )
                .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                        error.getMessage(), object ) ) )
                .onErrorReturn( Archive
                        .getArchive()
                        .getErrorResponse()
                        .get() ); }

    @MessageMapping ( value = "getCurrentUser" )
    public Mono< Patrul > getCurrentUser ( String passportSeries ) {
        if ( passportSeries.length() < 30 ) return Mono.empty();
        return CassandraDataControl
                .getInstance()
                .getGetPatrulByUUID()
                .apply( UUID.fromString( passportSeries ) )
                .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                        error.getMessage(), object ) ) ); }

    @MessageMapping( value = "deletePatrul" )
    public Mono< ApiResponseModel > deletePatrul ( String passportNumber ) {
        UnirestController
                .getInstance()
                .deleteUser( passportNumber );
        return CassandraDataControl
                .getInstance()
                .deletePatrul( UUID.fromString( passportNumber.split( "@" )[0] ) )
                .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                        error.getMessage(), object ) ) )
                .onErrorReturn( Archive
                        .getArchive()
                        .getErrorResponse()
                        .get() ); }

    @MessageMapping ( value = "getAllPatrulTasks" ) // for front end
    public Mono< ApiResponseModel > getListOfPatrulTasks ( String uuid ) { return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( UUID.fromString( uuid ) )
            .flatMap( patrul -> patrul.getListOfTasks().keySet().size() > 0 ?
                    TaskInspector
                            .getInstance()
                            .getListOfPatrulTasks( patrul,
                                    0,
                                    patrul.getListOfTasks().keySet().size() * 2 )
                            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                                    error.getMessage(), object ) ) )
                            .onErrorReturn( Archive
                                    .getArchive()
                                    .getErrorResponse()
                                    .get() )
                    : Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of(
                            "message", "You have not completed any task, so try to fix this problem please",
                            "success", false,
                            "code", 201 ) ) ); }

    @MessageMapping ( value = "getListOfPatrulTasks" )
    public Mono< ApiResponseModel > getListOfPatrulTasks ( Request request ) { return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( CassandraDataControl.getInstance().decode( request.getData() ) )
            .flatMap( patrul -> patrul.getListOfTasks().keySet().size() > 0 ?
                    TaskInspector
                    .getInstance()
                    .getListOfPatrulTasks(
                            patrul, (Integer) request.getObject(),
                            (Integer) request.getSubject() )
                    : Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of(
                            "message", "You have not completed any task, so try to fix this problem please",
                            "success", false,
                            "code", 201 ) ) )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) )
            .onErrorReturn( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() ); }

    @MessageMapping ( value = "addAllPatrulsToChatService" )
    public Mono< ApiResponseModel > addAllPatrulsToChatService ( String token ) {
        CassandraDataControl
            .getInstance()
            .addAllPatrulsToChatService( token );
        return Archive
                .getArchive()
                .getFunction()
                .apply( Map.of(
                        "message", "Successfully added to chat service" ) ); }

    @MessageMapping ( value = "getListOfPatrulsByUUID" )
    public Flux< Patrul > getListOfPatrulsByUUID ( CardRequest< ? > cardRequest ) { return Flux.fromStream(
            cardRequest.getPatruls().stream() )
            .flatMap( uuid -> CassandraDataControl
                .getInstance()
                .getGetPatrulByUUID()
                    .apply( uuid ) )
            .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                    error.getMessage(), object ) ) ); }

    @MessageMapping ( value = "getPatrulStatistics" )
    public Mono< PatrulActivityStatistics > getPatrulStatistics ( Request request ) {
        return request.getData() != null ? CassandraDataControl
                .getInstance()
                .getGetPatrulByUUID()
                .apply( UUID.fromString( request.getData() ) )
                .flatMap( patrul -> CassandraDataControl
                        .getInstance()
                        .getPatrulStatistics( request ) )
                .onErrorContinue( ( (error, object) -> log.error( "Error: {} and reason: {}: ",
                        error.getMessage(), object ) ) )
                : Mono.empty(); }

    @MessageMapping ( value = "getAllUsedTablets" )
    public Mono< List< TabletUsage > > getAllUsedTablets ( RequestForTablets request ) { return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( UUID.fromString( request.getPatrulId() ) )
            .flatMap( patrul -> request.getStartTime() != null
                    && request.getEndTime() != null ? CassandraDataControl
                    .getInstance()
                    .getAllUsedTablets( patrul )
                    .filter( tabletUsages -> tabletUsages
                            .getStartedToUse()
                            .before( request.getEndTime() )
                            && tabletUsages
                            .getStartedToUse()
                            .after( request.getStartTime() ) )
                    .collectList()
                    : CassandraDataControl
                    .getInstance()
                    .getAllUsedTablets( patrul )
                    .collectList() ); }
}