package com.ssd.mvd.gpstabletsservice.database;

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.*;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.response.PatrulActivityStatistics;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
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

import java.util.logging.Logger;
import java.util.UUID;
import java.util.Date;

@Data
public final class CassandraDataControl {
    private final Cluster cluster;
    private final Session session;

    private final String dbName = "TABLETS";

    private final String car = "CARS";
    private final String lustre = "LUSTRA";
    private final String patrols = "PATRULS"; // for table with Patruls info
    private final String polygon = "POLYGON";

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
                .registerCodecForPatrul( this.dbName, this.getPatrulType() );

        CassandraConverter
                .getInstance()
                .registerCodecForPolygonEntity( this.dbName, this.getPolygonType() );

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
        this.createType( this.getViolationListType(), ViolationsInformation.class );

        this.createTable( this.getCar(), ReqCar.class, ", PRIMARY KEY ( uuid ) );" );
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
                        "patrulList list< frozen< " + this.getPatrulType() + " > >, " +
                        "latlngs list < frozen< " + this.getPolygonEntity()+ " > >, " +
                        "PRIMARY KEY ( uuid ) );" );

        this.createTable( this.getSelfEmployment(), SelfEmploymentTask.class,
                ", images list< text >, " +
                        "patruls map< uuid, frozen< " + this.getPatrulType() + " > >, " +
                        "reportForCards list< frozen <" + this.getReportForCard() + "> >, " +
                        "PRIMARY KEY ( (uuid) ) );" );

        this.createTable( this.getLustre(), AtlasLustra.class,
                ", cameraLists list< frozen< "
                        + this.getLustreType()
                        + " > >, PRIMARY KEY (uuid) );" );

        this.createTable ( this.getNotification(), Notification.class,
                ", taskTypes text, " +
                        "status text, " +
                        "PRIMARY KEY( (id), notificationWasCreated ) );" );

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
                        + this.dbName + "." + this.polygonType + " ;"
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

    public Boolean addValue ( ReqCar reqCar, String key ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.car +
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
            + reqCar.getAverageFuelConsumption() + ");" ).isDone(); }

    public Mono< ReqCar > getCar ( UUID uuid ) { return Mono.just(
            this.session.execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.getCar()
                            + " WHERE uuid = " + uuid + ";"
            ).one()
    ).map( ReqCar::new ); }

    public Flux< Patrul > getPatrul () { return Flux.fromStream(
            this.session.execute(
                    "SELECT * FROM" + ";"
            ).all().stream()
    ).map( Patrul::new ); }

    public Mono< Patrul > getPatrul ( UUID uuid ) { return Mono.just(
            this.session.execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.getPatrols()
                            + " WHERE uuid = " + uuid + ";"
            ).one()
    ).map( Patrul::new ); }

    public Boolean addValue ( Patrul patrul ) {
        patrul.setInPolygon( false );
        patrul.setTuplePermission( false );
        patrul.setUuid( UUID.randomUUID() );
        patrul.setStatus( com.ssd.mvd.gpstabletsservice.constants.Status.FREE );
        patrul.setTaskTypes( com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FREE );
        patrul.setSurnameNameFatherName( patrul.getName() + " " + patrul.getSurname() + " " + patrul.getFatherName() );
        return this.session.execute( "INSERT INTO "
                + this.dbName + "." + this.patrols +
                CassandraConverter
                        .getInstance()
                        .getALlNames( patrul.getClass() ) + " VALUES ('" +
                ( patrul.getTaskDate() != null ? patrul.getTaskDate().toInstant() : null ) + "', '" +
                ( patrul.getLastActiveDate() != null ? patrul.getLastActiveDate().toInstant() : null ) + "', '" +
                ( patrul.getStartedToWorkDate() != null ? patrul.getStartedToWorkDate().toInstant() : null ) + "', '" +
                ( patrul.getDateOfRegistration() != null ? patrul.getDateOfRegistration().toInstant() : null ) + "', " +

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

                patrul.getName().replaceAll( "'", "" ) + "', '" +
                patrul.getRank().replaceAll( "'", "" ) + "', '" +
                patrul.getEmail().replaceAll( "'", "" ) + "', '" +
                patrul.getLogin().replaceAll( "'", "" ) + "', '" +
                patrul.getTaskId() + "', '" +
                patrul.getCarType() + "', '" +
                patrul.getSurname().replaceAll( "'", "" ) + "', '" +
                patrul.getPassword().replaceAll( "'", "" ) + "', '" +
                patrul.getCarNumber() + "', '" +
                patrul.getOrganName().replaceAll( "'", "" ) + "', '" +
                patrul.getRegionName().replaceAll( "'", "" ) + "', '" +
                patrul.getPoliceType().replaceAll( "'", "" ) + "', '" +
                patrul.getFatherName().replaceAll( "'", "" ) + "', '" +
                patrul.getDateOfBirth().replaceAll( "'", "" ) + "', '" +
                patrul.getPhoneNumber().replaceAll( "'", "" ) + "', '" +
                patrul.getSpecialToken() + "', '" +
                patrul.getTokenForLogin() + "', '" +
                patrul.getSimCardNumber() + "', '" +
                patrul.getPassportNumber() + "', '" +
                patrul.getPatrulImageLink() + "', '" +
                patrul.getSurnameNameFatherName().replaceAll( "'", "" ) + "', '" +
                patrul.getStatus() + "', '" +
                patrul.getTaskTypes() + "', " +
                CassandraConverter
                        .getInstance()
                        .convertMapToCassandra( patrul.getListOfTasks() ) + " ) IF NOT EXISTS;"
        ).wasApplied(); }

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
                                .convertListOfPolygonEntityToCassandra( polygon.getLatlngs() ) + ") IF EXISTS;" )
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
                        .flatMap( this::getPatrul )
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

    public Boolean deleteCar ( String gosNumber ) {
        return this.session.execute( "delete from "
                + this.dbName + "." + this.car
                + " where gosnumber = '" + gosNumber + "';" )
                .wasApplied(); }

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
                        + this.dbName + "." + this.notification
                + "( id, taskId, type, latitudeOfTask, wasRead, longitudeOfTask," +
                        " notificationWasCreated, status, taskTypes," +
                        " title, address, carNumber, nsfOfPatrul, passportSeries, policeType ) VALUES ("
                + notification.getUuid() + ", '"
                + notification.getId() + "', '"
                + notification.getType() + "', "
                + notification.getLatitudeOfTask() + ", "
                + false + ", "
                + notification.getLongitudeOfTask() + ", '"
                + notification.getNotificationWasCreated().toInstant() + "', '"
                + notification.getStatus() + "', '"
                + notification.getTaskTypes() + "', '"
                + notification.getTitle() + "', '"
                + notification.getAddress() + "', '"
                + notification.getCarNumber() + "', '"
                + notification.getNsfOfPatrul() + "', '"
                + notification.getPassportSeries() + "', '"
                + notification.getPoliceType() + "');"
        ); return notification; }

    public Mono< ApiResponseModel > setNotificationAsRead ( UUID uuid ) {
        return this.session.execute(
                "UPDATE"
                        + this.dbName + "." + this.notification
                        + " SET wasRead = " + true
                        + " WHERE id = '" + uuid + "' IF EXISTS;"
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
            + cos( first.getLatitude() * p ) * cos( second.getLatitude() * p ) * ( 1 - cos( ( second.getLongitude() - first.getLongitude() ) * p ) ) / 2 ) ) * 1000; }

    public Flux< Patrul > findTheClosestPatruls ( Point point ) {
        return Flux.fromStream(
                this.session.execute(
                        "SELECT * FROM" + ";"
                ).all().stream()
        ).filter( row ->
                Status.valueOf( row.getString( "status" ) ).compareTo( com.ssd.mvd.gpstabletsservice.constants.Status.FREE ) == 0
                && TaskTypes.valueOf( row.getString( "taskTypes" ) ).compareTo( com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FREE ) == 0
        ).map( Patrul::new )
                .flatMap( patrul -> {
                    patrul.setDistance( this.calculate( point, patrul ) );
                    return Mono.just( patrul ); } ); }
}
