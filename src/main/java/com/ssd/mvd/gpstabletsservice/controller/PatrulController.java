package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.entity.Data;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.database.SerDes;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.entity.TaskInspector;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.database.RedisDataControl;
import com.ssd.mvd.gpstabletsservice.request.PatrulLoginRequest;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.PatrulActivityStatistics;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.Comparator;
import java.util.UUID;

@RestController
public class PatrulController {
    @MessageMapping ( value = "findTheClosestPatruls" )
    public Flux< Patrul > findTheClosestPatruls ( Point point ) { return CassandraDataControl
            .getInstance()
            .findTheClosestPatruls( point )
            .sort( Comparator.comparing( Patrul::getDistance ) ); }

    @MessageMapping ( value = "getTaskDetails" )
    public Mono< ApiResponseModel > getTaskDetails ( Data data ) { return TaskInspector.getInstance()
            .getTaskDetails( SerDes.getSerDes().deserialize( data.getData() ) ); }

    @MessageMapping ( value = "ARRIVED" )
    public Mono< ApiResponseModel > arrived ( String token ) { return RedisDataControl.getRedis().arrived( token ); }

    @MessageMapping ( value = "ACCEPTED" )
    public Mono< ApiResponseModel > accepted ( String token ) { return RedisDataControl.getRedis().accepted( token ); }

    @MessageMapping ( value = "SET_IN_PAUSE" )
    public Mono< ApiResponseModel > setInPause ( String token ) { return RedisDataControl.getRedis().setInPause( token ); }

    @MessageMapping ( value = "LOGOUT" ) // used to Log out from current Account
    public Mono< ApiResponseModel > patrulLogout ( String token ) { return RedisDataControl.getRedis().logout( token ); }

    @MessageMapping ( value = "RETURNED_TO_WORK" )
    public Mono< ApiResponseModel > setInActive ( String token ) { return RedisDataControl.getRedis().backToWork( token ); }

    @MessageMapping ( value = "START_TO_WORK" )
    public Mono< ApiResponseModel > starToWork ( String token ) { return RedisDataControl.getRedis().startToWork( token ); }

    @MessageMapping ( value = "STOP_TO_WORK" )
    public Mono< ApiResponseModel > finishWorkOfPatrul ( String token ) { return RedisDataControl.getRedis().stopToWork( token ); }

    @MessageMapping ( value = "LOGIN" ) // for checking login data of Patrul with his Login and password
    public Mono< ApiResponseModel > patrulLogin ( PatrulLoginRequest patrulLoginRequest ) { return RedisDataControl.getRedis().login( patrulLoginRequest ); }

    @MessageMapping( value = "getAllUsersList" ) // returns the list of all created Users
    public Flux< Patrul > getAllUsersList () { return CassandraDataControl
            .getInstance()
            .getPatrul(); }

    @MessageMapping( value = "addUser" ) // adding new user
    public Mono< ApiResponseModel > addUser ( Patrul patrul ) { return RedisDataControl.getRedis().addValue( patrul ); }

    @MessageMapping ( value = "updatePatrul" )
    public Mono< ApiResponseModel > updatePatrul ( Patrul patrul ) { return RedisDataControl
            .getRedis()
            .update( patrul ); }

    @MessageMapping ( value = "checkToken" )
    public Mono< ApiResponseModel > checkToken ( String token ) { return RedisDataControl.getRedis().checkToken( token ); }

    @MessageMapping ( value = "getCurrentUser" )
    public Mono< Patrul > getCurrentUser ( String passportSeries ) { return CassandraDataControl
            .getInstance()
            .getPatrul( UUID.fromString( passportSeries ) ); }

    @MessageMapping ( value = "getPatrulDataByToken" )
    public Mono< Status > getPatrulDataByToken ( String token ) { return RedisDataControl
            .getRedis()
            .checkToken( token )
            .flatMap( apiResponseModel -> Mono.just( apiResponseModel.getStatus() ) ); }

    @MessageMapping( value = "deletePatrul" )
    public Mono< ApiResponseModel > deletePatrul ( String passportNumber ) { return RedisDataControl
            .getRedis()
            .deletePatrul( passportNumber ); }

    @MessageMapping ( value = "getPatrulStatistics" )
    public Mono< PatrulActivityStatistics > getPatrulStatistics ( Request passportNumber ) { return RedisDataControl
            .getRedis()
            .getPatrulStatistics( passportNumber ); }

    @MessageMapping ( value = "getPatrulByPortion" ) // searching Patruls by their partion name
    public Flux< Patrul > getPatrulByPortion ( String name ) { return CassandraDataControl
            .getInstance()
            .getPatrul()
            .filter( patrul -> patrul.getSurnameNameFatherName().contains( name ) ); }

    @MessageMapping ( value = "addAllPatrulsToChatService" )
    public Flux< ApiResponseModel > addAllPatrulsToChatService ( String token ) { return RedisDataControl
            .getRedis()
            .addAllPatrulsToChatService( token ); }
}