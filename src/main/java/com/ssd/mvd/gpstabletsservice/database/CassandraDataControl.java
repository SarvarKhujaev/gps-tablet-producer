package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;
import com.ssd.mvd.gpstabletsservice.response.PatrulActivityStatistics;
import com.ssd.mvd.gpstabletsservice.request.PatrulActivityRequest;
import com.ssd.mvd.gpstabletsservice.request.AndroidVersionUpdate;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import com.ssd.mvd.gpstabletsservice.controller.UnirestController;
import com.ssd.mvd.gpstabletsservice.response.PatrulInRadiusList;
import com.ssd.mvd.gpstabletsservice.request.PatrulImageRequest;
import com.ssd.mvd.gpstabletsservice.request.PatrulLoginRequest;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskInspector;
import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.card.PositionInfo;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.controller.Point;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.*;

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.*;

import java.nio.charset.StandardCharsets;
import static java.lang.Math.*;

import java.util.function.*;
import java.time.Duration;
import java.util.*;

import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.Disposable;

@lombok.Data
public final class CassandraDataControl extends CassandraConverter {
    private final Cluster cluster;
    private final Session session;

    private final CodecRegistry codecRegistry = new CodecRegistry();
    private static CassandraDataControl INSTANCE = new CassandraDataControl();

    public static CassandraDataControl getInstance () { return INSTANCE != null ? INSTANCE : ( INSTANCE = new CassandraDataControl() ); }

    public void register () {
        super.registerCodecForPolygonEntity(
                CassandraTables.ESCORT.name(),
                CassandraTables.POLYGON_ENTITY.name() );

        super.registerCodecForPointsList(
                CassandraTables.ESCORT.name(),
                CassandraTables.POINTS_ENTITY.name() );

        super.registerCodecForPatrul(
                CassandraTables.TABLETS.name(),
                CassandraTables.PATRUL_TYPE.name() );

        super.registerCodecForPositionInfo(
                CassandraTables.TABLETS.name(),
                CassandraTables.POSITION_INFO.name() );

        super.registerCodecForCameraList(
                CassandraTables.TABLETS.name(),
                CassandraTables.CAMERA_LIST.name() );

        super.registerCodecForReport(
                CassandraTables.TABLETS.name(),
                CassandraTables.REPORT_FOR_CARD.name() );

        super.registerCodecForPoliceType(
                CassandraTables.TABLETS.name(),
                CassandraTables.POLICE_TYPE.name() );

        super.registerCodecForPolygonType(
                CassandraTables.TABLETS.name(),
                CassandraTables.POLYGON_TYPE.name() );

        super.registerCodecForPolygonEntity(
                CassandraTables.TABLETS.name(),
                CassandraTables.POLYGON_ENTITY.name() );

        super.registerCodecForViolationsInformation(
                CassandraTables.TABLETS.name(),
                CassandraTables.VIOLATION_LIST_TYPE.name() ); }

    private void createType ( final String typeName, final Class object ) {
        this.getSession().execute( "CREATE TYPE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + typeName +
                super.convertClassToCassandra( object ) + " );" ); }

