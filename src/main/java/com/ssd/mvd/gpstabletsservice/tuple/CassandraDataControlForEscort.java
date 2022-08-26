package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.database.CassandraConverter;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Row;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;
import java.util.UUID;
import lombok.Data;

@Data
public class CassandraDataControlForEscort {
    private final Session session = CassandraDataControl.getInstance().getSession();
    private final Cluster cluster = CassandraDataControl.getInstance().getCluster();

    private final String dbName = "ESCORT";

    private final String patrols = "PATRULS"; // for table with Patruls info
    private final String tupleOfCar = "TUPLE_OF_CAR";
    private final String tupleOfEscort = "TUPLE_OF_ESCORT";
    private final String polygonForEscort = "POLYGON_FOR_ESCORT_TEST";

    private final String polygonType = "POLYGON_ENTITY";
    private final String carForEscortType = "CAR_FOR_ESCORT_TYPE";

    private final Logger logger = Logger.getLogger( CassandraDataControl.class.toString() );
    private static CassandraDataControlForEscort cassandraDataControl = new CassandraDataControlForEscort();

    public static CassandraDataControlForEscort getInstance() { return cassandraDataControl != null ? cassandraDataControl
            : ( cassandraDataControl = new CassandraDataControlForEscort() ); }

    private CassandraDataControlForEscort () {
        this.session.execute( "CREATE KEYSPACE IF NOT EXISTS "
                + this.dbName +
                " WITH REPLICATION = {" +
                "'class' : 'NetworkTopologyStrategy'," +
                "'datacenter1':3 }" +
                "AND DURABLE_WRITES = false;" );

        this.session.execute("CREATE TYPE IF NOT EXISTS "
                + this.dbName + "." + this.polygonType
                + "( lat double,"
                + "lng double );" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.polygonForEscort
                + "( id uuid PRIMARY KEY, " +
                "name text, " +
                "latlngs list< frozen < "
                + this.polygonType + " > > );" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.tupleOfEscort +
                " ( id uuid PRIMARY KEY," +
                " countries text," +
                " uuidOfPolygon uuid," +
                " tupleOfCarsList list< uuid >," +
                " patrulList list< uuid > );" );

        CassandraConverter
                .getInstance()
                .registerCodecForPolygonEntity( this.dbName, this.getPolygonType() );

        this.logger.info( "CassandraDataControlForEscort is ready" ); }

