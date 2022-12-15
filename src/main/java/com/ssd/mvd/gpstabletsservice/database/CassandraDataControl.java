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
import java.util.logging.Logger;
import java.util.function.*;
import java.util.*;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.Disposable;
import lombok.Data;

import static java.lang.Math.cos;
import static java.lang.Math.*;

@Data
public final class CassandraDataControl {
    private final Cluster cluster;
    private final Session session;

    private final CodecRegistry codecRegistry = new CodecRegistry();
    private static CassandraDataControl cassandraDataControl = new CassandraDataControl();
    private final Logger logger = Logger.getLogger( CassandraDataControl.class.toString() );

    public static CassandraDataControl getInstance () { return cassandraDataControl != null
            ? cassandraDataControl
            : ( cassandraDataControl = new CassandraDataControl() ); }

    public void register () {
        CassandraConverter
                .getInstance()
                .registerCodecForPatrul(
                        CassandraTables.TABLETS.name(),
                        CassandraTables.PATRUL_TYPE.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForPositionInfo(
                        CassandraTables.TABLETS.name(),
                        CassandraTables.POSITION_INFO.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForCameraList(
                        CassandraTables.TABLETS.name(),
                        CassandraTables.CAMERA_LIST.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForReport(
                        CassandraTables.TABLETS.name(),
                        CassandraTables.REPORT_FOR_CARD.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForPoliceType(
                        CassandraTables.TABLETS.name(),
                        CassandraTables.POLICE_TYPE.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForPolygonType(
                        CassandraTables.TABLETS.name(),
                        CassandraTables.POLYGON_TYPE.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForPolygonEntity(
                        CassandraTables.TABLETS.name(),
                        CassandraTables.POLYGON_ENTITY.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForViolationsInformation(
                        CassandraTables.TABLETS.name(),
                        CassandraTables.VIOLATION_LIST_TYPE.name() ); }

    private void createType ( String typeName, Class object ) {
        this.getSession().execute( "CREATE TYPE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + typeName +
                CassandraConverter
                        .getInstance()
                        .convertClassToCassandra( object ) + " );" ); }

    private void createTable ( String tableName, Class object, String prefix ) {
        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                        + CassandraTables.TABLETS.name() + "." + tableName +
                        CassandraConverter
                                .getInstance()
                                .convertClassToCassandra( object ) + prefix ); }

    private CassandraDataControl () {
        SocketOptions options = new SocketOptions();
        options.setConnectTimeoutMillis( 30000 );
        options.setReadTimeoutMillis( 300000 );
//        options.setReuseAddress( true );
        options.setTcpNoDelay( true );
        options.setKeepAlive( true );
        ( this.session = ( this.cluster = Cluster
                .builder()
                .withClusterName( "GpsTablet" )
                .withPort( Integer.parseInt( GpsTabletsServiceApplication
                        .context
                        .getEnvironment()
                        .getProperty( "variables.CASSANDRA_PORT" ) ) )
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
                            .getProperty( "variables.CASSANDRA_CORE_CONN_REMOTE" ) ) )
                    .setCoreConnectionsPerHost( HostDistance.LOCAL,
                            Integer.parseInt( GpsTabletsServiceApplication
                            .context
                            .getEnvironment()
                            .getProperty( "variables.CASSANDRA_CORE_CONN_LOCAL" ) ) )
                    .setMaxConnectionsPerHost( HostDistance.REMOTE,
                            Integer.parseInt( GpsTabletsServiceApplication
                            .context
                            .getEnvironment()
                            .getProperty( "variables.CASSANDRA_MAX_CONN_REMOTE" ) ) )
                    .setMaxConnectionsPerHost( HostDistance.LOCAL,
                            Integer.parseInt( GpsTabletsServiceApplication
                            .context
                            .getEnvironment()
                            .getProperty( "variables.CASSANDRA_MAX_CONN_LOCAL" ) ) )
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
                        "PRIMARY KEY( (uuid) ) );" );

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

        this.logger.info( "Cassandra is ready" ); }

    private final Function< PatrulActivityRequest, Mono< List< PositionInfo > > > getHistory = request -> {
        try { return Flux.fromStream(
                this.getSession().execute( "SELECT * FROM "
                        + CassandraTables.GPSTABLETS.name() + "."
                        + CassandraTables.TABLETS_LOCATION_TABLE.name()
                        + " WHERE userId = '" + request.getPatrulUUID()
                        + "' AND date >= '" + request.getStartDate().toInstant()
                        + "' AND date <= '"
                        + request.getEndDate().toInstant() + "';" )
                .all()
                .stream()
                .parallel() )
                .parallel()
                .runOn( Schedulers.parallel() )
                .flatMap( row -> Mono.justOrEmpty( row != null ? new PositionInfo( row ) : new PositionInfo() ) )
                .sequential()
                .publishOn( Schedulers.single() )
                .collectList();
        } catch ( Exception e ) { return Mono.empty(); } };

    private final Supplier< Flux< PoliceType > > getAllPoliceTypes = () -> Flux.fromStream(
            this.getSession()
                    .execute("SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.POLICE_TYPE.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .flatMap( row -> Mono.just( new PoliceType( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .doOnError( throwable -> this.delete() );

    public Mono< ApiResponseModel > update ( PoliceType policeType ) {
        this.getGetPatrul()
                .get()
                .filter( patrul -> patrul.getPoliceType().equals( policeType.getPoliceType() ) )
                .subscribe( patrul -> this.getSession().executeAsync(
                        "UPDATE "
                                + CassandraTables.TABLETS.name() + "."
                                + CassandraTables.PATRULS.name()
                        + " SET policeType = '" + policeType.getPoliceType() + "';" ) );
        return this.getSession().execute( "UPDATE "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.POLICE_TYPE.name()
                        + " SET policeType = '" + policeType.getPoliceType() + "', "
                        + "icon = '" + policeType.getIcon() + "'"
                        + " WHERE uuid = " + policeType.getUuid() + " IF EXISTS;" )
                .wasApplied()
                ? Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", "PoliceType was updated successfully" ) )
                : Archive
                .getArchive()
                .getFunction()
                .apply( Map.of(
                        "message", "This PoliceType has already been applied",
                        "success", false,
                        "code", 201 ) )
                .doOnError( throwable -> this.delete() ); }

    public Mono< ApiResponseModel > addValue ( PoliceType policeType ) { return this.getGetAllPoliceTypes()
            .get()
            .filter( policeType1 -> policeType1.getPoliceType().equals( policeType.getPoliceType() ) )
            .count()
            .flatMap( aBoolean1 -> aBoolean1 == 0 ?
                    this.getSession().execute( "INSERT INTO "
                                    + CassandraTables.TABLETS.name() + "."
                                    + CassandraTables.POLICE_TYPE.name() +
                                    CassandraConverter
                                            .getInstance()
                                            .getALlNames( PoliceType.class ) +
                                    " VALUES("
                                    + policeType.getUuid() + ", '"
                                    + policeType.getIcon() + "', '"
                                    + policeType.getPoliceType() + "' );" )
                                .wasApplied()
                                ? Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of(
                                        "message", "PoliceType was saved successfully" ) )
                                : Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of(
                                        "message", "This PoliceType has already been applied",
                                        "success", false,
                                        "code", 201 ) )
                        : Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of(
                                "message", "This policeType name is already defined, choose another one",
                                "success", false,
                                "code", 201 ) ) )
                .doOnError( throwable -> this.delete() ); }

    private final Supplier< Flux< AtlasLustra > > getAllLustra = () -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.LUSTRA.name() + " ;" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .flatMap( row -> Mono.just( new AtlasLustra( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .doOnError( throwable -> {
                this.delete();
                this.logger.info(  "ERROR: " + throwable.getMessage() ); } );

    public Mono< ApiResponseModel > addValue ( AtlasLustra atlasLustra, Boolean check ) { return this.getSession()
            .execute(
            "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.LUSTRA.name() +
                    CassandraConverter
                            .getInstance()
                            .getALlNames( AtlasLustra.class ) +
                    " VALUES("
                    + atlasLustra.getUUID() + ", '"
                    + atlasLustra.getLustraName() + "', '"
                    + atlasLustra.getCarGosNumber() + "', "
                    + CassandraConverter
                        .getInstance()
                        .convertListOfPointsToCassandra( atlasLustra.getCameraLists() )
                    + " )"+ ( check ? " IF NOT EXISTS" : "" ) + ";" )
            .wasApplied()
            ? Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", "Lustra was saved successfully" ) )
            : Archive
            .getArchive()
            .getFunction()
            .apply( Map.of(
                    "message", "This Lustra has already been applied",
                    "success", false,
                    "code", 201 ) )
            .doOnError( throwable -> this.delete() ); }

    public Mono< ApiResponseModel > addValue ( PolygonType polygonType ) { return this.getSession().execute(
            "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON_TYPE.name()
            + CassandraConverter
                    .getInstance()
                    .getALlNames( PolygonType.class ) +
            " VALUES("
            + polygonType.getUuid() + ", '"
            + polygonType.getName() + "') IF NOT EXISTS;" )
            .wasApplied()
            ? Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", "PolygonType was saved successfully" ) )
            : Archive
            .getArchive()
            .getFunction()
            .apply( Map.of(
                    "message", "This PolygonType has already been applied",
                    "success", false,
                    "code", 201 ) )
            .doOnError( throwable -> {
                this.delete();
                this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    private final Function< UUID, Mono< PolygonType > > getAllPolygonTypeByUUID = uuid -> Mono.just(
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON_TYPE.name()
                    + " WHERE uuid = " + uuid + ";" ).one() )
            .map( PolygonType::new )
            .doOnError( throwable -> {
                this.delete();
                this.logger.info(  "ERROR: " + throwable.getMessage() ); } );

    private final Supplier< Flux< PolygonType > > getAllPolygonType = () -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.POLYGON_TYPE.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .flatMap( row -> Mono.just( new PolygonType( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() );

    public Mono< ApiResponseModel > addValue ( Polygon polygon ) { return this.getSession().execute(
            "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON.name()
                    + CassandraConverter
                    .getInstance()
                    .getALlNames( Polygon.class ) +
                    " VALUES ("
                    + polygon.getUuid() + ", "
                    + polygon.getOrgan() + ", "

                    + polygon.getRegionId() + ", "
                    + polygon.getMahallaId() + ", "
                    + polygon.getDistrictId() + ", '"

                    + polygon.getName() + "', '"
                    + polygon.getColor() + "', " +

                    CassandraConverter
                            .getInstance()
                            .convertClassToCassandraTable ( polygon.getPolygonType() ) + ", " +

                    CassandraConverter
                            .getInstance()
                            .convertListToCassandra( polygon.getPatrulList() ) + ", " +

                    CassandraConverter
                            .getInstance()
                            .convertListOfPointsToCassandra( polygon.getLatlngs() ) + ") IF NOT EXISTS;" )

                .wasApplied()
            ? Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", "Polygon was successfully saved" ) )
            : Archive
            .getArchive()
            .getFunction()
            .apply( Map.of(
                    "message", "This polygon has already been saved",
                    "success", false,
                    "code", 201 ) )
            .doOnError( throwable -> {
                this.delete();
                this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Mono< ApiResponseModel > update ( Polygon polygon ) { return this.getSession().execute(
            "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON.name() +
            CassandraConverter
                    .getInstance()
                    .getALlNames( Polygon.class ) +
            " VALUES ("
                    + polygon.getUuid() + ", "
                    + polygon.getOrgan() + ", "

                    + polygon.getRegionId() + ", "
                    + polygon.getMahallaId() + ", "
                    + polygon.getDistrictId() + ", '"

                    + polygon.getName() + "', '"
                    + polygon.getColor() + "', " +

            CassandraConverter
                .getInstance()
                .convertClassToCassandraTable ( polygon.getPolygonType() ) + ", " +

            CassandraConverter
                    .getInstance()
                    .convertListToCassandra( polygon.getPatrulList() ) + ", " +

            CassandraConverter
                    .getInstance()
                    .convertListOfPointsToCassandra( polygon.getLatlngs() ) + ");" )
                .wasApplied()
            ? Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", "Polygon was saved successfully" ) )
            : Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", "This polygon does not exists" ) )
            .doOnError( throwable -> {
                this.delete();
                this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    private final Function< UUID, Mono< Polygon > > getPolygonByUUID = uuid -> {
        Row row = this.getSession().execute( "SELECT * FROM "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.POLYGON.name()
                + " where uuid = " + uuid ).one();
        return Mono.justOrEmpty( row != null ? new Polygon( row ) : null ); };

    private final Supplier< Flux< Polygon > > getAllPolygons = () -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.POLYGON.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .flatMap( row -> Mono.just( new Polygon( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() );

    private final Predicate< String > checkTracker = trackerId -> this.getSession()
            .execute ( "SELECT * FROM "
                    + CassandraTables.ESCORT + "."
                    + CassandraTables.TRACKERSID
                    + " WHERE trackersId = '" + trackerId + "';" ).one() == null
            && this.getSession().execute( "SELECT * FROM "
            + CassandraTables.TRACKERS + "."
            + CassandraTables.TRACKERSID
            + " WHERE trackersId = '" + trackerId + "';" ).one() == null;

    private final Predicate< String > checkCarNumber = carNumber -> this.getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.TUPLE_OF_CAR.name() +
                    " where gosnumber = '" + carNumber + "';" ).one() == null
            && this.getSession().execute( "SELECT * FROM "
            + CassandraTables.TABLETS.name() + "."
            + CassandraTables.CARS.name() +
            " where gosnumber = '" + carNumber + "';" ).one() == null;

    private final Supplier< Flux< ReqCar > > getCar = () -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.CARS.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .flatMap( row -> Mono.just( new ReqCar( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() );

    private final Function< UUID, Mono< ReqCar > > getCarByUUID = uuid -> Mono.just(
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.CARS.name()
                    + " WHERE uuid = " + uuid + ";" ).one() )
            .map( ReqCar::new );

    public Mono< ApiResponseModel > delete ( String gosno ) { return this.getGetCarByUUID()
            .apply( UUID.fromString( gosno ) )
            .flatMap( reqCar -> {
                    if ( reqCar.getPatrulPassportSeries() == null
                    && reqCar.getPatrulPassportSeries().equals( "null" ) ) {
                        this.getSession().execute( "DELETE FROM "
                                + CassandraTables.TRACKERS + "."
                                + CassandraTables.TRACKERSID
                                + " where trackersId = '"
                                + reqCar.getTrackerId() + "';" );
                        return this.delete( CassandraTables.CARS.name(),
                                "uuid",
                                gosno );
                    } else return Archive
                            .getArchive()
                            .getFunction()
                            .apply( Map.of(
                                    "message", "This car is linked to patrul",
                                    "success", false,
                                    "code", 201 ) ); } )
                .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Mono< ApiResponseModel > update ( ReqCar reqCar ) { return this.getGetCarByUUID()
            .apply( reqCar.getUuid() )
            .flatMap( reqCar1 -> {
                if ( !reqCar.getTrackerId().equals( reqCar1.getTrackerId() )
                        && !this.getCheckTracker().test( reqCar.getTrackerId() ) ) return
                        Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of(
                                        "message", "Wrong TrackerId",
                                        "success", false,
                                        "code", 201 ) );
                if ( !reqCar.getPatrulPassportSeries().equals( reqCar1.getPatrulPassportSeries() ) ) {
                    this.getSession().execute ( "UPDATE "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.PATRULS.name()
                            + " SET carnumber = '" + reqCar.getGosNumber() + "', "
                            + "cartype = '" + reqCar.getVehicleType()
                            + "' where uuid = " + this.getGetPatrulByPassportNumber()
                            .apply( reqCar1.getPatrulPassportSeries() )
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
                        + CassandraTables.CARS.name() +
                        CassandraConverter
                                .getInstance()
                                .getALlNames( ReqCar.class ) +
                        " VALUES ("
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
                        + ");" ).wasApplied()
                        ? Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "Car was successfully saved" ) )
                        : Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of(
                                "message", "This car does not exist, choose another one",
                                "success", false,
                                "code", 201 ) ); } ); }

    public Mono< ApiResponseModel > addValue ( ReqCar reqCar ) {
        return this.getCheckTracker().test( reqCar.getTrackerId() )
                && this.getCheckCarNumber().test( reqCar.getGosNumber() )
                ? this.getSession().execute( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.CARS.name() +
                CassandraConverter
                        .getInstance()
                        .getALlNames( ReqCar.class ) +
                " VALUES ("
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
                + ") IF NOT EXISTS;" ).wasApplied()
                ? Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", "Car was successfully saved" ) )
                : Archive
                .getArchive()
                .getFunction()
                .apply( Map.of(
                        "message", "This car was already saved, choose another one",
                        "success", false,
                        "code", 201 ) )
                : Archive
                .getArchive()
                .getFunction()
                .apply( Map.of(
                        "message", "This trackers or gosnumber is already registered to another car, so choose another one",
                        "success", false,
                        "code", 201 ) )
                .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    private final Supplier< Flux< Patrul > > getPatrul = () -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.PATRULS.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .flatMap( row -> Mono.just( new Patrul( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() );

    private final Function< UUID, Mono< Patrul > > getPatrulByUUID = uuid -> {
        Row row = this.getSession().execute( "SELECT * FROM "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS.name()
                + " WHERE uuid = " + uuid + ";" ).one();
        return Mono.justOrEmpty( row != null ? new Patrul( row ) : null ); };

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
                    + " taskDate = '" + ( patrul.getTaskDate() != null
                    ? patrul.getTaskDate().toInstant() : new Date().toInstant() ) + "', "
                    + "listOfTasks = "
                    + CassandraConverter
                    .getInstance()
                    .convertMapToCassandra( patrul.getListOfTasks() )
                    + " WHERE uuid = " + patrul.getUuid() + " IF EXISTS;" );

    // обновляет время последней активности патрульного
    private final Consumer< Patrul > updatePatrulActivity = patrul ->
        this.getSession().execute( "UPDATE "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS.name()
                + " SET lastActiveDate = '" + new Date().toInstant() + "'"
//                + ", totalActivityTime = "
//                + patrul.getTotalActivityTime() +
//                TimeInspector
//                        .getInspector()
//                        .getGetTimeDifferenceInHours()
//                        .apply( patrul.getLastActiveDate().toInstant() )
                + " WHERE uuid = " + patrul.getUuid() + " IF EXISTS;" );

    public Mono< ApiResponseModel > update ( Patrul patrul ) {
        Row row = this.getGetPatrulByPassportNumber().apply( patrul.getPassportNumber() );
        if ( row == null ) return Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", "There is no such a patrul",
                        "success", false,
                        "code", 201 ) );

        if ( row.getUUID( "uuid" ).compareTo( patrul.getUuid() ) == 0 ) {
            if ( patrul.getLogin() == null ) patrul.setLogin( patrul.getPassportNumber() );
            if ( patrul.getName().contains( "'" ) ) patrul.setName( patrul.getName().replaceAll( "'", "" ) );
            if ( patrul.getSurname().contains( "'" ) ) patrul.setSurname( patrul.getSurname().replaceAll( "'", "" ) );
            if ( patrul.getOrganName().contains( "'" ) ) patrul.setOrganName( patrul.getOrganName().replaceAll( "'", "" ) );
            if ( patrul.getFatherName().contains( "'" ) ) patrul.setFatherName( patrul.getFatherName().replaceAll( "'", "" ) );
            if ( patrul.getRegionName().contains( "'" ) ) patrul.setRegionName( patrul.getRegionName().replaceAll( "'", "" ) );

            this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRULS_LOGIN_TABLE.name()
                    + " SET password = '" + patrul.getPassword()
                    + "' WHERE login = '" + patrul.getPassportNumber()
                    + "' AND uuid = " + patrul.getUuid() + ";" );

            return this.getSession().execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRULS.name() +
                    CassandraConverter
                            .getInstance()
                            .getALlNames( Patrul.class ) + " VALUES ('" +
                    ( patrul.getTaskDate() != null ? patrul.getTaskDate().toInstant() : new Date().toInstant() ) + "', '" +
                    ( patrul.getLastActiveDate() != null ? patrul.getLastActiveDate().toInstant() : new Date().toInstant() ) + "', '" +
                    ( patrul.getStartedToWorkDate() != null ? patrul.getStartedToWorkDate().toInstant() : new Date().toInstant() ) + "', '" +
                    ( patrul.getDateOfRegistration() != null ? patrul.getDateOfRegistration().toInstant() : new Date().toInstant() ) + "', " +

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
                    CassandraConverter
                            .getInstance()
                            .convertMapToCassandra( patrul.getListOfTasks() ) + " );" )
                    .wasApplied()
                    ? Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of( "message", "Patrul was successfully updated" ) )
                    : Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of(
                            "message", "There is no such a patrul",
                            "success", false,
                            "code", 201 ) ); }
        else return Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", "There is no such a patrul",
                        "success", false,
                        "code", 201 ) )
                .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

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
                    ? Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of( "message", "Image was updated successfully" ) )
                    : Mono.just( Archive
                    .getArchive()
                    .getErrorResponse()
                    .get() );

    // проверяет не имеет ли патрульный задание или не привязан ли он к эскорту или машине
    private final Predicate< Patrul > checkPatrulLinks = patrul ->
            patrul.getTaskId().equals( "null" )
            && patrul.getUuidOfEscort() == null
            && patrul.getUuidForPatrulCar() == null
            && patrul.getUuidForEscortCar() == null
            && patrul.getCarNumber().equals( "null" )
            && patrul.getTaskTypes().compareTo( TaskTypes.FREE ) == 0;

    public Mono< ApiResponseModel > deletePatrul ( UUID uuid ) { return this.getGetPatrulByUUID()
            .apply( uuid )
            .flatMap( patrul -> {
                if ( this.getCheckPatrulLinks().test( patrul ) ) {
                    this.getSession().execute ( "DELETE FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.PATRULS_LOGIN_TABLE.name()
                        + " WHERE login = '" + patrul.getLogin()
                        + "' AND uuid = " + patrul.getUuid() + ";" );

                    return this.delete( CassandraTables.PATRULS.name(),
                            "uuid",
                            patrul.getUuid().toString() ); }

                else return Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "You cannot delete this patrul",
                                "success", false,
                                "code", 201 ) ); } )
            .doOnError( throwable -> {
                this.logger.info(  "ERROR: " + throwable.getMessage() );
                this.delete(); } ); }

    public Mono< ApiResponseModel > addValue ( Patrul patrul ) {
        if ( this.getGetPatrulByPassportNumber()
                .apply( patrul.getPassportNumber() ) == null ) {
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
            if ( this.checkLogin.apply( patrul.getLogin() ) != null ) return Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of(
                            "message", "Patrul with this login has already been inserted, choose another one",
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
                    CassandraConverter
                            .getInstance()
                            .getALlNames( Patrul.class ) + " VALUES ('" +
                    ( patrul.getTaskDate() != null
                            ? patrul.getTaskDate().toInstant()
                            : new Date().toInstant() ) + "', '" +
                    ( patrul.getLastActiveDate() != null
                            ? patrul.getLastActiveDate().toInstant()
                            : new Date().toInstant() ) + "', '" +
                    ( patrul.getStartedToWorkDate() != null
                            ? patrul.getStartedToWorkDate().toInstant()
                            : new Date().toInstant() ) + "', '" +
                    ( patrul.getDateOfRegistration() != null
                            ? patrul.getDateOfRegistration().toInstant()
                            : new Date().toInstant() ) + "', " +

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
                    ( patrul.getTotalActivityTime() != null
                            ? patrul.getTotalActivityTime() : 0 ) + ", " +

                    ( patrul.getBatteryLevel() != null
                            ? patrul.getBatteryLevel() : 0 ) + ", " +
                    ( patrul.getInPolygon() != null
                            ? patrul.getInPolygon() : false ) + ", " +
                    ( patrul.getTuplePermission() != null
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
                    CassandraConverter
                            .getInstance()
                            .convertMapToCassandra( patrul.getListOfTasks() ) + " ) IF NOT EXISTS;" )
                    .wasApplied()
                    ? Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of( "message", "Patrul was successfully saved" ) )
                    : Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of(
                            "message", "Patrul has already been saved, choose another one",
                            "success", false,
                            "code", 201 ) );
        } else return Archive
                .getArchive()
                .getFunction()
                .apply( Map.of(
                        "message", "This patrul is already exists",
                        "success", false,
                        "code", 201 ) )
                .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    private final Supplier< Flux< Polygon > > getAllPolygonForPatrul = () -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.POLYGON_FOR_PATRUl.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .flatMap( row -> Mono.just( new Polygon( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() );

    private final Function< String, Mono< Polygon > > getPolygonForPatrul = id -> {
        Row row = this.getSession().execute( "SELECT * FROM "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.POLYGON_FOR_PATRUl.name()
                + " where uuid = " + UUID.fromString( id ) ).one();
        return Mono.just( row != null ? new Polygon( row ) : null ); };

    public Mono< ApiResponseModel > deletePolygonForPatrul ( String id ) { return this.getGetPolygonForPatrul()
            .apply( id )
            .flatMap( polygon1 -> {
                polygon1.getPatrulList()
                        .parallelStream()
                        .forEach( uuid -> this.getSession().executeAsync( "UPDATE " +
                                CassandraTables.TABLETS.name() + "."
                                + CassandraTables.PATRULS.name() +
                                " SET inPolygon = " + false
                                + " where uuid = " + uuid + ";" ) );
                return Mono.just( polygon1 ); } )
            .flatMap( polygon1 -> {
                    this.getSession().execute( "DELETE FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.POLYGON_FOR_PATRUl.name()
                            + " where uuid = " + UUID.fromString( id ) + ";" );
                    return Archive
                            .getArchive()
                            .getFunction()
                            .apply( Map.of( "message", "Polygon " + id + " successfully deleted" ) ); } ); }

    public Mono< ApiResponseModel > addPolygonForPatrul ( Polygon polygon ) { return this.getSession().execute(
            "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON_FOR_PATRUl.name() +
                CassandraConverter
                        .getInstance()
                        .getALlNames( Polygon.class ) +
                " VALUES ("
                + polygon.getUuid() + ", "
                + polygon.getOrgan() + ", "

                + polygon.getRegionId() + ", "
                + polygon.getMahallaId() + ", "
                + polygon.getDistrictId() + ", '"

                + polygon.getName() + "', '"
                + ( polygon.getColor() == null ? "Qizil" : polygon.getColor() ) + "', " +

                CassandraConverter
                        .getInstance()
                        .convertClassToCassandraTable ( polygon.getPolygonType() ) + ", " +

                CassandraConverter
                        .getInstance()
                        .convertListToCassandra( polygon.getPatrulList() ) + ", " +

                CassandraConverter
                        .getInstance()
                        .convertListOfPointsToCassandra( polygon.getLatlngs() ) + ") IF NOT EXISTS;" )
                .wasApplied()
            ? Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", "Polygon: " + polygon.getUuid() + " was saved successfully" ) )
            : Archive
            .getArchive()
            .getFunction()
            .apply( Map.of(
                    "message", "This polygon has already been created",
                    "success", false,
                    "code", 201 ) )
            .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Mono< ApiResponseModel > updatePolygonForPatrul ( Polygon polygon ) {
        return this.getGetPolygonForPatrul()
                .apply( polygon.getUuid().toString() )
                .flatMap( polygon1 -> {
                    polygon.getPatrulList().addAll( polygon1.getPatrulList() );
                    polygon.getPatrulList()
                            .forEach( uuid -> this.getSession().executeAsync(
                                    "UPDATE " +
                                            CassandraTables.TABLETS.name() + "."
                                            + CassandraTables.PATRULS.name() +
                                            " SET inPolygon = " + true
                                            + " where uuid = " + uuid + ";" ) );
                    return Mono.just( polygon ); } )
                .flatMap( polygon1 -> this.getSession().execute( "INSERT INTO "
                                + CassandraTables.TABLETS.name() + "."
                                + CassandraTables.POLYGON_FOR_PATRUl.name() +
                                CassandraConverter
                                        .getInstance()
                                        .getALlNames( Polygon.class ) +
                                " VALUES ("
                                + polygon.getUuid() + ", "
                                + polygon.getOrgan() + ", "

                                + polygon.getRegionId() + ", "
                                + polygon.getMahallaId() + ", "
                                + polygon.getDistrictId() + ", '"

                                + polygon.getName() + "', '"
                                + polygon.getColor() + "', " +

                                CassandraConverter
                                        .getInstance()
                                        .convertClassToCassandraTable ( polygon.getPolygonType() ) + ", " +

                                CassandraConverter
                                        .getInstance()
                                        .convertListToCassandra( polygon.getPatrulList() ) + ", " +

                                CassandraConverter
                                        .getInstance()
                                        .convertListOfPointsToCassandra( polygon.getLatlngs() ) + " );" )
                                .wasApplied()
                        ? Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "Polygon: " + polygon.getUuid() + " was updated successfully" ) )
                        : Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of(
                                "message", "This polygon has already been created",
                                "success", false,
                                "code", 201 ) )
                .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ) ); }

    private final Function< PatrulActivityRequest, Mono< PatrulActivityStatistics > > getPatrulStatistics = request ->
            this.getGetPatrulByUUID()
            .apply( UUID.fromString( request.getPatrulUUID() ) )
            .flatMap( patrul -> Flux.fromStream( this.getSession()
                            .execute( "SELECT * FROM "
                                    + CassandraTables.TABLETS.name() + "."
                                    + CassandraTables.PATRULS_STATUS_TABLE.name()
                                    + " WHERE uuid = " + patrul.getUuid()
                                    + ( request.getEndDate() != null
                                    && request.getStartDate() != null
                                    ? " AND date >= '"
                                    + request.getStartDate().toInstant()
                                    + "' AND date <= '"
                                    + request.getEndDate().toInstant() + "'" : "" )
                                    + ";" )
                            .all()
                            .stream()
                            .parallel() )
                    .parallel()
                    .runOn( Schedulers.parallel() )
                    .filter( row -> Status.valueOf( row.getString( "status" ) )
                            .compareTo( Status.LOGOUT ) == 0 )
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
            scheduleForPolygonPatrul -> this.getGetPolygonForPatrul()
                    .apply( scheduleForPolygonPatrul.getUuid() )
                    .flatMap( polygon -> Flux.fromStream( scheduleForPolygonPatrul
                                    .getPatrulUUIDs()
                                    .stream() )
                            .parallel()
                            .runOn( Schedulers.parallel() )
                            .flatMap( uuid -> this.getGetPatrulByUUID().apply( uuid ) )
                            .flatMap( patrul -> {
                                this.getSession().executeAsync(
                                        "UPDATE " +
                                                CassandraTables.TABLETS.name() + "."
                                                + CassandraTables.PATRULS.name() +
                                                " SET inPolygon = " + true
                                                + " where uuid = " + patrul.getUuid() + ";" );
                                return Mono.just( patrul.getUuid() ); } )
                            .sequential()
                            .publishOn( Schedulers.single() )
                            .collectList()
                            .flatMap( uuidList -> {
                                polygon.setPatrulList( uuidList );
                                return this.updatePolygonForPatrul( polygon ); } ) );

    private final Supplier< Flux< Notification > > getAllNotification = () -> Flux.fromStream (
            this.getSession().execute ( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.NOTIFICATION.name() + ";" )
            .all()
            .stream()
            .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .flatMap( row -> Mono.just( new Notification( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() );

    private final Function< Notification, Notification > saveNotification = notification -> {
        this.getSession().execute( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.NOTIFICATION.name() +
                CassandraConverter
                        .getInstance()
                        .getALlNames( Notification.class )
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

    private final Function< UUID, Mono< ApiResponseModel > > setNotificationAsRead = uuid -> this.getSession()
            .execute( "UPDATE "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.NOTIFICATION.name()
                    + " SET wasRead = " + true
                    + " WHERE uuid = " + uuid + ";" )
            .wasApplied()
            ? Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", "Notification " + uuid + " was updated successfully" ) )
            : Archive
            .getArchive()
            .getFunction()
            .apply( Map.of(
                    "message", "Notification " + uuid + " was not updated",
                    "success", false,
                    "code", 201 ) );

    public Mono< ApiResponseModel > delete ( String table,
                                             String param,
                                             String id ) {
        this.getSession().execute ( "DELETE FROM "
                + CassandraTables.TABLETS.name() + "." + table
                + " WHERE " + param + " = " + UUID.fromString( id ) + ";" );
        return Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", "Deleting has been finished successfully" ) ); }

    private static final Double p = PI / 180;

    private final BiFunction< Point, Patrul, Double > calculate = ( first, second ) ->
            12742 * asin( sqrt( 0.5 - cos( ( second.getLatitude() - first.getLatitude() ) * p ) / 2
            + cos( first.getLatitude() * p ) * cos( second.getLatitude() * p )
            * ( 1 - cos( ( second.getLongitude() - first.getLongitude() ) * p ) ) / 2 ) ) * 1000;

    private final Predicate< Row > checkPatrulStatus = row ->
            row.getDouble( "latitude" ) > 0
            && row.getDouble( "longitude" ) > 0;

    private final BiFunction< Point, Integer, Flux< Patrul > > findTheClosestPatruls = ( point, integer ) -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.PATRULS.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .filter( row -> this.checkPatrulStatus.test( row )
                    && ( integer != 1 || Status.valueOf( row.getString( "status" ) )
                    .compareTo( Status.FREE ) == 0
                    && TaskTypes.valueOf( row.getString( "taskTypes" ) )
                    .compareTo( TaskTypes.FREE ) == 0 ) )
            .flatMap( row -> Mono.just( new Patrul( row ) ) )
            .flatMap( patrul -> {
                patrul.setDistance( this.getCalculate().apply( point, patrul ) );
                return Mono.just( patrul ); } )
            .sequential()
            .sort( Comparator.comparing( Patrul::getDistance ) )
            .publishOn( Schedulers.single() );

    private final BiFunction< Point, UUID, Flux< Patrul > > findTheClosestPatrulsForSos = ( point, uuid ) ->
            Flux.fromStream( this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRULS.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
                    .parallel()
                    .runOn( Schedulers.parallel() )
                    .filter( row -> this.checkPatrulStatus.test( row )
                        && row.getUUID( "uuid" ).compareTo( uuid ) != 0
                        && row.getUUID( "uuidOfEscort" ) == null )
                    .flatMap( row -> Mono.just( new Patrul( row ) ) )
                    .flatMap( patrul -> {
                        patrul.setDistance( this.getCalculate().apply( point, patrul ) );
                        return Mono.just( patrul ); } )
                    .sequential()
                    .sort( Comparator.comparing( Patrul::getDistance ) )
                    .publishOn( Schedulers.single() )
                    .take( 20 );

    private final BiFunction< Patrul, Status, Boolean > updatePatrulStatus = ( patrul, status ) -> switch ( status ) {
        // in case when Patrul wants to leave his account
        case LOGOUT -> this.getSession().executeAsync( "INSERT INTO "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.PATRULS_STATUS_TABLE.name()
                        + "(uuid, date, status, message, totalActivityTime) VALUES("
                        + patrul.getUuid() + ", '"
                        + new Date().toInstant() + "', '"
                        + status + "', 'log out at: "
                        + new Date().toInstant() + "', "
                        + patrul.getTotalActivityTime() + ");" )
                .isDone();

        case ACCEPTED -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + " ( uuid, date, status, message, totalActivityTime ) VALUES("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'accepted new task at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // when Patrul wants to set in pause his work
        case SET_IN_PAUSE -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'put in pause at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses when at the end of the day User finishes his job
        case STOP_TO_WORK -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'stopped to work at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses to when User wants to back to work after pause
        case START_TO_WORK -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'started to work at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses to start to work every day in the morning
        case RETURNED_TO_WORK -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'returned to work at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        case ARRIVED -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'arrived to given task location at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // by default, it means t o log in to account
        default -> this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES ("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'log in at: "
                + patrul.getStartedToWorkDate().toInstant()
                + " with simCard "
                + patrul.getSimCardNumber() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone(); };

    private final Function< Patrul, TabletUsage > checkTableUsage = patrul -> {
        Row row = this.getSession().execute( "SELECT * FROM "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.TABLETS_USAGE_TABLE.name()
                + " WHERE uuidOfPatrul = " + patrul.getUuid()
                + " AND simCardNumber = '" + patrul.getSimCardNumber() + "';" ).one();
        return row != null ? new TabletUsage( row ) : null; };

    private BiFunction< Patrul, Status, Disposable > updateStatus = ( patrul, status ) ->
        Mono.just( this.getSession().execute ( "SELECT * FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.TABLETS_USAGE_TABLE.name()
                        + " WHERE uuidOfPatrul = " + patrul.getUuid()
                        + " AND simCardNumber = '" + patrul.getSimCardNumber() + "';" ).one() )
                .map( row -> this.getSession().execute( "UPDATE "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.TABLETS_USAGE_TABLE.name()
                        + " SET lastActiveDate = '" + new Date().toInstant() + "'"
                        + ( status.compareTo( LOGOUT ) == 0
                        ? ", totalActivityTime = " + abs( TimeInspector
                        .getInspector()
                        .getGetTimeDifferenceInSeconds()
                        .apply( row.getTimestamp( "startedToUse" ).toInstant() ) ) : "" )
                        + " WHERE uuidOfPatrul = " + patrul.getUuid()
                        + " AND simCardNumber = '" + row.getString( "simCardNumber" ) + "';" ) )
                .subscribe();

    private final Function< String, Row > checkLogin = login ->
            this.getSession().execute( "SELECT * FROM " +
            CassandraTables.TABLETS.name() + "."
            + CassandraTables.PATRULS_LOGIN_TABLE.name()
            + " where login = '" + login + "';" ).one();

    private final Function< PatrulLoginRequest, Mono< ApiResponseModel > > login = patrulLoginRequest -> {
        Row row = this.checkLogin.apply( patrulLoginRequest.getLogin() );
        return row != null
                ? this.getGetPatrulByUUID()
                .apply( row.getUUID( "uuid" ) )
                .flatMap( patrul -> {
                    if ( patrul.getPassword().equals( patrulLoginRequest.getPassword() ) ) {
                        patrul.setStartedToWorkDate( new Date() );
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
                                                    + "@" + Archive.getArchive().generateToken() )
                                            .getBytes( StandardCharsets.UTF_8 ) ) );
                        this.getSession().execute( "UPDATE "
                                + CassandraTables.TABLETS.name() + "."
                                + CassandraTables.PATRULS.name()
                                + " SET startedToWorkDate = '" + patrul.getStartedToWorkDate().toInstant() + "', "
                                + "simCardNumber = '" + patrul.getSimCardNumber() + "', "
                                + "tokenForLogin = '" + patrul.getTokenForLogin() + "' "
                                + " WHERE uuid = " + patrul.getUuid() + " IF EXISTS;" );
                        this.getUpdatePatrulActivity().accept( patrul );
                        TabletUsage tabletUsage1 = this.getCheckTableUsage().apply( patrul );
                        if ( tabletUsage1 == null ) Mono.just( new TabletUsage( patrul ) )
                                .subscribe( tabletUsage -> this.getSession().execute( "INSERT INTO "
                                        + CassandraTables.TABLETS.name() + "."
                                        + CassandraTables.TABLETS_USAGE_TABLE.name()
                                        + CassandraConverter
                                        .getInstance()
                                        .getALlNames( TabletUsage.class )
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
                                        + " SET lastActiveDate = '" + new Date().toInstant() + "' "
                                        + " WHERE uuidOfPatrul = " + patrul.getUuid()
                                        + " AND simCardNumber = '" + patrul.getSimCardNumber() + "' IF EXISTS;" ) );

                        return Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of(
                                        "message", "Authentication successfully passed",
                                        "success", this.getUpdatePatrulStatus()
                                                .apply( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGIN ),
                                        "data",  com.ssd.mvd.gpstabletsservice.entity.Data
                                                .builder()
                                                .type( patrul.getUuid().toString() )
                                                .data( patrul )
                                                .build() ) ); }
                    else return Archive
                            .getArchive()
                            .getFunction()
                            .apply( Map.of(
                                    "message", "Wrong Login or password",
                                    "code", 201,
                                    "success", false ) ); } )
                : Archive
                .getArchive()
                .getFunction()
                .apply( Map.of(
                        "message", "Wrong Login or password",
                        "code", 201,
                        "success", false ) ); };

    private final Function< String, Mono< ApiResponseModel > > startToWork = token -> this.getGetPatrulByUUID()
            .apply( this.getDecode().apply( token ) )
            .flatMap( patrul -> {
                this.getUpdatePatrulActivity().accept( patrul );
                this.getUpdateStatus().apply( patrul, START_TO_WORK );
                patrul.setTotalActivityTime( 0L ); // set to 0 every day
                patrul.setStartedToWorkDate( new Date() ); // registration of time every day
                this.getSession().execute( "UPDATE "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.PATRULS.name()
                        + " SET startedToWorkDate = '" + patrul.getStartedToWorkDate().toInstant() + "', "
                        + " WHERE uuid = " + patrul.getUuid() + " IF EXISTS;" );
                return Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of(
                                        "message", "Patrul started to work",
                                        "success", this.getUpdatePatrulStatus()
                                                .apply( patrul, Status.START_TO_WORK ) ) ); } );

    private final Function< String, Mono< ApiResponseModel > > checkToken = token -> this.getGetPatrulByUUID()
            .apply( this.getDecode().apply( token ) )
            .flatMap( patrul -> Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of(
                            "message", patrul.getUuid().toString(),
                            "success", this.getUpdatePatrulStatus()
                                    .apply( patrul, Status.LOGIN ),
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( patrul )
                                    .build() ) ) );

    private final Function< String, Mono< ApiResponseModel > > setInPause = token -> this.getGetPatrulByUUID()
            .apply( this.getDecode().apply( token ) )
            .flatMap( patrul -> {
                this.getUpdateStatus().apply( patrul, SET_IN_PAUSE );
                this.getUpdatePatrulActivity().accept( patrul );
                return Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of(
                                "message", "Patrul set in pause",
                                "success", this.getUpdatePatrulStatus()
                                        .apply( patrul, SET_IN_PAUSE ) ) ); } );

    private final Function< String, Mono< ApiResponseModel > > backToWork = token -> this.getGetPatrulByUUID()
            .apply( this.getDecode().apply( token ) )
            .flatMap( patrul -> {
                this.getUpdatePatrulActivity().accept( patrul );
                this.getUpdateStatus().apply( patrul, RETURNED_TO_WORK );
                return Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of(
                                "message", "Patrul returned to work",
                                "success", this.getUpdatePatrulStatus()
                                        .apply( patrul, RETURNED_TO_WORK ) ) ); } );

    private final Function< String, Mono< ApiResponseModel > > stopToWork = token -> this.getGetPatrulByUUID()
            .apply( this.getDecode().apply( token ) )
            .flatMap( patrul -> {
                this.getUpdateStatus().apply( patrul, STOP_TO_WORK );
                this.getUpdatePatrulActivity().accept( patrul );
                return Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of(
                                "message", "Patrul stopped his job",
                                "success", this.getUpdatePatrulStatus()
                                        .apply( patrul, STOP_TO_WORK ) ) ); } );

    private final Function< String, Mono< ApiResponseModel > > accepted = token -> this.getGetPatrulByUUID()
            .apply( this.getDecode().apply( token ) )
            .flatMap( patrul -> {
                this.getUpdateStatus().apply( patrul, ACCEPTED );
                this.getUpdatePatrulActivity().accept( patrul );
                return TaskInspector
                        .getInstance()
                        .getChangeTaskStatus()
                        .apply( patrul, ACCEPTED ); } );

    private final Function< String, Mono< ApiResponseModel > > arrived = token -> this.getGetPatrulByUUID()
            .apply( this.getDecode().apply( token ) )
            .flatMap( patrul -> {
                this.getUpdatePatrulActivity().accept( patrul );
                if ( Math.abs( TimeInspector
                        .getInspector()
                        .getGetTimeDifferenceInHours()
                        .apply( patrul.getTaskDate().toInstant() ) ) >= 24 ) {
                    this.getUpdateStatus().apply( patrul, CANCEL );
                    return TaskInspector
                            .getInstance()
                            .getRemovePatrulFromTask()
                            .apply( patrul )
                            .flatMap( apiResponseModel -> Archive
                                    .getArchive()
                                    .getErrorResponseForLateComing()
                                    .get() ); }
                else {
                    this.getUpdateStatus().apply( patrul, ARRIVED );
                    return TaskInspector
                            .getInstance()
                            .getChangeTaskStatus()
                            .apply( patrul, ARRIVED ); } } );

    private final Function< String, Mono< ApiResponseModel > > logout = token -> this.getGetPatrulByUUID()
            .apply( this.getDecode().apply( token ) )
            .flatMap( patrul -> {
                patrul.setTokenForLogin( null );
                patrul.setSimCardNumber( null );
                this.getUpdateStatus().apply( patrul, LOGOUT );
                this.getUpdatePatrulActivity().accept( patrul );
                return this.update( patrul )
                        .flatMap( aBoolean -> Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of(
                                        "message", "See you soon my darling )))",
                                        "success", this.getUpdatePatrulStatus()
                                                .apply( patrul, LOGOUT ) ) ) ); } );

    private final Function< Patrul, Flux< TabletUsage > > getAllUsedTablets = patrul -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.TABLETS_USAGE_TABLE.name()
                            + " WHERE uuidOfPatrul = " + patrul.getUuid() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .flatMap( row -> Mono.just( new TabletUsage( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() );

    private final Consumer< String > addAllPatrulsToChatService = token -> this.getGetPatrul()
            .get()
            .collectList()
            .subscribe( patruls -> patruls
                    .parallelStream()
                    .forEach( patrul -> {
                        patrul.setSpecialToken( token );
                        UnirestController
                                .getInstance()
                                .addUser( patrul ); } ) );

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
        Row row = this.getSession().execute( "SELECT * FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.ANDROID_VERSION_CONTROL_TABLE.name()
                        + " WHERE id = 'id';" )
                .one();
        return row.getString( "version" ).compareTo( version ) == 0
                ? Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", "you have the last version",
                        "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                .builder()
                                .data( new AndroidVersionUpdate( row, LAST ) )
                                .build() ) )
                : Archive
                .getArchive()
                .getFunction()
                .apply( Map.of(
                        "message", "you have to update to last version",
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
                    ? Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of( "message", "Last version was saved" ) )
                    : Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of(
                            "message", "Error during the saving of version",
                            "code", 201 ) );

    public void delete () {
        this.getSession().close();
        this.getCluster().close();
        cassandraDataControl = null;
        KafkaDataControl.getInstance().clear();
        this.logger.info( "Cassandra is closed!!!" ); }
}