package com.ssd.mvd.gpstabletsservice.database;

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.*;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvents;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvents;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.response.PatrulActivityStatistics;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.entity.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;
import java.time.Duration;
import java.util.Date;

public final class CassandraDataControl {
    private final Cluster cluster;
    private final Session session;
    private final String car = "CARS";
    private final String tuple = "TUPLE";
    private final String lustre = "LUSTRA";
    private final String patrols = "PATRULS"; // for table with Patruls info
    private final String polygon = "POLYGON";
    private final String dbName = "TABLETS";
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
    public static CassandraDataControl getInstance() { return cassandraDataControl != null ? cassandraDataControl : ( cassandraDataControl = new CassandraDataControl() ); }

    private CassandraDataControl () {
        SocketOptions options = new SocketOptions();
        options.setConnectTimeoutMillis( 30000 );
        options.setReadTimeoutMillis( 300000 );
        options.setTcpNoDelay( true );
        ( this.session = ( this.cluster = Cluster.builder()
            .withPort( Integer.parseInt( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.CASSANDRA_PORT" ) ) )
                .addContactPoints( "10.254.5.1, 10.254.5.2, 10.254.5.3".split( ", " ) )
            .withProtocolVersion( ProtocolVersion.V4 ).withRetryPolicy( DefaultRetryPolicy.INSTANCE )
            .withQueryOptions( new QueryOptions().setDefaultIdempotence( true ) )
            .withSocketOptions( options )
            .withLoadBalancingPolicy( new TokenAwarePolicy( DCAwareRoundRobinPolicy.builder().build() ) )
            .withPoolingOptions( new PoolingOptions()
                    .setCoreConnectionsPerHost( HostDistance.REMOTE, Integer.parseInt( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.CASSANDRA_CORE_CONN_REMOTE" ) ) )
                    .setCoreConnectionsPerHost( HostDistance.LOCAL, Integer.parseInt( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.CASSANDRA_CORE_CONN_LOCAL" ) ) )
                    .setMaxConnectionsPerHost( HostDistance.REMOTE, Integer.parseInt( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.CASSANDRA_MAX_CONN_REMOTE" ) ) )
                    .setMaxConnectionsPerHost( HostDistance.LOCAL, Integer.parseInt( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.CASSANDRA_MAX_CONN_LOCAL" ) ) )
                    .setMaxRequestsPerConnection( HostDistance.REMOTE, 256 )
                    .setPoolTimeoutMillis( 60000 ) ).build() ).connect() )
            .execute( "CREATE KEYSPACE IF NOT EXISTS " + this.dbName + " WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor':3 };" );
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.patrols +
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

        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.eventFace
                + "(id text, camera int, matched boolean, date timestamp, confidence double, object text, PRIMARY KEY( (id), date ) );" ); // the table for polygons
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.eventBody
                + "(id text, camera int, matched boolean, date timestamp, confidence double, object text, PRIMARY KEY( (id), date ) );" ); // the table for polygons
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.eventCar
                + "(id text, camera int, matched boolean, date timestamp, confidence double, object text, PRIMARY KEY( (id), date ) );" ); // the table for polygons
//        this.session.execute( "CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.tuple
//                + "(id uuid PRIMARY KEY, carList list<text>, country text, object text );" );
//        this.session.execute( "CREATE INDEX IF NOT EXISTS ON " + this.dbName + this.tuple + " (country);" );
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.polygon
                + "(id uuid PRIMARY KEY, polygonName text, polygonType text);" ); // the table for polygons
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.facePerson
                + "(id text PRIMARY KEY, object text);" ); // the table for polygons
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.faceCar
                + "(id text PRIMARY KEY, object text);" ); // the table for polygons
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.patrols
                + "(passportNumber text PRIMARY KEY, NSF text, object text);" ); // the table for patruls
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.polygonForPatrul
                + "(id uuid PRIMARY KEY, object text);" ); // the table for polygons for patrul
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.polygonType
                + "(id uuid PRIMARY KEY, polygonType text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.policeTypes
                + "(id uuid PRIMARY KEY, policeType text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.selfEmployment
                + "(id uuid PRIMARY KEY, object text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.car
                + "(gosNumber text PRIMARY KEY, object text);" ); // the table for cars
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.lustre
                + "(id uuid PRIMARY KEY, object text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName
                + ".trackers(imei text PRIMARY KEY, status text);" ); // the table for trackers
        this.logger.info( "Cassandra is ready" ); }

