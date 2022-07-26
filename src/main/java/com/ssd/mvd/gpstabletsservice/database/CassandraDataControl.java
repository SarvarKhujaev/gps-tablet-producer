package com.ssd.mvd.gpstabletsservice.database;

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.*;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.response.PatrulActivityStatistics;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
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
    public final String car = "CARS";
    public final String lustre = "LUSTRA";
    public final String patrols = "PATRULS"; // for table with Patruls info
    public final String polygon = "POLYGON";
    private final String dbName = "TABLETS";
    public final String eventCar = "eventCar";
    public final String eventFace = "eventFace";
    public final String eventBody = "eventBody";
    public final String policeTypes = "POLICETYPES";
    public final String polygonType = "POLYGONTYPE";
    public final String selfEmployment = "SELFEMPLOYMENT";
    public final String polygonForPatrul = "POLYGONFORPATRUl";
    private final String findFaceTaskFromAssomidin = "findFaceTaskFromAssomidin";
    private final String findFaceTaskFromShamsiddin = "findFaceTaskFromShamsiddin";
    private static CassandraDataControl cassandraDataControl = new CassandraDataControl();
    private final Logger logger = Logger.getLogger( CassandraDataControl.class.toString() );
    public static CassandraDataControl getInstance() { return cassandraDataControl != null ? cassandraDataControl : ( cassandraDataControl = new CassandraDataControl() ); }

    private CassandraDataControl () { ( this.session = ( this.cluster = Cluster.builder()
            .withPort( Integer.parseInt( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.CASSANDRA_PORT" ) ) )
            .addContactPoint( GpsTabletsServiceApplication.context.getEnvironment().getProperty( "variables.CASSANDRA_HOST" ) )
            .withProtocolVersion( ProtocolVersion.V4 ).withRetryPolicy( DefaultRetryPolicy.INSTANCE )
            .withSocketOptions( new SocketOptions().setReadTimeoutMillis( 30000 ) )
            .withLoadBalancingPolicy( new TokenAwarePolicy( DCAwareRoundRobinPolicy.builder().build() ) )
            .withPoolingOptions( new PoolingOptions().setMaxConnectionsPerHost( HostDistance.LOCAL, 1024 )
                    .setMaxRequestsPerConnection( HostDistance.REMOTE, 16 ).setPoolTimeoutMillis( 60000 ) ).build() ).connect() )
            .execute( "CREATE KEYSPACE IF NOT EXISTS " + this.dbName + " WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor':1 };" );
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.patrols + "(passportNumber text, NSF text, object text, PRIMARY KEY( (passportNumber), NSF ) );" ); // the table for patruls
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

        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.eventFace + "(id text, camera int, matched boolean, date timestamp, confidence double, object text, PRIMARY KEY( (id), date ) );" ); // the table for polygons
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.eventBody + "(id text, camera int, matched boolean, date timestamp, confidence double, object text, PRIMARY KEY( (id), date ) );" ); // the table for polygons
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.eventCar + "(id text, camera int, matched boolean, date timestamp, confidence double, object text, PRIMARY KEY( (id), date ) );" ); // the table for polygons
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.polygon + "(id uuid PRIMARY KEY, polygonName text, polygonType text);" ); // the table for polygons
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.patrols + "(passportNumber text PRIMARY KEY, NSF text, object text);" ); // the table for patruls
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.polygonForPatrul + "(id uuid PRIMARY KEY, object text);" ); // the table for polygons for patrul
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.polygonType + "(id uuid PRIMARY KEY, polygonType text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.policeTypes + "(id uuid PRIMARY KEY, policeType text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.selfEmployment + "(id uuid PRIMARY KEY, object text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.car + "(gosNumber text PRIMARY KEY, object text);" ); // the table for cars
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.lustre + "(id uuid PRIMARY KEY, object text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + ".trackers(imei text PRIMARY KEY, status text);" ); // the table for trackers
        this.logger.info( "Cassandra is ready" ); }

    public Boolean addValue ( EventFace face ) { return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.eventFace
            + "( id text, camera, matched, date, confidence, object ) VALUES('"
            + face.getId() + "', " + face.getCamera() + ", " + face.getMatched() + ", '" + face.getCreated_date().toInstant() + "', " ).isDone(); }

    public Boolean addValue ( PolygonType polygonType ) { return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.polygonType + "(id, polygonType) VALUES('" + polygonType.getUuid() + "', '" + polygonType.getName() + "');" ).isDone(); }

    public ResultSetFuture addValue ( Polygon polygon ) { return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.polygon + "(id, polygonName, polygonType) " +
            "VALUES (" + polygon.getUuid() + ", '" + polygon.getName() + "', '" + polygon.getPolygonType() + "');" ); }

    public String addValue ( PoliceType policeType, String value ) {
        this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.policeTypes + "(id, policeType) " + "VALUES (" + policeType.getUuid() + ", '" + value + "');" );
        return value; }

    public Boolean addValue ( ReqCar reqCar, String key ) { return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.car + "(gosNumber, object) VALUES ('" + reqCar.getGosNumber() + "', '" + key + "');" ).isDone(); }

    public Boolean addValue ( SelfEmploymentTask selfEmploymentTask, String key ) { return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.selfEmployment + "(id, object) VALUES(" + selfEmploymentTask.getUuid() + ", '" + key + "');" ).isDone(); }

    public Boolean addValue ( Patrul patrul, String key ) { this.session.executeAsync( "CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + "(date timestamp PRIMARY KEY, status text, message text, totalActivityTime double );" ); // creating new journal for new patrul
        return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.patrols + "(passportNumber, NSF, object) VALUES('" + patrul.getPassportNumber() + "', '" + patrul.getSurnameNameFatherName() + "', '" + key + "');" ).isDone(); }

    public ResultSetFuture addValue ( AtlasLustra atlasLustra, String key ) { return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.lustre + "(id, object) " + "VALUES ('" + atlasLustra.getUUID() + "', " + key + ");" ); }

    public ResultSetFuture addValue ( Polygon polygon, String object ) { return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.polygonForPatrul + "(id, object) " + "VALUES (" + polygon.getUuid() + ", '" + object + "');" ); }

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
        // by default it means t o log in to account
        default -> this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + "(date, status, message, totalActivityTime) VALUES ('" + new Date().toInstant() + "', '" + status + "', 'log in at: " + patrul.getStartedToWorkDate().toInstant() + " with simCard " + patrul.getSimCardNumber() + "', " + patrul.getTotalActivityTime() + ");" ).isDone(); }; }

    public Mono< PatrulActivityStatistics > getPatrulStatistics ( Request request ) { return RedisDataControl.getRedis().getPatrul( request.getData() ).flatMap( patrul -> request == null ?
                Mono.just( new PatrulActivityStatistics( patrul, Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.patrols + patrul.getPassportNumber() ).all().stream() ) ) )
                : Mono.just( new PatrulActivityStatistics( patrul, Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + " WHERE date >= '" + SerDes.getSerDes().convertDate( request.getObject().toString() ).toInstant() + "' and date <= '" + SerDes.getSerDes().convertDate( request.getSubject().toString() ).toInstant() + "';" ).all().stream() ) ) ) ); }

    public Flux< Row > getPatruls ( String param ) { return Flux.fromStream( this.session.execute( "SELECT nsf FROM TABLETS.patruls WHERE nsf LIKE '%" + param  + "%';" ).all().stream() ); }

    public void resetData () { Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.selfEmployment + ";" ).all().stream() )
            .map( row -> SerDes.getSerDes().deserializeSelfEmployment( row.getString( "object" ) ) )
            .doOnError( throwable -> this.delete() )
            .filter( selfEmploymentTask -> selfEmploymentTask.getPatruls().size() > 0 && selfEmploymentTask.getTaskStatus().compareTo( Status.FINISHED ) != 0 )
            .delayElements( Duration.ofMillis( 100 ) )
            .mapNotNull( selfEmploymentTask -> Archive.getAchieve().getSelfEmploymentTaskMap().putIfAbsent( selfEmploymentTask.getUuid(), selfEmploymentTask ) ).subscribe(); }

    public void delete () {
        this.session.close();
        this.cluster.close();
        cassandraDataControl = null;
        this.logger.info( "Cassandra is closed!!!" ); }
}
