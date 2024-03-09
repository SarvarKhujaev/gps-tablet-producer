package com.ssd.mvd.gpstabletsservice.controller;

import java.util.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.SerDes;
import com.ssd.mvd.gpstabletsservice.request.CardRequest;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.*;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskInspector;
import com.ssd.mvd.gpstabletsservice.inspectors.ExelInspector;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.PatrulActivityResponse;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests.PatrulLoginRequest;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests.PatrulImageRequest;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests.PatrulActivityRequest;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.messaging.handler.annotation.MessageMapping;

@RestController
public final class PatrulController extends SerDes {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MessageMapping ( value = "ping" )
    public Mono< Boolean > ping () {
        return super.convert( Boolean.TRUE );
    }

    @MessageMapping ( value = "GET_FILTERED_ACTIVE_PATRULS" )
    public Flux< Patrul > getFilteredActivePatruls ( final Map< String, String > params ) {
        final List< String > policeTypes = params.containsKey( "policeType" )
                ? Arrays.asList( params.get( "policeType" ).split( "," ) )
                : super.emptyList();

        return CassandraDataControl
                .getInstance()
                .getAllEntities
                .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
                .filter( row -> super.filterPatrul( row, params, policeTypes, 0 ) )
                .map( Patrul::new )
                .sequential()
                .publishOn( Schedulers.single() )
                .onErrorContinue( super::logging );
    }

    @MessageMapping ( value = "GET_EXEL_FILE" )
    public Mono< ApiResponseModel > GET_EXEL_FILE ( final Map< String, String > params ) {
        final List< String > policeTypes = params.containsKey( "policeType" )
                ? super.convertArrayToList( params.get( "policeType" ).split( "," ) )
                : super.emptyList();

        return CassandraDataControl
                .getInstance()
                .getAllEntities
                .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
                .filter( row -> super.filterPatrul( row, params, policeTypes, 0 ) )
                .map( Patrul::new )
                .sequential()
                .publishOn( Schedulers.single() )
                .collectList()
                .flatMap( patruls -> super.function(
                        Map.of( "message", "Exel is done",
                                "data", Data.from( new ExelInspector().download( patruls, params, policeTypes ) ) ) ) )
                .onErrorContinue( super::logging );
    }

    @MessageMapping ( value = "GET_ACTIVE_PATRULS" )
    public Mono< PatrulActivityResponse > getActivePatruls ( final Map< String, String > params ) {
        final List< String > policeTypes = params.containsKey( "policeType" )
                ? super.convertArrayToList( params.get( "policeType" ).split( "," ) )
                : super.emptyList();

        final SortedMap< Long, PatrulDivisionByRegions > regions = super.newTreeMap();

        super.analyze(
                UnirestController
                        .getInstance()
                        .getRegions
                        .apply( !params.containsKey( "regionId" ) ? -1L : Long.parseLong( params.get( "regionId" ) ) ),
                regionData -> regions.put( regionData.getId(), new PatrulDivisionByRegions( regionData ) )
        );

        return CassandraDataControl
                .getInstance()
                .getAllEntities
                .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
                .sequential()
                .publishOn( Schedulers.single() )
                .filter( row -> super.filterPatrul( row, params, policeTypes, 1 ) )
                .map( row -> regions.get( !params.containsKey( "regionId" )
                        ? row.getLong( "regionId" )
                        : row.getLong( "districtId" ) ).save( row ) )
                .collectList()
                .onErrorContinue( super::logging )
                .map( patrulDivisionByRegions -> new PatrulActivityResponse( regions ) );
    }

