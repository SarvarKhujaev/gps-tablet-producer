package com.ssd.mvd.gpstabletsservice.database;

import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import com.datastax.driver.core.*;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.response.PatrulActivityStatistics;
import com.ssd.mvd.gpstabletsservice.payload.ReqExchangeLocation;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.request.Request;
import com.ssd.mvd.gpstabletsservice.entity.*;

import reactor.core.publisher.Flux;
import java.util.logging.Logger;
import java.time.Duration;
import java.util.UUID;
import java.util.Date;

public final class CassandraDataControl {
    private final Cluster cluster;
    private final Session session;
    public final String car = "CARS";
    public final String lustre = "LUSTRA";
    public final String patrols = "PATRULS"; // for table with Patruls info
    public final String polygon = "POLYGON";
    private final String dbName = "TABLETS";
    public final String tablets = "tablets";
    public final String policeTypes = "POLICETYPES";
    public final String polygonType = "POLYGONTYPE";
    public final String selfEmployment = "SELFEMPLOYMENT";
    public final String polygonForPatrul = "POLYGONFORPATRUl";
    private static CassandraDataControl cassandraDataControl = new CassandraDataControl();
    private final Logger logger = Logger.getLogger( CassandraDataControl.class.toString() );

    public static CassandraDataControl getInstance() { return cassandraDataControl != null ? cassandraDataControl : ( cassandraDataControl = new CassandraDataControl() ); }