    private void createTable ( final String tableName, final Class object, final String prefix ) {
        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "." + tableName +
                super.convertClassToCassandra( object ) + prefix ); }

    private CassandraDataControl () {
        final SocketOptions options = new SocketOptions();
        options.setConnectTimeoutMillis( 30000 );
        options.setReadTimeoutMillis( 300000 );
        options.setTcpNoDelay( true );
        options.setKeepAlive( true );
        ( this.session = ( this.cluster = Cluster
                .builder()
                .withClusterName( "GpsTablet" )
                .withPort( Integer.parseInt( GpsTabletsServiceApplication
                        .context
                        .getEnvironment()
                        .getProperty( "variables.CASSANDRA_VARIABLES.CASSANDRA_PORT" ) ) )
                .addContactPoints( "10.254.5.1, 10.254.5.2, 10.254.5.3".split( ", " ) )
            .withProtocolVersion( ProtocolVersion.V4 )
            .withCodecRegistry( this.getCodecRegistry() )
            .withRetryPolicy( DefaultRetryPolicy.INSTANCE )
            .withQueryOptions( new QueryOptions()
                    .setDefaultIdempotence( true )
                    .setConsistencyLevel( ConsistencyLevel.QUORUM ) )
            .withSocketOptions( options )
            .withLoadBalancingPolicy( new TokenAwarePolicy( DCAwareRoundRobinPolicy
                    .builder()
                    .build() ) )
            .withPoolingOptions( new PoolingOptions()
                    .setCoreConnectionsPerHost( HostDistance.REMOTE,
                            Integer.parseInt( GpsTabletsServiceApplication
                            .context
                            .getEnvironment()
                            .getProperty( "variables.CASSANDRA_VARIABLES.CASSANDRA_CORE_CONN_REMOTE" ) ) )
                    .setCoreConnectionsPerHost( HostDistance.LOCAL,
                            Integer.parseInt( GpsTabletsServiceApplication
                            .context
                            .getEnvironment()
                            .getProperty( "variables.CASSANDRA_VARIABLES.CASSANDRA_CORE_CONN_LOCAL" ) ) )
                    .setMaxConnectionsPerHost( HostDistance.REMOTE,
                            Integer.parseInt( GpsTabletsServiceApplication
                            .context
                            .getEnvironment()
                            .getProperty( "variables.CASSANDRA_VARIABLES.CASSANDRA_MAX_CONN_REMOTE" ) ) )
                    .setMaxConnectionsPerHost( HostDistance.LOCAL,
                            Integer.parseInt( GpsTabletsServiceApplication
                            .context
                            .getEnvironment()
                            .getProperty( "variables.CASSANDRA_VARIABLES.CASSANDRA_MAX_CONN_LOCAL" ) ) )
                    .setMaxRequestsPerConnection( HostDistance.REMOTE, 256 )
                    .setMaxRequestsPerConnection( HostDistance.LOCAL, 256 )
                    .setPoolTimeoutMillis( 60000 ) ).build() ).connect() )
                .execute( "CREATE KEYSPACE IF NOT EXISTS "
                        + CassandraTables.TABLETS.name() +
                        " WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy'," +
                        "'datacenter1':3 } AND DURABLE_WRITES = false;" );

        this.createType( CassandraTables.PATRUL_TYPE.name(), Patrul.class );
        this.createType( CassandraTables.POLICE_TYPE.name(), PoliceType.class );
        this.createType( CassandraTables.CAMERA_LIST.name(), CameraList.class );
        this.createType( CassandraTables.POLYGON_TYPE.name(), PolygonType.class );
        this.createType( CassandraTables.POSITION_INFO.name(), PositionInfo.class );
        this.createType( CassandraTables.POLYGON_ENTITY.name(), PolygonEntity.class );
        this.createType( CassandraTables.REPORT_FOR_CARD.name(), ReportForCard.class );
        this.createType( CassandraTables.VIOLATION_LIST_TYPE.name(), ViolationsInformation.class );

        this.createTable( CassandraTables.CARS.name(), ReqCar.class, ", PRIMARY KEY ( uuid ) );" );
        this.createTable( CassandraTables.POLICE_TYPE.name(), PoliceType.class, ", PRIMARY KEY ( uuid ) );" );
        this.createTable( CassandraTables.POLYGON_TYPE.name(), PolygonType.class, ", PRIMARY KEY ( uuid ) );" );
        this.createTable( CassandraTables.TABLETS_USAGE_TABLE.name(),
                TabletUsage.class, ", PRIMARY KEY ( uuidOfPatrul, simCardNumber ) );" );
        this.createTable( CassandraTables.PATRULS.name(), Patrul.class,
                ", status text, taskTypes text, listOfTasks map< text, text >, PRIMARY KEY ( uuid ) );" );

        this.createTable( CassandraTables.POLYGON.name(), Polygon.class,
                ", polygonType frozen< " + CassandraTables.POLYGON_TYPE.name() + " >, " +
                        "patrulList list< uuid >, " +
                        "latlngs list < frozen< " + CassandraTables.POLYGON_ENTITY.name() + " > >, " +
                "PRIMARY KEY ( uuid ) );" );

        this.createTable( CassandraTables.POLYGON_FOR_PATRUl.name(), Polygon.class,
                ", polygonType frozen< " + CassandraTables.POLYGON_TYPE.name() + " >, " +
                        "patrulList list< uuid >, " +
                        "latlngs list < frozen< " + CassandraTables.POLYGON_ENTITY.name()+ " > >, " +
                        "PRIMARY KEY ( uuid ) );" );

        this.createTable( CassandraTables.LUSTRA.name(), AtlasLustra.class,
                ", cameraLists list< frozen< "
                        + CassandraTables.CAMERA_LIST.name()
                        + " > >, PRIMARY KEY (uuid) );" );

        this.createTable ( CassandraTables.NOTIFICATION.name(), Notification.class,
                ", taskTypes text, " +
                        "status text, " +
                        "taskStatus text, " +
                        "PRIMARY KEY( uuid ) );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_LOGIN_TABLE.name()
                + " ( login text, password text, uuid uuid, PRIMARY KEY ( (login), uuid ) );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + " ( uuid uuid, " +
                "date timestamp, " +
                "status text, " +
                "message text, " +
                "totalActivityTime bigint, " +
                "PRIMARY KEY( uuid, date, status ) );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.ANDROID_VERSION_CONTROL_TABLE.name()
                + " ( id text PRIMARY KEY, version text, link text );" );

        super.logging( "Cassandra is ready" ); }

    private final Function< PatrulActivityRequest, Mono< List< PositionInfo > > > getHistory = request -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.GPSTABLETS.name() + "."
                    + CassandraTables.TABLETS_LOCATION_TABLE.name()
                    + " WHERE userId = '" + request.getPatrulUUID()
                    + "' AND date >= '" + request.getStartDate().toInstant()
                    + "' AND date <= '" + request.getEndDate().toInstant() + "';" )
            .all()
            .stream()
            .parallel() )
            .parallel( super.getCheckDifference().apply(
                    (int) Math.abs( Duration.between( request.getStartDate().toInstant(), request.getEndDate().toInstant() ).toDays() ) ) )
            .runOn( Schedulers.parallel() )
            .map( PositionInfo::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .collectList();

    private final Function< PoliceType, Mono< ApiResponseModel > > updatePoliceType = policeType -> {
            this.getGetAllEntities().apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
                    .filter( row -> row.getString( "policeType" ).compareTo( policeType.getPoliceType() ) == 0 )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .subscribe( row -> this.getSession().execute( "UPDATE "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.PATRULS.name()
                            + " SET policeType = '" + policeType.getPoliceType() + "'"
                            + " WHERE uuid = " + row.getUUID( "uuid" ) + ";" ) );
            return this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLICE_TYPE.name()
                    + " SET policeType = '" + policeType.getPoliceType() + "', "
                    + "icon = '" + policeType.getIcon() + "',"
                    + "icon2 = '" + policeType.getIcon2() + "'"
                    + " WHERE uuid = " + policeType.getUuid() + " IF EXISTS;" )
                    .wasApplied()
                    ? super.getFunction().apply( Map.of( "message", "PoliceType was updated successfully" ) )
                    : super.getFunction().apply(
                            Map.of( "message", "This PoliceType has already been applied",
                            "success", false,
                            "code", 201 ) )
                    .doOnError( this::delete ); };

    private final Function< PoliceType, Mono< ApiResponseModel > > savePoliceType = policeType -> this.getGetAllEntities()
            .apply( CassandraTables.TABLETS, CassandraTables.POLICE_TYPE )
            .filter( row -> row.getString( "PoliceType" ).compareTo( policeType.getPoliceType() ) == 0 )
            .sequential()
            .publishOn( Schedulers.single() )
            .count()
            .flatMap( aLong -> aLong == 0
                    ? this.getSession().execute( "INSERT INTO "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.POLICE_TYPE.name()
                            + super.getALlNames( PoliceType.class )
                            + " VALUES("
                            + policeType.getUuid() + ", '"
                            + policeType.getIcon() + "', '"
                            + policeType.getIcon2() + "', '"
                            + policeType.getPoliceType() + "' );" )
                            .wasApplied()
                            ? super.getFunction().apply( Map.of( "message", "PoliceType was saved successfully" ) )
                            : super.getFunction().apply(
                                    Map.of( "message", "This PoliceType has already been applied",
                                    "success", false,
                                    "code", 201 ) )
                    : super.getFunction().apply(
                            Map.of( "message", "This policeType name is already defined, choose another one",
                            "success", false,
                            "code", 201 ) ) )
            .doOnError( this::delete );

    private final BiFunction< AtlasLustra, Boolean, Mono< ApiResponseModel > > saveLustra = ( atlasLustra, check ) ->
            this.getSession().execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.LUSTRA.name()
                    + super.getALlNames( AtlasLustra.class )
                    + " VALUES("
                    + atlasLustra.getUUID() + ", '"
                    + atlasLustra.getLustraName() + "', '"
                    + atlasLustra.getCarGosNumber() + "', "
                    + super.convertListOfPointsToCassandra( atlasLustra.getCameraLists() )
                    + " )" + ( check ? " IF NOT EXISTS" : " IF EXISTS" ) + ";" )
            .wasApplied()
            ? super.getFunction().apply( Map.of( "message", "Lustra was saved successfully" ) )
            : super.getFunction().apply( Map.of( "message", "This Lustra has already been applied",
                    "success", false,
                    "code", 201 ) )
            .doOnError( this::delete );

    private final Function< PolygonType, Mono< ApiResponseModel > > savePolygonType = polygonType ->
            this.getSession().execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON_TYPE.name()
                    + super.getALlNames( PolygonType.class ) +
                    " VALUES("
                    + polygonType.getUuid() + ", '"
                    + polygonType.getName() + "') IF NOT EXISTS;" )
                    .wasApplied()
                    ? super.getFunction().apply( Map.of( "message", "PolygonType was saved successfully" ) )
                    : super.getFunction().apply(
                            Map.of( "message", "This PolygonType does not exists",
                                    "success", false,
                                "code", 201 ) )
                    .doOnError( this::delete );

    private final Function< PolygonType, Mono< ApiResponseModel > > updatePolygonType = polygonType ->
            this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON_TYPE.name()
                    + " SET name = '" + polygonType.getName() + "'"
                    + " WHERE uuid = " + polygonType.getUuid() + " IF EXISTS;" )
                    .wasApplied()
                    ? super.getFunction().apply( Map.of( "message", "PolygonType was updated successfully" ) )
                    : super.getFunction().apply( Map.of( "message", "This PolygonType does not exists",
                            "success", false,
                            "code", 201 ) )
                    .doOnError( this::delete );

    private final Function< UUID, Mono< PolygonType > > getAllPolygonTypeByUUID = uuid -> Mono.just(
            new PolygonType( this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON_TYPE.name()
                    + " WHERE uuid = " + uuid + ";" ).one() ) )
            .doOnError( this::delete );

    private final Function< Polygon, Mono< ApiResponseModel > > updatePolygon = polygon -> this.getSession().execute(
            "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON.name() +
                    super.getALlNames( Polygon.class ) +
                    " VALUES ("
                    + polygon.getUuid() + ", "
                    + polygon.getOrgan() + ", "

                    + polygon.getRegionId() + ", "
                    + polygon.getMahallaId() + ", "
                    + polygon.getDistrictId() + ", '"

                    + polygon.getName() + "', '"
                    + polygon.getColor() + "', " +

                    super.convertClassToCassandraTable ( polygon.getPolygonType() ) + ", " +

                    super.convertListToCassandra( polygon.getPatrulList() ) + ", " +

                    super.convertListOfPointsToCassandra( polygon.getLatlngs() ) + ");" )
            .wasApplied()
            ? super.getFunction().apply( Map.of( "message", "Polygon was saved successfully" ) )
            : super.getFunction().apply( Map.of( "message", "This polygon does not exists" ) )
            .doOnError( this::delete );

    private final Function< Polygon, Mono< ApiResponseModel > > savePolygon = polygon -> this.getSession().execute(
            "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON.name()
                    + super.getALlNames( Polygon.class ) +
                    " VALUES ("
                    + polygon.getUuid() + ", "
                    + polygon.getOrgan() + ", "

                    + polygon.getRegionId() + ", "
                    + polygon.getMahallaId() + ", "
                    + polygon.getDistrictId() + ", '"

                    + polygon.getName() + "', '"
                    + polygon.getColor() + "', " +

                    super.convertClassToCassandraTable ( polygon.getPolygonType() ) + ", " +

                    super.convertListToCassandra( polygon.getPatrulList() ) + ", " +

                    super.convertListOfPointsToCassandra( polygon.getLatlngs() ) + ") IF NOT EXISTS;" )

            .wasApplied()
            ? super.getFunction().apply( Map.of( "message", "Polygon was successfully saved" ) )
            : super.getFunction().apply(
                    Map.of( "message", "This polygon has already been saved",
                    "success", false,
                    "code", 201 ) )
            .doOnError( this::delete );

    private final Function< UUID, Mono< Polygon > > getPolygonByUUID = uuid -> {
            final Row row = this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON.name()
                    + " where uuid = " + uuid ).one();
            return Mono.justOrEmpty( super.getCheckParam().test( row ) ? new Polygon( row ) : null ); };

    private final Function< UUID, Mono< ReqCar > > getCarByUUID = uuid -> Mono.just(
            new ReqCar( this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.CARS.name()
                    + " WHERE uuid = " + uuid + ";" ).one() ) );

    private final Function< String, Mono< ApiResponseModel > > deleteCar = gosno ->
            this.getGetCarByUUID().apply( UUID.fromString( gosno ) )
                    .flatMap( reqCar -> {
                        if ( reqCar.getPatrulPassportSeries() == null
                                && reqCar.getPatrulPassportSeries().equals( "null" ) ) {
                            this.getSession().execute( "DELETE FROM "
                                    + CassandraTables.TRACKERS + "."
                                    + CassandraTables.TRACKERSID
                                    + " where trackersId = '"
                                    + reqCar.getTrackerId() + "';" );
                            return this.delete( CassandraTables.CARS.name(), "uuid", gosno ); }
                        else return super.getFunction().apply(
                                Map.of( "message", "This car is linked to patrul",
                                "success", false,
                                "code", 201 ) ); } )
                    .doOnError( this::delete );

    private final Function< ReqCar, Mono< ApiResponseModel > > updateCar = reqCar ->
            this.getGetCarByUUID().apply( reqCar.getUuid() )
                .flatMap( reqCar1 -> {
                if ( !reqCar.getTrackerId().equals( reqCar1.getTrackerId() )
                        && !super.getCheckTracker().test( reqCar.getTrackerId() ) ) return super.getFunction().apply(
                                Map.of( "message", "Wrong TrackerId",
                                        "success", false,
                                        "code", 201 ) );
                if ( !reqCar.getPatrulPassportSeries().equals( reqCar1.getPatrulPassportSeries() ) ) {
                    this.getSession().execute ( "UPDATE "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.PATRULS.name()
                            + " SET carnumber = '" + reqCar.getGosNumber() + "', "
                            + "cartype = '" + reqCar.getVehicleType()
                            + "' where uuid = " + this.getGetPatrulByPassportNumber().apply( reqCar1.getPatrulPassportSeries() )
                            .getUUID( "uuid" ) + ";" );

                    this.getSession().execute ( "UPDATE "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.PATRULS.name()
                            + " SET carnumber = '" + null + "', "
                            + "cartype = '" + null + "' where uuid = "
                            + this.getGetPatrulByPassportNumber()
                            .apply( reqCar1.getPatrulPassportSeries() )
                            .getUUID( "uuid" ) + ";" ); }
                return this.getSession().execute( "INSERT INTO "
                                + CassandraTables.TABLETS.name() + "."
                                + CassandraTables.CARS.name()
                                + super.getALlNames( ReqCar.class )
                                + " VALUES ("
                                + reqCar.getUuid() + ", "
                                + reqCar.getLustraId() + ", '"

                                + reqCar.getGosNumber() + "', '"
                                + reqCar.getTrackerId() + "', '"
                                + reqCar.getVehicleType() + "', '"
                                + reqCar.getCarImageLink() + "', '"
                                + reqCar.getPatrulPassportSeries() + "', "

                                + reqCar.getSideNumber() + ", "
                                + reqCar.getSimCardNumber() + ", "

                                + reqCar.getLatitude() + ", "
                                + reqCar.getLongitude() + ", "
                                + reqCar.getAverageFuelSize() + ", "
                                + reqCar.getAverageFuelConsumption() + ");" )
                        .wasApplied()
                        ? super.getFunction().apply( Map.of( "message", "Car was successfully saved" ) )
                        : super.getFunction().apply(
                                Map.of( "message", "This car does not exist, choose another one",
                                "success", false,
                                "code", 201 ) ); } );

    private final Function< ReqCar, Boolean > linkPatrulWithCar = reqCar -> {
            final Row row = this.getGetPatrulByPassportNumber().apply( reqCar.getPatrulPassportSeries() );
            return super.getCheckParam().test( row ) && this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.PATRULS
                    + " SET carNumber = '" + reqCar.getGosNumber() + "',"
                    + " carType = '" + reqCar.getVehicleType() + "',"
                    + " uuidForPatrulCar = " + reqCar.getUuid()
                    + " WHERE uuid = " + row.getUUID( "uuid" ) + ";" )
                    .wasApplied(); };

    private final Function< ReqCar, Mono< ApiResponseModel > > saveCar = reqCar ->
            super.getCheckTracker().test( reqCar.getTrackerId() )
            && super.getCheckCarNumber().test( reqCar.getGosNumber() )
            ? this.getSession().execute( "INSERT INTO "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.CARS.name()
                            + super.getALlNames( ReqCar.class )
                            + " VALUES ("
                            + reqCar.getUuid() + ", "
                            + reqCar.getLustraId() + ", '"

                            + reqCar.getGosNumber() + "', '"
                            + reqCar.getTrackerId() + "', '"
                            + reqCar.getVehicleType() + "', '"
                            + reqCar.getCarImageLink() + "', '"
                            + reqCar.getPatrulPassportSeries() + "', "

                            + reqCar.getSideNumber() + ", "
                            + reqCar.getSimCardNumber() + ", "

                            + reqCar.getLatitude() + ", "
                            + reqCar.getLongitude() + ", "
                            + reqCar.getAverageFuelSize() + ", "
                            + reqCar.getAverageFuelConsumption()
                            + ") IF NOT EXISTS;" )
                    .wasApplied()
                    ? super.getFunction().apply(
                            Map.of( "message", "Car was successfully saved",
                                    "success", this.getLinkPatrulWithCar().apply( reqCar ) ) )
                    : super.getFunction().apply(
                            Map.of( "message", "This car was already saved, choose another one",
                            "success", false,
                            "code", 201 ) )
                    : super.getFunction().apply(
                            Map.of( "message", "This trackers or gosnumber is already registered to another car, so choose another one",
                            "success", false,
                            "code", 201 ) )
                    .doOnError( this::delete );

    private final Function< UUID, Mono< Patrul > > getPatrulByUUID = uuid -> {
            final Row row = this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRULS.name()
                    + " WHERE uuid = " + uuid + ";" ).one();
            return Mono.justOrEmpty( super.getCheckParam().test( row ) ? new Patrul( row ) : null ); };

    private final Function< String, Row > getPatrulByPassportNumber = passportNumber ->
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRULS.name()
                    + " WHERE passportNumber = '" + passportNumber + "';" ).one();

    // обновляет время последней активности патрульного
    private final Consumer< Patrul > updatePatrulAfterTask = patrul ->
            this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRULS.name()
                    + " SET status = '" + patrul.getStatus() + "', "
                    + " taskTypes = '" + patrul.getTaskTypes() + "', "
                    + " taskId = '" + patrul.getTaskId() + "', "
                    + " uuidOfEscort = " + patrul.getUuidOfEscort() + ", "
                    + " uuidForEscortCar = " + patrul.getUuidForEscortCar() + ", "
                    + " longitudeOfTask = " + patrul.getLongitudeOfTask() + ", "
                    + " latitudeOfTask = " + patrul.getLatitudeOfTask() + ", "
                    + " taskDate = '" + ( super.getCheckParam().test( patrul.getTaskDate() )
                    ? patrul.getTaskDate().toInstant()
                    : TimeInspector
                    .getInspector()
                    .getGetNewDate()
                    .get().toInstant() ) + "', "
                    + "listOfTasks = " + super.convertMapToCassandra( patrul.getListOfTasks() )
                    + " WHERE uuid = " + patrul.getUuid() + " IF EXISTS;" );

    // обновляет время последней активности патрульного
    private final Consumer< Patrul > updatePatrulActivity = patrul ->
            this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRULS.name()
                    + " SET lastActiveDate = '" + TimeInspector
                    .getInspector()
                    .getGetNewDate()
                    .get().toInstant() + "'"
                    + " WHERE uuid = " + patrul.getUuid() + ";" );

    private final Function< Patrul, Mono< ApiResponseModel > > updatePatrul = patrul -> {
        final Row row = this.getGetPatrulByPassportNumber().apply( patrul.getPassportNumber() );
        if ( row == null ) return super.getFunction().apply(
                Map.of( "message", "There is no such a patrul",
                        "success", false,
                        "code", 201 ) );

        if ( row.getUUID( "uuid" ).compareTo( patrul.getUuid() ) == 0 ) {
            if ( patrul.getLogin() == null ) patrul.setLogin( patrul.getPassportNumber() );
            if ( patrul.getName().contains( "'" ) ) patrul.setName( patrul.getName().replaceAll( "'", "" ) );
            if ( patrul.getSurname().contains( "'" ) ) patrul.setSurname( patrul.getSurname().replaceAll( "'", "" ) );
            if ( patrul.getOrganName().contains( "'" ) ) patrul.setOrganName( patrul.getOrganName().replaceAll( "'", "" ) );
            if ( patrul.getFatherName().contains( "'" ) ) patrul.setFatherName( patrul.getFatherName().replaceAll( "'", "" ) );
            if ( patrul.getRegionName().contains( "'" ) ) patrul.setRegionName( patrul.getRegionName().replaceAll( "'", "" ) );

            if ( row.getString( "login" ).compareTo( patrul.getLogin() ) == 0
                    && row.getString( "password" ).compareTo( patrul.getPassword() ) != 0 )
                this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRULS_LOGIN_TABLE.name()
                    + " SET password = '" + patrul.getPassword()
                    + "' WHERE login = '" + patrul.getLogin()
                    + "' AND uuid = " + patrul.getUuid() + ";" );

            return this.getSession().execute( "INSERT INTO "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.PATRULS.name() +
                            super.getALlNames( Patrul.class ) + " VALUES ('" +
                            ( patrul.getTaskDate() != null ? patrul.getTaskDate().toInstant() : TimeInspector
                                    .getInspector()
                                    .getGetNewDate()
                                    .get().toInstant() ) + "', '" +
                            ( patrul.getLastActiveDate() != null ? patrul.getLastActiveDate().toInstant() : TimeInspector
                                    .getInspector()
                                    .getGetNewDate()
                                    .get().toInstant() ) + "', '" +
                            ( patrul.getStartedToWorkDate() != null ? patrul.getStartedToWorkDate().toInstant() : TimeInspector
                                    .getInspector()
                                    .getGetNewDate()
                                    .get().toInstant() ) + "', '" +
                            ( patrul.getDateOfRegistration() != null ? patrul.getDateOfRegistration().toInstant() : TimeInspector
                                    .getInspector()
                                    .getGetNewDate()
                                    .get().toInstant() ) + "', " +

                            patrul.getDistance() + ", " +
                            patrul.getLatitude() + ", " +
                            patrul.getLongitude() + ", " +
                            patrul.getLatitudeOfTask() + ", " +
                            patrul.getLongitudeOfTask() + ", " +

                            patrul.getUuid() + ", " +
                            patrul.getOrgan() + ", " +
                            patrul.getSos_id() + ", " +
                            patrul.getUuidOfEscort() + ", " +
                            patrul.getUuidForPatrulCar() + ", " +
                            patrul.getUuidForEscortCar() + ", " +

                            patrul.getRegionId() + ", " +
                            patrul.getMahallaId() + ", " +
                            patrul.getDistrictId() + ", " +
                            patrul.getTotalActivityTime() + ", " +

                            ( patrul.getBatteryLevel() != null ? patrul.getBatteryLevel() : 0 ) + ", " +
                            patrul.getInPolygon() + ", " +
                            patrul.getTuplePermission() + ", '" +

                            patrul.getName() + "', '" +
                            patrul.getRank() + "', '" +
                            patrul.getEmail() + "', '" +
                            patrul.getLogin() + "', '" +
                            patrul.getTaskId() + "', '" +
                            patrul.getCarType() + "', '" +
                            patrul.getSurname() + "', '" +
                            patrul.getPassword() + "', '" +
                            patrul.getCarNumber() + "', '" +
                            patrul.getOrganName() + "', '" +
                            patrul.getRegionName() + "', '" +
                            patrul.getPoliceType() + "', '" +
                            patrul.getFatherName() + "', '" +
                            patrul.getDateOfBirth() + "', '" +
                            patrul.getPhoneNumber() + "', '" +
                            patrul.getSpecialToken() + "', '" +
                            patrul.getTokenForLogin() + "', '" +
                            patrul.getSimCardNumber() + "', '" +
                            patrul.getPassportNumber() + "', '" +
                            patrul.getPatrulImageLink() + "', '" +
                            patrul.getSurnameNameFatherName() + "', '" +
                            patrul.getStatus() + "', '" +
                            patrul.getTaskTypes() + "', " +
                            super.convertMapToCassandra( patrul.getListOfTasks() ) + " );" )
                    .wasApplied()
                    ? super.getFunction().apply( Map.of( "message", "Patrul was successfully updated" ) )
                    : super.getFunction().apply(
                            Map.of( "message", "There is no such a patrul",
                            "success", false,
                            "code", 201 ) ); }
        else return super.getFunction().apply(
                Map.of( "message", "There is no such a patrul",
                        "success", false,
                        "code", 201 ) ); };

    public void update ( UUID uuidOfEscort, UUID uuidForEscortCar, UUID patrulUUID ) {
            this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRULS.name()
                    + " SET uuidForEscortCar " + uuidForEscortCar
                    + ", uuidOfEscort = " + uuidOfEscort
                    + " WHERE uuid = " + patrulUUID + ";" ); }

    // обновляет фото патрульного
    private final Function< PatrulImageRequest, Mono< ApiResponseModel > > updatePatrulImage = request ->
            this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRULS.name()
                    + " SET patrulImageLink = '" + request.getNewImage() + "'"
                    + " WHERE uuid = " + request.getPatrulUUID() + " IF EXISTS;" )
                    .wasApplied()
                    ? super.getFunction().apply( Map.of( "message", "Image was updated successfully" ) )
                    : Mono.just( super.getErrorResponse().get() );

    private final Function< UUID, Mono< ApiResponseModel > > deletePatrul = uuid -> this.getGetPatrulByUUID().apply( uuid )
            .flatMap( patrul -> {
                if ( super.getCheckRequest().test( patrul, 3 ) ) {
                    this.getSession().execute ( "DELETE FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.PATRULS_LOGIN_TABLE.name()
                            + " WHERE login = '" + patrul.getLogin()
                            + "' AND uuid = " + patrul.getUuid() + ";" );

                    return this.delete( CassandraTables.PATRULS.name(), "uuid", patrul.getUuid().toString() ); }

                else return super.getFunction().apply(
                        Map.of( "message", "You cannot delete this patrul",
                                "success", false,
                                "code", 201 ) ); } )
            .doOnError( this::delete );

    private final Function< Patrul, Mono< ApiResponseModel > > savePatrul = patrul -> {
            if ( !super.getCheckParam().test( this.getGetPatrulByPassportNumber().apply( patrul.getPassportNumber() ) ) ) {
                patrul.setStatus( FREE );
                patrul.setInPolygon( false );
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setListOfTasks( new HashMap<>() );
                if ( patrul.getBatteryLevel() == null ) patrul.setBatteryLevel( 0 );
                if ( patrul.getLogin() == null ) patrul.setLogin( patrul.getPassportNumber() );
                if ( patrul.getPassword() == null ) patrul.setPassword( patrul.getPassportNumber() );
                if ( patrul.getName().contains( "'" ) ) patrul.setName( patrul.getName().replaceAll( "'", "" ) );
                if ( patrul.getSurname().contains( "'" ) ) patrul.setSurname( patrul.getSurname().replaceAll( "'", "" ) );
                if ( patrul.getOrganName() != null && patrul.getOrganName().contains( "'" ) )
                    patrul.setOrganName( patrul.getOrganName().replaceAll( "'", "" ) );
                if ( patrul.getFatherName().contains( "'" ) ) patrul.setFatherName( patrul.getFatherName().replaceAll( "'", "" ) );
                if ( patrul.getRegionName().contains( "'" ) ) patrul.setRegionName( patrul.getRegionName().replaceAll( "'", "" ) );
                if ( this.getCheckLogin().apply( patrul.getLogin() ) != null ) return super.getFunction().apply(
                        Map.of( "message", "Patrul with this login has already been inserted, choose another one",
                                "success", false,
                                "code", 201 ) );

                this.getSession().execute( "INSERT INTO "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.PATRULS_LOGIN_TABLE.name()
                        + " ( login, password, uuid ) VALUES( '"
                        + patrul.getLogin() + "', '"
                        + patrul.getPassword() + "', "
                        + patrul.getUuid() + " ) IF NOT EXISTS;" );

                CassandraDataControlForTasks
                        .getInstance()
                        .getCreateRowInPatrulSosListTable()
                        .accept( patrul.getUuid() );

                return this.getSession().execute( "INSERT INTO "
                                + CassandraTables.TABLETS.name() + "."
                                + CassandraTables.PATRULS.name() +
                                super.getALlNames( Patrul.class )
                                + " VALUES ('" +
                                ( super.getCheckParam().test( patrul.getTaskDate() )
                                        ? patrul.getTaskDate().toInstant()
                                        : TimeInspector
                                        .getInspector()
                                        .getGetNewDate()
                                        .get().toInstant() ) + "', '" +
                                ( super.getCheckParam().test( patrul.getLastActiveDate() )
                                        ? patrul.getLastActiveDate().toInstant()
                                        : TimeInspector
                                        .getInspector()
                                        .getGetNewDate()
                                        .get().toInstant() ) + "', '" +
                                ( super.getCheckParam().test( patrul.getStartedToWorkDate() )
                                        ? patrul.getStartedToWorkDate().toInstant()
                                        : TimeInspector
                                        .getInspector()
                                        .getGetNewDate()
                                        .get().toInstant() ) + "', '" +
                                ( super.getCheckParam().test( patrul.getDateOfRegistration() )
                                        ? patrul.getDateOfRegistration().toInstant()
                                        : TimeInspector
                                        .getInspector()
                                        .getGetNewDate()
                                        .get().toInstant() ) + "', " +

                                patrul.getDistance() + ", " +
                                patrul.getLatitude() + ", " +
                                patrul.getLongitude() + ", " +
                                patrul.getLatitudeOfTask() + ", " +
                                patrul.getLongitudeOfTask() + ", " +

                                patrul.getUuid() + ", " +
                                patrul.getOrgan() + ", " +
                                patrul.getSos_id() + ", " +
                                patrul.getUuidOfEscort() + ", " +
                                patrul.getUuidForPatrulCar() + ", " +
                                patrul.getUuidForEscortCar() + ", " +

                                patrul.getRegionId() + ", " +
                                patrul.getMahallaId() + ", " +
                                patrul.getDistrictId() + ", " +
                                ( super.getCheckParam().test( patrul.getTotalActivityTime() )
                                        ? patrul.getTotalActivityTime() : 0 ) + ", " +

                                ( super.getCheckParam().test( patrul.getBatteryLevel() )
                                        ? patrul.getBatteryLevel() : 0 ) + ", " +
                                ( super.getCheckParam().test( patrul.getInPolygon() )
                                        ? patrul.getInPolygon() : false ) + ", " +
                                ( super.getCheckParam().test( patrul.getTuplePermission() )
                                        ? patrul.getTuplePermission() : false ) + ", '" +

                                patrul.getName() + "', '" +
                                patrul.getRank() + "', '" +
                                patrul.getEmail() + "', '" +
                                patrul.getLogin() + "', '" +
                                patrul.getTaskId() + "', '" +
                                patrul.getCarType() + "', '" +
                                patrul.getSurname() + "', '" +
                                patrul.getPassword() + "', '" +
                                patrul.getCarNumber() + "', '" +
                                patrul.getOrganName() + "', '" +
                                patrul.getRegionName() + "', '" +
                                patrul.getPoliceType() + "', '" +
                                patrul.getFatherName() + "', '" +
                                patrul.getDateOfBirth() + "', '" +
                                patrul.getPhoneNumber() + "', '" +
                                patrul.getSpecialToken() + "', '" +
                                patrul.getTokenForLogin() + "', '" +
                                patrul.getSimCardNumber() + "', '" +
                                patrul.getPassportNumber() + "', '" +
                                patrul.getPatrulImageLink() + "', '" +
                                patrul.getSurnameNameFatherName() + "', '" +
                                patrul.getStatus() + "', '" +
                                patrul.getTaskTypes() + "', " +
                                super.convertMapToCassandra( patrul.getListOfTasks() ) + " ) IF NOT EXISTS;" )
                        .wasApplied()
                        ? super.getFunction().apply( Map.of( "message", "Patrul was successfully saved" ) )
                        : super.getFunction().apply(
                                Map.of( "message", "Patrul has already been saved, choose another one",
                                "success", false,
                                "code", 201 ) ); }
            else return super.getFunction().apply(
                    Map.of( "message", "This patrul is already exists",
                            "success", false,
                            "code", 201 ) ); };

    private final Function< String, Mono< Polygon > > getPolygonForPatrul = id -> {
            final Row row = this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON_FOR_PATRUl.name()
                    + " where uuid = " + UUID.fromString( id ) ).one();
            return Mono.just( super.getCheckParam().test( row ) ? new Polygon( row ) : null ); };

    private final Function< String, Mono< ApiResponseModel > > deletePolygonForPatrul = id -> this.getGetPolygonForPatrul()
            .apply( id )
            .map( polygon1 -> {
                polygon1.getPatrulList()
                        .parallelStream()
                        .forEach( uuid -> this.getSession().executeAsync( "UPDATE " +
                                CassandraTables.TABLETS.name() + "."
                                + CassandraTables.PATRULS.name() +
                                " SET inPolygon = " + false
                                + " where uuid = " + uuid + ";" ) );
                return polygon1; } )
            .flatMap( polygon1 -> {
                this.getSession().execute( "DELETE FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.POLYGON_FOR_PATRUl.name()
                        + " where uuid = " + UUID.fromString( id ) + ";" );
                return super.getFunction().apply( Map.of( "message", "Polygon " + id + " successfully deleted" ) ); } );

    private final Function< Polygon, Mono< ApiResponseModel > > addPolygonForPatrul = polygon -> this.getSession().execute(
            "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON_FOR_PATRUl.name() +
                    super.getALlNames( Polygon.class ) +
                    " VALUES ("
                    + polygon.getUuid() + ", "
                    + polygon.getOrgan() + ", "

                    + polygon.getRegionId() + ", "
                    + polygon.getMahallaId() + ", "
                    + polygon.getDistrictId() + ", '"

                    + polygon.getName() + "', '"
                    + ( polygon.getColor() == null ? "Qizil" : polygon.getColor() ) + "', " +

                    super.convertClassToCassandraTable ( polygon.getPolygonType() ) + ", " +

                    super.convertListToCassandra( polygon.getPatrulList() ) + ", " +

                    super.convertListOfPointsToCassandra( polygon.getLatlngs() ) + ") IF NOT EXISTS;" )
            .wasApplied()
            ? super.getFunction().apply( Map.of( "message", "Polygon: " + polygon.getUuid() + " was saved successfully" ) )
            : super.getFunction().apply(
                    Map.of( "message", "This polygon has already been created",
                            "success", false,
                            "code", 201 ) )
            .doOnError( this::delete );

    private final Function< Polygon, Mono< ApiResponseModel > > updatePolygonForPatrul = polygon -> this.getGetPolygonForPatrul()
            .apply( polygon.getUuid().toString() )
            .map( polygon1 -> {
                polygon.getPatrulList().addAll( polygon1.getPatrulList() );
                polygon
                        .getPatrulList()
                        .parallelStream()
                        .forEach( uuid -> this.getSession().executeAsync(
                                "UPDATE " + CassandraTables.TABLETS.name() + "."
                                        + CassandraTables.PATRULS.name() +
                                        " SET inPolygon = " + true
                                        + " WHERE uuid = " + uuid + ";" ) );
                return polygon; } )
            .flatMap( polygon1 -> this.getSession().execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON_FOR_PATRUl.name() +
                    super.getALlNames( Polygon.class ) +
                    " VALUES ("
                    + polygon.getUuid() + ", "
                    + polygon.getOrgan() + ", "

                    + polygon.getRegionId() + ", "
                    + polygon.getMahallaId() + ", "
                    + polygon.getDistrictId() + ", '"

                    + polygon.getName() + "', '"
                    + polygon.getColor() + "', " +

                    super.convertClassToCassandraTable ( polygon.getPolygonType() ) + ", " +

                    super.convertListToCassandra( polygon.getPatrulList() ) + ", " +

                    super.convertListOfPointsToCassandra( polygon.getLatlngs() ) + " );" )
                    .wasApplied()
                    ? super.getFunction().apply( Map.of( "message", "Polygon: " + polygon.getUuid() + " was updated successfully" ) )
                    : super.getFunction().apply(
                            Map.of( "message", "This polygon has already been created",
                            "success", false,
                            "code", 201 ) )
                    .doOnError( this::delete ) );

    private final Function< PatrulActivityRequest, Mono< PatrulActivityStatistics > > getPatrulStatistics = request ->
            this.getGetPatrulByUUID().apply( UUID.fromString( request.getPatrulUUID() ) )
                    .flatMap( patrul -> Flux.fromStream(
                            this.getSession().execute( "SELECT * FROM "
                                    + CassandraTables.TABLETS.name() + "."
                                    + CassandraTables.PATRULS_STATUS_TABLE.name()
                                    + " WHERE uuid = " + patrul.getUuid()
                                    + ( super.getCheckParam().test( request.getEndDate() )
                                    && super.getCheckParam().test( request.getStartDate() )
                                    ? " AND date >= '" + request.getStartDate().toInstant()
                                    + "' AND date <= '" + request.getEndDate().toInstant() + "'" : "" ) + ";" )
                                    .all()
                                    .stream()
                                    .parallel() )
                            .parallel( super.getCheckDifference().apply(
                                    (int) Math.abs( Duration.between( request.getStartDate().toInstant(), request.getEndDate().toInstant() ).toDays() ) ) )
                            .runOn( Schedulers.parallel() )
                            .filter( row -> super.getCheckEquality().test( Status.valueOf( row.getString( "status" ) ), LOGOUT ) )
                            .map( row -> row.getLong( "totalActivityTime" ) )
                            .sequential()
                            .publishOn( Schedulers.single() )
                            .collectList()
                            .map( longs -> PatrulActivityStatistics
                                    .builder()
                                    .dateList( longs )
                                    .patrul( patrul )
                                    .build() ) );

    private final Function< ScheduleForPolygonPatrul, Mono< ApiResponseModel > > addPatrulToPolygon =
            scheduleForPolygonPatrul -> this.getGetPolygonForPatrul().apply( scheduleForPolygonPatrul.getUuid() )
                    .flatMap( polygon -> Flux.fromStream( scheduleForPolygonPatrul
                                    .getPatrulUUIDs()
                                    .stream() )
                            .parallel( super.getCheckDifference().apply( scheduleForPolygonPatrul.getPatrulUUIDs().size() ) )
                            .runOn( Schedulers.parallel() )
                            .flatMap( uuid -> this.getGetPatrulByUUID().apply( uuid ) )
                            .map( patrul -> {
                                this.getSession().executeAsync( "UPDATE "
                                        + CassandraTables.TABLETS.name() + "."
                                        + CassandraTables.PATRULS.name() +
                                        " SET inPolygon = " + true + " where uuid = " + patrul.getUuid() + ";" );
                                return patrul.getUuid(); } )
                            .sequential()
                            .publishOn( Schedulers.single() )
                            .collectList()
                            .flatMap( uuidList -> {
                                polygon.setPatrulList( uuidList );
                                return this.getUpdatePolygonForPatrul().apply( polygon ); } ) );

    private final Supplier< Flux< Notification > > getUnreadNotifications = () -> Flux.fromStream (
            this.getSession().execute ( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.NOTIFICATION.name()
                            + " WHERE wasread = false;" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .map( Notification::new )
            .sequential()
            .publishOn( Schedulers.single() );

    private final Supplier< Mono< Long > > getUnreadNotificationQuantity = () -> Mono.just(
            this.getSession().execute ( "SELECT count(*) as quantity FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.NOTIFICATION.name()
                    + " WHERE wasread = false;" )
                    .one()
                    .getLong( "quantity" ) );

    private final Function< Notification, Notification > saveNotification = notification -> {
            this.getSession().execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.NOTIFICATION.name() +
                    super.getALlNames( Notification.class )
                    + " VALUES ('"
                    + notification.getId() + "', '"
                    + notification.getType() + "', '"
                    + notification.getTitle() + "', '"
                    + notification.getAddress() + "', '"
                    + notification.getCarNumber() + "', '"
                    + notification.getPoliceType() + "', '"
                    + notification.getNsfOfPatrul() + "', '"
                    + notification.getPassportSeries() + "', "

                    + notification.getLongitudeOfTask() + ", "
                    + notification.getLongitudeOfTask() + ", "

                    + notification.getUuid() + ", '"
                    + notification.getStatus() + "', '"
                    + notification.getTaskStatus() + "', "

                    + false + ", '"
                    + notification.getTaskTypes() + "', '"
                    + notification.getNotificationWasCreated().toInstant() + "');" );
            return notification; };

    private final Function< UUID, Mono< ApiResponseModel > > setNotificationAsRead = uuid ->
            this.getSession().execute( "UPDATE "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.NOTIFICATION.name()
                        + " SET wasRead = " + true
                        + " WHERE uuid = " + uuid + ";" )
                .wasApplied()
                ? super.getFunction().apply( Map.of( "message", "Notification " + uuid + " was updated successfully" ) )
                : super.getFunction().apply(
                        Map.of( "message", "Notification " + uuid + " was not updated",
                        "success", false,
                        "code", 201 ) );

    public Mono< ApiResponseModel > delete ( final String table,
                                             final String param,
                                             final String id ) {
            this.getSession().execute ( "DELETE FROM "
                    + CassandraTables.TABLETS.name() + "." + table
                    + " WHERE " + param + " = " + UUID.fromString( id ) + ";" );
            return super.getFunction().apply( Map.of( "message", "Deleting has been finished successfully" ) ); }

    private final BiFunction< Point, Integer, Flux< Patrul > > findTheClosestPatruls = ( point, integer ) ->
            this.getGetAllEntities().apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
                    .filter( row -> super.getCheckPatrulStatus().test( row )
                            && ( integer != 1 || Status.valueOf( row.getString( "status" ) ).compareTo( Status.FREE ) == 0
                            && TaskTypes.valueOf( row.getString( "taskTypes" ) ).compareTo( TaskTypes.FREE ) == 0 ) )
                    .map( Patrul::new )
                    .map( patrul -> {
                        patrul.setDistance( super.getCalculate().apply( point, patrul ) );
                        return patrul; } )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .sort( Comparator.comparing( Patrul::getDistance ) );

    private final BiFunction< Point, UUID, Flux< Patrul > > findTheClosestPatrulsForSos = ( point, uuid ) ->
            this.getGetAllEntities()
                    .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
                    .filter( row -> super.getCheckPatrulStatus().test( row )
                            && row.getUUID( "uuid" ).compareTo( uuid ) != 0
                            && row.getUUID( "uuidOfEscort" ) == null )
                    .map( Patrul::new )
                    .map( patrul -> {
                        patrul.setDistance( super.getCalculate().apply( point, patrul ) );
                        return patrul; } )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .sort( Comparator.comparing( Patrul::getDistance ) )
                    .take( 20 );

    private final BiFunction< Patrul, Status, Boolean > updatePatrulStatus = ( patrul, status ) -> switch ( status ) {
        // in case when Patrul wants to leave his account
        case LOGOUT -> this.getSession().executeAsync( "INSERT INTO "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.PATRULS_STATUS_TABLE.name()
                        + "(uuid, date, status, message, totalActivityTime) VALUES("
                        + patrul.getUuid() + ", '"
                        + TimeInspector
                        .getInspector()
                        .getGetNewDate()
                        .get().toInstant() + "', '"
                        + status + "', 'log out at: "
                        + TimeInspector
                        .getInspector()
                        .getGetNewDate()
                        .get().toInstant() + "', "
                        + patrul.getTotalActivityTime() + ");" )
                .isDone();

        case ACCEPTED -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + " ( uuid, date, status, message, totalActivityTime ) VALUES("
                + patrul.getUuid() + ", '"
                + TimeInspector
                .getInspector()
                .getGetNewDate()
                .get().toInstant() + "', '"
                + status + "', 'accepted new task at: "
                + TimeInspector
                .getInspector()
                .getGetNewDate()
                .get().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // when Patrul wants to set in pause his work
        case SET_IN_PAUSE -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + TimeInspector
                .getInspector()
                .getGetNewDate()
                .get().toInstant() + "', '"
                + status + "', 'put in pause at: "
                + TimeInspector
                .getInspector()
                .getGetNewDate()
                .get().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses when at the end of the day User finishes his job
        case STOP_TO_WORK -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + TimeInspector
                .getInspector()
                .getGetNewDate()
                .get()
                .toInstant() + "', '"
                + status + "', 'stopped to work at: "
                + TimeInspector
                .getInspector()
                .getGetNewDate()
                .get()
                .toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses to when User wants to back to work after pause
        case START_TO_WORK -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + TimeInspector
                .getInspector()
                .getGetNewDate()
                .get()
                .toInstant() + "', '"
                + status + "', 'started to work at: "
                + TimeInspector
                .getInspector()
                .getGetNewDate()
                .get()
                .toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses to start to work every day in the morning
        case RETURNED_TO_WORK -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + TimeInspector
                .getInspector()
                .getGetNewDate()
                .get()
                .toInstant() + "', '"
                + status + "', 'returned to work at: "
                + TimeInspector
                .getInspector()
                .getGetNewDate()
                .get()
                .toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        case ARRIVED -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + TimeInspector
                .getInspector()
                .getGetNewDate()
                .get()
                .toInstant() + "', '"
                + status + "', 'arrived to given task location at: "
                + TimeInspector
                .getInspector()
                .getGetNewDate()
                .get()
                .toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // by default, it means t o log in to account
        default -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES ("
                + patrul.getUuid() + ", '"
                + TimeInspector
                .getInspector()
                .getGetNewDate()
                .get()
                .toInstant() + "', '"
                + status + "', 'log in at: "
                + patrul.getStartedToWorkDate().toInstant()
                + " with simCard "
                + patrul.getSimCardNumber() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone(); };

    private final Function< Patrul, TabletUsage > checkTableUsage = patrul -> {
            final Row row = this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.TABLETS_USAGE_TABLE.name()
                    + " WHERE uuidOfPatrul = " + patrul.getUuid()
                    + " AND simCardNumber = '" + patrul.getSimCardNumber() + "';" ).one();
            return super.getCheckParam().test( row ) ? new TabletUsage( row ) : null; };

    private final BiFunction< Patrul, Status, Disposable > updateStatus = ( patrul, status ) ->
            Mono.just( this.getSession().execute ( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.TABLETS_USAGE_TABLE.name()
                    + " WHERE uuidOfPatrul = " + patrul.getUuid()
                    + " AND simCardNumber = '" + patrul.getSimCardNumber() + "';" ).one() )
                    .map( row -> this.getSession().execute( "UPDATE "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.TABLETS_USAGE_TABLE.name()
                            + " SET lastActiveDate = '" + TimeInspector
                            .getInspector()
                            .getGetNewDate()
                            .get().toInstant() + "'"
                            + ( status.compareTo( LOGOUT ) == 0
                            ? ", totalActivityTime = " + abs( TimeInspector
                            .getInspector()
                            .getGetTimeDifferenceInSeconds()
                            .apply( row.getTimestamp( "startedToUse" ).toInstant() ) )
                            : "" )
                            + " WHERE uuidOfPatrul = " + patrul.getUuid()
                            + " AND simCardNumber = '" + row.getString( "simCardNumber" ) + "';" ) )
                    .subscribe();

    private final Function< String, Row > checkLogin = login ->
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRULS_LOGIN_TABLE.name()
                    + " WHERE login = '" + login + "';" ).one();

    private final Function< PatrulLoginRequest, Mono< ApiResponseModel > > login = patrulLoginRequest -> {
            final Row row = this.getCheckLogin().apply( patrulLoginRequest.getLogin() );
            return super.getCheckParam().test( row )
                    ? this.getGetPatrulByUUID().apply( row.getUUID( "uuid" ) )
                    .flatMap( patrul -> {
                        if ( patrul.getPassword().equals( patrulLoginRequest.getPassword() ) ) {
                            patrul.setStartedToWorkDate( TimeInspector
                                    .getInspector()
                                    .getGetNewDate()
                                    .get() );
                            if ( !patrul.getSimCardNumber().equals( "null" )
                                    && !patrul.getSimCardNumber().equals( patrulLoginRequest.getSimCardNumber() ) )
                                this.getUpdateStatus().apply( patrul, LOGOUT );
                            patrul.setSimCardNumber( patrulLoginRequest.getSimCardNumber() );
                            patrul.setTokenForLogin (
                                    Base64
                                        .getEncoder()
                                        .encodeToString( (
                                                patrul.getUuid()
                                                        + "@" + patrul.getPassportNumber()
                                                        + "@" + patrul.getPassword()
                                                        + "@" + patrul.getSimCardNumber()
                                                        + "@" + super.getGenerateToken().get() )
                                                .getBytes( StandardCharsets.UTF_8 ) ) );
                            this.getSession().execute( "UPDATE "
                                    + CassandraTables.TABLETS.name() + "."
                                    + CassandraTables.PATRULS.name()
                                    + " SET startedToWorkDate = '" + patrul.getStartedToWorkDate().toInstant() + "', "
                                    + "simCardNumber = '" + patrul.getSimCardNumber() + "', "
                                    + "tokenForLogin = '" + patrul.getTokenForLogin() + "' "
                                    + " WHERE uuid = " + patrul.getUuid() + " IF EXISTS;" );
                            this.getUpdatePatrulActivity().accept( patrul );
                            final TabletUsage tabletUsage1 = this.getCheckTableUsage().apply( patrul );
                            if ( tabletUsage1 == null ) Mono.just( new TabletUsage( patrul ) )
                                    .subscribe( tabletUsage -> this.getSession().execute( "INSERT INTO "
                                            + CassandraTables.TABLETS.name() + "."
                                            + CassandraTables.TABLETS_USAGE_TABLE.name()
                                            + super.getALlNames( TabletUsage.class )
                                            + " VALUES ('"
                                            + tabletUsage.getStartedToUse().toInstant() + "', '"
                                            + tabletUsage.getLastActiveDate().toInstant() + "', "

                                            + tabletUsage.getUuidOfPatrul() + ", '"
                                            + tabletUsage.getSimCardNumber() + "', "
                                            + tabletUsage.getTotalActivityTime() + ") IF NOT EXISTS;" ) );
                            else Mono.just( tabletUsage1 )
                                    .subscribe( tabletUsage -> this.getSession().execute( "UPDATE "
                                            + CassandraTables.TABLETS.name() + "."
                                            + CassandraTables.TABLETS_USAGE_TABLE.name()
                                            + " SET lastActiveDate = '" + TimeInspector
                                            .getInspector()
                                            .getGetNewDate()
                                            .get()
                                            .toInstant()
                                            + "' WHERE uuidOfPatrul = " + patrul.getUuid()
                                            + " AND simCardNumber = '" + patrul.getSimCardNumber() + "' IF EXISTS;" ) );

                            return super.getFunction().apply(
                                    Map.of( "message", "Authentication successfully passed",
                                            "success", this.getUpdatePatrulStatus().apply( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGIN ),
                                            "data",  com.ssd.mvd.gpstabletsservice.entity.Data
                                                    .builder()
                                                    .type( patrul.getUuid().toString() )
                                                    .data( patrul )
                                                    .build() ) ); }
                        else return super.getFunction().apply(
                                Map.of( "message", "Wrong Login or password",
                                        "code", 201,
                                        "success", false ) ); } )
                    : super.getFunction().apply(
                            Map.of( "message", "Wrong Login or password",
                            "code", 201,
                            "success", false ) ); };

    private final BiFunction< String, Status, Mono< ApiResponseModel > > changeStatus = ( token, status ) -> this.getGetPatrulByUUID()
            .apply( this.getDecode().apply( token ) )
            .flatMap( patrul -> {
                this.getUpdateStatus().apply( patrul, status );
                this.getUpdatePatrulActivity().accept( patrul );

                if ( super.getCheckEquality().test( status, START_TO_WORK ) ) {
                    patrul.setTotalActivityTime( 0L ); // set to 0 every day
                    patrul.setStartedToWorkDate( TimeInspector
                            .getInspector()
                            .getGetNewDate()
                            .get() ); // registration of time every day

                    this.getSession().execute( "UPDATE "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.PATRULS.name()
                            + " SET startedToWorkDate = '" + patrul.getStartedToWorkDate().toInstant() + "'"
                            + " WHERE uuid = " + patrul.getUuid() + ";" );

                    return super.getFunction().apply(
                            Map.of( "message", "Patrul: " + START_TO_WORK,
                                    "success", this.getUpdatePatrulStatus().apply( patrul, START_TO_WORK ) ) ); }

                else if ( super.getCheckEquality().test( status, ARRIVED ) ) {
                    if ( super.getCheckRequest().test( patrul.getTaskDate(), 5 ) ) {
                        this.getUpdateStatus().apply( patrul, CANCEL );
                        return TaskInspector
                                .getInstance()
                                .getTest()
                                .apply( patrul, TaskTypes.FREE )
                                .flatMap( apiResponseModel -> super.errorResponseForLateComing.get() ); }
                    else return TaskInspector
                            .getInstance()
                            .getChangeTaskStatus()
                            .apply( patrul, ARRIVED ); }

                else if ( super.getCheckEquality().test( status, LOGOUT ) ) {
                    patrul.setTokenForLogin( null );
                    patrul.setSimCardNumber( null );
                    return this.getUpdatePatrul().apply( patrul )
                            .flatMap( aBoolean -> super.getFunction().apply(
                                    Map.of( "message", "See you soon my darling )))",
                                            "success", this.getUpdatePatrulStatus().apply( patrul, LOGOUT ) ) ) ); }

                else if ( super.getCheckEquality().test( status, ACCEPTED ) ) return TaskInspector
                        .getInstance()
                        .getChangeTaskStatus()
                        .apply( patrul, ACCEPTED );

                else return super.getFunction().apply(
                            Map.of( "message", "Patrul: " + status,
                                    "success", this.getUpdatePatrulStatus().apply( patrul, status ) ) ); } );

    private final Function< String, Mono< ApiResponseModel > > checkToken = token -> this.getGetPatrulByUUID()
            .apply( this.getDecode().apply( token ) )
            .flatMap( patrul -> super.getFunction().apply(
                    Map.of( "message", patrul.getUuid().toString(),
                            "success", this.getUpdatePatrulStatus().apply( patrul, Status.LOGIN ),
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( patrul )
                                    .build() ) ) );

    private final BiFunction< UUID, PatrulActivityRequest, Mono< List< TabletUsage > > > getAllUsedTablets = ( uuid, request ) -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.TABLETS_USAGE_TABLE
                    + " WHERE uuidOfPatrul = " + uuid + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel( super.getCheckDifference().apply( uuid.toString().length() ) )
            .runOn( Schedulers.parallel() )
            .filter( row -> !super.getCheckRequest().test( request, 2 ) || super.getCheckTabletUsage().test( row, request ) )
            .map( TabletUsage::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .collectList();

    private final Consumer< String > addAllPatrulsToChatService = token -> this.getGetAllEntities()
            .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
            .map( Patrul::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .collectList()
            .subscribe( patruls -> patruls
                    .parallelStream()
                    .forEach( patrul -> {
                        patrul.setSpecialToken( token );
                        UnirestController
                                .getInstance()
                                .getAddUser()
                                .accept( patrul ); } ) );

    private final Function< String, UUID > decode = token -> UUID.fromString(
            new String( Base64
                    .getDecoder()
                    .decode( token ) )
                    .split( "@" )[ 0 ] );

    // возвращает список патрульных которые макс близко к камере
    private final Function< Point, Mono< PatrulInRadiusList > > getPatrulInRadiusList = point ->
            this.getFindTheClosestPatruls()
                    .apply( point, 2 )
                    .collectList()
                    .map( PatrulInRadiusList::new );

    // проверяет последнюю версию андроид приложения
    private final Function< String, Mono< ApiResponseModel > > checkVersionForAndroid = version -> {
            final Row row = this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.ANDROID_VERSION_CONTROL_TABLE.name()
                            + " WHERE id = 'id';" ).one();
            return row.getString( "version" ).compareTo( version ) == 0
                    ? super.getFunction().apply(
                            Map.of( "message", "you have the last version",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new AndroidVersionUpdate( row, LAST ) )
                                            .build() ) )
                    : super.getFunction().apply(
                            Map.of( "message", "you have to update to last version",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new AndroidVersionUpdate( row, OPTIONAL ) )
                                            .build() ) ); };

    // обновляет последнюю версию андроид приложения
    private final Function< AndroidVersionUpdate, Mono< ApiResponseModel > > saveLastVersion = androidVersionUpdate ->
            this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.ANDROID_VERSION_CONTROL_TABLE.name()
                    + " SET version = '" + androidVersionUpdate.getVersion()
                    + "', link = '" + androidVersionUpdate.getLink()
                    + "' WHERE id = 'id';" )
                    .wasApplied()
                    ? super.getFunction().apply( Map.of( "message", "Last version was saved" ) )
                    : super.getFunction().apply( Map.of( "message", "Error during the saving of version", "code", 201 ) );

    private final Supplier< Mono< ApiResponseModel > > getLastVersion = () -> super.getFunction().apply(
            Map.of( "message", "you have to update to last version",
                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                            .builder()
                            .data( new AndroidVersionUpdate(
                                    this.getSession().execute( "SELECT * FROM "
                                            + CassandraTables.TABLETS.name() + "."
                                            + CassandraTables.ANDROID_VERSION_CONTROL_TABLE.name()
                                            + " WHERE id = 'id';" )
                                    .one(),
                                    LAST ) )
                            .build() ) );

    private final BiFunction< CassandraTables, CassandraTables, ParallelFlux< Row > > getAllEntities =
            ( keyspace, table ) -> Flux.fromStream(
                    this.getSession().execute( "SELECT * FROM " + keyspace + "." + table + ";" )
                            .all()
                            .stream() )
                    .parallel( super.getCheckDifference().apply( table.name().length() + keyspace.name().length() ) )
                    .runOn( Schedulers.parallel() );

    public void delete ( final Throwable throwable ) {
        INSTANCE = null;
        this.getSession().close();
        this.getCluster().close();
        super.logging( throwable );
        KafkaDataControl.getInstance().clear();
        super.logging( "Cassandra is closed!!!" ); }
}