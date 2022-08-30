package com.ssd.mvd.gpstabletsservice.database;

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.*;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;
import com.ssd.mvd.gpstabletsservice.response.PatrulActivityStatistics;
import static com.ssd.mvd.gpstabletsservice.constants.Status.ACCEPTED;
import static com.ssd.mvd.gpstabletsservice.constants.Status.ARRIVED;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import com.ssd.mvd.gpstabletsservice.request.PatrulLoginRequest;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.controller.Point;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.entity.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.Data;

import static java.lang.Math.cos;
import static java.lang.Math.*;

import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;
import java.util.Base64;
import java.util.UUID;
import java.util.Date;

@Data
public final class CassandraDataControl {
    private final Cluster cluster;
    private final Session session;

    private final String dbName = "TABLETS";

    private final String cars = "CARS";
    private final String lustre = "LUSTRA";
    private final String patrols = "PATRULS"; // for table with Patruls info
    private final String polygon = "POLYGON";
    private final String patrolsLogin = "PATRULS_LOGIN_TABLE"; // using in login situation

    private final String faceCar = "faceCar";
    private final String eventCar = "eventCar";
    private final String eventFace = "eventFace";
    private final String eventBody = "eventBody";
    private final String facePerson = "facePerson";
    private final String carTotalData = "carTotalData";
    private final String notification = "notification";

    private final String patrulType = "PATRUL_TYPE";
    private final String policeType = "POLICE_TYPE";
    private final String lustreType = "CAMERA_LIST";
    private final String polygonType = "POLYGON_TYPE";
    private final String polygonEntity = "POLYGON_ENTITY";
    private final String violationListType = "VIOLATION_LIST_TYPE";

    private final String reportForCard = "REPORT_FOR_CARD";
    private final String selfEmployment = "SELFEMPLOYMENT";
    private final String polygonForPatrul = "POLYGON_FOR_PATRUl";

    private CodecRegistry codecRegistry = new CodecRegistry();
    private static CassandraDataControl cassandraDataControl = new CassandraDataControl();
    private final Logger logger = Logger.getLogger( CassandraDataControl.class.toString() );
    public static CassandraDataControl getInstance() { return cassandraDataControl != null ? cassandraDataControl
            : ( cassandraDataControl = new CassandraDataControl() ); }

    public void register () {
        CassandraConverter
                .getInstance()
                .registerCodecForPolygonType( this.dbName, this.getPolygonType() );

        CassandraConverter
                .getInstance()
                .registerCodecForPatrul( this.dbName, this.getPatrulType() );

        CassandraConverter
                .getInstance()
                .registerCodecForPolygonEntity( this.dbName, this.getPolygonEntity() );

        CassandraConverter
                .getInstance()
                .registerCodecForCameraList( this.dbName, this.getLustreType() );

        CassandraConverter
                .getInstance()
                .registerCodecForViolationsInformation( this.dbName, this.getViolationListType() );

        CassandraConverter
                .getInstance()
                .registerCodecForReport( this.dbName, this.getReportForCard() );

        CassandraConverter
                .getInstance()
                .registerCodecForPoliceType( this.dbName, this.getPoliceType() ); }

    private void createType ( String typeName, Class object ) {
        this.session.execute("CREATE TYPE IF NOT EXISTS "
                + this.dbName + "."
                + typeName +
                CassandraConverter
                        .getInstance()
                        .convertClassToCassandra( object ) + " );" ); }