    private CassandraDataControl () { ( this.session = ( this.cluster = Cluster.builder().withPort( 9042 ).addContactPoint( "localhost" ).withProtocolVersion( ProtocolVersion.V4 ).withRetryPolicy( DefaultRetryPolicy.INSTANCE )
                .withSocketOptions( new SocketOptions().setReadTimeoutMillis( 30000 ) ).withLoadBalancingPolicy( new TokenAwarePolicy( DCAwareRoundRobinPolicy.builder().build() ) )
                .withPoolingOptions( new PoolingOptions().setMaxConnectionsPerHost( HostDistance.LOCAL, 1024 ).setMaxRequestsPerConnection( HostDistance.REMOTE, 256 ).setPoolTimeoutMillis( 60000 ) ).build() ).connect() )
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

        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.polygon + "(id uuid PRIMARY KEY, polygonName text, polygonType text);" ); // the table for polygons
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.patrols + "(passportNumber text PRIMARY KEY, NSF text, object text);" ); // the table for patruls
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.polygonForPatrul + "(id uuid PRIMARY KEY, object text);" ); // the table for polygons for patrul
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.polygonType + "(id uuid PRIMARY KEY, polygonType text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.selfEmployment + "(id uuid PRIMARY KEY, object text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.car + "(gosNumber text PRIMARY KEY, object text);" ); // the table for cars
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.policeTypes + "(policeTypes text PRIMARY KEY);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.lustre + "(id uuid PRIMARY KEY, object text);" ); // the table for police types
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + ".trackers(imei text PRIMARY KEY, status text);" ); // the table for trackers
        this.logger.info( "Cassandra is ready" ); }

    public Trackers addValue ( Trackers trackers ) {
        this.session.execute("CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.tablets + trackers.getTopicName() + "(userId text, date timestamp, latitude double, longitude double, PRIMARY KEY( (userId), date ) );");
        this.session.executeAsync( "INSERT INTO " + this.dbName + ".trackers" + "(imei, status) " + "VALUES ('" + trackers.getTopicName()  + "', '" + trackers.getKafkaConsumer().status + "');" );
        return trackers; }

    public Boolean addValue ( PolygonType polygonType ) { return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.polygonType + "(id, polygonType) VALUES('" + polygonType.getUuid() + "', '" + polygonType.getName() + "');" ).isDone(); }

    public ResultSetFuture addValue ( Polygon polygon ) { return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.polygon + "(id, polygonName, polygonType) " +
            "VALUES (" + polygon.getUuid() + ", '" + polygon.getName() + "', '" + polygon.getPolygonType() + "');" ); }

    public PoliceType addValue ( PoliceType policeTypes ) {
        this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.policeTypes + "(policeTypes) " + "VALUES ('" + policeTypes + "');" );
        return policeTypes; }

    public Boolean addValue ( ReqCar reqCar, String key ) { return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.car + "(gosNumber, object) VALUES ('" + reqCar.getGosNumber() + "', '" + key + "');" ).isDone(); }

    public Boolean addValue ( SelfEmploymentTask selfEmploymentTask, String key ) { return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.selfEmployment + "(id, object) VALUES(" + selfEmploymentTask.getUuid() + ", '" + key + "');" ).isDone(); }

    public Boolean addValue ( Patrul patrul, String key ) { this.session.execute( "CREATE TABLE IF NOT EXISTS " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + "(date timestamp, status text, message text, totalActivityTime double, PRIMARY KEY( (date) ));" ); // creating new journal for new patrul
        return this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.patrols + "(passportNumber, NSF, object) VALUES('" + patrul.getPassportNumber() + "', '" + patrul.getSurnameNameFatherName() + "', '" + key + "');" ).isDone(); }

    public void addValue ( ReqExchangeLocation position ) { Archive.getAchieve().save( position ); // checking for existence of the same Tracker in database
        Flux.fromStream( position.getReqLocationExchanges().stream() ).onErrorStop().subscribe( value -> {
            KafkaDataControl.getInstance().writeToKafka( position.getPassport(), value ); // wrint ing all new Data to Kafka
            this.logger.info( "Cassandra got: " + position.getPassport() + "\t" + value.getDate() );
            this.session.executeAsync("INSERT INTO " + this.dbName + "." + this.tablets + position.getPassport() + "(userId, date, latitude, longitude) VALUES ('" + position.getPassportSeries() + "', '" + value.getDate() + "', " + value.getLat() + ", " + value.getLan() + ");"); } ); }

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
        default -> this.session.executeAsync( "INSERT INTO " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + "(date, status, message, totalActivityTime) VALUES ('" + new Date().toInstant() + "', '" + status + "', 'log in at: " + patrul.getStartedToWorkDate().toInstant() + "', " + patrul.getTotalActivityTime() + ");" ).isDone(); }; }

    public PatrulActivityStatistics getPatrulStatistics ( Patrul patrul ) { return new PatrulActivityStatistics( patrul, Flux.fromStream( this.session.execute( "SELECT totalActivityTime FROM " + this.dbName + "." + this.patrols + patrul.getPassportNumber() + " WHERE status=logout;" ).all().stream() ) ); }

    public void delete ( String parameter, String value ) { this.session.execute( "DELETE FROM " + this.dbName + "." + parameter + " WHERE imei='" + value + "';" ); }

    public Flux< Row > getPatruls ( String param ) { return Flux.fromStream( this.session.execute( "SELECT nsf FROM TABLETS.patruls WHERE nsf LIKE '%" + param  + "%';" ).all().stream() ); }

    public Flux< Row > getHistory ( Request request ) { return Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + ".tracker" + UUID.fromString( request.getStartTime() ) + " WHERE userId = '" + UUID.fromString( request.getStartTime() ) + "' AND date >= '" + request.getStartTime() + "' AND date <= '" + request.getEndTime() + "';" ).all().stream() ); }

    public void resetData () { Flux.fromStream( this.session.execute( "SELECT * FROM " + this.dbName + "." + this.selfEmployment + ";" ).all().stream() )
            .map( row -> SerDes.getSerDes().deserializeSelfEmployment( row.getString( "object" ) ) )
            .filter( selfEmploymentTask -> selfEmploymentTask.getPatruls().size() > 0 )
            .delayElements( Duration.ofMillis( 100 ) ).mapNotNull( selfEmploymentTask -> Archive.getAchieve().getSelfEmploymentTaskMap().putIfAbsent( selfEmploymentTask.getUuid(), selfEmploymentTask ) ).subscribe(); }


    public void delete () {
        this.session.close();
        this.cluster.close();
        cassandraDataControl = null;
        this.logger.info( "Cassandra is closed!!!" ); }
}
