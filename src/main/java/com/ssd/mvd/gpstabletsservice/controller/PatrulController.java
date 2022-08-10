package com.ssd.mvd.gpstabletsservice.controller;

import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.entity.Data;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.database.SerDes;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.response.PatrulInfo;
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
import static java.lang.Math.cos;
import static java.lang.Math.*;
import java.util.Comparator;
import java.util.List;

@RestController
public class PatrulController {
    private static final Double p = PI / 180;

    private Double calculate ( Point first, Patrul second ) { return 12742 * asin( sqrt( 0.5 - cos( ( second.getLatitude() - first.getLatitude() ) * p ) / 2
            + cos( first.getLatitude() * p ) * cos( second.getLatitude() * p ) * ( 1 - cos( ( second.getLongitude() - first.getLongitude() ) * p ) ) / 2 ) ) * 1000; }

    @MessageMapping ( value = "findTheClosestPatruls" )
    public Flux< Patrul > findTheClosestPatruls (Point point ) { return CassandraDataControl.getInstance().getAllPatruls()
            .filter( patrul -> patrul.getStatus().compareTo( com.ssd.mvd.gpstabletsservice.constants.Status.FREE ) == 0
                    && patrul.getTaskTypes().compareTo( TaskTypes.FREE ) == 0
                    && patrul.getLatitude() != null && patrul.getLongitude() != null )
            .map( patrul -> { patrul.setDistance(  this.calculate( point, patrul ) );
                return patrul; } )
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
    public Flux< Patrul > getAllUsersList () { return RedisDataControl.getRedis().getAllPatruls(); }

    @MessageMapping( value = "addUser" ) // adding new user
    public Mono< ApiResponseModel > addUser ( Patrul patrul ) { return RedisDataControl.getRedis().addValue( patrul ); }

    @MessageMapping ( value = "updatePatrul" )
    public Mono< ApiResponseModel > updatePatrul ( Patrul patrul ) { return RedisDataControl.getRedis().update( patrul ); }

    @MessageMapping ( value = "checkToken" )
    public Mono< ApiResponseModel > checkToken ( String token ) { return RedisDataControl.getRedis().checkToken( token ); }

    @MessageMapping ( value = "getCurrentUser" )
    public Mono< Patrul > getCurrentUser ( String passportSeries ) { return RedisDataControl.getRedis().getPatrul( passportSeries ); }

    @MessageMapping ( value = "getPatrulDataByToken" )
    public Mono< Status > getPatrulDataByToken ( String token ) { return RedisDataControl.getRedis().checkToken( token )
            .flatMap( apiResponseModel -> Mono.just( apiResponseModel.getStatus() ) ); }

    @MessageMapping( value = "deletePatrul" )
    public Mono< ApiResponseModel > deletePatrul ( String passportNumber ) { return RedisDataControl.getRedis().deletePatrul( passportNumber ); }

    @MessageMapping ( value = "getPatrulStatistics" )
    public Mono< PatrulActivityStatistics > getPatrulStatistics ( Request passportNumber ) { return RedisDataControl.getRedis().getPatrulStatistics( passportNumber ); }

    @MessageMapping ( value = "getPatrulByPortion" ) // searching Patruls by their partion name
    public Flux< PatrulInfo > getPatrulByPortion ( String name ) { return CassandraDataControl.getInstance()
            .getPatruls( name )
            .map( row -> new PatrulInfo( row.getString( "NSF" ) ) ); }
}