    private void createTable ( String tableName, Class object, String prefix ) {
        this.session.execute(
                "CREATE TABLE IF NOT EXISTS "
                        + this.dbName + "." + tableName +
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
                    .setConsistencyLevel( ConsistencyLevel.QUORUM )
                    .setDefaultIdempotence( true ) )
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
                .execute( "CREATE KEYSPACE IF NOT EXISTS " + this.dbName + " WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy'," +
                        "'datacenter1':3 } AND DURABLE_WRITES = false;" );

        this.createType( this.getPatrulType(), Patrul.class );
        this.createType( this.getPoliceType(), PoliceType.class );
        this.createType( this.getLustreType(), CameraList.class );
        this.createType( this.getPolygonType(), PolygonType.class );
        this.createType( this.getReportForCard(), ReportForCard.class );
        this.createType( this.getPolygonEntity(), PolygonEntity.class );
        this.createType( this.getViolationListType(), ViolationsInformation.class );

        this.createTable( this.getCars(), ReqCar.class, ", PRIMARY KEY ( uuid ) );" );
        this.createTable( this.getPoliceType(), PoliceType.class, ", PRIMARY KEY ( uuid ) );" );
        this.createTable( this.getPolygonType(), PolygonType.class, ", PRIMARY KEY ( uuid ) );" );
        this.createTable( this.getPatrols(), Patrul.class, ", status text, taskTypes text, listOfTasks map< text, text >," +
                "PRIMARY KEY ( uuid ) );" );

        this.createTable( this.getPolygon(), Polygon.class,
                ", polygonType frozen< " + this.getPolygonType() + " >, " +
                "patrulList list< uuid >, " +
                        "latlngs list < frozen< " + this.getPolygonEntity() + " > >, " +
                "PRIMARY KEY ( uuid ) );" );

        this.createTable( this.getPolygonForPatrul(), Polygon.class,
                ", polygonType frozen< " + this.getPolygonType() + " >, " +
                        "patrulList list< uuid >, " +
                        "latlngs list < frozen< " + this.getPolygonEntity()+ " > >, " +
                        "PRIMARY KEY ( uuid ) );" );

        this.createTable( this.getLustre(), AtlasLustra.class,
                ", cameraLists list< frozen< "
                        + this.getLustreType()
                        + " > >, PRIMARY KEY (uuid) );" );

        this.createTable ( this.getNotification(), Notification.class,
                ", taskTypes text, " +
                        "status text, " +
                        "PRIMARY KEY( (id), notificationWasCreated ) );" );

        this.session.execute(
                "CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.getPatrolsLogin()
                + " ( login text, password text, uuid uuid, PRIMARY KEY ( (login), uuid ) );" );

        this.logger.info( "Cassandra is ready" ); }