    @MessageMapping ( value = "ARRIVED" )
    public Mono< ApiResponseModel > arrived ( final String token, final Status status ) {
        return CassandraDataControl
            .getInstance()
            .changeStatus
            .apply( token, status )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "getTaskDetails" )
    public Mono< ApiResponseModel > getTaskDetails ( final Data data ) { return TaskInspector
            .getInstance()
            .getTaskData
            .apply( this.objectMapper.convertValue( data.getData(), new TypeReference<>() {} ), TaskTypes.CARD_DETAILS )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "LOGIN" ) // for checking login data of Patrul with his Login and password
    public Mono< ApiResponseModel > patrulLogin ( final PatrulLoginRequest patrulLoginRequest ) {
        return super.checkObject( patrulLoginRequest )
                ? CassandraDataControl
                    .getInstance()
                    .login
                    .apply( patrulLoginRequest )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.errorResponse() )
                : super.errorResponse( "Wrong Params" );
    }

    @MessageMapping( value = "getAllUsersList" ) // returns the list of all created Users
    public Flux<Patrul> getAllUsersList () {
        return CassandraDataControl
            .getInstance()
            .getAllEntities
            .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
            .map( Patrul::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging );
    }

    @MessageMapping ( value = "getPatrulByPortion" ) // searching Patruls by their partion name
    public Flux< Patrul > getPatrulByPortion ( final String name ) {
        return CassandraDataControl
            .getInstance()
            .getAllEntities
            .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
            .map( Patrul::new )
            .filter( patrul -> patrul.getPatrulFIOData().getSurnameNameFatherName().contains( name ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging );
    }

    @MessageMapping( value = "addUser" ) // adding new user
    public Mono< ApiResponseModel > addUser ( final Patrul patrul ) {
        UnirestController
                .getInstance()
                .addUser
                .accept( patrul );

        return CassandraDataControl
                .getInstance()
                .savePatrul
                .apply( patrul )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "updatePatrulImage" )
    public Mono< ApiResponseModel > updatePatrulImage ( final PatrulImageRequest request ) {
        return CassandraDataControl
                .getInstance()
                .updatePatrulImage
                .apply( request, 0 );
    }

    @MessageMapping ( value = "UPDATE_PATRUL_PHONE_NUMBER" )
    public Mono< ApiResponseModel > updatePatrulPhoneNumber ( final PatrulImageRequest request ) {
        return CassandraDataControl
                .getInstance()
                .updatePatrulImage
                .apply( request, 1 );
    }

    @MessageMapping ( value = "findTheClosestPatruls" )
    public Flux< Patrul > findTheClosestPatruls ( final Point point ) {
            return super.checkObject( point )
                    ? CassandraDataControl
                    .getInstance()
                    .findTheClosestPatruls
                    .apply( point, 1 )
                    .onErrorContinue( super::logging )
                    : Flux.empty();
    }

    @MessageMapping ( value = "checkToken" )
    public Mono< ApiResponseModel > checkToken ( final String token ) {
        return CassandraDataControl
            .getInstance()
            .checkToken
            .apply( token )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "getPatrulDataByToken" )
    public Mono< ApiResponseModel > getPatrulDataByToken ( final String token ) {
        return CassandraDataControl
            .getInstance()
            .checkToken
            .apply( token )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "updatePatrul" )
    public Mono< ApiResponseModel > updatePatrul ( final Patrul patrul ) {
        UnirestController
                .getInstance()
                .updateUser
                .accept( patrul );

        return CassandraDataControl
                .getInstance()
                .updatePatrul
                .apply( patrul )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "getCurrentUser" )
    public Mono< Patrul > getCurrentUser ( final String passportSeries ) {
        return passportSeries.length() > 30
                ? CassandraDataControl
                .getInstance()
                .getPatrulByUUID
                .apply( UUID.fromString( passportSeries ) )
                .onErrorContinue( super::logging )
                : Mono.empty();
    }

    @MessageMapping( value = "deletePatrul" )
    public Mono< ApiResponseModel > deletePatrul ( final String passportNumber ) {
        UnirestController
                .getInstance()
                .deleteUser
                .accept( passportNumber );

        return CassandraDataControl
                .getInstance()
                .deletePatrul
                .apply( UUID.fromString( passportNumber.split( "@" )[0] ) )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "getAllPatrulTasks" ) // for front end
    public Mono< ApiResponseModel > getListOfPatrulTasks ( final String uuid ) {
        return CassandraDataControl
            .getInstance()
            .getPatrulByUUID
            .apply( UUID.fromString( uuid ) )
            .flatMap( patrul -> super.isCollectionNotEmpty(
                    patrul
                        .getPatrulTaskInfo()
                        .getListOfTasks()
                        .keySet() )
                    ? TaskInspector
                            .getInstance()
                            .getListOfCompletedTasksOfPatrul(
                                    patrul,
                                    0,
                                    patrul
                                            .getPatrulTaskInfo()
                                            .getListOfTasks()
                                            .keySet().size() * 2 )
                            .onErrorContinue( super::logging )
                            .onErrorReturn( super.errorResponse() )
                    : super.errorResponse( "You have not completed any task, so try to fix this problem please" ) );
    }

    @MessageMapping ( value = "getListOfPatrulTasks" )
    public Mono< ApiResponseModel > getListOfPatrulTasks ( final Request request ) {
        return CassandraDataControl
            .getInstance()
            .getPatrulByUUID
            .apply( super.decode( request.getData() ) )
            .flatMap( patrul -> super.isCollectionNotEmpty(
                    patrul
                        .getPatrulTaskInfo()
                        .getListOfTasks()
                        .keySet() )
                    ? TaskInspector
                    .getInstance()
                    .getListOfCompletedTasksOfPatrul( patrul, (Integer) request.getObject(), (Integer) request.getSubject() )
                    : super.errorResponse( "You have not completed any task, so try to fix this problem please" ) )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.errorResponse() );
    }

    @MessageMapping ( value = "addAllPatrulsToChatService" )
    public Mono< ApiResponseModel > addAllPatrulsToChatService ( final String token ) {
        CassandraDataControl
                .getInstance()
                .addAllPatrulsToChatService
                .accept( token );

        return super.function( Map.of( "message", "Successfully added to chat service" ) );
    }

    @MessageMapping ( value = "getListOfPatrulsByUUID" )
    public Flux< Patrul > getListOfPatrulsByUUID ( final CardRequest< ? > cardRequest ) {
        return Flux.fromStream ( cardRequest.getPatruls().stream() )
                .flatMap( uuid -> CassandraDataControl
                        .getInstance()
                        .getPatrulByUUID
                        .apply( uuid ) )
                .onErrorContinue( super::logging );
    }

    @MessageMapping ( value = "getPatrulStatistics" )
    public Mono< PatrulActivityStatistics > getPatrulStatistics ( final PatrulActivityRequest request ) {
            return super.objectIsNotNull( request.getPatrulUUID() )
                    ? CassandraDataControl
                    .getInstance()
                    .getPatrulStatistics
                    .apply( request )
                    .onErrorContinue( super::logging )
                    : Mono.empty();
    }

    @MessageMapping ( value = "GET_TABLETS_USAGE_STATISTICS" )
    public Mono< ApiResponseModel > getTabletsUsageStatistics ( final PatrulActivityRequest request ) {
        return CassandraDataControl
                .getInstance()
                .getTabletUsageStatistics
                .apply( request )
                .onErrorContinue( super::logging );
    }

    // возвращает данные обо всех использованных планшетах для каждого патрульного
    @MessageMapping ( value = "getAllUsedTablets" )
    public Mono< List< TabletUsage > > getAllUsedTablets ( final PatrulActivityRequest request ) {
        return CassandraDataControl
                .getInstance()
                .getAllUsedTablets
                .apply( UUID.fromString( request.getPatrulUUID() ), request );
    }

    @MessageMapping ( value = "getPatrulInRadiusList" )
    public Mono< PatrulInRadiusList > getPatrulInRadiusList ( final Point point ) {
        return super.checkObject( point )
                ? CassandraDataControl
                .getInstance()
                .getPatrulInRadiusList
                .apply( point )
                .onErrorContinue( super::logging )
                .onErrorReturn( new PatrulInRadiusList() )
                : super.convert( new PatrulInRadiusList() );
    }
}