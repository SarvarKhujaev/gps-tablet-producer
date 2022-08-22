package com.ssd.mvd.gpstabletsservice.database;

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.*;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvents;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvents;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.response.PatrulActivityStatistics;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.card.CardDetails;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.entity.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import lombok.Data;

import java.util.List;
import java.util.logging.Logger;
import java.time.Duration;
import java.util.UUID;
import java.util.Date;

@Data
public final class CassandraDataControl {
    private final Cluster cluster;
    private final Session session;
    private final String car = "CARS";
    private final String lustre = "LUSTRA";
    private final String dbName = "TABLETS";
    private final String patrols = "PATRULS"; // for table with Patruls info
    private final String polygon = "POLYGON";

    private final String carTotalData = "carTotalData";
    private final String notification = "notification";
    private final String violationListType = "violationListType";

    private final String faceCar = "faceCar";
    private final String eventCar = "eventCar";
    private final String eventFace = "eventFace";
    private final String eventBody = "eventBody";
    private final String facePerson = "facePerson";

    private final String policeTypes = "POLICETYPES";
    private final String polygonType = "POLYGONTYPE";
    private final String selfEmployment = "SELFEMPLOYMENT";
    private final String polygonForPatrul = "POLYGONFORPATRUl";

    private static CassandraDataControl cassandraDataControl = new CassandraDataControl();
    private final Logger logger = Logger.getLogger( CassandraDataControl.class.toString() );
    public static CassandraDataControl getInstance() { return cassandraDataControl != null ? cassandraDataControl
            : ( cassandraDataControl = new CassandraDataControl() ); }