    public Mono< ApiResponseModel > addValue ( PoliceType policeType ) {
        return this.getAllPoliceTypes()
                .filter( policeType1 -> policeType1.getPoliceType().equals( policeType.getPoliceType() ) )
                .count()
                .flatMap( aBoolean1 -> aBoolean1 == 0 ?
                        this.session
                                .execute( "INSERT INTO "
                                        + this.dbName + "." + this.getPoliceType() +
                                        CassandraConverter
                                                .getInstance()
                                                .getALlNames( PoliceType.class ) +
                                        " VALUES("
                                        + policeType.getUuid() + ", '"
                                        + policeType.getPoliceType()
                                        + "' );" )
                                .wasApplied() ? Mono.just(
                                    ApiResponseModel.builder()
                                            .status(
                                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                            .message( "PoliceType was saved successfully" )
                                                            .code( 200 )
                                                            .build()
                                            ).build() ) : Mono.just(
                                    ApiResponseModel.builder()
                                            .status(
                                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                            .message( "This PoliceType has already been applied" )
                                                            .code( 201 )
                                                            .build()
                                            ).build() ) :
                                Mono.just( ApiResponseModel.builder()
                                        .success( false )
                                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                .message( "This policeType name is already defined, choose another one" )
                                                .code( 201 ).build() ).build() ) ); }

    public Mono< ApiResponseModel > update ( PoliceType policeType ) {
        this.getPatrul()
                .filter( patrul -> patrul.getPoliceType().equals( policeType.getPoliceType() ) )
                .subscribe( patrul -> this.session.executeAsync(
                        "UPDATE "
                        + this.dbName + "." + this.getPatrols()
                        + " SET policeType = '" + policeType.getPoliceType() + "';" ) );
        return this.session
                .execute( "INSERT INTO "
                        + this.dbName + "." + this.getPoliceType() +
                        CassandraConverter
                                .getInstance()
                                .getALlNames( PoliceType.class ) +
                        " VALUES("
                        + policeType.getUuid() + ", '"
                        + policeType.getPoliceType() + "' );" )
                .wasApplied() ? Mono.just(
                    ApiResponseModel.builder()
                            .status(
                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "PoliceType was updated successfully" )
                                            .code( 200 )
                                            .build()
                            ).build() ) : Mono.just(
                    ApiResponseModel.builder()
                            .status(
                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "This PoliceType has already been applied" )
                                            .code( 201 )
                                            .build() ).build() ); }

    public Flux< PoliceType > getAllPoliceTypes () {
        return Flux.fromStream(
                this.session.execute(
                        "SELECT * FROM "
                                + this.dbName + "." + this.getPoliceType() + " ;"
                ).all().stream()
        ).map( PoliceType::new ); }

    public Flux< AtlasLustra > getAllLustra () {
        return Flux.fromStream(
                this.session.execute(
                        "SELECT * FROM "
                                + this.dbName + "." + this.getLustre() + " ;"
                ).all().stream()
        ).map( AtlasLustra::new ); }

    public Mono< ApiResponseModel > addValue ( AtlasLustra atlasLustra, Boolean check ) { return this.session
            .execute( "INSERT INTO "
                    + this.dbName + "." + this.getLustre() +
                    CassandraConverter
                            .getInstance()
                            .getALlNames( AtlasLustra.class ) +
                    " VALUES("
                    + atlasLustra.getUUID() + ", '"
                    + atlasLustra.getLustraName() + "', '"
                    + atlasLustra.getCarGosNumber() + "', '"
                    + CassandraConverter
                        .getInstance()
                        .convertListOfCameraListToCassandra( atlasLustra.getCameraLists() )
                    + " )"+ ( check ? " IF NOT EXISTS" : "" ) + ";" )
            .wasApplied() ? Mono.just(
                ApiResponseModel.builder()
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Lustra was saved successfully" )
                                        .code( 200 )
                                        .build()
                        ).build() ) : Mono.just(
                ApiResponseModel.builder()
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "This Lustra has already been applied" )
                                        .code( 201 )
                                        .build()
                        ).build() ); }

    public Mono< ApiResponseModel > addValue ( PolygonType polygonType ) { return this.session
            .execute( "INSERT INTO "
            + this.dbName + "." + this.polygonType +
            CassandraConverter
                    .getInstance()
                    .getALlNames( PolygonType.class ) +
            " VALUES("
            + polygonType.getUuid() + ", '"
            + polygonType.getName() + "') IF NOT EXISTS;" )
            .wasApplied() ? Mono.just(
                ApiResponseModel.builder()
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "PolygonType was saved successfully" )
                                        .code( 200 )
                                        .build()
                        ).build() ) : Mono.just(
                        ApiResponseModel.builder()
                                .status(
                                        com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                .message( "This polygonType has already been applied" )
                                                .code( 201 )
                                                .build()
                                ).build() ); }

    public Mono< PolygonType > getCurrentPolygonType ( UUID uuid ) {
        return Mono.just(
                this.session.execute(
                        "SELECT * FROM "
                        + this.dbName + "." + this.polygonType
                                + " WHERE uuid = " + uuid + " ;"
                ).one()
        ).map( PolygonType::new ); }

    public Flux< PolygonType > getAllPolygonType () {
        return Flux.fromStream(
                this.session.execute(
                        "SELECT * FROM "
                        + this.dbName + "." + this.polygonType + ";"
                ).all().stream()
        ).map( PolygonType::new ); }

    public Mono< ApiResponseModel > addValue ( Polygon polygon ) {
        return this.session.execute( "INSERT INTO "
            + this.dbName + "." + this.polygon +
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
                    .convertListOfPolygonEntityToCassandra( polygon.getLatlngs() ) + ") IF NOT EXISTS;" )
                .wasApplied() ? Mono.just(
                        ApiResponseModel.builder()
                                .success( true )
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Polygon was successfully saved" )
                                        .code( 200 )
                                        .build() )
                                .build()
        ) : Mono.just(
                ApiResponseModel.builder()
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "This polygon has alread been saved" )
                                        .code( 201 )
                                        .build() ).build() ); }

    public Flux< Polygon > getAllPolygons () { return Flux.fromStream(
            this.session.execute(
                    "SELECT * FROM "
                    + this.dbName + "." + this.getPolygon() + ";"
            ).all().stream()
    ).map( Polygon::new ); }

    public Mono< Polygon > getPolygon ( UUID uuid ) {
        Row row = this.session.execute(
                "Select * from "
                        + this.dbName + "." + this.getPolygon()
                        + " where uuid = " + uuid ).one();
        return Mono.just( row != null ? new Polygon( row ) : null ); }

    public Mono< ApiResponseModel > addValue ( ReqCar reqCar ) { return this.session.execute( "INSERT INTO "
            + this.dbName + "." + this.getCars() +
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
            + ") IF NOT EXISTS;" ).wasApplied() ? Mono.just(
                    ApiResponseModel.builder()
                            .success( true )
                            .status(
                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "Car was successfully saved" )
                                            .code( 200 )
                                            .build()
                            ).build()
            ) : Mono.just(
                    ApiResponseModel.builder()
                            .success( false )
                            .status(
                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "This car was already saved, choose another one" )
                                            .code( 201 )
                                            .build()
                            ).build() ); }

    public Mono< ApiResponseModel > update ( ReqCar reqCar ) { return this.session.execute( "INSERT INTO "
            + this.dbName + "." + this.getCars() +
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
            + ") IF NOT EXISTS;" ).wasApplied() ? Mono.just(
                    ApiResponseModel.builder()
                            .success( true )
                            .status(
                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "Car was successfully saved" )
                                            .code( 200 )
                                            .build()
                            ).build()
            ) : Mono.just(
                    ApiResponseModel.builder()
                            .success( false )
                            .status(
                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "This car does not exist, choose another one" )
                                            .code( 201 )
                                            .build()
                            ).build() ); }

    public Mono< ReqCar > getCar ( UUID uuid ) { return Mono.just(
            this.session.execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.getCars()
                            + " WHERE uuid = " + uuid + ";"
            ).one()
    ).map( ReqCar::new ); }

    public Flux< ReqCar > getCar () { return Flux.fromStream(
            this.session.execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.getCars() + ";"
            ).all().stream()
    ).map( ReqCar::new ); }

    public Flux< Patrul > getPatrul() { return Flux.fromStream(
            this.session.execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.getPatrols() + ";"
            ).all().stream()
    ).map( Patrul::new ); }

    public Mono< Patrul > getPatrul ( UUID uuid ) {
        Row row = this.session.execute(
                "SELECT * FROM "
                        + this.dbName + "." + this.getPatrols()
                        + " WHERE uuid = " + uuid + ";"
        ).one();
        return Mono.justOrEmpty( row != null ? new Patrul( row ) : null ); }

    public Row getPatrul( String pasportNumber ) { return
            this.session.execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.getPatrols()
                            + " WHERE passportNumber = '" + pasportNumber + "';"
            ).one(); }

    public Mono< ApiResponseModel > update ( Patrul patrul ) {
        Row row = this.getPatrul( patrul.getPassportNumber() );
        if ( row == null ) return Mono.just(
                ApiResponseModel.builder()
                        .success( false )
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Wrong patrul data" )
                                        .code( 201 )
                                        .build() )
                        .build() );
        if ( row.getUUID( "uuid" ).compareTo( patrul.getUuid() ) == 0 ) {
            return this.session.execute( "INSERT INTO "
                    + this.dbName + "." + this.patrols +
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
                    ( patrul.getOrganName().contains( "'" ) ? patrul.getOrganName().replace( "'", "") : patrul.getOrganName() ) + "', '" +
                    ( patrul.getRegionName().contains( "'" ) ? patrul.getRegionName().replace( "'", "") : patrul.getRegionName() ) + "', '" +
                    patrul.getPoliceType() + "', '" +
                    ( patrul.getFatherName().contains( "'" ) ? patrul.getFatherName().replace( "'", "") : patrul.getFatherName() ) + "', '" +
                    patrul.getDateOfBirth() + "', '" +
                    patrul.getPhoneNumber() + "', '" +
                    patrul.getSpecialToken() + "', '" +
                    patrul.getTokenForLogin() + "', '" +
                    patrul.getSimCardNumber() + "', '" +
                    patrul.getPassportNumber() + "', '" +
                    patrul.getPatrulImageLink() + "', '" +
                    ( patrul.getSurnameNameFatherName().contains( "'" ) ? patrul.getSurnameNameFatherName().replace( "'", "") : patrul.getSurnameNameFatherName() ) + "', '" +
                    patrul.getStatus() + "', '" +
                    patrul.getTaskTypes() + "', " +
                    CassandraConverter
                            .getInstance()
                            .convertMapToCassandra( patrul.getListOfTasks() ) + " );"
            ).wasApplied() ? Mono.just(
                    ApiResponseModel.builder()
                            .success( true )
                            .status(
                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "Patrul was successfully updated" )
                                            .code( 200 )
                                            .build() )
                            .build()
            ) : Mono.just(
                    ApiResponseModel.builder()
                            .success( false )
                            .status(
                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "There is no such a patrul" )
                                            .code( 201 )
                                            .build() )
                            .build() ); }
        else return Mono.just(
                ApiResponseModel.builder()
                        .success( false )
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "There is no such a patrul" )
                                        .code( 201 )
                                        .build() )
                        .build() ); }

    public Mono< ApiResponseModel > addValue ( Patrul patrul ) {
        if ( this.getPatrul( patrul.getPassportNumber() ) == null ) {
            patrul.setInPolygon( false );
            patrul.setTuplePermission( false );
            patrul.setUuid( UUID.randomUUID() );
            patrul.setStatus( com.ssd.mvd.gpstabletsservice.constants.Status.FREE );
            patrul.setTaskTypes( com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FREE );
            if ( patrul.getLogin() == null ) patrul.setLogin( patrul.getPassportNumber() );
            patrul.setSurnameNameFatherName( patrul.getName() + " " + patrul.getSurname() + " " + patrul.getFatherName() );
            return this.session.execute(
                    "INSERT INTO "
                            + this.dbName + "." + this.getPatrolsLogin()
                            + " ( login, password, uuid ) VALUES( '"
                            + patrul.getLogin() + "', '"
                            + patrul.getPassword() + "', "
                            + patrul.getUuid() + " ) IF NOT EXISTS; " ).wasApplied() ?
            this.session.execute( "INSERT INTO "
                    + this.dbName + "." + this.getPatrols() +
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
                    ( patrul.getOrganName().contains( "'" ) ? patrul.getOrganName().replace( "'", "") : patrul.getOrganName() ) + "', '" +
                            ( patrul.getRegionName().contains( "'" ) ? patrul.getRegionName().replace( "'", "") : patrul.getRegionName() ) + "', '" +
                    patrul.getPoliceType() + "', '" +
                            ( patrul.getFatherName().contains( "'" ) ? patrul.getFatherName().replace( "'", "") : patrul.getFatherName() ) + "', '" +
                    patrul.getDateOfBirth() + "', '" +
                    patrul.getPhoneNumber() + "', '" +
                    patrul.getSpecialToken() + "', '" +
                    patrul.getTokenForLogin() + "', '" +
                    patrul.getSimCardNumber() + "', '" +
                    patrul.getPassportNumber() + "', '" +
                    patrul.getPatrulImageLink() + "', '" +
                            ( patrul.getSurnameNameFatherName().contains( "'" ) ? patrul.getSurnameNameFatherName().replace( "'", "") : patrul.getSurnameNameFatherName() ) + "', '" +
                    patrul.getStatus() + "', '" +
                    patrul.getTaskTypes() + "', " +
                    CassandraConverter
                            .getInstance()
                            .convertMapToCassandra( patrul.getListOfTasks() ) + " ) IF NOT EXISTS;" )
                    .wasApplied() ? Mono.just(
                    ApiResponseModel.builder()
                            .success( true )
                            .status(
                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "Patrul was successfully saved" )
                                            .code( 200 )
                                            .build() )
                            .build()
            ) : Mono.just(
                    ApiResponseModel.builder()
                            .success( false )
                            .status(
                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "Patrul has already been saved. choose another one" )
                                            .code( 201 )
                                            .build() )
                            .build() ) : Mono.just(
                    ApiResponseModel.builder()
                            .status(
                                    com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "Wrong login. it has to be unique" )
                                            .code( 200 )
                                            .build()
                            ).build() );
        } else return Mono.just(
                ApiResponseModel.builder()
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "This patrul is already exists" )
                                        .code( 200 )
                                        .build()
                        ).build() ); }

    public Flux< Polygon > getAllPoygonForPatrul () {
        return Flux.fromStream(
                this.session.execute(
                        "Select * from "
                                + this.dbName + "." + this.getPolygonForPatrul() + ";"
                ).all().stream()
        ).map( Polygon::new ); }

    public Mono< Polygon > getPolygonForPatrul ( String id ) {
        Row row = this.session.execute(
                "Select * from "
                        + this.dbName + "." + this.getPolygonForPatrul()
                        + " where uuid = " + UUID.fromString( id ) ).one();
        return Mono.just( row != null ? new Polygon( row ) : null ); }

    public Mono< ApiResponseModel > addPolygonForPatrul ( Polygon polygon ) {
        return this.session.execute( "INSERT INTO "
                + this.dbName + "." + this.polygonForPatrul +
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
                        .convertListOfPolygonEntityToCassandra( polygon.getLatlngs() ) + ") IF NOT EXISTS;" )
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
                        .build() ); }

    public Mono< ApiResponseModel > updatePolygonForPatrul ( Polygon polygon ) {
        return this.session.execute( "INSERT INTO "
                        + this.dbName + "." + this.polygonForPatrul +
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
                                .convertListOfPolygonEntityToCassandra( polygon.getLatlngs() ) + ");" )
                .wasApplied() ? Mono.just(
                ApiResponseModel.builder()
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Polygon: " + polygon.getUuid() + " was updated successfully" )
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
                        .build() ); }

    public Mono< ApiResponseModel > addPatrulToPolygon ( ScheduleForPolygonPatrul scheduleForPolygonPatrul ) {
        return this.getPolygonForPatrul( scheduleForPolygonPatrul.getUuid() )
                .flatMap( polygon -> Flux.fromStream( scheduleForPolygonPatrul.getPatrulUUIDs().stream() )
                        .flatMap( this::getPatrul)
                        .flatMap( patrul -> {
                            this.session.executeAsync(
                                    "UPDATE " +
                                            this.dbName + "." + this.getPatrols() +
                                            " inPolygon = " + true + " where uuid = " + patrul.getUuid() + " IF EXISTS;" );
                            return Mono.just( patrul.getUuid() ); } )
                        .collectList()
                        .flatMap( uuidList ->
                                Mono.just(
                                        ApiResponseModel.builder()
                                                .success(
                                                        this.session.execute(
                                                                "UPDATE " +
                                                                        this.dbName + "." + this.getPolygonForPatrul() +
                                                                        " SET patrulList " +
                                                                        CassandraConverter
                                                                                .getInstance()
                                                                                .convertListToCassandra( uuidList ) + " IF EXISTS;" )
                                                                .wasApplied() )
                                                .status(
                                                        com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                                .message( "Patrul was successfully linked to polygon" )
                                                                .code( 200 )
                                                                .build()
                                                ).build() ) ) ); }

    public Mono< PatrulActivityStatistics > getPatrulStatistics ( Request request ) { return this
            .getPatrul( UUID.fromString( request.getData() ) )
            .flatMap( patrul -> request.getSubject() == null && request.getSubject() == null ?
                    Mono.just( new PatrulActivityStatistics( patrul, Flux.fromStream( this.session.execute( "SELECT * FROM "
                            + this.dbName + "."
                            + this.patrols + patrul.getPassportNumber() ).all().stream() ) ) )
                    : Mono.just( new PatrulActivityStatistics( patrul, Flux.fromStream( this.session.execute( "SELECT * FROM "
                            + this.dbName + "." + this.patrols + patrul.getPassportNumber() + " WHERE date >= '"
                            + SerDes.getSerDes().convertDate( request.getObject().toString() ).toInstant()
                            + "' and date <= '" + SerDes.getSerDes().convertDate( request.getSubject().toString() ).toInstant() + "';" )
                    .all().stream() ) ) ) ); }

    public Boolean login ( Patrul patrul, Status status ) { return switch ( status ) {
        // in case when Patrul wants to leave his account
        case LOGOUT -> this.session.executeAsync( "INSERT INTO "
                + this.dbName + "." + this.patrols
                + patrul.getPassportNumber() + "(date, status, message, totalActivityTime) VALUES('"
                + new Date().toInstant() + "', '"
                + status + "', 'log out at: "
                + new Date().toInstant() + "', " + patrul.getTotalActivityTime() + ");" ).isDone();
        case ACCEPTED -> this.session.executeAsync( "INSERT INTO "
                + this.dbName + "." + this.patrols
                + patrul.getPassportNumber() + "(date, status, message, totalActivityTime) VALUES('"
                + new Date().toInstant() + "', '" + status + "', 'accepted new task at: "
                + new Date().toInstant() + "', " + patrul.getTotalActivityTime() + ");" ).isDone();
        // when Patrul wants to set in pause his work
        case SET_IN_PAUSE -> this.session.executeAsync( "INSERT INTO "
                + this.dbName + "." + this.patrols + patrul.getPassportNumber()
                + "(date, status, message, totalActivityTime) VALUES('" + new Date().toInstant() + "', '"
                + status + "', 'put in pause at: " + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses when at the end of the day User finishes his job
        case STOP_TO_WORK -> this.session.executeAsync( "INSERT INTO "
                + this.dbName + "." + this.patrols + patrul.getPassportNumber()
                + "(date, status, message, totalActivityTime) VALUES('"
                + new Date().toInstant() + "', '"
                + status + "', 'stopped to work at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses to when User wants to back to work after pause
        case START_TO_WORK -> this.session.executeAsync( "INSERT INTO "
                + this.dbName + "." + this.patrols + patrul.getPassportNumber()
                + "(date, status, message, totalActivityTime) VALUES('"
                + new Date().toInstant() + "', '" + status + "', 'started to work at: "
                + new Date().toInstant() + "', " + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses to start to work every day in the morning
        case RETURNED_TO_WORK -> this.session.executeAsync( "INSERT INTO "
                + this.dbName + "." + this.patrols + patrul.getPassportNumber()
                + "(date, status, message, totalActivityTime) VALUES('"
                + new Date().toInstant() + "', '"
                + status + "', 'returned to work at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        case ARRIVED -> this.session.executeAsync( "INSERT INTO "
                + this.dbName + "." + this.patrols
                + patrul.getPassportNumber()
                + "(date, status, message, totalActivityTime) VALUES('"
                + new Date().toInstant() + "', '"
                + status + "', 'arrived to given task location at: "
                + new Date().toInstant() + "', "
                + patrul.getTotalActivityTime() + ");" ).isDone();
        // by default, it means t o log in to account
        default -> this.session.executeAsync( "INSERT INTO "
                + this.dbName + "." + this.patrols
                + patrul.getPassportNumber()
                + "(date, status, message, totalActivityTime) VALUES ('"
                + new Date().toInstant() + "', '"
                + status + "', 'log in at: "
                + patrul.getStartedToWorkDate().toInstant()
                + " with simCard "
                + patrul.getSimCardNumber() + "', " + patrul.getTotalActivityTime() + ");" ).isDone(); }; }

    public void delete () {
        this.session.close();
        this.cluster.close();
        cassandraDataControl = null;
        this.logger.info( "Cassandra is closed!!!" ); }

    public Flux< Notification > getAllNotification () {
        return Flux.fromStream(
                this.session.execute(
                        "SELECT * FROM "
                                + this.dbName + "." + this.notification + ";"
                ).all().stream()
        ).map( row -> Notification.builder()
                .uuid( row.getUUID( "id" ) )
                .id( row.getString( "taskId" ) )
                .type( row.getString( "type" ) )
                .title( row.getString( "title" ) )
                .wasRead( row.getBool( "wasRead" ) )
                .address( row.getString( "address" ) )
                .carNumber( row.getString( "carNumber" ) )
                .policeType( row.getString( "policeType" ) )
                .nsfOfPatrul( row.getString( "nsfOfPatrul" ) )
                .passportSeries( row.getString( "passportSeries" ) )
                .latitudeOfTask( row.getDouble( "latitudeOfTask" ) )
                .longitudeOfTask( row.getDouble( "longitudeOfTask" ) )
                .status( Status.valueOf( row.getString( "status" ) ) )
                .taskTypes( TaskTypes.valueOf( row.getString( "taskTypes" ) ) )
                .notificationWasCreated( row.getTimestamp( "notificationWasCreated" ) )
                .build() ); }

    public Notification addValue ( Notification notification ) {
        this.session.execute(
                "INSERT INTO "
                        + this.dbName + "." + this.notification +
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

    public Mono< ApiResponseModel > setNotificationAsRead ( UUID uuid ) {
        return this.session.execute(
                "UPDATE"
                        + this.dbName + "." + this.notification
                        + " SET wasRead = " + true
                        + " WHERE id = '" + uuid + ";"
        ).wasApplied() ? Mono.just(
                ApiResponseModel.builder()
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Notification " + uuid + " was updated successfully" )
                                        .code( 200 )
                                        .build()
                        ).success( true )
                        .build() )
                : Mono.just( ApiResponseModel.builder()
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Notification " + uuid + " was not updated" )
                                        .code( 200 )
                                        .build()
                        ).success( false )
                        .build() ); }

    public Mono< ApiResponseModel > delete ( String table, String param, String id ) {
        this.session.execute(
                "DELETE FROM "
                        + this.dbName + "." + table
                        + " WHERE " + param + " = " + UUID.fromString( id ) + ";" );
        return Mono.just(
                ApiResponseModel.builder()
                        .status(
                                com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Deleting has been finished successfully" )
                                        .code( 200 )
                                        .build()
                        ).success( true )
                        .build() ); }

    private static final Double p = PI / 180;

    private Double calculate ( Point first, Patrul second ) { return 12742 * asin( sqrt( 0.5 - cos( ( second.getLatitude() - first.getLatitude() ) * p ) / 2
            + cos( first.getLatitude() * p ) * cos( second.getLatitude() * p )
            * ( 1 - cos( ( second.getLongitude() - first.getLongitude() ) * p ) ) / 2 ) ) * 1000; }

    public Flux< Patrul > findTheClosestPatruls ( Point point ) {
        return Flux.fromStream(
                this.session.execute(
                        "SELECT * FROM " +
                                this.dbName + "." + this.getPatrols() + ";"
                ).all().stream()
        ).filter( row ->
                Status.valueOf( row.getString( "status" ) ).compareTo( com.ssd.mvd.gpstabletsservice.constants.Status.FREE ) == 0
                && TaskTypes.valueOf( row.getString( "taskTypes" ) ).compareTo( com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FREE ) == 0
        ).map( Patrul::new )
                .flatMap( patrul -> {
                    patrul.setDistance( this.calculate( point, patrul ) );
                    return Mono.just( patrul ); } ); }

    public UUID decode ( String token ) { return UUID.fromString(
            new String( Base64.getDecoder()
                    .decode( token ) )
                    .split( "@" )[ 0 ] ); }

    public Mono< ApiResponseModel > login ( PatrulLoginRequest patrulLoginRequest ) {
        Row row = this.session.execute(
                "SELECT * FROM " +
                        this.dbName + "." + this.getPatrolsLogin()
                        + " where login = '" + patrulLoginRequest.getLogin() + "';" ).one();
        return row != null ? this.getPatrul( row.getUUID( "uuid" ) )
            .flatMap( patrul -> {
                if ( patrul.getPassword()
                        .equals( patrulLoginRequest.getPassword() ) ) {
                    patrul.setStartedToWorkDate( new Date() );
                    patrul.setSimCardNumber( patrulLoginRequest.getSimCardNumber() );
                    patrul.setTokenForLogin( Base64.getEncoder().encodeToString( (
                            patrul.getUuid() + "@" +
                                    patrul.getPassportNumber()
                                    + "@" + patrul.getPassword()
                                    + "@" + Archive.getAchieve().generateToken() )
                            .getBytes( StandardCharsets.UTF_8 ) ) );
                    this.update( patrul ).subscribe(); // savs all new changes in patrul object
                    return Mono.just( ApiResponseModel.builder()
                            .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .type( patrul.getUuid().toString() )
                                    .data( patrul )
                                    .build() )
                            .success( CassandraDataControl.getInstance()
                                    .login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGIN ) )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "Authentication successfully passed" )
                                    .code( 200 )
                                    .build() )
                            .build() );
                } else return Mono.just( ApiResponseModel.builder() // in case of wrong password
                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                .code( 201 )
                                .message( "Wrong Login or password" )
                                .build() )
                        .success( false )
                        .build() ); } ) : Mono.just( ApiResponseModel.builder() // in case of wrong login
                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                        .code( 201 )
                        .message( "Wrong Login or password" )
                        .build() )
                .success( false )
                .build() ); }

    public Mono< ApiResponseModel > checkToken ( String token ) { return this.getPatrul( this.decode( token ) )
            .flatMap( patrul -> Mono.just( ApiResponseModel.builder()
                    .data( com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                            .data( patrul ).build() )
                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                            .message( patrul.getUuid().toString() )
                            .code( 200 )
                            .build() )
                    .success( true )
                    .build() ) ); }

    public Mono< ApiResponseModel > accepted ( String token ) { return this.getPatrul( this.decode( token ) )
            .flatMap( patrul -> TaskInspector.getInstance()
                    .changeTaskStatus( patrul, ACCEPTED ) ); }

    public Mono< ApiResponseModel > arrived ( String token ) { return this.getPatrul( this.decode( token ) )
            .map( s -> SerDes.getSerDes().deserialize( s ) )
            .flatMap( patrul -> TaskInspector.getInstance()
                    .changeTaskStatus( patrul, ARRIVED ) ); }

    // uses when Patrul wants to change his status from active to pause
    public Mono< ApiResponseModel > setInPause ( String token ) { return this.getPatrul( this.decode( token ) )
                            .flatMap( patrul -> Mono.just( ApiResponseModel.builder()
                                    .success( this.login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGIN ) )
                                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                            .message( "Patrul set in pause" )
                                            .code( 200 )
                                            .build() )
                                    .build() ) ); }

    // uses when Patrul wants to change his status from pause to active
    public Mono< ApiResponseModel > backToWork ( String token ) { return this.getPatrul( this.decode( token ) )
            .flatMap( patrul -> Mono.just( ApiResponseModel.builder()
                    .success( this.login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.RETURNED_TO_WORK ) )
                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                            .message( "Patrul returned to work" )
                            .code( 200 )
                            .build() )
                    .build() ) ); }

    // sets every day when Patrul start to work in morning
    public Mono< ApiResponseModel > startToWork ( String token ) { return this.getPatrul( this.decode( token ) )
            .flatMap( patrul -> {
                patrul.setTotalActivityTime( 0L ); // set to 0 every day
                patrul.setStartedToWorkDate( new Date() ); // registration of time every day
                return this.update( patrul )
                        .flatMap( aBoolean1 -> Mono.just( ApiResponseModel.builder()
                                .success( CassandraDataControl.getInstance()
                                        .login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.START_TO_WORK ) )
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Patrul started to work" )
                                        .code( 200 )
                                        .build() )
                                .build() ) ); } ); }

//    uses when patrul finishes his work in the evening
    public Mono< ApiResponseModel > stopToWork ( String token ) { return this.getPatrul( this.decode( token ) )
            .flatMap( patrul -> Mono.just( ApiResponseModel.builder()
                    .success( this.login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.STOP_TO_WORK ) )
                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                            .message( "Patrul stopped his job" )
                            .code( 200 )
                            .build() )
                    .build() ) ); }

    public Mono< ApiResponseModel > logout ( String token ) { return this.getPatrul( this.decode( token ) )
            .flatMap( patrul -> {
                patrul.setTokenForLogin( null );
                return this.update( patrul )
                        .flatMap( aBoolean -> Mono.just( ApiResponseModel.builder()
                                .success( this
                                        .login( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.LOGOUT ) )
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "See you soon my darling )))" )
                                        .code( 200 )
                                        .build() ).build() ) ); } ); }
}
