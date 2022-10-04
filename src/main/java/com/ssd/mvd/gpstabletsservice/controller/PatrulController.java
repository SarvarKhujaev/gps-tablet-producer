package com.ssd.mvd.gpstabletsservice.controller;

import java.util.List;
import java.util.UUID;
import java.util.Comparator;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.database.SerDes;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.task.card.CardRequest;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.request.PatrulLoginRequest;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.PatrulActivityStatistics;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.handler.annotation.MessageMapping;

@RestController
public class PatrulController {
    @MessageMapping ( value = "ping" )
    public Mono< Boolean > ping () { return Mono.just( true ); }

    @MessageMapping ( value = "ARRIVED" )
    public Mono< ApiResponseModel > arrived ( String token ) { return CassandraDataControl
            .getInstance()
            .arrived( token ); }

    @MessageMapping ( value = "ACCEPTED" )
    public Mono< ApiResponseModel > accepted ( String token ) { return CassandraDataControl
            .getInstance()
            .accepted( token ); }

    @MessageMapping ( value = "SET_IN_PAUSE" )
    public Mono< ApiResponseModel > setInPause ( String token ) { return CassandraDataControl
            .getInstance()
            .setInPause( token ); }

    @MessageMapping ( value = "START_TO_WORK" )
    public Mono< ApiResponseModel > starToWork ( String token ) { return CassandraDataControl
            .getInstance()
            .startToWork( token ); }

    @MessageMapping ( value = "getTaskDetails" )
    public Mono< ApiResponseModel > getTaskDetails ( Data data ) { return TaskInspector
            .getInstance()
            .getTaskDetails( SerDes
                    .getSerDes()
                    .deserialize( data.getData() ) ); }

    @MessageMapping ( value = "LOGOUT" ) // used to Log out from current Account
    public Mono< ApiResponseModel > patrulLogout ( String token ) { return CassandraDataControl
            .getInstance()
            .logout( token ); }

    @MessageMapping ( value = "RETURNED_TO_WORK" )
    public Mono< ApiResponseModel > setInActive ( String token ) { return CassandraDataControl
            .getInstance()
            .backToWork( token ); }

    @MessageMapping ( value = "STOP_TO_WORK" )
    public Mono< ApiResponseModel > finishWorkOfPatrul ( String token ) { return CassandraDataControl
            .getInstance()
            .stopToWork( token ); }

    @MessageMapping ( value = "LOGIN" ) // for checking login data of Patrul with his Login and password
    public Mono< ApiResponseModel > patrulLogin ( PatrulLoginRequest patrulLoginRequest ) { return CassandraDataControl
            .getInstance()
            .login( patrulLoginRequest ); }

    @MessageMapping( value = "getAllUsersList" ) // returns the list of all created Users
    public Flux< Patrul > getAllUsersList () { return CassandraDataControl
            .getInstance()
            .getPatrul(); }

    @MessageMapping ( value = "getPatrulByPortion" ) // searching Patruls by their partion name
    public Flux< Patrul > getPatrulByPortion ( String name ) { return CassandraDataControl
            .getInstance()
            .getPatrul()
            .filter( patrul -> patrul
                    .getSurnameNameFatherName()
                    .contains( name ) ); }

    @MessageMapping( value = "addUser" ) // adding new user
    public Mono< ApiResponseModel > addUser ( Patrul patrul ) {
        UnirestController
                .getInstance()
                .addUser( patrul );
        patrul.setSpecialToken( null );
        return CassandraDataControl
            .getInstance()
            .addValue( patrul ); }

    @MessageMapping ( value = "findTheClosestPatruls" )
    public Flux< Patrul > findTheClosestPatruls ( Point point ) {
        if ( point.getLatitude() == null && point.getLongitude() == null ) return Flux.empty();
        return CassandraDataControl
                .getInstance()
                .findTheClosestPatruls( point )
                .sort( Comparator.comparing( Patrul::getDistance ) ); }

    @MessageMapping ( value = "checkToken" )
    public Mono< ApiResponseModel > checkToken ( String token ) { return CassandraDataControl
            .getInstance()
            .checkToken( token ); }

    @MessageMapping ( value = "getPatrulDataByToken" )
    public Mono< ApiResponseModel > getPatrulDataByToken ( String token ) { return CassandraDataControl
            .getInstance()
            .checkToken( token ); }

    @MessageMapping ( value = "updatePatrul" )
    public Mono< ApiResponseModel > updatePatrul ( Patrul patrul ) {
        UnirestController
                .getInstance()
                .updateUser( patrul );
        patrul.setSpecialToken( null );
        return CassandraDataControl
                .getInstance()
                .update( patrul ); }

    @MessageMapping ( value = "getCurrentUser" )
    public Mono< Patrul > getCurrentUser ( String passportSeries ) {
        if ( passportSeries.length() < 30 ) return Mono.empty();
        return CassandraDataControl
                .getInstance()
                .getPatrul( UUID.fromString( passportSeries ) ); }

    @MessageMapping( value = "deletePatrul" )
    public Mono< ApiResponseModel > deletePatrul ( String passportNumber ) {
        UnirestController
                .getInstance()
                .deleteUser( passportNumber );
        return CassandraDataControl
                .getInstance()
                .deletePatrul( UUID.fromString( passportNumber.split( "@" )[0] ) ); }

    @MessageMapping ( value = "addAllPatrulsToChatService" )
    public Mono< ApiResponseModel > addAllPatrulsToChatService ( String token ) {
        CassandraDataControl
            .getInstance()
            .addAllPatrulsToChatService( token );
        return Mono.just( ApiResponseModel
                .builder()
                .success( true )
                .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .message( "Successfully added to chat service" )
                                .code( 200 )
                                .build() )
                .build() ); }

    @MessageMapping ( value = "getListOfPatrulsByUUID" )
    public Flux< Patrul > getListOfPatrulsByUUID ( CardRequest< ? > cardRequest ) { return Flux.fromStream(
            cardRequest.getPatruls().stream() )
            .flatMap( uuid -> CassandraDataControl
                .getInstance()
                .getPatrul( uuid ) ); }

    @MessageMapping ( value = "getPatrulStatistics" )
    public Mono< PatrulActivityStatistics > getPatrulStatistics ( Request request ) {
        return request.getData() != null ? CassandraDataControl
            .getInstance()
            .getPatrul( UUID.fromString( request.getData() ) )
            .flatMap( patrul -> CassandraDataControl
                    .getInstance()
                    .getPatrulStatistics( request ) ) : Mono.empty(); }

    @MessageMapping ( value = "getAllUsedTablets" )
    public Mono< List< TabletUsage > > getAllUsedTablets ( RequestForTablets request ) { return CassandraDataControl
            .getInstance()
            .getPatrul( UUID.fromString( request.getPatrulId() ) )
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