package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;
import com.ssd.mvd.gpstabletsservice.response.PatrulActivityStatistics;
import com.ssd.mvd.gpstabletsservice.controller.UnirestController;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import com.ssd.mvd.gpstabletsservice.request.PatrulLoginRequest;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.controller.Point;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.entity.*;

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.Data;

import static java.lang.Math.cos;
import static java.lang.Math.*;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Logger;

@Data
public final class CassandraDataControl {
    private final Cluster cluster;
    private final Session session;

    private final CodecRegistry codecRegistry = new CodecRegistry();
    private static CassandraDataControl cassandraDataControl = new CassandraDataControl();
    private final Logger logger = Logger.getLogger( CassandraDataControl.class.toString() );

    public static CassandraDataControl getInstance () { return cassandraDataControl != null ? cassandraDataControl
            : ( cassandraDataControl = new CassandraDataControl() ); }

    public void register () {
        CassandraConverter
                .getInstance()
                .registerCodecForPatrul( CassandraTables.TABLETS.name(), CassandraTables.PATRUL_TYPE.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForCameraList( CassandraTables.TABLETS.name(), CassandraTables.CAMERA_LIST.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForReport( CassandraTables.TABLETS.name(), CassandraTables.REPORT_FOR_CARD.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForPoliceType( CassandraTables.TABLETS.name(), CassandraTables.POLICE_TYPE.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForPolygonType( CassandraTables.TABLETS.name(), CassandraTables.POLYGON_TYPE.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForPolygonEntity( CassandraTables.TABLETS.name(), CassandraTables.POLYGON_ENTITY.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForViolationsInformation( CassandraTables.TABLETS.name(), CassandraTables.VIOLATION_LIST_TYPE.name() ); }

    private void createType ( String typeName, Class object ) {
        this.session.execute( "CREATE TYPE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + typeName +
                CassandraConverter
                        .getInstance()
                        .convertClassToCassandra( object ) + " );" ); }

    private void createTable ( String tableName, Class object, String prefix ) {
        this.session.execute( "CREATE TABLE IF NOT EXISTS "
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
        ( this.session = ( this.cluster = Cluster.builder()
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
            .withLoadBalancingPolicy( new TokenAwarePolicy( DCAwareRoundRobinPolicy.builder().build() ) )
            .withPoolingOptions( new PoolingOptions()
                    .setCoreConnectionsPerHost( HostDistance.REMOTE, Integer.parseInt( GpsTabletsServiceApplication
                            .context
                            .getEnvironment()
                            .getProperty( "variables.CASSANDRA_CORE_CONN_REMOTE" ) ) )
                    .setCoreConnectionsPerHost( HostDistance.LOCAL, Integer.parseInt( GpsTabletsServiceApplication
                            .context
                            .getEnvironment()
                            .getProperty( "variables.CASSANDRA_CORE_CONN_LOCAL" ) ) )
                    .setMaxConnectionsPerHost( HostDistance.REMOTE, Integer.parseInt( GpsTabletsServiceApplication
                            .context
                            .getEnvironment()
                            .getProperty( "variables.CASSANDRA_MAX_CONN_REMOTE" ) ) )
                    .setMaxConnectionsPerHost( HostDistance.LOCAL, Integer.parseInt( GpsTabletsServiceApplication
                            .context
                            .getEnvironment()
                            .getProperty( "variables.CASSANDRA_MAX_CONN_LOCAL" ) ) )
                    .setMaxRequestsPerConnection( HostDistance.REMOTE, 256 )
                    .setMaxRequestsPerConnection( HostDistance.LOCAL, 256 )
                    .setPoolTimeoutMillis( 60000 ) ).build() ).connect() )
                .execute( "CREATE KEYSPACE IF NOT EXISTS " + CassandraTables.TABLETS.name() +
                        " WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy'," +
                        "'datacenter1':3 } AND DURABLE_WRITES = false;" );

        this.createType( CassandraTables.PATRUL_TYPE.name(), Patrul.class );
        this.createType( CassandraTables.POLICE_TYPE.name(), PoliceType.class );
        this.createType( CassandraTables.CAMERA_LIST.name(), CameraList.class );
        this.createType( CassandraTables.POLYGON_TYPE.name(), PolygonType.class );
        this.createType( CassandraTables.POLYGON_ENTITY.name(), PolygonEntity.class );
        this.createType( CassandraTables.REPORT_FOR_CARD.name(), ReportForCard.class );
        this.createType( CassandraTables.VIOLATION_LIST_TYPE.name(), ViolationsInformation.class );

        this.createTable( CassandraTables.CARS.name(), ReqCar.class, ", PRIMARY KEY ( uuid ) );" );
        this.createTable( CassandraTables.POLICE_TYPE.name(), PoliceType.class, ", PRIMARY KEY ( uuid ) );" );
        this.createTable( CassandraTables.POLYGON_TYPE.name(), PolygonType.class, ", PRIMARY KEY ( uuid ) );" );
        this.createTable( CassandraTables.TABLETS_USAGE_TABLE.name(), TabletUsage.class, ", PRIMARY KEY ( uuidOfPatrul, simCardNumber ) );" );
        this.createTable( CassandraTables.PATRULS.name(), Patrul.class, ", status text, taskTypes text, listOfTasks map< text, text >, PRIMARY KEY ( uuid ) );" );

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
                        "PRIMARY KEY( (uuid) ) );" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS_LOGIN_TABLE.name()
                + " ( login text, password text, uuid uuid, PRIMARY KEY ( (login), uuid ) );" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                        + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS_STATUS_TABLE.name()
                        + " ( uuid uuid, " +
                        "date timestamp, " +
                        "status text, " +
                        "message text, " +
                        "totalActivityTime bigint, " +
                        " PRIMARY KEY( status, uuid ) );" );

        this.logger.info( "Cassandra is ready" ); }

    public Flux< PoliceType > getAllPoliceTypes () { return Flux.fromStream( this.session.execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "." + CassandraTables.POLICE_TYPE.name() + ";" )
                    .all().stream() )
            .map( PoliceType::new )
            .doOnError( throwable -> this.delete() ); }

    public Mono< ApiResponseModel > update ( PoliceType policeType ) {
        this.getPatrul()
                .filter( patrul -> patrul.getPoliceType().equals( policeType.getPoliceType() ) )
                .subscribe( patrul -> this.session.executeAsync(
                        "UPDATE "
                        + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS.name()
                        + " SET policeType = '" + policeType.getPoliceType() + "';" ) );
        return this.session
                .execute( "INSERT INTO "
                        + CassandraTables.TABLETS.name() + "." + CassandraTables.POLICE_TYPE.name() +
                        CassandraConverter
                                .getInstance()
                                .getALlNames( PoliceType.class ) +
                        " VALUES("
                        + policeType.getUuid() + ", '"
                        + policeType.getPoliceType() + "' );" )
                .wasApplied() ? Mono.just( ApiResponseModel
                            .builder()
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "PoliceType was updated successfully" )
                                            .code( 200 )
                                            .build()
                            ).build() ) : Mono.just( ApiResponseModel.builder()
                            .status( com.ssd.mvd.gpstabletsservice.response.Status
                                    .builder()
                                    .message( "This PoliceType has already been applied" )
                                    .code( 201 )
                                    .build() )
                        .build() )
                .doOnError( throwable -> this.delete() ); }

    public Mono< ApiResponseModel > addValue ( PoliceType policeType ) { return this.getAllPoliceTypes()
                .filter( policeType1 -> policeType1.getPoliceType().equals( policeType.getPoliceType() ) )
                .count()
                .flatMap( aBoolean1 -> aBoolean1 == 0 ?
                        this.session
                                .execute( "INSERT INTO "
                                        + CassandraTables.TABLETS.name() + "." + CassandraTables.POLICE_TYPE.name() +
                                        CassandraConverter
                                                .getInstance()
                                                .getALlNames( PoliceType.class ) +
                                        " VALUES("
                                        + policeType.getUuid() + ", '"
                                        + policeType.getPoliceType()
                                        + "' );" )
                                .wasApplied() ? Mono.just( ApiResponseModel
                                .builder()
                                .status( com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "PoliceType was saved successfully" )
                                        .code( 200 )
                                        .build()
                                ).build() ) : Mono.just( ApiResponseModel
                                .builder()
                                .status( com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "This PoliceType has already been applied" )
                                        .code( 201 )
                                        .build()
                                ).build() ) :
                        Mono.just( ApiResponseModel
                                .builder()
                                .success( false )
                                .status( com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "This policeType name is already defined, choose another one" )
                                        .code( 201 )
                                        .build() )
                                .build() ) )
                .doOnError( throwable -> this.delete() ); }

