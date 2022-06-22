package com.ssd.mvd.gpstracker.controller;

import com.ssd.mvd.gpstracker.database.Archive;
import com.ssd.mvd.gpstracker.database.CassandraDataControl;
import com.ssd.mvd.gpstracker.database.RedisDataControl;
import com.ssd.mvd.gpstracker.entity.Patrul;
import com.ssd.mvd.gpstracker.request.PatrulLoginRequest;
import com.ssd.mvd.gpstracker.response.ApiResponseModel;
import com.ssd.mvd.gpstracker.response.PatrulActivityStatistics;
import com.ssd.mvd.gpstracker.response.PatrulInfo;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
public class PatrulController {

//    @MessageMapping ( value = "getTaskDetails" )
//    public Mono< CardDetails > getTaskDetails ( Data data ) { return new CardDetails( data.getObject() instanceof SelfEmploymentTask ? SerDes.getSerDes().deserializeSelfEmployment( data.getObject() ) : SerDes.getSerDes().deserializeCard( data.getObject() ), data.getType() ); }

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

    @MessageMapping( value = "usersList" ) // returns the list of all created Users
    public Flux< Patrul > getUsersList () { return RedisDataControl.getRedis().getAllPatruls(); }

    @MessageMapping ( value = "patrulStatus" ) // returns all Patruls with the current status
    public Flux< Patrul > patrulStatus ( String status ) { return Archive.getAchieve().getPatrulStatus( status ); }

    @MessageMapping( value = "addUser" ) // adding new user
    public Mono< ApiResponseModel > addUser ( Patrul patrul ) { return RedisDataControl.getRedis().addValue( patrul ); }

    @MessageMapping ( value = "updatePatrul" )
    public Mono< ApiResponseModel > updatePatrul ( Patrul patrul ) { return RedisDataControl.getRedis().update( patrul ); }

    @MessageMapping ( value = "getCurrentUser" )
    public Mono< Patrul > getCurrentUser ( String passportSeries ) { return RedisDataControl.getRedis().getPatrul( passportSeries ); }

    @MessageMapping( value = "deletePatrul" )
    public Mono< ApiResponseModel > deletePatrul ( String passportNumber ) { return RedisDataControl.getRedis().deletePatrul( passportNumber ); }

    @MessageMapping ( value = "getPatrulByPortion" ) // searching Patruls by their partion name
    public Flux< PatrulInfo > getPatrulByPortion ( String name ) { return CassandraDataControl.getInstance().getPatruls( name ).map( row -> new PatrulInfo( row.getString( "NSF" ) ) ); }

    @MessageMapping ( value = "getPatrulStatistics" )
    public Mono< PatrulActivityStatistics > getPatrulStatistics ( String passportNumber ) { return RedisDataControl.getRedis().getPatrulStatistics( passportNumber ); }

    @MessageMapping ( value = "checkToken" )
    public Mono< ApiResponseModel > checkToken ( String token ) { return RedisDataControl.getRedis().checkToken( token ); }
}