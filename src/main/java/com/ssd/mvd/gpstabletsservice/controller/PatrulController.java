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
    public Mono< Boolean > ping () { return super.convert( Boolean.TRUE ); }

    @MessageMapping ( value = "GET_FILTERED_ACTIVE_PATRULS" )
    public Flux< Patrul > getFilteredActivePatruls ( final Map< String, String > params ) {
        final List< String > policeTypes = params.containsKey( "policeType" )
                ? Arrays.asList( params.get( "policeType" ).split( "," ) )
                : Collections.emptyList();

        return CassandraDataControl
                .getInstance()
                .getGetAllEntities()
                .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
                .filter( row -> super.filterPatrul( row, params, policeTypes, 0 ) )
                .map( Patrul::new )
                .sequential()
                .publishOn( Schedulers.single() )
                .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "GET_EXEL_FILE" )
    public Mono< ApiResponseModel > GET_EXEL_FILE ( final Map< String, String > params ) {
        final List< String > policeTypes = params.containsKey( "policeType" )
                ? Arrays.asList( params.get( "policeType" ).split( "," ) )
                : Collections.emptyList();

        return CassandraDataControl
                .getInstance()
                .getGetAllEntities()
                .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
                .filter( row -> super.filterPatrul( row, params, policeTypes, 0 ) )
                .map( Patrul::new )
                .sequential()
                .publishOn( Schedulers.single() )
                .collectList()
                .map( patruls -> ApiResponseModel
                        .builder()
                        .success( Boolean.TRUE )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .code( 200 )
                                .message( new ExelInspector().download( patruls, params, policeTypes ) )
                                .build() )
                        .build() )
                .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "GET_ACTIVE_PATRULS" )
    public Mono< PatrulActivityResponse > getActivePatruls ( final Map< String, String > params ) {
        final List< String > policeTypes = params.containsKey( "policeType" )
                ? Arrays.asList( params.get( "policeType" ).split( "," ) )
                : Collections.emptyList();

        final SortedMap< Long, PatrulDivisionByRegions > regions = new TreeMap<>();
        UnirestController
                .getInstance()
                .getGetRegions()
                .apply( !params.containsKey( "regionId" ) ? -1L : Long.parseLong( params.get( "regionId" ) ) )
                .forEach( regionData -> regions.put( regionData.getId(), new PatrulDivisionByRegions( regionData ) ) );

        return CassandraDataControl
                .getInstance()
                .getGetAllEntities()
                .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
                .sequential()
                .publishOn( Schedulers.single() )
                .filter( row -> super.filterPatrul( row, params, policeTypes, 1 ) )
                .map( row -> regions.get( !params.containsKey( "regionId" )
                        ? row.getLong( "regionId" )
                        : row.getLong( "districtId" ) ).save( row ) )
                .collectList()
                .onErrorContinue( super::logging )
                .map( patrulDivisionByRegions -> new PatrulActivityResponse( regions ) ); }

    @MessageMapping ( value = "ARRIVED" )
    public Mono< ApiResponseModel > arrived ( final String token ) { return CassandraDataControl
            .getInstance()
            .getChangeStatus()
            .apply( token, Status.ARRIVED )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "ACCEPTED" )
    public Mono< ApiResponseModel > accepted ( final String token ) { return CassandraDataControl
            .getInstance()
            .getChangeStatus()
            .apply( token, Status.ACCEPTED )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "SET_IN_PAUSE" )
    public Mono< ApiResponseModel > setInPause ( final String token ) { return CassandraDataControl
            .getInstance()
            .getChangeStatus()
            .apply( token, Status.SET_IN_PAUSE )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "START_TO_WORK" )
    public Mono< ApiResponseModel > starToWork ( final String token ) { return CassandraDataControl
            .getInstance()
            .getChangeStatus()
            .apply( token, Status.START_TO_WORK )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "getTaskDetails" )
    public Mono< ApiResponseModel > getTaskDetails ( final Data data ) { return TaskInspector
            .getInstance()
            .getTaskData
            .apply( this.objectMapper.convertValue( data.getData(), new TypeReference<>() {} ), TaskTypes.CARD_DETAILS )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "LOGOUT" ) // used to Log out from current Account
    public Mono< ApiResponseModel > patrulLogout ( final String token ) { return CassandraDataControl
            .getInstance()
            .getChangeStatus()
            .apply( token, Status.LOGOUT )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "RETURNED_TO_WORK" )
    public Mono< ApiResponseModel > setInActive ( final String token ) { return CassandraDataControl
            .getInstance()
            .getChangeStatus()
            .apply( token, Status.RETURNED_TO_WORK )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "STOP_TO_WORK" )
    public Mono< ApiResponseModel > finishWorkOfPatrul ( final String token ) { return CassandraDataControl
            .getInstance()
            .getChangeStatus()
            .apply( token, Status.STOP_TO_WORK )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "LOGIN" ) // for checking login data of Patrul with his Login and password
    public Mono< ApiResponseModel > patrulLogin ( final PatrulLoginRequest patrulLoginRequest ) {
        return super.checkRequest.test( patrulLoginRequest, 0 )
                ? CassandraDataControl
                    .getInstance()
                    .getLogin()
                    .apply( patrulLoginRequest )
                    .onErrorContinue( super::logging )
                    .onErrorReturn( super.getErrorResponse().get() )
                : super.error.apply( 2 ); }

    @MessageMapping( value = "getAllUsersList" ) // returns the list of all created Users
    public Flux<Patrul> getAllUsersList () { return CassandraDataControl
            .getInstance()
            .getGetAllEntities()
            .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
            .map( Patrul::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "getPatrulByPortion" ) // searching Patruls by their partion name
    public Flux< Patrul > getPatrulByPortion ( final String name ) { return CassandraDataControl
            .getInstance()
            .getGetAllEntities()
            .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
            .map( Patrul::new )
            .filter( patrul -> patrul.getSurnameNameFatherName().contains( name ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .onErrorContinue( super::logging ); }

    @MessageMapping( value = "addUser" ) // adding new user
    public Mono< ApiResponseModel > addUser ( final Patrul patrul ) {
        UnirestController
                .getInstance()
                .getAddUser()
                .accept( patrul );
        patrul.setSpecialToken( null );
        return CassandraDataControl
                .getInstance()
                .getSavePatrul()
                .apply( patrul )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "updatePatrulImage" )
    public Mono< ApiResponseModel > updatePatrulImage ( final PatrulImageRequest request ) {
            return CassandraDataControl
                .getInstance()
                .getUpdatePatrulImage()
                .apply( request ); }

    @MessageMapping ( value = "findTheClosestPatruls" )
    public Flux< Patrul > findTheClosestPatruls ( final Point point ) {
            return super.checkRequest.test( point, 1 )
                    ? CassandraDataControl
                    .getInstance()
                    .getFindTheClosestPatruls()
                    .apply( point, 1 )
                    .onErrorContinue( super::logging )
                    : Flux.empty(); }

    @MessageMapping ( value = "checkToken" )
    public Mono< ApiResponseModel > checkToken ( final String token ) {
        return CassandraDataControl
            .getInstance()
            .getCheckToken()
            .apply( token )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "getPatrulDataByToken" )
    public Mono< ApiResponseModel > getPatrulDataByToken ( final String token ) { return CassandraDataControl
            .getInstance()
            .getCheckToken()
            .apply( token )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "updatePatrul" )
    public Mono< ApiResponseModel > updatePatrul ( final Patrul patrul ) {
        UnirestController
                .getInstance()
                .getUpdateUser()
                .accept( patrul );
        patrul.setSpecialToken( null );
        return CassandraDataControl
                .getInstance()
                .getUpdatePatrul()
                .apply( patrul )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "getCurrentUser" )
    public Mono< Patrul > getCurrentUser ( final String passportSeries ) {
        if ( passportSeries.length() < 30 ) return Mono.empty();
        return CassandraDataControl
                .getInstance()
                .getGetPatrulByUUID()
                .apply( UUID.fromString( passportSeries ) )
                .onErrorContinue( super::logging ); }

    @MessageMapping( value = "deletePatrul" )
    public Mono< ApiResponseModel > deletePatrul ( final String passportNumber ) {
        UnirestController
                .getInstance()
                .getDeleteUser()
                .accept( passportNumber );
        return CassandraDataControl
                .getInstance()
                .getDeletePatrul()
                .apply( UUID.fromString( passportNumber.split( "@" )[0] ) )
                .onErrorContinue( super::logging )
                .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "getAllPatrulTasks" ) // for front end
    public Mono< ApiResponseModel > getListOfPatrulTasks ( final String uuid ) { return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( UUID.fromString( uuid ) )
            .flatMap( patrul -> super.checkRequest.test( patrul.getListOfTasks().keySet(), 6 )
                    ? TaskInspector
                            .getInstance()
                            .getListOfPatrulTasks( patrul,
                                    0,
                                    patrul.getListOfTasks().keySet().size() * 2 )
                            .onErrorContinue( super::logging )
                            .onErrorReturn( super.getErrorResponse().get() )
                    : super.getFunction().apply(
                            Map.of( "message", "You have not completed any task, so try to fix this problem please",
                            "success", false,
                            "code", 200,
                            "data", Data.builder().build() ) ) ); }

    @MessageMapping ( value = "getListOfPatrulTasks" )
    public Mono< ApiResponseModel > getListOfPatrulTasks ( final Request request ) { return CassandraDataControl
            .getInstance()
            .getGetPatrulByUUID()
            .apply( CassandraDataControl
                    .getInstance()
                    .getDecode()
                    .apply( request.getData() ) )
            .flatMap( patrul -> super.checkRequest.test( patrul.getListOfTasks().keySet(), 6 )
                    ? TaskInspector
                    .getInstance()
                    .getListOfPatrulTasks(
                            patrul, (Integer) request.getObject(),
                            (Integer) request.getSubject() )
                    : super.getFunction().apply(
                            Map.of( "message", "You have not completed any task, so try to fix this problem please",
                                    "success", false,
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( Collections.emptyList() )
                                            .build(),
                                    "code", 200 ) ) )
            .onErrorContinue( super::logging )
            .onErrorReturn( super.getErrorResponse().get() ); }

    @MessageMapping ( value = "addAllPatrulsToChatService" )
    public Mono< ApiResponseModel > addAllPatrulsToChatService ( final String token ) {
        CassandraDataControl
                .getInstance()
                .getAddAllPatrulsToChatService()
                .accept( token );
        return super.getFunction().apply( Map.of( "message", "Successfully added to chat service" ) ); }

    @MessageMapping ( value = "getListOfPatrulsByUUID" )
    public Flux< Patrul > getListOfPatrulsByUUID ( final CardRequest< ? > cardRequest ) { return Flux.fromStream(
            cardRequest.getPatruls().stream() )
            .flatMap( uuid -> CassandraDataControl
                    .getInstance()
                    .getGetPatrulByUUID()
                    .apply( uuid ) )
            .onErrorContinue( super::logging ); }

    @MessageMapping ( value = "getPatrulStatistics" )
    public Mono< PatrulActivityStatistics > getPatrulStatistics ( final PatrulActivityRequest request ) {
            return super.checkParam.test( request.getPatrulUUID() )
                    ? CassandraDataControl
                    .getInstance()
                    .getGetPatrulStatistics()
                    .apply( request )
                    .onErrorContinue( super::logging )
                    : Mono.empty(); }

    // возвращает данные обо всех использованных планшетах для каждого патрульного
    @MessageMapping ( value = "getAllUsedTablets" )
    public Mono< List<TabletUsage> > getAllUsedTablets (final PatrulActivityRequest request ) { return CassandraDataControl
            .getInstance()
            .getGetAllUsedTablets()
            .apply( UUID.fromString( request.getPatrulUUID() ), request ); }

    @MessageMapping ( value = "getPatrulInRadiusList" )
    public Mono< PatrulInRadiusList > getPatrulInRadiusList ( final Point point ) {
        return super.checkRequest.test( point, 1 )
                ? CassandraDataControl
                .getInstance()
                .getGetPatrulInRadiusList()
                .apply( point )
                .onErrorContinue( super::logging )
                .onErrorReturn( new PatrulInRadiusList() )
                : super.convert( new PatrulInRadiusList() ); }

    @MessageMapping ( value = "GET_TABLETS_USAGE_STATISTICS" )
    public Mono< TabletUsageStatistics > getTabletsUsageStatistics ( final PatrulActivityRequest request ) {
        return CassandraDataControl
                .getInstance()
                .getGetTabletUsageStatistics()
                .apply( request )
                .onErrorContinue( super::logging ); }
}