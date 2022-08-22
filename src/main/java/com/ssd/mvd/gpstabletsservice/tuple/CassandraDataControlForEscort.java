package com.ssd.mvd.gpstabletsservice.tuple;


import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.database.SerDes;

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
    private final String tupleOfCar = "TUPLE_OF_CAR";
    private final String tupleOfEscort = "TUPLE_OF_ESCORT";
    private final String polygonForEscort = "POLYGON_FOR_ESCORT";

    private final String polygonType = "POLYGON_ENTITY";
    private final String carForEscortType = "CAR_FOR_ESCORT_TYPE";

    private final Logger logger = Logger.getLogger( CassandraDataControl.class.toString() );
    private static CassandraDataControlForEscort cassandraDataControl = new CassandraDataControlForEscort();

    public static CassandraDataControlForEscort getInstance() { return cassandraDataControl != null ? cassandraDataControl
            : ( cassandraDataControl = new CassandraDataControlForEscort() ); }

    private CassandraDataControlForEscort () {
        this.session.execute( "CREATE KEYSPACE IF NOT EXISTS " + this.dbName
                + " WITH REPLICATION = { 'class' : 'SimpleStrategy', 'replication_factor':3 };" );

        this.session.execute("CREATE TYPE IF NOT EXISTS "
                + this.dbName + "." + this.polygonType
                + "( lat double,"
                + "lng double);" );
        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.polygonForEscort
                + "( id uuid PRIMARY KEY, name text, object text);" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.tupleOfEscort
                + "(id uuid PRIMARY KEY, country text, object text );" );

        this.session.execute( "CREATE INDEX IF NOT EXISTS ON "
                + this.dbName + "." + this.tupleOfEscort + " (country);" );

        this.logger.info( "CassandraDataControlForEscort is ready" ); }

    public Flux< EscortTuple > getAllTupleOfEscort() { return Flux.fromStream( this.session
                    .execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.tupleOfEscort + ";" )
                    .all().stream() )
            .map( row -> SerDes.getSerDes().deleteTupleOfPatrul( row.getString( "object" ) ) ); }

    public Flux< EscortTuple > getAllTupleOfEscort ( String id ) { return Flux.fromStream( this.session.execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.tupleOfEscort
                            + " where id = " + UUID.fromString( id ) + ";" ).all().stream() )
            .map( row -> SerDes.getSerDes().deleteTupleOfPatrul( row.getString( "object" ) ) ); }

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

    public Flux< ApiResponseModel > addValue ( EscortTuple escortTuple ) {
        return this.session.execute( "INSERT INTO "
                + this.dbName + "." + this.tupleOfEscort
                + "( id, country, object ) VALUES ("
                + escortTuple.getPolygon().getUuid() + ", '"
                + escortTuple.getCountries().name() + "', '"
                + SerDes.getSerDes().serialize( escortTuple ) + "') IF NOT EXISTS;" )
                .wasApplied() ?
        Flux.just(
                ApiResponseModel.builder()
                        .success( true )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                .message( "Tuple was successfully created" )
                                .code( 200 )
                                .build() )
                        .build()
        )
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
                + "( id, carList, object ) VALUES ("
                + escortTuple.getPolygon().getUuid() + ", '"
                + escortTuple.getCountries().name() + "', '"
                + SerDes.getSerDes().serialize( escortTuple ) + "') IF EXISTS;" )
                .wasApplied() ? Mono.just(
                        ApiResponseModel.builder()
                                .success( true )
                                .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                        .message( escortTuple.getPolygon().getName() + " was updated successfully" )
                                        .code( 200 )
                                        .build() )
                                .build() )
                : Mono.just(
                ApiResponseModel.builder()
                        .success( false )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                .message( escortTuple.getPolygon().getName() + " does not exists" )
                                .code( 201 )
                                .build() )
                        .build() ); }

    public Mono< ApiResponseModel > addValue ( TupleOfCar tupleOfCar ) { return this.session.execute(
                "INSERT INTO " + this.dbName + "." + this.tupleOfCar
                        + "(gosNumber, trackerId, patrul, simCardNumber, carModel, passportNumber, averageFuelConsumption)"
                        + "VALUES('"
                        + tupleOfCar.getGosNumber() + "', '"
                        + tupleOfCar.getTrackerId() + "', '"
                        + tupleOfCar.getNsfOfPatrul() + "', '"
                        + tupleOfCar.getSimCardNumber() + "', '"
                        + tupleOfCar.getCarModel() + "', '"
                        + tupleOfCar.getPassportSeries() + "', "
                        + tupleOfCar.getAverageFuelConsumption() + ") IF NOT EXISTS;" )
                .wasApplied() ? Mono.just(
                    ApiResponseModel.builder()
                            .success( true )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( "New car was saved successfully" )
                                    .code( 200 )
                                    .build() ).build() )
            : Mono.just( ApiResponseModel.builder()
                    .success( false )
                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                            .code( 201 )
                            .message( "This car is already exists" )
                            .build() ).build() ); }

    public Mono< ApiResponseModel > deleteCar ( String gosNumber ) { return Mono.just(
            ApiResponseModel.builder()
                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                            .code( 200 )
                            .message( gosNumber + " was removed successfully" )
                            .build() )
                    .success( this.session.execute (
                            "DELETE FROM "
                                    + this.dbName + "." + this.tupleOfCar
                                    + " where gosNumber = " + gosNumber + ";"
                    ).wasApplied() )
                    .build() ); }

    public Flux< TupleOfCar > getAllTupleOfCar () {
        return Flux.fromStream(
                this.session.execute(
                        "SELECT * FROM " + this.dbName + "." + this.tupleOfCar + ";"
                ).all().stream()
        ).map( TupleOfCar::new ); }

    public Flux< TupleOfCar > getAllTupleOfCar ( String gosNumber ) {
        return Flux.fromStream(
                this.session.execute(
                        "SELECT * FROM " + this.dbName + "." + this.tupleOfCar
                                + " where gosNumber = '" + gosNumber + "';"
                ).all().stream()
        ).map( TupleOfCar::new ); }

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

    public Flux< PolygonForEscort > getAllPolygonForEscort() { return Flux.fromStream(
            this.session.execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.polygonForEscort + ";"
            ).all().stream()
        ).map( row -> SerDes.getSerDes()
            .deserializePolygonForEscort( row.getString( "object" ) ) ); }

    public Mono< ApiResponseModel > updatePolygonForEscort ( PolygonForEscort polygon ) { return this.session.execute(
            "INSERT INTO "
                    + this.dbName + '.' + this.polygonForEscort
                    + "(id, name, object) VALUES ("
                    + polygon.getUuid() + ", '"
                    + polygon.getName() + "', '"
                    + SerDes.getSerDes().serialize( polygon ) + "') IF EXISTS;"
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

    public Mono< ApiResponseModel > addValue ( PolygonForEscort polygon ) {
        return this.session.execute(
            "INSERT INTO "
                    + this.dbName + '.' + this.polygonForEscort
                    + "(id, name, object) VALUES ("
                    + polygon.getUuid() + ", '"
                    + polygon.getName() + "', '"
                    + SerDes.getSerDes().serialize( polygon ) + "') IF NOT EXISTS;"
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

    public Mono< PolygonForEscort > getAllPolygonForEscort ( String id ) {
        Row row = this.session.execute(
                "SELECT * FROM "
                        + this.dbName + "." + this.polygonForEscort
                        + " where id = " + UUID.fromString( id ) + ";" ).one();
        return Mono.justOrEmpty( row != null ? SerDes.getSerDes()
                    .deserializePolygonForEscort( row.getString( "object" ) ) : null ); }
}