    private CassandraDataControl () {
        SocketOptions options = new SocketOptions();
        options.setConnectTimeoutMillis( 30000 );
        options.setReadTimeoutMillis( 300000 );
//        options.setReuseAddress( true );
        options.setTcpNoDelay( true );
        options.setKeepAlive( true );
        ( this.session = ( this.cluster = Cluster.builder()
            .withClusterName( "GpsTablet" )
            .withPort( Integer.parseInt( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.CASSANDRA_PORT" ) ) )
                .addContactPoints( "10.254.5.1, 10.254.5.2, 10.254.5.3".split( ", " ) )
            .withProtocolVersion( ProtocolVersion.V4 )
            .withRetryPolicy( DefaultRetryPolicy.INSTANCE )
            .withQueryOptions( new QueryOptions()
                    .setConsistencyLevel( ConsistencyLevel.QUORUM )
                    .setDefaultIdempotence( true ) )
            .withSocketOptions( options )
            .withLoadBalancingPolicy( new TokenAwarePolicy( DCAwareRoundRobinPolicy.builder().build() ) )
            .withPoolingOptions( new PoolingOptions()
                    .setCoreConnectionsPerHost( HostDistance.REMOTE, Integer.parseInt( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.CASSANDRA_CORE_CONN_REMOTE" ) ) )
                    .setCoreConnectionsPerHost( HostDistance.LOCAL, Integer.parseInt( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.CASSANDRA_CORE_CONN_LOCAL" ) ) )
                    .setMaxConnectionsPerHost( HostDistance.REMOTE, Integer.parseInt( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.CASSANDRA_MAX_CONN_REMOTE" ) ) )
                    .setMaxConnectionsPerHost( HostDistance.LOCAL, Integer.parseInt( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.CASSANDRA_MAX_CONN_LOCAL" ) ) )
                    .setMaxRequestsPerConnection( HostDistance.REMOTE, 256 )
                    .setMaxRequestsPerConnection( HostDistance.LOCAL, 256 )
                    .setPoolTimeoutMillis( 60000 ) ).build() ).connect() )
                .execute( "CREATE KEYSPACE IF NOT EXISTS " + this.dbName + " WITH REPLICATION = { 'class' : 'NetworkTopologyStrategy'," +
                        "'datacenter1':3 } AND DURABLE_WRITES = false;" );

        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.patrols +
                "(passportNumber text, NSF text, object text, PRIMARY KEY( (passportNumber), NSF ) );" ); // the table for patruls

        this.session.execute("""
                CREATE CUSTOM INDEX IF NOT EXISTS patrul_name_idx ON TABLETS.PATRULS(NSF) USING 'org.apache.cassandra.index.sasi.SASIIndex'
                WITH OPTIONS = {
                    'mode': 'CONTAINS',
                    'analyzer_class': 'org.apache.cassandra.index.sasi.analyzer.StandardAnalyzer',
                    'tokenization_enable_stemming': 'true',
                    'tokenization_locale': 'en',
                    'tokenization_skip_stop_words': 'true',
                    'analyzed': 'true',
                    'tokenization_normalize_lowercase': 'true' };""");

        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.eventFace
                + "(id text, camera int, matched boolean, date timestamp, confidence double, object text, PRIMARY KEY( (id), date ) );" ); // the table for polygons
        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.eventBody
                + "(id text, camera int, matched boolean, date timestamp, confidence double, object text, PRIMARY KEY( (id), date ) );" ); // the table for polygons
        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.eventCar
                + "(id text, camera int, matched boolean, date timestamp, confidence double, object text, PRIMARY KEY( (id), date ) );" ); // the table for polygons

        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.polygon
                + "(id uuid PRIMARY KEY, polygonName text, polygonType text);" ); // the table for polygons

        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.facePerson
                + "(id text PRIMARY KEY, object text);" ); // the table for polygons
        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.faceCar
                + "(id text PRIMARY KEY, object text);" ); // the table for polygons

        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.patrols
                + "(passportNumber text PRIMARY KEY, NSF text, object text);" ); // the table for patruls
        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.polygonForPatrul
                + "(id uuid PRIMARY KEY, object text);" ); // the table for polygons for patrul
        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.polygonType
                + "(id uuid PRIMARY KEY, polygonType text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.policeTypes
                + "(id uuid PRIMARY KEY, policeType text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.selfEmployment
                + "(id uuid PRIMARY KEY, object text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.car
                + "(uuid uuid, trackersId text, gosNumber text PRIMARY KEY, object text);" ); // the table for cars
        this.session.execute("CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.lustre
                + "(id uuid PRIMARY KEY, object text);" ); // the table for police types

        this.session.execute(
                "CREATE TYPE IF NOT EXISTS "
                        + this.dbName + "." + this.violationListType
                        + "( DecreeStatus int, "
                        + "Amount int, "
                        + "DecreeSerialNumber text, "
                        + "Violation text, "
                        + "Division text, "
                        + "PayDate text, "
                        + "Address text, "
                        + "Article text, "
                        + "Owner text, "
                        + "Model text, "
                        + "Bill text );" );

        this.session.execute(
                "CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.carTotalData
                + "( gosnumber text PRIMARY KEY," +
                        "cameraImage text," +
                        "violationList list< frozen<" + this.violationListType + "> >," +
                        "object text );" );

        this.session.execute(
                "CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.notification +
                "( id uuid, taskId text, type text," +
                " latitudeOfTask double, wasRead boolean, longitudeOfTask double," +
                " notificationWasCreated timestamp, status text, taskTypes text," +
                " title text, address text, carNumber text, nsfOfPatrul text, passportSeries text," +
                        "policeType text, PRIMARY KEY( (id), notificationWasCreated ) );" );

        this.logger.info( "Cassandra is ready" ); }

