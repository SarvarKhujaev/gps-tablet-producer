package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.database.CassandraConverter;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.TaskInspector;
import com.ssd.mvd.gpstabletsservice.response.Status;

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

    private final String carForEscortType = "CAR_FOR_ESCORT_TYPE";

    private final String pointsList = "POINTS_ENTITY";
    private final String polygonEntity = "POLYGON_ENTITY";
    private final String polygonForEscort = "POLYGON_FOR_ESCORT";

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
                + this.dbName + "." + this.getPointsList()
                + "( lat double, "
                + "lng double, " +
                "pointId uuid, " +
                "pointName text );" );

        this.session.execute("CREATE TYPE IF NOT EXISTS "
                + this.dbName + "." + this.getPolygonEntity()
                + "( lat double,"
                + "lng double );" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.getPolygonForEscort()
                + "( uuid uuid PRIMARY KEY, " +
                "name text, " +
                "totalTime int, " +
                "totalDistance int, " +
                "pointsList list< frozen < " + this.getPointsList() + " > >, " +
                "latlngs list< frozen < " + this.getPolygonEntity() + " > > );" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + this.dbName + "." + this.getTupleOfEscort() +
                " ( uuid uuid PRIMARY KEY, " +
                " countries text, " +
                " uuidOfPolygon uuid, " +
                " tupleOfCarsList list< uuid >," +
                " patrulList list< uuid > );" );

        CassandraConverter
                .getInstance()
                .registerCodecForPolygonEntity( this.dbName, this.getPolygonEntity() );

        CassandraConverter
                .getInstance()
                .registerCodecForPointsList( this.dbName, this.getPointsList() );

        this.logger.info( "CassandraDataControlForEscort is ready" ); }

    public Flux< EscortTuple > getAllTupleOfEscort () { return Flux.fromStream(
            this.session
                    .execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.tupleOfEscort + ";" )
                    .all().stream() )
            .map( EscortTuple::new ); }

    public Mono< EscortTuple > getAllTupleOfEscort ( String id ) {
        Row row = this.session.execute(
                "SELECT * FROM "
                        + this.dbName + "." + this.getTupleOfEscort()
                        + " where id = " + UUID.fromString( id ) + ";" ).one();
        return Mono.justOrEmpty( row != null ? new EscortTuple( row ) : null ); }

    public Mono< ApiResponseModel > deleteTupleOfPatrul ( String id ) {
        return this.getAllTupleOfEscort( id )
                .flatMap( escortTuple -> {
                    if ( escortTuple.getPatrulList() != null
                            && !escortTuple.getPatrulList().isEmpty()
                            && escortTuple.getPatrulList().size() > 0 ) escortTuple.getPatrulList()
                            .forEach( uuid -> this.session.execute(
                                    "UPDATE tablets.patruls SET uuidOfEscort = " + null
                                    + ", uuidForEscortCar = " + null + " where uuid = " + uuid + ";" ) );

                    if ( escortTuple.getTupleOfCarsList() != null
                            && !escortTuple.getTupleOfCarsList().isEmpty()
                            && escortTuple.getTupleOfCarsList().size() > 0 ) escortTuple.getTupleOfCarsList()
                            .forEach( uuid -> this.session.execute(
                                    "UPDATE escort.tuple_of_car SET uuidOfEscort = " + null
                                            + ", uuidOfPatrul = " + null + " where uuid = " + uuid + ";" ) );

                    return Mono.just( ApiResponseModel.builder()
                                    .success(
                                            this.session.execute( "DELETE FROM "
                                                    + this.dbName + "." + this.tupleOfEscort
                                                    + " where id = " + UUID.fromString( id ) + ";" ).wasApplied() )
                            .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                    .message( id + " was successfully deleted" )
                                    .code( 200 )
                                    .build() )
                            .success( true )
                            .build() ); } ); }

    private static Integer i;

    private void linkPatrulWithEscortCar ( EscortTuple escortTuple ) {
        for ( i = 0; i < escortTuple.getPatrulList().size(); i++ ) CassandraDataControl
                .getInstance()
                .getPatrul( escortTuple.getPatrulList().get( i ) )
                .subscribe( patrul -> {
                    patrul.setUuidOfEscort( escortTuple.getUuid() );
                    patrul.setUuidForEscortCar( escortTuple.getTupleOfCarsList().get( i ) );
                    CassandraDataControlForEscort
                            .getInstance()
                            .getAllTupleOfCar( patrul.getUuidForEscortCar() )
                            .subscribe( tupleOfCar -> {
                                tupleOfCar.setUuidOfPatrul( patrul.getUuid() );
                                tupleOfCar.setUuidOfEscort( escortTuple.getUuid() );
                                CassandraDataControlForEscort
                                        .getInstance()
                                        .update( tupleOfCar )
                                        .subscribe(); } );
                    TaskInspector
                            .getInstance()
                            .changeTaskStatus( patrul,
                                    com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                    escortTuple ); } ); }

    public Flux< ApiResponseModel > addValue ( EscortTuple escortTuple ) {
        this.linkPatrulWithEscortCar( escortTuple );
        return this.session.execute( "INSERT INTO "
                + this.dbName + "." + this.getTupleOfEscort()
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
        this.linkPatrulWithEscortCar( escortTuple );
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
                        + " );" )
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
        return Mono.just( ApiResponseModel.builder()
                        .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                                .code( 200 )
                                .message( id + " was removed successfully" )
                                .build() )
                        .success( this.session.execute(
                                "DELETE FROM "
                                        + this.dbName + "." + this.polygonForEscort
                                        + " where uuid = " + UUID.fromString( id ) + ";"
                        ).wasApplied() )
                        .build() ); }

    public Flux< PolygonForEscort > getAllPolygonForEscort () { return Flux.fromStream(
            this.session.execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.getPolygonForEscort() + ";"
            ).all().stream()
        ).map( PolygonForEscort::new ); }

    public Mono< PolygonForEscort > getAllPolygonForEscort ( String id ) {
        Row row = this.session.execute(
                "SELECT * FROM "
                        + this.dbName + "." + this.getPolygonForEscort()
                        + " where uuid = " + UUID.fromString( id ) + ";" ).one();
        return Mono.justOrEmpty( row != null ? new PolygonForEscort( row ) : null ); }

    public Mono< ApiResponseModel > addValue ( PolygonForEscort polygon ) {
        return this.session.execute(
            "INSERT INTO "
                    + this.dbName + '.' + this.getPolygonForEscort()
                    + "(uuid, " +
                    "name, " +
                    "totalTime, " +
                    "totalDistance, " +
                    "pointsList, " +
                    "latlngs ) VALUES ("
                    + polygon.getUuid() + ", '"
                    + polygon.getName() + "', "

                    + polygon.getTotalTime() + ", "
                    + polygon.getTotalDistance() + ", "

                    + CassandraConverter
                    .getInstance()
                    .convertListOfPointsToCassandra( polygon.getPointsList() ) + ", "

                    + CassandraConverter
                    .getInstance()
                    .convertListOfPointsToCassandra( polygon.getLatlngs() )
                    + ") IF NOT EXISTS;"
            ).wasApplied() ? Mono.just( ApiResponseModel
                .builder()
                .data( com.ssd.mvd.gpstabletsservice.entity.Data
                        .builder()
                        .type( polygon.getUuid().toString() )
                        .build() )
                .status( com.ssd.mvd.gpstabletsservice.response.Status
                        .builder()
                        .code( 200 )
                        .message( "Polygon was saved" )
                        .build() )
                .success( true )
                .build() )
                    : Mono.just( ApiResponseModel
                        .builder()
                        .success( false )
                        .status( com.ssd.mvd.gpstabletsservice.response.Status
                                .builder()
                                .code( 201 )
                                .message( "This polygon has already been saved" )
                                .build() )
                        .build() ); }

    public Mono< ApiResponseModel > update ( PolygonForEscort polygon ) { return this.session.execute(
            "INSERT INTO "
                    + this.dbName + '.' + this.getPolygonForEscort()
                    + "(uuid, " +
                    "name, " +
                    "totalTime, " +
                    "totalDistance, " +
                    "pointsList, " +
                    "latlngs ) VALUES ("
                    + polygon.getUuid() + ", '"
                    + polygon.getName() + "', "

                    + polygon.getTotalTime() + ", "
                    + polygon.getTotalDistance() + ", "

                    + CassandraConverter
                    .getInstance()
                    .convertListOfPointsToCassandra( polygon.getPointsList() ) + ", "

                    + CassandraConverter
                    .getInstance()
                    .convertListOfPointsToCassandra( polygon.getLatlngs() ) + ");"
        ).wasApplied() ? Mono.just( ApiResponseModel
            .builder()
            .data( com.ssd.mvd.gpstabletsservice.entity.Data
                    .builder()
                    .type( polygon.getUuid().toString() )
                    .build() )
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

    public Mono< ApiResponseModel > update ( TupleOfCar tupleOfCar ) { return this.session.execute(
            "INSERT INTO "
                    + this.dbName + "." + this.getTupleOfCar()
                    + CassandraConverter
                    .getInstance()
                    .getALlNames( TupleOfCar.class )
                    + " VALUES("
                    + tupleOfCar.getUuid() + ", "
                    + tupleOfCar.getUuidOfEscort() + ", "
                    + tupleOfCar.getUuidOfPatrul() + ", '"
                    + tupleOfCar.getCarModel() + "', '"
                    + tupleOfCar.getGosNumber() + "', '"
                    + tupleOfCar.getTrackerId() + "', '"
                    + tupleOfCar.getNsfOfPatrul() + "', '"
                    + tupleOfCar.getSimCardNumber() + "', "

                    + tupleOfCar.getLatitude() + ", "
                    + tupleOfCar.getLongitude() + ", " +
                    tupleOfCar.getAverageFuelConsumption() + ") "
            ).wasApplied() ? Mono.just (
                    ApiResponseModel.builder()
                            .success( true )
                            .status( Status.builder()
                                    .message( "Car" + tupleOfCar.getGosNumber() + " was updated successfully" )
                                    .code( 200 )
                                    .build() ).build() )
                    : Mono.just( ApiResponseModel.builder()
                    .success( false )
                    .status( Status.builder()
                            .code( 201 )
                            .message( "This car does not exists" )
                            .build() ).build() ); }

    public Mono< TupleTotalData > getTupleTotalData ( String uuid ) {
        TupleTotalData tupleTotalData = new TupleTotalData();
        return this.getAllTupleOfEscort( uuid )
                .flatMap( escortTuple -> {
                    if ( escortTuple.getUuidOfPolygon() != null )
                        this.getAllPolygonForEscort( escortTuple.getUuidOfPolygon().toString() )
                                .subscribe( tupleTotalData::setPolygonForEscort );
                    escortTuple.getPatrulList().forEach( uuid1 -> CassandraDataControl
                            .getInstance()
                            .getPatrul( uuid1 )
                            .subscribe( patrul -> tupleTotalData.getPatrulList().add( patrul ) ) );
                    escortTuple.getTupleOfCarsList().forEach( uuid1 -> CassandraDataControlForEscort
                            .getInstance()
                            .getAllTupleOfCar( uuid1 )
                            .subscribe( escortTuple1 -> tupleTotalData.getTupleOfCarList().add( escortTuple1 ) ) );
                    return Mono.just( tupleTotalData ); } ); }

    public Mono< TupleOfCar > getAllTupleOfCar ( UUID uuid ) {
        return Mono.just( this.session.execute(
                "SELECT * FROM "
                        + this.dbName + "." + this.getTupleOfCar()
                        + " where uuid = " + uuid + ";"
        ).one() ).map( TupleOfCar::new ); }
}