    public Flux< AtlasLustra > getAllLustra () { return Flux.fromStream(
                    this.session.execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                                    + CassandraTables.LUSTRA.name() + " ;" )
                            .all().stream() )
            .map( AtlasLustra::new )
            .doOnError( throwable -> {
                this.delete();
                this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Mono< ApiResponseModel > addValue ( AtlasLustra atlasLustra, Boolean check ) { return this.session
            .execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "." + CassandraTables.LUSTRA.name() +
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
            .wasApplied() ? Mono.just( ApiResponseModel
                        .builder()
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "Lustra was saved successfully" )
                                        .code( 200 )
                                        .build()
                        ).build() ) : Mono.just( ApiResponseModel
                        .builder()
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "This Lustra has already been applied" )
                                        .code( 201 )
                                        .build()
                        ).build() )
            .doOnError( throwable -> this.delete() ); }

    public Mono< ApiResponseModel > addValue ( PolygonType polygonType ) { return this.session.execute(
            "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.POLYGON_TYPE.name()
            + CassandraConverter
                    .getInstance()
                    .getALlNames( PolygonType.class ) +
            " VALUES("
            + polygonType.getUuid() + ", '"
            + polygonType.getName() + "') IF NOT EXISTS;" )
            .wasApplied() ? Mono.just( ApiResponseModel
                        .builder()
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "PolygonType was saved successfully" )
                                        .code( 200 )
                                        .build()
                        ).build() ) : Mono.just( ApiResponseModel
                                .builder()
                                .status( com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "This polygonType has already been applied" )
                                        .code( 201 )
                                        .build() )
                    .build() )
            .doOnError( throwable -> {
                this.delete();
                this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Mono< PolygonType > getAllPolygonType ( UUID uuid ) { return Mono.just(
                this.session.execute( "SELECT * FROM "
                        + CassandraTables.TABLETS.name() + "." + CassandraTables.POLYGON_TYPE.name()
                        + " WHERE uuid = " + uuid + ";" ).one() )
            .map( PolygonType::new )
                .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Flux< PolygonType > getAllPolygonType () { return Flux.fromStream(
                this.session.execute( "SELECT * FROM "
                        + CassandraTables.TABLETS.name() + "." + CassandraTables.POLYGON_TYPE.name() + ";" )
                        .all().stream() )
            .map( PolygonType::new ); }

    public Mono< ApiResponseModel > addValue ( Polygon polygon ) { return this.session.execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "." + CassandraTables.POLYGON.name()
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

                .wasApplied() ? Mono.just( ApiResponseModel
                        .builder()
                        .success( true )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .message( "Polygon was successfully saved" )
                                .code( 200 )
                                .build() )
                        .build()
        ) : Mono.just( ApiResponseModel
                        .builder()
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "This polygon has already been saved" )
                                        .code( 201 )
                                        .build() ).build() )
                        .doOnError( throwable -> {
                            this.delete();
                            this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Mono< ApiResponseModel > update ( Polygon polygon ) { return this.session.execute( "INSERT INTO "
            + CassandraTables.TABLETS.name() + "." + CassandraTables.POLYGON.name() +
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
                .wasApplied() ? Mono.just( ApiResponseModel
                                .builder()
                                .success( true )
                                .status( com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "Polygon was successfully updated" )
                                        .code( 200 )
                                        .build() )
                                .build()
        ) : Mono.just( ApiResponseModel
                        .builder()
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "This polygon does not exists" )
                                        .code( 201 )
                                        .build() ).build() )
                .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Mono< Polygon > getPolygon ( UUID uuid ) {
        Row row = this.session.execute( "SELECT * FROM "
                        + CassandraTables.TABLETS.name() + "." + CassandraTables.POLYGON.name()
                        + " where uuid = " + uuid ).one();
        return Mono.justOrEmpty( row != null ? new Polygon( row ) : null ); }

    public Flux< Polygon > getAllPolygons () { return Flux.fromStream(
            this.session.execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.POLYGON.name() + ";" )
                    .all().stream() )
            .map( Polygon::new ); }

    public Mono< ReqCar > getCar ( UUID uuid ) { return Mono.just(
            this.session.execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.CARS.name()
                    + " WHERE uuid = " + uuid + ";" ).one() )
            .map( ReqCar::new ); }

    // checks trackers. if this tracker already exists in database, it checks usual cars and escort database
    private Boolean checkTracker ( String trackerId ) { return this.session.execute( "SELECT * FROM "
            + CassandraTables.ESCORT.name() + "." + CassandraTables.TRACKERS_ID.name()
            + " where trackersId = '" + trackerId + "';" ).one() == null
            && this.session.execute( "SELECT * FROM "
            + CassandraTables.TRACKERS.name() + "." + CassandraTables.TRACKERS_ID.name()
            + " where trackersId = '" + trackerId + "';" ).one() == null; }

    private Boolean checkCarNumber ( String carNumber ) { return this.session.execute(
                "SELECT * FROM "
                        + CassandraTables.ESCORT.name() + "." + CassandraTables.TUPLE_OF_CAR.name()
                        + " where gosnumber = '" + carNumber + "';" ).one() == null
                && this.session.execute( "SELECT * FROM "
            + CassandraTables.TABLETS.name() + "."
            + CassandraTables.CARS.name()
                        + " where trackersId = '" + carNumber + "';" ).one() == null; }

    public Mono< ApiResponseModel > delete ( String gosno ) { return this.getCar( UUID.fromString( gosno ) )
                .flatMap( reqCar -> {
                    if ( reqCar.getPatrulPassportSeries() == null
                    && reqCar.getPatrulPassportSeries().equals( "null" ) ) {
                        this.session.execute(
                                "DELETE FROM trackers.trackersId where trackersId = '"
                                        + reqCar.getTrackerId() + "';" );
                        return this.delete( CassandraTables.CARS.name(),
                                "uuid",
                                gosno );
                    } else return Mono.just( ApiResponseModel
                                    .builder()
                                    .success( false )
                                    .status( com.ssd.mvd.gpstabletsservice.response.Status
                                            .builder()
                                            .message( "This car is linked to patrul" )
                                            .code( 201 )
                                            .build() )
                                    .build() ); } )
                .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Mono< ApiResponseModel > update ( ReqCar reqCar ) { return this.getCar( reqCar.getUuid() )
                .flatMap( reqCar1 -> {
                    if ( !reqCar.getTrackerId().equals( reqCar1.getTrackerId() )
                            && !this.checkTracker( reqCar.getTrackerId() ) ) return Mono.just( ApiResponseModel.builder()
                                    .success( false )
                                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "Wrong TrackerId" )
                                            .code( 201 )
                                            .build() ).build() );
                    if ( !reqCar.getPatrulPassportSeries().equals( reqCar1.getPatrulPassportSeries() ) ) {
                        this.session.execute ( "UPDATE "
                                + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS.name()
                                + " SET carnumber = '" + reqCar.getGosNumber() + "', "
                                + "cartype = '" + reqCar.getVehicleType() + "' where uuid = " + this.getPatrul(
                                        reqCar1.getPatrulPassportSeries() )
                                .getUUID( "uuid" ) + ";" );

                        this.session.execute ( "UPDATE "
                                + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS.name()
                                + " SET carnumber = '" + null + "', "
                                + "cartype = '" + null + "' where uuid = " + this.getPatrul(
                                        reqCar1.getPatrulPassportSeries() )
                                .getUUID( "uuid" ) + ";" ); }
                    return this.session.execute( "INSERT INTO "
                            + CassandraTables.TABLETS.name() + "." + CassandraTables.CARS.name() +
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
                            + ");" ).wasApplied() ? Mono.just( ApiResponseModel
                            .builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status
                                    .builder()
                                    .message( "Car was successfully saved" )
                                    .code( 200 )
                                    .build()
                            ).build()
                    ) : Mono.just( ApiResponseModel
                            .builder()
                            .success( false )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status
                                    .builder()
                                    .message( "This car does not exist, choose another one" )
                                    .code( 201 )
                                    .build()
                            ).build() ); } ); }

    public Mono< ApiResponseModel > addValue ( ReqCar reqCar ) {
        return this.checkTracker( reqCar.getTrackerId() ) && this.checkCarNumber( reqCar.getGosNumber() )
                ? this.session.execute( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "." + CassandraTables.CARS.name() +
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
                + ") IF NOT EXISTS;" ).wasApplied() ? Mono.just( ApiResponseModel
                        .builder()
                        .success( true )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .message( "Car was successfully saved" )
                                .code( 200 )
                                .build()
                        ).build()
        ) : Mono.just( ApiResponseModel
                .builder()
                .success( false )
                .status( com.ssd.mvd.gpstabletsservice.response.Status
                        .builder()
                        .message( "This car was already saved, choose another one" )
                        .code( 201 )
                        .build()
                ).build() ) : Mono.just( ApiResponseModel
                        .builder()
                        .success( false )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .message( "This trackers or gosnumber is already registered to another car, so choose another one" )
                                .code( 201 )
                                .build() )
                        .build() )
                        .doOnError( throwable -> {
                            this.delete();
                            this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Flux< ReqCar > getCar () { return Flux.fromStream(
            this.session.execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.CARS.name() + ";" )
                    .all().stream() )
            .map( ReqCar::new ); }

    public Flux< Patrul > getPatrul () { return Flux.fromStream(
            this.session.execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.PATRULS.name() + ";" )
            .all().stream() )
            .map( Patrul::new ); }

    public Mono< Patrul > getPatrul ( UUID uuid ) {
        Row row = this.session.execute( "SELECT * FROM "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS.name()
                + " WHERE uuid = " + uuid + ";" ).one();
        return Mono.justOrEmpty( row != null ? new Patrul( row ) : null ); }

    private Row getPatrul ( String pasportNumber ) { return this.session.execute( "SELECT * FROM "
            + CassandraTables.TABLETS.name() + "."
            + CassandraTables.PATRULS.name()
            + " WHERE passportNumber = '" + pasportNumber + "';" ).one(); }

    public Mono< ApiResponseModel > update ( Patrul patrul ) {
        Row row = this.getPatrul( patrul.getPassportNumber() );
        if ( row == null ) return Mono.just( ApiResponseModel
                        .builder()
                        .success( false )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .message( "Wrong patrul data" )
                                .code( 201 )
                                .build() ).build() );

        if ( row.getUUID( "uuid" ).compareTo( patrul.getUuid() ) == 0 ) {
            if ( patrul.getLogin() == null ) patrul.setLogin( patrul.getPassportNumber() );
            if ( patrul.getName().contains( "'" ) ) patrul.setName( patrul.getName().replaceAll( "'", "" ) );
            if ( patrul.getSurname().contains( "'" ) ) patrul.setSurname( patrul.getSurname().replaceAll( "'", "" ) );
            if ( patrul.getOrganName().contains( "'" ) ) patrul.setOrganName( patrul.getOrganName().replaceAll( "'", "" ) );
            if ( patrul.getFatherName().contains( "'" ) ) patrul.setFatherName( patrul.getFatherName().replaceAll( "'", "" ) );
            if ( patrul.getRegionName().contains( "'" ) ) patrul.setRegionName( patrul.getRegionName().replaceAll( "'", "" ) );

            this.session.execute(
                    "UPDATE "
                            + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS_LOGIN_TABLE.name()
                            + " SET password = '" + patrul.getPassword() +
                            "' WHERE login = '" + patrul.getPassportNumber()
                            + "' AND uuid = " + patrul.getUuid() + ";" );

            return this.session.execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS.name() +
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
                            .convertMapToCassandra( patrul.getListOfTasks() ) + " );"
            ).wasApplied() ? Mono.just( ApiResponseModel
                    .builder()
                    .success( true )
                    .status( com.ssd.mvd.gpstabletsservice.response.Status
                            .builder()
                            .message( "Patrul was successfully updated" )
                            .code( 200 )
                            .build() )
                    .build()
            ) : Mono.just( ApiResponseModel
                    .builder()
                    .success( false )
                    .status(
                            com.ssd.mvd.gpstabletsservice.response.Status
                                    .builder()
                                    .message( "There is no such a patrul" )
                                    .code( 201 )
                                    .build() )
                    .build() ); }
        else return Mono.just( ApiResponseModel
                        .builder()
                        .success( false )
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "There is no such a patrul" )
                                        .code( 201 )
                                        .build() )
                        .build() )
                .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Mono< ApiResponseModel > deletePatrul ( UUID uuid ) { return this.getPatrul( uuid )
            .flatMap( patrul -> {
                if ( patrul.getTaskId().equals( "null" )
                        && patrul.getCarNumber().equals( "null" )
                        && patrul.getUuidOfEscort().compareTo( null ) == 0
                        && patrul.getUuidForPatrulCar().compareTo( null ) == 0
                        && patrul.getUuidForEscortCar().compareTo( null ) == 0
                        && patrul.getTaskTypes().compareTo( TaskTypes.FREE ) == 0
                ) { this.session.execute (
                            "DELETE FROM "
                                    + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS_LOGIN_TABLE.name()
                                    + " WHERE login = '" + patrul.getLogin() + "';" );

                    return this.delete( CassandraTables.PATRULS.name(),
                            "uuid",
                            patrul.getUuid().toString() ); }

                else return Mono.just( ApiResponseModel
                                .builder()
                                .success( false )
                                .status( com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "You cannot delete this patrul" )
                                        .code( 201 )
                                        .build() )
                        .build() ); } )
                .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Mono< ApiResponseModel > addValue ( Patrul patrul ) {
        if ( this.getPatrul( patrul.getPassportNumber() ) == null ) {
            patrul.setInPolygon( false );
            patrul.setListOfTasks( new HashMap<>() );
            patrul.setStatus( com.ssd.mvd.gpstabletsservice.constants.Status.FREE );
            patrul.setTaskTypes( com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FREE );
            if ( patrul.getBatteryLevel() == null ) patrul.setBatteryLevel( 0 );
            if ( patrul.getLogin() == null ) patrul.setLogin( patrul.getPassportNumber() );
            if ( patrul.getName().contains( "'" ) ) patrul.setName( patrul.getName().replaceAll( "'", "" ) );
            if ( patrul.getSurname().contains( "'" ) ) patrul.setSurname( patrul.getSurname().replaceAll( "'", "" ) );
            if ( patrul.getOrganName() != null && patrul.getOrganName().contains( "'" ) )
                patrul.setOrganName( patrul.getOrganName().replaceAll( "'", "" ) );
            if ( patrul.getFatherName().contains( "'" ) ) patrul.setFatherName( patrul.getFatherName().replaceAll( "'", "" ) );
            if ( patrul.getRegionName().contains( "'" ) ) patrul.setRegionName( patrul.getRegionName().replaceAll( "'", "" ) );
            return this.session.execute(
                    "INSERT INTO "
                            + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS_LOGIN_TABLE.name()
                            + " ( login, password, uuid ) VALUES( '"
                            + patrul.getLogin() + "', '"
                            + patrul.getPassword() + "', "
                            + patrul.getUuid() + " ) IF NOT EXISTS;" ).wasApplied() ?
            this.session.execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS.name() +
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
                    patrul.getUuidOfEscort() + ", " +
                    patrul.getUuidForPatrulCar() + ", " +
                    patrul.getUuidForEscortCar() + ", " +

                    patrul.getRegionId() + ", " +
                    patrul.getMahallaId() + ", " +
                    patrul.getDistrictId() + ", " +
                    patrul.getTotalActivityTime() + ", " +

                    ( patrul.getBatteryLevel() != null ? patrul.getBatteryLevel() : 0 ) + ", " +
                    patrul.getInPolygon() + ", " +
                    ( patrul.getTuplePermission() != null ? patrul.getTuplePermission() : false ) + ", '" +

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
                    .wasApplied() ? Mono.just( ApiResponseModel
                            .builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status
                                    .builder()
                                    .message( "Patrul was successfully saved" )
                                    .code( 200 )
                                    .build() )
                            .build()
            ) : Mono.just( ApiResponseModel
                            .builder()
                            .success( false )
                            .status(
                                    com.ssd.mvd.gpstabletsservice.response.Status
                                            .builder()
                                            .message( "Patrul has already been saved. choose another one" )
                                            .code( 201 )
                                            .build() )
                            .build() )
                    : Mono.just( ApiResponseModel
                            .builder()
                            .status( com.ssd.mvd.gpstabletsservice.response.Status
                                    .builder()
                                    .message( "Wrong login. it has to be unique" )
                                    .code( 200 )
                                    .build()
                            ).build() );
        } else return Mono.just( ApiResponseModel
                        .builder()
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "This patrul is already exists" )
                                        .code( 201 )
                                        .build() )
                        .build() )
                .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Flux< Polygon > getAllPolygonForPatrul () { return Flux.fromStream(
            this.session.execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.POLYGON_FOR_PATRUl.name() + ";" )
                    .all().stream() )
            .map( Polygon::new ); }

    public Mono< Polygon > getPolygonForPatrul ( String id ) {
        Row row = this.session.execute( "SELECT * FROM "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.POLYGON_FOR_PATRUl.name()
                + " where uuid = " + UUID.fromString( id ) ).one();
        return Mono.just( row != null ? new Polygon( row ) : null ); }

    public Mono< ApiResponseModel > deletePolygonForPatrul ( String id ) { return this.getPolygonForPatrul( id )
                .flatMap( polygon1 -> {
                    polygon1.getPatrulList()
                            .forEach( uuid -> this.session.executeAsync( "UPDATE " +
                                    CassandraTables.TABLETS.name() + "."
                                    + CassandraTables.PATRULS.name() +
                                    " SET inPolygon = " + false
                                    + " where uuid = " + uuid + ";" ) );
                    return Mono.just( polygon1 );
                } ).flatMap( polygon1 -> {
                    this.session.execute( "DELETE FROM "
                                    + CassandraTables.TABLETS.name() + "."
                                    + CassandraTables.POLYGON_FOR_PATRUl.name()
                                    + " where uuid = " + UUID.fromString( id ) + ";" );
                    return Mono.just( ApiResponseModel
                            .builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status
                                    .builder()
                                    .message( "Polygon " + id + " successfully deleted" )
                                    .code( 200 )
                                    .build() )
                            .build() ); } ); }

    public Mono< ApiResponseModel > addPolygonForPatrul ( Polygon polygon ) { return this.session.execute(
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
                .wasApplied() ? Mono.just(
                ApiResponseModel.builder()
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Polygon: " + polygon.getUuid() + " was saved successfully" )
                                        .code( 200 )
                                        .build()
                        ).success( true )
                        .build()
            ) : Mono.just(
                ApiResponseModel.builder()
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "This polygon has already been created" )
                                        .code( 201 )
                                        .build()
                        ).success( false )
                        .build() )
                .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Mono< ApiResponseModel > updatePolygonForPatrul ( Polygon polygon ) {
        return this.getPolygonForPatrul( polygon.getUuid().toString() )
                        .flatMap( polygon1 -> {
                            polygon.getPatrulList().addAll( polygon1.getPatrulList() );
                            polygon.getPatrulList()
                                    .forEach( uuid -> this.session.executeAsync(
                                            "UPDATE " +
                                                    CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS.name() +
                                                    " SET inPolygon = " + true
                                                    + " where uuid = " + uuid + ";" ) );
                            return Mono.just( polygon );
                        } ).flatMap( polygon1 -> this.session.execute( "INSERT INTO "
                                        + CassandraTables.TABLETS.name() + "." + CassandraTables.POLYGON_FOR_PATRUl.name() +
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
                                .wasApplied() ? Mono.just( ApiResponseModel
                                        .builder()
                                        .status(
                                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                        .message( "Polygon: " + polygon.getUuid() + " was updated successfully" )
                                                        .code( 200 )
                                                        .build()
                                        ).success( true )
                                        .build()
                        ) : Mono.just( ApiResponseModel
                            .builder()
                            .status(
                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "This polygon has already been created" )
                                            .code( 201 )
                                            .build()
                                        ).success( false )
                                        .build() ) )
                .doOnError( throwable -> {
                    this.delete();
                    this.logger.info(  "ERROR: " + throwable.getMessage() ); } ); }

    public Mono< PatrulActivityStatistics > getPatrulStatistics ( Request request ) { return this
            .getPatrul( UUID.fromString( request.getData() ) )
            .flatMap( patrul -> {
                if ( request.getSubject() == null && request.getObject() == null ) return Flux.fromStream(
                                this.session.execute( "SELECT * FROM "
                                        + CassandraTables.TABLETS.name() + "."
                                        + CassandraTables.PATRULS_STATUS_TABLE.name() + ";" )
                                        .all().stream() )
                        .filter( row -> Status.valueOf( row.getString( "status" ) ).compareTo( Status.LOGOUT ) == 0 )
                        .map( row -> row.getLong( "totalActivityTime" ) )
                        .collectList()
                        .map( longs -> PatrulActivityStatistics
                                .builder()
                                .dateList( longs )
                                .patrul( patrul )
                                .build() );
                else return Flux.fromStream( this.session.execute( "SELECT * FROM "
                                        + CassandraTables.TABLETS.name() + "."
                                        + CassandraTables.PATRULS_STATUS_TABLE.name()
                                        + " WHERE date >= '"
                                        + SerDes
                                        .getSerDes()
                                        .convertDate( request.getObject().toString() ).toInstant()
                                        + "' and date <= '"
                                        + SerDes
                                        .getSerDes()
                                        .convertDate( request.getSubject().toString() ).toInstant() + "';"
                                ).all().stream() )
                        .filter( row -> Status.valueOf( row.getString( "status" ) ).compareTo( Status.LOGOUT ) == 0 )
                        .map( row -> row.getLong( "totalActivityTime" ) )
                        .collectList()
                        .map( longs -> PatrulActivityStatistics
                                .builder()
                                .dateList( longs )
                                .patrul( patrul )
                                .build() ); } ); }

    public Mono< ApiResponseModel > addPatrulToPolygon ( ScheduleForPolygonPatrul scheduleForPolygonPatrul ) {
        return this.getPolygonForPatrul( scheduleForPolygonPatrul.getUuid() )
                .flatMap( polygon -> Flux.fromStream( scheduleForPolygonPatrul.getPatrulUUIDs().stream() )
                        .flatMap( this::getPatrul)
                        .flatMap( patrul -> {
                            this.session.executeAsync(
                                    "UPDATE " +
                                            CassandraTables.TABLETS.name() + "."
                                            + CassandraTables.PATRULS.name() +
                                            " SET inPolygon = " + true
                                            + " where uuid = " + patrul.getUuid() + ";" );
                            return Mono.just( patrul.getUuid() ); } )
                        .collectList()
                        .flatMap( uuidList -> {
                            polygon.setPatrulList( uuidList );
                            return this.updatePolygonForPatrul( polygon ); } ) ); }

    public Flux< Notification > getAllNotification () { return Flux.fromStream (
                this.session.execute ( "SELECT * FROM "
                                + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.NOTIFICATION.name() + ";" )
                        .all().stream() )
            .map( row -> Notification
                    .builder()
                .id( row.getString( "id" ) )
                .type( row.getString( "type" ) )
                .title( row.getString( "title" ) )
                .address( row.getString( "address" ) )
                .carNumber( row.getString( "carNumber" ) )
                .policeType( row.getString( "policeType" ) )
                .nsfOfPatrul( row.getString( "nsfOfPatrul" ) )
                .passportSeries( row.getString( "passportSeries" ) )

                .latitudeOfTask( row.getDouble( "latitudeOfTask" ) )
                .longitudeOfTask( row.getDouble( "longitudeOfTask" ) )

                .uuid( row.getUUID( "uuid" ) )
                .wasRead( row.getBool( "wasRead" ) )
                .status( Status.valueOf( row.getString( "status" ) ) )
                .taskTypes( TaskTypes.valueOf( row.getString( "taskTypes" ) ) )
                .notificationWasCreated( row.getTimestamp( "notificationWasCreated" ) )
                .build() ); }

    public Notification addValue ( Notification notification ) { this.session.execute(
                "INSERT INTO "
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
                        + notification.getStatus() + "', "
                        + false + ", '"
                        + notification.getTaskTypes() + "', '"
                        + notification.getNotificationWasCreated().toInstant() + "');"
        ); return notification; }

    public Mono< ApiResponseModel > setNotificationAsRead ( UUID uuid ) { return this.session.execute(
                "UPDATE "
                        + CassandraTables.TABLETS.name() + "." + CassandraTables.NOTIFICATION.name()
                        + " SET wasRead = " + true
                        + " WHERE uuid = " + uuid + ";" )
            .wasApplied() ? Mono.just( ApiResponseModel
                        .builder()
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .message( "Notification " + uuid + " was updated successfully" )
                                .code( 200 )
                                .build()
                        ).success( true )
                        .build() )
                : Mono.just( ApiResponseModel
                        .builder()
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .message( "Notification " + uuid + " was not updated" )
                                .code( 200 )
                                .build()
                        ).success( false )
                        .build() ); }

    public Mono< ApiResponseModel > delete ( String table, String param, String id ) { this.session.execute (
                "DELETE FROM "
                        + CassandraTables.TABLETS.name() + "." + table
                        + " WHERE " + param + " = " + UUID.fromString( id ) + ";" );
        return Mono.just( ApiResponseModel
                        .builder()
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .message( "Deleting has been finished successfully" )
                                .code( 200 )
                                .build()
                        ).success( true )
                        .build() ); }

    private static final Double p = PI / 180;

    private Double calculate ( Point first, Patrul second ) { return 12742 * asin( sqrt( 0.5 -
            cos( ( second.getLatitude() - first.getLatitude() ) * p ) / 2
            + cos( first.getLatitude() * p ) * cos( second.getLatitude() * p )
            * ( 1 - cos( ( second.getLongitude() - first.getLongitude() ) * p ) ) / 2 ) ) * 1000; }

    public Flux< Patrul > findTheClosestPatruls ( Point point ) {
        return Flux.fromStream(
                this.session.execute(
                        "SELECT * FROM " +
                                CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS.name() + ";"
                ).all().stream() )
            .filter( row -> Status.valueOf( row.getString( "status" ) )
                            .compareTo( com.ssd.mvd.gpstabletsservice.constants.Status.FREE ) == 0
                    && TaskTypes.valueOf( row.getString( "taskTypes" ) )
                            .compareTo( com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FREE ) == 0
                    && row.getDouble( "latitude" ) > 0
                            && row.getDouble( "longitude" ) > 0
            ).map( Patrul::new )
                    .flatMap( patrul -> {
                        patrul.setDistance( this.calculate( point, patrul ) );
                        return Mono.just( patrul ); } ); }

    public Boolean login ( Patrul patrul, Status status ) { return switch ( status ) {
        // in case when Patrul wants to leave his account
        case LOGOUT -> this.session.executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(uuid, date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'log out at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" )
                .isDone();

        case ACCEPTED -> this.session.executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS_STATUS_TABLE.name()
                + " ( uuid, date, status, message, totalActivityTime ) VALUES("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'accepted new task at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // when Patrul wants to set in pause his work
        case SET_IN_PAUSE -> this.session.executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'put in pause at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses when at the end of the day User finishes his job
        case STOP_TO_WORK -> this.session.executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'stopped to work at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses to when User wants to back to work after pause
        case START_TO_WORK -> this.session.executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'started to work at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses to start to work every day in the morning
        case RETURNED_TO_WORK -> this.session.executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'returned to work at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        case ARRIVED -> this.session.executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'arrived to given task location at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // by default, it means t o log in to account
        default -> this.session.executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "." + CassandraTables.PATRULS_STATUS_TABLE.name()
                + "(date, status, message, totalActivityTime) VALUES ("
                + patrul.getUuid() + ", '"
                + new Date().toInstant() + "', '"
                + status + "', 'log in at: "
                + patrul.getStartedToWorkDate().toInstant()
                + " with simCard "
                + patrul.getSimCardNumber() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone(); }; }

    private TabletUsage checkTableUsage ( Patrul patrul ) {
        Row row = this.session.execute( "SELECT * FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.TABLETS_USAGE_TABLE.name()
                        + " WHERE uuidOfPatrul = " + patrul.getUuid()
                        + " AND simCardNumber = '" + patrul.getSimCardNumber() + "';" ).one();
        return row != null ? new TabletUsage( row ) : null; }

    private void updateStatus ( Patrul patrul, Status status ) {
        Mono.just( this.session.execute ( "SELECT * FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.TABLETS_USAGE_TABLE.name()
                        + " WHERE uuidOfPatrul = " + patrul.getUuid()
                        + " AND simCardNumber = '" + patrul.getSimCardNumber() + "';" ).one() )
                .subscribe( row -> this.session.execute( "UPDATE "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.TABLETS_USAGE_TABLE.name()
                        + " SET lastActiveDate = '" + new Date().toInstant() + "'"
                        + ( status.compareTo( LOGOUT ) == 0
                        ? ", totalActivityTime = " + Math.abs( TimeInspector
                        .getInspector()
                        .getTimeDifferenceInSEconds( row.getTimestamp( "startedToUse" ).toInstant() ) ) : "" )
                        + " WHERE uuidOfPatrul = " + patrul.getUuid()
                        + " AND simCardNumber = '" + row.getString( "simCardNumber" ) + "';" ) ); }

    public Mono< ApiResponseModel > login ( PatrulLoginRequest patrulLoginRequest ) {
        Row row = this.session.execute( "SELECT * FROM " +
                CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRULS_LOGIN_TABLE.name()
                + " where login = '" + patrulLoginRequest.getLogin() + "';" ).one();
        return row != null ? this.getPatrul( row.getUUID( "uuid" ) )
            .flatMap( patrul -> {
                if ( patrul.getPassword().equals( patrulLoginRequest.getPassword() ) ) {
                    patrul.setStartedToWorkDate( new Date() );
                    if ( !patrul.getSimCardNumber().equals( "null" )
                        && !patrul.getSimCardNumber().equals( patrulLoginRequest.getSimCardNumber() ) )
                        this.updateStatus( patrul, LOGOUT );
                    patrul.setSimCardNumber( patrulLoginRequest.getSimCardNumber() );
                    patrul.setTokenForLogin( Base64.getEncoder().encodeToString( (
                            patrul.getUuid() + "@" +
                                    patrul.getPassportNumber()
                                    + "@" + patrul.getPassword()
                                    + "@" + patrul.getSimCardNumber()
                                    + "@" + Archive.getAchieve().generateToken() )
                            .getBytes( StandardCharsets.UTF_8 ) ) );
                    this.update( patrul ).subscribe(); // savs all new changes in patrul object
                    TabletUsage tabletUsage1 = this.checkTableUsage( patrul );
                    if ( tabletUsage1 == null ) Mono.just( new TabletUsage( patrul ) )
                            .subscribe( tabletUsage -> this.session.execute( "INSERT INTO "
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
                            .subscribe( tabletUsage -> this.session.execute( "UPDATE "
                                    + CassandraTables.TABLETS.name() + "."
                                    + CassandraTables.TABLETS_USAGE_TABLE.name()
                                    + " SET lastActiveDate = '" + new Date().toInstant() + "' "
                                    + " WHERE uuidOfPatrul = " + patrul.getUuid()
                                    + " AND simCardNumber = '" + patrul.getSimCardNumber() + "' IF EXISTS;" ) );

                    return Mono.just( ApiResponseModel
                            .builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .type( patrul.getUuid().toString() )
                                    .data( patrul )
                                    .build() )
                            .success( CassandraDataControl
                                    .getInstance()
                                    .login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGIN ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status
                                    .builder()
                                    .message( "Authentication successfully passed" )
                                    .code( 200 )
                                    .build() )
                            .build() );
                } else return Mono.just( ApiResponseModel
                        .builder() // in case of wrong password
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .code( 201 )
                                .message( "Wrong Login or password" )
                                .build() )
                        .success( false )
                        .build() ); } ) :
                Mono.just( ApiResponseModel
                    .builder() // in case of wrong login
                    .status( com.ssd.mvd.gpstabletsservice.response.Status
                            .builder()
                            .code( 201 )
                            .message( "Wrong Login or password" )
                            .build() )
                    .success( false )
                    .build() ); }

    // sets every day when Patrul start to work in morning
    public Mono< ApiResponseModel > startToWork ( String token ) { return this.getPatrul( this.decode( token ) )
            .flatMap( patrul -> {
                this.updateStatus( patrul, START_TO_WORK );
                patrul.setTotalActivityTime( 0L ); // set to 0 every day
                patrul.setStartedToWorkDate( new Date() ); // registration of time every day
                return this.update( patrul )
                        .flatMap( aBoolean1 -> Mono.just( ApiResponseModel
                                .builder()
                                .success( CassandraDataControl
                                        .getInstance()
                                        .login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.START_TO_WORK ) )
                                .status( com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "Patrul started to work" )
                                        .code( 200 )
                                        .build() )
                                .build() ) ); } ); }

    public Mono< ApiResponseModel > checkToken ( String token ) { return this.getPatrul( this.decode( token ) )
            .flatMap( patrul -> Mono.just( ApiResponseModel
                    .builder()
                    .data( com.ssd.mvd.gpstabletsservice.entity.Data
                            .builder()
                            .data( patrul )
                            .build() )
                    .status( com.ssd.mvd.gpstabletsservice.response.Status
                            .builder()
                            .message( patrul.getUuid().toString() )
                            .code( 200 )
                            .build() )
                    .success( true )
                    .build() ) ); }

    // uses when Patrul wants to change his status from active to pause
    public Mono< ApiResponseModel > setInPause ( String token ) { return this.getPatrul( this.decode( token ) )
            .flatMap( patrul -> {
                this.updateStatus( patrul, SET_IN_PAUSE );
                return Mono.just( ApiResponseModel
                        .builder()
                        .success( this.login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGIN ) )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .message( "Patrul set in pause" )
                                .code( 200 )
                                .build() )
                        .build() ); } ); }

    // uses when Patrul wants to change his status from pause to active
    public Mono< ApiResponseModel > backToWork ( String token ) { return this.getPatrul( this.decode( token ) )
            .flatMap( patrul -> {
                this.updateStatus( patrul, RETURNED_TO_WORK );
                return Mono.just( ApiResponseModel
                        .builder()
                        .success( this.login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.RETURNED_TO_WORK ) )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .message( "Patrul returned to work" )
                                .code( 200 )
                                .build() )
                        .build() ); } ); }

    // uses when patrul finishes his work in the evening
    public Mono< ApiResponseModel > stopToWork ( String token ) { return this.getPatrul( this.decode( token ) )
            .flatMap( patrul -> {
                this.updateStatus( patrul, STOP_TO_WORK );
                return Mono.just( ApiResponseModel
                        .builder()
                        .success( this.login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.STOP_TO_WORK ) )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .message( "Patrul stopped his job" )
                                .code( 200 )
                                .build() )
                        .build() ); } ); }

    public Mono< ApiResponseModel > accepted ( String token ) { return this.getPatrul( this.decode( token ) )
            .flatMap( patrul -> {
                this.updateStatus( patrul, ACCEPTED );
                return TaskInspector
                        .getInstance()
                        .changeTaskStatus( patrul, ACCEPTED ); } ); }

    public Mono< ApiResponseModel > arrived ( String token ) { return this.getPatrul( this.decode( token ) )
            .map( s -> SerDes.getSerDes().deserialize( s ) )
            .flatMap( patrul -> {
                this.updateStatus( patrul, ARRIVED );
                return TaskInspector
                        .getInstance()
                        .changeTaskStatus( patrul, ARRIVED ); } ); }

    public Mono< ApiResponseModel > logout ( String token ) { return this.getPatrul( this.decode( token ) )
            .flatMap( patrul -> {
                patrul.setTokenForLogin( null );
                patrul.setSimCardNumber( null );
                this.updateStatus( patrul, LOGOUT );
                return this.update( patrul )
                        .flatMap( aBoolean -> Mono.just( ApiResponseModel
                                .builder()
                                .success( this
                                        .login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGOUT ) )
                                .status( com.ssd.mvd.gpstabletsservice.response.Status
                                        .builder()
                                        .message( "See you soon my darling )))" )
                                        .code( 200 )
                                        .build() )
                                .build() ) ); } ); }

    public Flux< TabletUsage > getAllUsedTablets ( Patrul patrul ) { return Flux.fromStream(
            this.session.execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.TABLETS_USAGE_TABLE.name()
                            + " WHERE uuidOfPatrul = " + patrul.getUuid() + ";" )
                .all().stream() )
                .map( TabletUsage::new ); }

    public void addAllPatrulsToChatService ( String token ) { this.getPatrul()
            .subscribe( patrul -> {
                patrul.setSpecialToken( token );
                UnirestController
                        .getInstance()
                        .addUser( patrul ); } ); }

    public UUID decode ( String token ) { return UUID.fromString(
            new String( Base64
                    .getDecoder()
                    .decode( token ) )
                    .split( "@" )[ 0 ] ); }

    public void delete () {
        this.getSession().close();
        this.getCluster().close();
        cassandraDataControl = null;
        this.logger.info( "Cassandra is closed!!!" ); }
}