    public Boolean addValue ( EventFace face ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.eventFace
            + "( id, camera, matched, date, confidence, object ) VALUES('"
            + face.getId() + "', "
            + face.getCamera() + ", "
            + face.getMatched() + ", '"
            + face.getCreated_date().toInstant() + "', "
            + face.getConfidence() + ", '"
            + SerDes.getSerDes().serialize( face ) + "');" ).isDone(); }

    public Boolean addValue ( EventCar face ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.eventCar
            + "( id, camera, matched, date, confidence, object ) VALUES('"
            + face.getId() + "', "
            + face.getCamera() + ", "
            + face.getMatched() + ", '"
            + face.getCreated_date().toInstant() + "', "
            + face.getConfidence() + ", '"
            + SerDes.getSerDes().serialize( face ) + "');" ).isDone(); }

    public Boolean addValue ( EventBody face ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.eventBody
            + "( id, camera, matched, date, confidence, object ) VALUES('"
            + face.getId() + "', "
            + face.getCamera() + ", "
            + face.getMatched() + ", '"
            + face.getCreated_date().toInstant() + "', "
            + face.getConfidence() + ", '"
            + SerDes.getSerDes().serialize( face ) + "');" ).isDone(); }

    public Boolean addValue ( PolygonType polygonType ) { return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.polygonType + "(id, polygonType) VALUES('"
            + polygonType.getUuid() + "', '" + polygonType.getName() + "');" ).isDone(); }

    public ResultSetFuture addValue ( Polygon polygon ) { return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.polygon + "(id, polygonName, polygonType) " +
            "VALUES (" + polygon.getUuid() + ", '" + polygon.getName() + "', '" + polygon.getPolygonType() + "');" ); }

    public String addValue ( PoliceType policeType, String value ) {
        this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.policeTypes + "(id, policeType) " + "VALUES (" + policeType.getUuid() + ", '" + value + "');" );
        return value; }

    public Boolean addValue ( ReqCar reqCar, String key ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.car + "(gosNumber, object) VALUES ('" + reqCar.getGosNumber() + "', '" + key + "');" ).isDone(); }

    public Boolean addValue ( SelfEmploymentTask selfEmploymentTask, String key ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.selfEmployment + "(id, object) VALUES(" + selfEmploymentTask.getUuid() + ", '" + key + "');" ).isDone(); }

    public Boolean addValue ( Patrul patrul, String key ) {
        this.session.executeAsync( "CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.patrols + patrul.getPassportNumber()
                + "(date timestamp PRIMARY KEY, status text, message text, totalActivityTime double );" ); // creating new journal for new patrul
        return this.session.executeAsync( "INSERT INTO "
                + this.dbName + "." + this.patrols + "(passportNumber, NSF, object) VALUES('"
                + patrul.getPassportNumber() + "', '" + patrul.getSurnameNameFatherName() + "', '" + key + "');" ).isDone(); }

    public ResultSetFuture addValue ( AtlasLustra atlasLustra, String key ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.lustre + "(id, object) " + "VALUES ('" + atlasLustra.getUUID() + "', " + key + ");" ); }

    public ResultSetFuture addValue ( Polygon polygon, String object ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.polygonForPatrul + "(id, object) " + "VALUES (" + polygon.getUuid() + ", '" + object + "');" ); }

    public ResultSetFuture addValue ( FaceEvents polygon ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.polygonForPatrul
            + "(id, object) " + "VALUES ('"
            + polygon.getId() + "', '"
            + SerDes.getSerDes().serialize( polygon ) + "');" ); }

    public ResultSetFuture addValue ( CarEvents polygon ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.polygonForPatrul
            + "(id, object) " + "VALUES ('"
            + polygon.getId() + "', '"
            + SerDes.getSerDes().serialize( polygon ) + "');" ); }

    public Boolean login ( Patrul patrul, Status status ) { return switch ( status ) {
        // in case when Patrul wants to leave his account
        case LOGOUT -> this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + "(date, status, message, totalActivityTime) VALUES('" + new Date().toInstant() + "', '" + status + "', 'log out at: " + new Date().toInstant() + "', " + patrul.getTotalActivityTime() + ");" ).isDone();
        case ACCEPTED -> this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + "(date, status, message, totalActivityTime) VALUES('" + new Date().toInstant() + "', '" + status + "', 'accepted new task at: " + new Date().toInstant() + "', " + patrul.getTotalActivityTime() + ");" ).isDone();
        // when Patrul wants to set in pause his work
        case SET_IN_PAUSE -> this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + "(date, status, message, totalActivityTime) VALUES('" + new Date().toInstant() + "', '" + status + "', 'put in pause at: " + new Date().toInstant() + "', " + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses when at the end of the day User finishes his job
        case STOP_TO_WORK -> this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + "(date, status, message, totalActivityTime) VALUES('" + new Date().toInstant() + "', '" + status + "', 'stopped to work at: " + new Date().toInstant() + "', " + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses to when User wants to back to work after pause
        case START_TO_WORK -> this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + "(date, status, message, totalActivityTime) VALUES('" + new Date().toInstant() + "', '" + status + "', 'started to work at: " + new Date().toInstant() + "', " + patrul.getTotalActivityTime() + ");" ).isDone();
        // uses to start to work every day in the morning
        case RETURNED_TO_WORK -> this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + "(date, status, message, totalActivityTime) VALUES('" + new Date().toInstant() + "', '" + status + "', 'returned to work at: " + new Date().toInstant() + "', " + patrul.getTotalActivityTime() + ");" ).isDone();
        case ARRIVED -> this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + "(date, status, message, totalActivityTime) VALUES('" + new Date().toInstant() + "', '" + status + "', 'arrived to given task location at: " + new Date().toInstant() + "', " + patrul.getTotalActivityTime() + ");" ).isDone();
        // by default, it means t o log in to account
        default -> this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + "(date, status, message, totalActivityTime) VALUES ('" + new Date().toInstant() + "', '" + status + "', 'log in at: " + patrul.getStartedToWorkDate().toInstant() + " with simCard " + patrul.getSimCardNumber() + "', " + patrul.getTotalActivityTime() + ");" ).isDone(); }; }

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
            .execute( "SELECT nsf FROM TABLETS.patruls WHERE nsf LIKE '%" + param  + "%';" ).all().stream() ); }

    public void resetData () {
        Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.selfEmployment + ";" ).all().stream() )
                .map( row -> SerDes.getSerDes().deserializeSelfEmployment( row.getString( "object" ) ) )
                .doOnError( throwable -> this.delete() )
                .filter( selfEmploymentTask -> selfEmploymentTask.getPatruls().size() > 0 && selfEmploymentTask.getTaskStatus().compareTo( Status.FINISHED ) != 0 )
                .delayElements( Duration.ofMillis( 100 ) )
                .mapNotNull( selfEmploymentTask -> Archive.getAchieve().getSelfEmploymentTaskMap().putIfAbsent( selfEmploymentTask.getUuid(), selfEmploymentTask ) ).subscribe();

        Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.eventBody + ";" ).all().stream() )
                .map( row -> SerDes.getSerDes().deserializeEventBody( row.getString( "object" ) ) )
                .doOnError( throwable -> this.delete() )
                .filter( eventBody -> eventBody.getPatruls().size() > 0 && eventBody.getStatus().compareTo( Status.FINISHED ) != 0 )
                .delayElements( Duration.ofMillis( 100 ) )
                .mapNotNull( eventBody1 -> Archive.getAchieve().getEventBodyMap().putIfAbsent( eventBody1.getId(), eventBody1 ) ).subscribe();

        Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.eventFace + ";" ).all().stream() )
                .map( row -> SerDes.getSerDes().deserializeEventFace( row.getString( "object" ) ) )
                .doOnError( throwable -> this.delete() )
                .filter( eventFace -> eventFace.getPatruls().size() > 0 && eventFace.getStatus().compareTo( Status.FINISHED ) != 0 )
                .delayElements( Duration.ofMillis( 100 ) )
                .mapNotNull( eventFace1 -> Archive.getAchieve().getEventFaceMap().putIfAbsent( eventFace1.getId(), eventFace1 ) ).subscribe();

        Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.eventCar + ";" ).all().stream() )
                .map( row -> SerDes.getSerDes().deserializeEventCar( row.getString( "object" ) ) )
                .doOnError( throwable -> this.delete() )
                .filter( eventCar -> eventCar.getPatruls().size() > 0 && eventCar.getStatus().compareTo( Status.FINISHED ) != 0 )
                .delayElements( Duration.ofMillis( 100 ) )
                .mapNotNull( eventCar1 -> Archive.getAchieve().getEventCarMap().putIfAbsent( eventCar1.getId(), eventCar1 ) ).subscribe();

        Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.facePerson + ";" ).all().stream() )
                .map( row -> SerDes.getSerDes().deserializeFaceEvents( row.getString( "object" ) ) )
                .doOnError( throwable -> this.delete() )
                .filter( eventFace -> eventFace.getPatruls().size() > 0 && eventFace.getStatus().compareTo( Status.FINISHED ) != 0 )
                .delayElements( Duration.ofMillis( 100 ) )
                .mapNotNull( eventFace1 -> Archive.getAchieve().getFaceEvents().putIfAbsent( eventFace1.getId(), eventFace1 ) ).subscribe();

        Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.faceCar + ";" ).all().stream() )
                .map( row -> SerDes.getSerDes().deserializeCarEvents( row.getString( "object" ) ) )
                .doOnError( throwable -> this.delete() )
                .filter( eventFace -> eventFace.getPatruls().size() > 0 && eventFace.getStatus().compareTo( Status.FINISHED ) != 0 )
                .delayElements( Duration.ofMillis( 100 ) )
                .mapNotNull( eventFace1 -> Archive.getAchieve().getCarEvents().putIfAbsent( eventFace1.getId(), eventFace1 ) ).subscribe(); }

    public void delete () {
        this.session.close();
        this.cluster.close();
        cassandraDataControl = null;
        this.logger.info( "Cassandra is closed!!!" ); }

    public Flux< TupleOfPatrul > getAllTupleOfPatrul () { return Flux.fromStream( this.session.execute(
            "SELECT * FROM "
            + this.dbName + this.tuple + ";" ).all().stream() )
            .map( TupleOfPatrul::new ); }

    public Flux< TupleOfPatrul > getAllTupleOfPatrul ( String id ) { return Flux.fromStream( this.session.execute(
            "SELECT * FROM "
                    + this.dbName + this.tuple
                    + " where id = " + id + ";" ).all().stream() )
            .map( TupleOfPatrul::new ); }

    public Mono< ApiResponseModel > deleteTupleOfPatrul ( String id ) {
        this.session.execute( "DELETE FROM "
                + this.dbName + this.tuple
                + " where id = " + id + ";" );
        return Mono.just( ApiResponseModel.builder()
                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                .message( id + " was successfully deleted" )
                                .code( 200 )
                                .build() )
                        .success( true )
                .build() ); }

    public Flux< ApiResponseModel > addValue ( TupleOfPatrul tupleOfPatrul ) {
        this.session.execute( "INSERT INTO "
                + this.dbName + "." + this.tuple
                + "( id, carList, country, object ) VALUES ("
                + tupleOfPatrul.getPolygon().getUuid() + ", "
                + tupleOfPatrul.getReqCarsList() + ", '"
                + tupleOfPatrul.getCountries().name() + "', '"
                + SerDes.getSerDes().serialize( tupleOfPatrul ) + "');" );
        return Flux.fromStream( tupleOfPatrul.getReqCarsList().stream() )
                .flatMap( reqCar ->
                        RedisDataControl.getRedis().getPatrul( reqCar.getPatrulPassportSeries() )
                                .flatMap( patrul -> RedisDataControl.getRedis().update( TaskInspector.getInstance()
                                        .changeTaskStatus( patrul, Status.ACCEPTED, tupleOfPatrul ) ) ) ); }
}