    public Boolean addValue ( CarTotalData carTotalData ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.carTotalData
            + "( gosnumber, cameraImage, violationList, object ) VALUES('"
            + carTotalData.getGosNumber() + "', '"
            + carTotalData.getCameraImage() + "', "
            + carTotalData.getViolationsList().getViolationsInformationsList() + ", '"
            + SerDes.getSerDes().serialize( carTotalData ) + "');" ).isDone(); }

    public Mono< CardDetails > getWarningCarDetails ( String gosnumber ) { return Mono.just(
            new CardDetails(
                    SerDes.getSerDes().deserializeCarTotalData(
                            this.session
                                    .execute(
                                            "select * FROM "
                                                    + this.dbName + "." + this.carTotalData
                                                    + " WHERE gosnumber = '" + gosnumber + "';"
                                    ).one().getString( "object" ) ) ) ); }

    public List< ViolationsInformation > getViolationsInformationList ( String gosnumber ) { return this.session
            .execute(
                    "SELECT * FROM "
                    + this.dbName + "." + this.carTotalData
                     + " WHERE gosnumber = '" + gosnumber + "';"
            ).one().getList( "violationList", ViolationsInformation.class ); }

    public Boolean addValue ( EventCar eventCar ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.eventCar
            + "( id, camera, matched, date, confidence, object ) VALUES('"
            + eventCar.getId() + "', "
            + eventCar.getCamera() + ", "
            + eventCar.getMatched() + ", '"
            + eventCar.getCreated_date().toInstant() + "', "
            + eventCar.getConfidence() + ", '"
            + SerDes.getSerDes().serialize( eventCar ) + "');" ).isDone(); }

    public Boolean addValue ( EventFace eventFace ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.eventFace
            + "( id, camera, matched, date, confidence, object ) VALUES('"
            + eventFace.getId() + "', "
            + eventFace.getCamera() + ", "
            + eventFace.getMatched() + ", '"
            + eventFace.getCreated_date().toInstant() + "', "
            + eventFace.getConfidence() + ", '"
            + SerDes.getSerDes().serialize( eventFace ) + "');" ).isDone(); }

    public Boolean addValue ( EventBody eventBody ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.eventBody
            + "( id, camera, matched, date, confidence, object ) VALUES('"
            + eventBody.getId() + "', "
            + eventBody.getCamera() + ", "
            + eventBody.getMatched() + ", '"
            + eventBody.getCreated_date().toInstant() + "', "
            + eventBody.getConfidence() + ", '"
            + SerDes.getSerDes().serialize( eventBody ) + "');" ).isDone(); }

    public Boolean addValue ( PolygonType polygonType ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.polygonType + "(id, polygonType) VALUES('"
            + polygonType.getUuid() + "', '" + polygonType.getName() + "');" ).isDone(); }

    public ResultSetFuture addValue ( Polygon polygon ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.polygon + "(id, polygonName, polygonType) " +
            "VALUES (" + polygon.getUuid() + ", '" + polygon.getName() + "', '" + polygon.getPolygonType() + "');" ); }

    public String addValue ( PoliceType policeType, String value ) {
        this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.policeTypes + "(id, policeType) "
                + "VALUES (" + policeType.getUuid() + ", '" + value + "');" );
        return value; }

    public Boolean addValue ( ReqCar reqCar, String key ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.car
            + "(uuid, trackersId, gosNumber, object) VALUES ("
            + reqCar.getUuid() + ", '"
            + reqCar.getTrackerId() + "', '"
            + reqCar.getGosNumber() + "', '"
            + key + "');" ).isDone(); }

    public Boolean addValue ( SelfEmploymentTask selfEmploymentTask, String key ) { return this.session
            .executeAsync( "INSERT INTO "
            + this.dbName + "." + this.selfEmployment
            + "(id, object) VALUES("
            + selfEmploymentTask.getUuid()
            + ", '" + key + "');" ).isDone(); }

    public Boolean addValue ( Patrul patrul, String key ) {
        this.session.executeAsync( "CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.patrols + patrul.getPassportNumber()
                + "(date timestamp PRIMARY KEY, status text, message text, totalActivityTime double );" ); // creating new journal for new patrul
        return this.session.execute( "INSERT INTO "
                + this.dbName + "." + this.patrols + "(passportNumber, NSF, object) VALUES('"
                + patrul.getPassportNumber() + "', '" + patrul.getSurnameNameFatherName() + "', '" + key + "');" ).wasApplied(); }

    public ResultSetFuture addValue ( Polygon polygon, String object ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.polygonForPatrul
            + "(id, object) VALUES ("
            + polygon.getUuid() + ", '" + object + "');" ); }

    public ResultSetFuture addValue ( AtlasLustra atlasLustra, String key ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.lustre
            + "(id, object) " + "VALUES ('"
            + atlasLustra.getUUID() + "', " + key + ");" ); }

    public ResultSetFuture addValue ( FaceEvents polygon ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.facePerson
            + "(id, object) " + "VALUES ('"
            + polygon.getId() + "', '"
            + SerDes.getSerDes().serialize( polygon ) + "');" ); }

    public ResultSetFuture addValue ( CarEvents polygon ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.faceCar
            + "(id, object) " + "VALUES ('"
            + polygon.getId() + "', '"
            + SerDes.getSerDes().serialize( polygon ) + "');" ); }

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

    public Mono< PatrulActivityStatistics > getPatrulStatistics ( Request request ) { return RedisDataControl.getRedis()
            .getPatrul( request.getData() )
            .flatMap( patrul -> request.getSubject() == null && request.getSubject() == null ?
                Mono.just( new PatrulActivityStatistics( patrul, Flux.fromStream( this.session.execute( "SELECT * FROM "
                        + this.dbName + "."
                        + this.patrols + patrul.getPassportNumber() ).all().stream() ) ) )
                : Mono.just( new PatrulActivityStatistics( patrul, Flux.fromStream( this.session.execute( "SELECT * FROM "
                    + this.dbName + "." + this.patrols + patrul.getPassportNumber() + " WHERE date >= '"
                    + SerDes.getSerDes().convertDate( request.getObject().toString() ).toInstant()
                    + "' and date <= '" + SerDes.getSerDes().convertDate( request.getSubject().toString() ).toInstant() + "';" )
                    .all().stream() ) ) ) ); }

    public Flux< Row > getPatruls ( String param ) { return Flux.fromStream( this.session
            .execute( "SELECT nsf FROM TABLETS.patruls WHERE nsf LIKE '%" + param  + "%';" )
            .all().stream() ); }

    public void resetData () {
        Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.selfEmployment + ";" ).all().stream() )
                .map( row -> SerDes.getSerDes().deserializeSelfEmployment( row.getString( "object" ) ) )
                .doOnError( throwable -> this.delete() )
                .filter( selfEmploymentTask -> selfEmploymentTask.getPatruls().size() > 0
                        && selfEmploymentTask.getTaskStatus().compareTo( Status.FINISHED ) != 0 )
                .delayElements( Duration.ofMillis( 100 ) )
                .mapNotNull( selfEmploymentTask -> Archive.getAchieve().getSelfEmploymentTaskMap()
                        .putIfAbsent( selfEmploymentTask.getUuid(), selfEmploymentTask ) ).subscribe();

        Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.eventBody + ";" ).all().stream() )
                .map( row -> SerDes.getSerDes().deserializeEventBody( row.getString( "object" ) ) )
                .doOnError( throwable -> this.delete() )
                .filter( eventBody -> eventBody.getPatruls().size() > 0 && eventBody.getStatus().compareTo( Status.FINISHED ) != 0 )
                .delayElements( Duration.ofMillis( 100 ) )
                .mapNotNull( eventBody1 -> Archive.getAchieve().getEventBodyMap()
                        .putIfAbsent( eventBody1.getId(), eventBody1 ) ).subscribe();

        Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.eventFace + ";" ).all().stream() )
                .map( row -> SerDes.getSerDes().deserializeEventFace( row.getString( "object" ) ) )
                .doOnError( throwable -> this.delete() )
                .filter( eventFace -> eventFace.getPatruls().size() > 0 && eventFace.getStatus().compareTo( Status.FINISHED ) != 0 )
                .delayElements( Duration.ofMillis( 100 ) )
                .mapNotNull( eventFace1 -> Archive.getAchieve().getEventFaceMap()
                        .putIfAbsent( eventFace1.getId(), eventFace1 ) ).subscribe();

        Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.eventCar + ";" ).all().stream() )
                .map( row -> SerDes.getSerDes().deserializeEventCar( row.getString( "object" ) ) )
                .doOnError( throwable -> this.delete() )
                .filter( eventCar -> eventCar.getPatruls().size() > 0 && eventCar.getStatus().compareTo( Status.FINISHED ) != 0 )
                .delayElements( Duration.ofMillis( 100 ) )
                .mapNotNull( eventCar1 -> Archive.getAchieve().getEventCarMap()
                        .putIfAbsent( eventCar1.getId(), eventCar1 ) ).subscribe();

        Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.facePerson + ";" ).all().stream() )
                .map( row -> SerDes.getSerDes().deserializeFaceEvents( row.getString( "object" ) ) )
                .doOnError( throwable -> this.delete() )
                .filter( eventFace -> eventFace.getPatruls().size() > 0 && eventFace.getStatus().compareTo( Status.FINISHED ) != 0 )
                .delayElements( Duration.ofMillis( 100 ) )
                .mapNotNull( eventFace1 -> Archive.getAchieve().getFaceEvents()
                        .putIfAbsent( eventFace1.getId(), eventFace1 ) ).subscribe();

        Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.faceCar + ";" ).all().stream() )
                .map( row -> SerDes.getSerDes().deserializeCarEvents( row.getString( "object" ) ) )
                .doOnError( throwable -> this.delete() )
                .filter( eventFace -> eventFace.getPatruls().size() > 0 && eventFace.getStatus().compareTo( Status.FINISHED ) != 0 )
                .delayElements( Duration.ofMillis( 100 ) )
                .mapNotNull( eventFace1 -> Archive.getAchieve().getCarEvents()
                        .putIfAbsent( eventFace1.getId(), eventFace1 ) ).subscribe(); }

    public void delete () {
        this.session.close();
        this.cluster.close();
        cassandraDataControl = null;
        this.logger.info( "Cassandra is closed!!!" ); }

    public Mono< EventBody > getEventBody ( String id ) { return Mono.just(
            SerDes.getSerDes().deserializeEventBody(
                    this.session.execute(
                            "select * from "
                                    + this.dbName + "." + this.eventBody
                                    + " where id = '" + id + "';"
                    ).one().getString( "object" ) ) ); }

    public Mono< EventCar > getEventCar ( String id ) { return Mono.just(
            SerDes.getSerDes().deserializeEventCar(
                    this.session.execute(
                            "select * from "
                                    + this.dbName + "." + this.eventCar
                                    + " where id = '" + id + "';"
                    ).one().getString( "object" ) ) ); }

    public Mono< EventFace > getEventFace ( String id ) { return Mono.just(
            SerDes.getSerDes().deserializeEventFace(
                    this.session.execute(
                            "select * from "
                                    + this.dbName + "." + this.eventFace
                                    + " where id = '" + id + "';"
                    ).one().getString( "object" ) ) ); }

    public Mono< SelfEmploymentTask > getSelfEmploymentTask ( UUID id ) { return Mono.just(
            SerDes.getSerDes().deserializeSelfEmployment(
                    this.session.execute(
                            "select * from "
                                    + this.dbName + "." + this.selfEmployment
                                    + " where id = " + id + ";"
                    ).one().getString( "object" ) ) ); }

    public Mono< FaceEvents > getFaceEvents ( String id ) { return Mono.just(
            SerDes.getSerDes().deserializeFaceEvents(
                    this.session.execute(
                            "select * from "
                                    + this.dbName + "." + this.facePerson
                                    + " where id = '" + id + "';"
                    ).one().getString( "object" ) ) ); }

    public Mono< CarEvents > getCarEvents ( String id ) { return Mono.just(
            SerDes.getSerDes().deserializeCarEvents(
                    this.session.execute(
                            "select * from "
                                    + this.dbName + "." + this.faceCar
                                    + " where id = '" + id + "';"
                    ).one().getString( "object" ) ) ); }

    public Boolean deleteCar ( String gosNumber ) {
        return this.session.execute( "delete from "
                + this.dbName + "." + this.car
                + " where gosnumber = '" + gosNumber + "';" )
                .wasApplied(); }

    public Boolean deletePatrul ( String passportNumber ) {
        return this.session.execute( "delete from "
                        + this.dbName + this.patrols
                        + " where passportNumber = '" + passportNumber + "';" )
                .wasApplied(); }

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

    public Mono< Patrul > getPatrul ( String id ) { return Mono.just(
            this.session.execute(
                    "SELECT * FROM tablets.patruls where passportNumber = '" + id + "';"
            ).one()
    ).map( row -> SerDes.getSerDes().deserialize( row.getString( "object" ) ) ); }
}