    public Flux< EscortTuple > getAllTupleOfEscort () { return Flux.fromStream(
            this.session
                    .execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.tupleOfEscort + ";" )
                    .all().stream() )
            .map( EscortTuple::new ); }

    public Flux< EscortTuple > getAllTupleOfEscort ( String id ) { return Flux.fromStream(
            this.session.execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.tupleOfEscort
                            + " where id = " + UUID.fromString( id ) + ";" ).all().stream() )
            .map( EscortTuple::new ); }

    public Mono< ApiResponseModel > deleteTupleOfPatrul ( String id ) {
        this.session.execute( "DELETE FROM "
                + this.dbName + "." + this.tupleOfEscort
                + " where id = " + UUID.fromString( id ) + ";" );
        return Mono.just( ApiResponseModel.builder()
                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                        .message( id + " was successfully deleted" )
                        .code( 200 )
                        .build() )
                .success( true )
                .build() ); }

    private static Integer i;

    public Flux< ApiResponseModel > addValue ( EscortTuple escortTuple ) {
        i = 0;
        escortTuple.getPatrulList().forEach( uuid -> {
            this.session.executeAsync(
                    "UPDATE "
                    + this.dbName + "." + this.getPatrols()
                    + " SET uuidOfEscort = " + escortTuple.getUuid() +", " +
                            " uuidForEscortCar = " + escortTuple.getTupleOfCarsList().get( i++ )
                    + " where uuid = " + uuid + ";" );

            this.session.executeAsync(
                    "UPDATE "
                    + this.dbName + "." + this.getTupleOfCar() +
                    " SET uuidOfEscort = " + escortTuple.getUuid() +", " +
                    "uuidOfPatrul = " + uuid
                    + " where uuid = " + uuid + ";" ); } );

        return this.session.execute( "INSERT INTO "
                + this.dbName + "." + this.tupleOfEscort
                + "( id," +
                        " countries," +
                        " uuidOfPolygon," +
                        " tupleOfCarsList," +
                        " patrulList ) VALUES ("
                + escortTuple.getUuid() + ", '"
                + escortTuple.getCountries().name() + "', "
                + escortTuple.getUuidOfPolygon() + ", "
                + CassandraConverter
                        .getInstance()
                        .convertListToCassandra( escortTuple.getTupleOfCarsList() ) + ", "
                + CassandraConverter
                        .getInstance()
                        .convertListToCassandra( escortTuple.getPatrulList() )
                        + " ) IF NOT EXISTS;" )
                .wasApplied() ?
                Flux.just(
                        ApiResponseModel.builder()
                                .success( true )
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( "Tuple was successfully created" )
                                        .code( 200 )
                                        .build() )
                                .build() )
                        : Flux.just(
                                ApiResponseModel.builder()
                                        .success( false )
                                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                                .message( "Such a tuple has already been created" )
                                                .code( 201 )
                                                .build() )
                                        .build() ); }

    public Mono< ApiResponseModel > update ( EscortTuple escortTuple ) {
        return this.session.execute( "INSERT INTO "
                + this.dbName + "." + this.tupleOfEscort
                        + "( id," +
                        " countries," +
                        " uuidOfPolygon," +
                        " tupleOfCarsList," +
                        " patrulList ) VALUES ("
                        + escortTuple.getUuid() + ", '"
                        + escortTuple.getCountries().name() + "', "
                        + escortTuple.getUuidOfPolygon() + ", "
                        + CassandraConverter
                            .getInstance()
                            .convertListToCassandra( escortTuple.getTupleOfCarsList() ) + ", "
                        + CassandraConverter
                            .getInstance()
                            .convertListToCassandra( escortTuple.getPatrulList() )
                        + " ) IF EXISTS;" )
                .wasApplied() ? Mono.just(
                        ApiResponseModel.builder()
                                .success( true )
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( escortTuple.getUuid() + " was updated successfully" )
                                        .code( 200 )
                                        .build() )
                                .build() )
                : Mono.just(
                ApiResponseModel.builder()
                        .success( false )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                .message( escortTuple.getUuid() + " does not exists" )
                                .code( 201 )
                                .build() )
                        .build() ); }

    public Mono< ApiResponseModel > delete ( String id ) {
        return Mono.just(
                ApiResponseModel.builder()
                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                .code( 200 )
                                .message( id + " was removed successfully" )
                                .build() )
                        .success( this.session.execute(
                                "DELETE FROM "
                                        + this.dbName + "." + this.polygonForEscort
                                        + " where id = " + UUID.fromString( id ) + ";"
                        ).wasApplied() )
                        .build() ); }

    public Flux< PolygonForEscort > getAllPolygonForEscort () { return Flux.fromStream(
            this.session.execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.polygonForEscort + ";"
            ).all().stream()
        ).map( PolygonForEscort::new ); }

    public Mono< PolygonForEscort > getAllPolygonForEscort ( String id ) {
        Row row = this.session.execute(
                "SELECT * FROM "
                        + this.dbName + "." + this.polygonForEscort
                        + " where id = " + UUID.fromString( id ) + ";" ).one();
        return Mono.justOrEmpty( row != null ? new PolygonForEscort( row ) : null ); }

    public Mono< ApiResponseModel > addValue ( PolygonForEscort polygon ) {
        return this.session.execute(
            "INSERT INTO "
                    + this.dbName + '.' + this.polygonForEscort
                    + "(id, name, latlngs) VALUES ("
                    + polygon.getUuid() + ", '"
                    + polygon.getName() + "', "
                    + CassandraConverter
                    .getInstance()
                    .convertListOfPolygonEntityToCassandra( polygon.getLatlngs() )
                    + ") IF NOT EXISTS;"
            ).wasApplied() ? Mono.just( ApiResponseModel.builder()
                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                            .code( 200 )
                            .message( "Polygon was saved" )
                            .build() )
                    .success( true )
                    .build() )
                    : Mono.just( ApiResponseModel.builder()
                    .success( false )
                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                            .code( 201 )
                            .message( "This polygon has already been saved" )
                            .build() )
                    .build() ); }

    public Mono< ApiResponseModel > update ( PolygonForEscort polygon ) { return this.session.execute(
            "INSERT INTO "
                    + this.dbName + '.' + this.polygonForEscort
                    + "(id, name, object) VALUES ("
                    + polygon.getUuid() + ", '"
                    + polygon.getName() + "', "
                    + CassandraConverter
                    .getInstance()
                    .convertListOfPolygonEntityToCassandra( polygon.getLatlngs() )
                    + ") IF EXISTS;"
    ).wasApplied() ? Mono.just( ApiResponseModel.builder()
            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                    .code( 200 )
                    .message( "Polygon was saved" )
                    .build() )
            .success( true )
            .build() )
            : Mono.just( ApiResponseModel.builder()
            .success( false )
            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                    .code( 201 )
                    .message( "This polygon has already been saved" )
                    .build() )
            .build() ); }
}
