package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.database.CassandraConverter;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.TaskInspector;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.entity.Country;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Row;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.logging.Logger;
import java.util.Locale;
import java.util.UUID;
import java.util.Map;
import lombok.Data;

@Data
public class CassandraDataControlForEscort {
    private final Session session = CassandraDataControl.getInstance().getSession();
    private final Cluster cluster = CassandraDataControl.getInstance().getCluster();

    private final Logger logger = Logger.getLogger( CassandraDataControl.class.toString() );
    private static CassandraDataControlForEscort cassandraDataControl = new CassandraDataControlForEscort();

    public static CassandraDataControlForEscort getInstance() { return cassandraDataControl != null ? cassandraDataControl
            : ( cassandraDataControl = new CassandraDataControlForEscort() ); }

    private CassandraDataControlForEscort () {
        this.session.execute( "CREATE KEYSPACE IF NOT EXISTS "
                + CassandraTables.ESCORT.name() +
                " WITH REPLICATION = {" +
                "'class' : 'NetworkTopologyStrategy'," +
                "'datacenter1':3 }" +
                "AND DURABLE_WRITES = false;" );

        this.session.execute("CREATE TYPE IF NOT EXISTS "
                + CassandraTables.ESCORT.name() + "." + CassandraTables.POINTS_ENTITY.name()
                + "( lat double, "
                + "lng double, " +
                "pointId uuid, " +
                "pointName text );" );

        this.session.execute("CREATE TYPE IF NOT EXISTS "
                + CassandraTables.ESCORT.name() + "." + CassandraTables.POLYGON_ENTITY.name()
                + "( lat double,"
                + "lng double );" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.ESCORT.name() + "." + CassandraTables.POLYGON_FOR_ESCORT.name()
                + "( uuid uuid PRIMARY KEY, " +
                "uuidOfEscort uuid, " +
                "name text, " +
                "totalTime int, " +
                "routeIndex int, " +
                "totalDistance int, " +
                "pointsList list< frozen < " + CassandraTables.POINTS_ENTITY.name() + " > >, " +
                "latlngs list< frozen < " + CassandraTables.POLYGON_ENTITY.name() + " > > );" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.ESCORT.name() + "." + CassandraTables.TUPLE_OF_ESCORT.name() +
                " ( uuid uuid PRIMARY KEY, " +
                " countries text, " +
                " uuidOfPolygon uuid, " +
                " tupleOfCarsList list< uuid >," +
                " patrulList list< uuid > );" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
            + CassandraTables.ESCORT.name() + "." + CassandraTables.COUNTRIES.name() +
            "( countryNameEN text PRIMARY KEY, " +
            "countryNameUz text, " +
            "countryNameRu text, " +
            "symbol text );" );

        CassandraConverter
                .getInstance()
                .registerCodecForPolygonEntity( CassandraTables.ESCORT.name(), CassandraTables.POLYGON_ENTITY.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForPointsList( CassandraTables.ESCORT.name(), CassandraTables.POINTS_ENTITY.name() );

        this.logger.info( "CassandraDataControlForEscort is ready" ); }

    public Flux< EscortTuple > getAllTupleOfEscort () { return Flux.fromStream(
            this.session.execute( "SELECT * FROM "
                            + CassandraTables.ESCORT.name() + "."
                            + CassandraTables.TUPLE_OF_ESCORT.name() + ";" )
                    .all().stream() )
            .map( EscortTuple::new ); }

    public Mono< EscortTuple > getAllTupleOfEscort ( String id ) {
        Row row = this.session.execute( "SELECT * FROM "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.TUPLE_OF_ESCORT.name()
                + " where id = " + UUID.fromString( id ) + ";" ).one();
        return Mono.justOrEmpty( row != null ? new EscortTuple( row ) : null ); }

    public Mono< ApiResponseModel > deleteTupleOfPatrul ( String id ) { return this.getAllTupleOfEscort( id )
                .flatMap( escortTuple -> {
                    if ( escortTuple.getPatrulList() != null
                            && !escortTuple.getPatrulList().isEmpty()
                            && escortTuple.getPatrulList().size() > 0 ) escortTuple.getPatrulList()
                                .forEach( uuid -> this.session.execute(
                                        "UPDATE " + CassandraTables.TABLETS.name() +
                                                "." + CassandraTables.PATRULS +
                                                " SET uuidOfEscort = " + null
                                        + ", uuidForEscortCar = " + null + " where uuid = " + uuid + ";" ) );

                    if ( escortTuple.getTupleOfCarsList() != null
                            && !escortTuple.getTupleOfCarsList().isEmpty()
                            && escortTuple.getTupleOfCarsList().size() > 0 ) escortTuple.getTupleOfCarsList()
                                .forEach( uuid -> this.getAllTupleOfCar( uuid )
                                        .subscribe( tupleOfCar1 -> this.session.execute(
                                                "UPDATE " +
                                                        CassandraTables.ESCORT.name() + "."
                                                        + CassandraTables.TUPLE_OF_CAR.name() +
                                                        " SET uuidOfEscort = " + null
                                                        + ", uuidOfPatrul = " + null + " where uuid = " + uuid +
                                                        " and trackerid = '" + tupleOfCar1.getTrackerId() + "';" ) ) );

                    return Archive
                            .getArchive()
                            .getFunction()
                            .apply( Map.of(
                                    "message", id + " was successfully deleted",
                                    "success", this.session.execute( "DELETE FROM "
                                                    + CassandraTables.ESCORT.name() + "."
                                                    + CassandraTables.TUPLE_OF_ESCORT.name()
                                                    + " where id = " + UUID.fromString( id ) + ";" )
                                            .wasApplied() ) ); } ); }

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

    public Mono< ApiResponseModel > update ( EscortTuple escortTuple ) {
        if ( escortTuple.getUuidOfPolygon() == null ) this.getAllTupleOfEscort( escortTuple.getUuid().toString() )
                .subscribe( escortTuple1 -> {
                    if ( escortTuple1.getUuidOfPolygon() != null ) this.session.execute (
                            "UPDATE "
                                    + CassandraTables.ESCORT.name() + "."
                                    + CassandraTables.POLYGON_FOR_ESCORT.name()
                                    + " SET uuidOfEscort = " + null
                                    + " WHERE uuid = " + escortTuple1.getUuidOfPolygon() + ";" ); } );

        this.linkPatrulWithEscortCar( escortTuple );
        return this.session.execute( "INSERT INTO "
                        + CassandraTables.ESCORT.name() + "."
                        + CassandraTables.TUPLE_OF_ESCORT.name()
                        + "( id," +
                        " countries," +
                        " uuidOfPolygon," +
                        " tupleOfCarsList," +
                        " patrulList ) VALUES ("
                        + escortTuple.getUuid() + ", '"
                        + escortTuple.getCountries() + "', "
                        + escortTuple.getUuidOfPolygon() + ", "
                        + CassandraConverter
                            .getInstance()
                            .convertListToCassandra( escortTuple.getTupleOfCarsList() ) + ", "
                        + CassandraConverter
                            .getInstance()
                            .convertListToCassandra( escortTuple.getPatrulList() )
                        + " );" )
                .wasApplied()
                ? Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", escortTuple.getUuid() + " was updated successfully" ) )
                : Archive
                .getArchive()
                .getFunction()
                .apply( Map.of(
                        "message", escortTuple.getUuid() + " does not exists",
                        "code", 201,
                        "success", false ) ); }

    public Flux< ApiResponseModel > addValue ( EscortTuple escortTuple ) {
        if ( escortTuple.getUuidOfPolygon() != null )
            this.getAllPolygonForEscort( escortTuple.getUuidOfPolygon().toString() )
                    .subscribe( polygonForEscort1 -> this.session.execute (
                            "UPDATE "
                                    + CassandraTables.ESCORT.name() + "."
                                    + CassandraTables.POLYGON_FOR_ESCORT.name()
                                    + " SET uuidOfEscort = " + escortTuple.getUuid()
                                    + " WHERE uuid = " + polygonForEscort1.getUuid() + ";" ) );

        this.linkPatrulWithEscortCar( escortTuple );
        return this.session.execute( "INSERT INTO "
                        + CassandraTables.ESCORT.name() + "."
                        + CassandraTables.TUPLE_OF_ESCORT.name()
                        + "( id," +
                        " countries," +
                        " uuidOfPolygon," +
                        " tupleOfCarsList," +
                        " patrulList ) VALUES ("
                        + escortTuple.getUuid() + ", '"
                        + escortTuple.getCountries() + "', "
                        + escortTuple.getUuidOfPolygon() + ", "
                        + CassandraConverter
                        .getInstance()
                        .convertListToCassandra( escortTuple.getTupleOfCarsList() ) + ", "
                        + CassandraConverter
                        .getInstance()
                        .convertListToCassandra( escortTuple.getPatrulList() )
                        + " ) IF NOT EXISTS;" )
                .wasApplied() ? Flux.just( ApiResponseModel
                .builder()
                .success( true )
                .status( com.ssd.mvd.gpstabletsservice.response.Status
                        .builder()
                        .message( "Tuple was successfully created" )
                        .code( 200 )
                        .build() )
                .build() )
                : Flux.just( ApiResponseModel
                .builder()
                .success( false )
                .status( com.ssd.mvd.gpstabletsservice.response.Status
                        .builder()
                        .message( "Such a tuple has already been created" )
                        .code( 201 )
                        .build() )
                .build() ); }

    public Mono< ApiResponseModel > addValue ( PolygonForEscort polygon ) { return this.session.execute(
            "INSERT INTO "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.POLYGON_FOR_ESCORT.name()
                    + "(uuid, " +
                    "uuidOfEscort, " +
                    "name, " +
                    "totalTime, " +
                    "routeIndex, " +
                    "totalDistance, " +
                    "pointsList, " +
                    "latlngs ) VALUES ("
                    + polygon.getUuid() + ", "
                    + polygon.getUuidOfEscort() + ", '"
                    + polygon.getName() + "', "

                    + polygon.getTotalTime() + ", "
                    + polygon.getRouteIndex() + ", "
                    + polygon.getTotalDistance() + ", "

                    + CassandraConverter
                    .getInstance()
                    .convertListOfPointsToCassandra( polygon.getPointsList() ) + ", "

                    + CassandraConverter
                    .getInstance()
                    .convertListOfPointsToCassandra( polygon.getLatlngs() )
                    + ") IF NOT EXISTS;" )
            .wasApplied()
            ? Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", "Polygon was saved",
                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                            .builder()
                            .type( polygon.getUuid().toString() )
                            .build() ) )
            : Archive
            .getArchive()
            .getFunction()
            .apply( Map.of(
                    "message", "This polygon has already been saved",
                    "code", 201,
                    "success", false ) ); }

    public Mono< PolygonForEscort > getAllPolygonForEscort ( String id ) {
        Row row = this.session.execute( "SELECT * FROM "
                        + CassandraTables.ESCORT.name() + "."
                        + CassandraTables.POLYGON_FOR_ESCORT.name()
                        + " where uuid = " + UUID.fromString( id ) + ";" ).one();
        return Mono.justOrEmpty( row != null ? new PolygonForEscort( row ) : null ); }

    public Mono< ApiResponseModel > deletePolygonForEscort ( String id ) { return this.getAllPolygonForEscort( id )
                .flatMap( polygonForEscort -> polygonForEscort.getUuidOfEscort() == null
                        ? Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", id + " was removed successfully",
                                "success", this.session.execute( "DELETE FROM "
                                                + CassandraTables.ESCORT.name() + "."
                                                + CassandraTables.POLYGON_FOR_ESCORT.name()
                                                + " where uuid = " + UUID.fromString( id ) + ";" )
                                        .wasApplied() ) )
                        : Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of(
                                "message", id + " cannot be removed. cause it is linked to Escort",
                                "code", 201,
                                "success", false ) ) ); }

    public Mono< ApiResponseModel > update ( PolygonForEscort polygon ) {
        return this.session.execute( "SELECT * FROM "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.POLYGON_FOR_ESCORT.name()
                + " where uuid = " + polygon.getUuid() + ";" ).one() != null
                ? this.session.execute( "INSERT INTO "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.POLYGON_FOR_ESCORT.name()
                    + "(uuid, " +
                    "uuidOfEscort, " +
                    "name, " +
                    "totalTime, " +
                    "routeIndex, " +
                    "totalDistance, " +
                    "pointsList, " +
                    "latlngs ) VALUES ("
                    + polygon.getUuid() + ", "
                    + polygon.getUuidOfEscort() + ", '"
                    + polygon.getName() + "', "

                    + polygon.getTotalTime() + ", "
                    + polygon.getRouteIndex() + ", "
                    + polygon.getTotalDistance() + ", "

                    + CassandraConverter
                    .getInstance()
                    .convertListOfPointsToCassandra( polygon.getPointsList() ) + ", "

                    + CassandraConverter
                    .getInstance()
                    .convertListOfPointsToCassandra( polygon.getLatlngs() ) + ");" )
                .wasApplied()
                ? Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", "Polygon was updated successfully",
                        "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                .builder()
                                .type( polygon.getUuid().toString() )
                                .build() ) )
                : Archive
                .getArchive()
                .getFunction()
                .apply( Map.of(
                        "message", "This polygon has already been saved",
                        "code", 201,
                        "success", false ) )
                : Archive
                .getArchive()
                .getFunction()
                .apply( Map.of(
                        "message", "This polygon does not exists. so u cannot update it",
                        "code", 201,
                        "success", false ) ); }

    public Flux< PolygonForEscort > getAllPolygonForEscort () { return Flux.fromStream(
            this.session.execute( "SELECT * FROM "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.POLYGON_FOR_ESCORT.name() + ";" )
                    .all().stream() )
            .map( PolygonForEscort::new ); }

    public Mono< ApiResponseModel > update ( TupleOfCar tupleOfCar ) { return this.session.execute(
            "INSERT INTO "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.TUPLE_OF_CAR.name()
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
                    tupleOfCar.getAverageFuelConsumption() + ");" )
            .wasApplied()
            ? Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", "Car" + tupleOfCar.getGosNumber() + " was updated successfully" ) )
            : Archive
            .getArchive()
            .getFunction()
            .apply( Map.of(
                    "message", "This car does not exists",
                    "code", 201,
                    "success", false ) ); }

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

    public Mono< TupleOfCar > getAllTupleOfCar ( UUID uuid ) { return Mono.just(
            this.session.execute(
            "SELECT * FROM "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.TUPLE_OF_CAR.name()
                    + " where uuid = " + uuid + ";" ).one() )
            .map( TupleOfCar::new ); }

    public Mono< ApiResponseModel > deleteCountry ( String countryName ) {
        this.session.execute ( "DELETE FROM "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.COUNTRIES.name()
                + " where countryNameEN = '" + countryName + "';" );
        return Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", "Country: " + countryName + " was deleted" ) ); }

    public Mono< Country > getAllCountries ( String countryName ) {
        Row row = this.session.execute( "SELECT * FROM "
                + CassandraTables.ESCORT.name() + "." + CassandraTables.COUNTRIES.name()
                + " WHERE countryNameEn = '" + countryName + "';" ).one();
        return Mono.justOrEmpty( row != null ? new Country( row ) : null ); }

    public Mono< ApiResponseModel > addValue ( Country country ) { return this.session.execute(
            "INSERT INTO "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.COUNTRIES.name() +
                    "( countryNameEN, " +
                    "countryNameUz, " +
                    "countryNameRu, " +
                    "symbol ) VALUES('"
                    + country.getCountryNameEn().toUpperCase( Locale.ROOT ).replaceAll( "'", "" ) + "', '"
                    + country.getCountryNameUz().toUpperCase( Locale.ROOT ).replaceAll( "'", "" ) + "', '"
                    + country.getCountryNameRu().toUpperCase( Locale.ROOT ).replaceAll( "'", "" ) + "', '"
                    + country.getSymbol().toUpperCase( Locale.ROOT ).replaceAll( "'", "" )
                    + "') IF NOT EXISTS;" )
            .wasApplied()
            ? Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", "New country was added to database" ) )
            : Archive
            .getArchive()
            .getFunction()
            .apply( Map.of(
                    "message", "This country has already been inserted",
                    "code", 201,
                    "success", false ) ); }

    public Mono< ApiResponseModel > update ( Country country ) { return this.session.execute(
                "UPDATE "
                        + CassandraTables.ESCORT.name() + "."
                        + CassandraTables.COUNTRIES.name() +
                        " SET countryNameUz = '" + country.getCountryNameUz()
                        .toUpperCase( Locale.ROOT )
                        .replaceAll( "'", "" ) + "', " +

                        "countryNameRu = '" + country.getCountryNameRu()
                        .toUpperCase( Locale.ROOT )
                        .replaceAll( "'", "" ) + "', " +

                        "symbol = '" + country.getSymbol()
                        .toUpperCase( Locale.ROOT )
                        .replaceAll( "'", "" ) +

                        "' WHERE countryNameEN = '" + country.getCountryNameEn()
                        .toUpperCase( Locale.ROOT )
                        .replaceAll( "'", "" )
                        + "' IF EXISTS;" ).wasApplied()
            ? Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", country.getCountryNameEn() + " was updated successfully" ) )
            : Archive
            .getArchive()
            .getFunction()
            .apply( Map.of(
                    "message", "This country has not been inserted yet",
                    "code", 201,
                    "success", false ) ); }

    public Flux< Country > getAllCountries () { return Flux.fromStream (
            this.session.execute( "SELECT * FROM "
                            + CassandraTables.ESCORT.name() + "."
                            + CassandraTables.COUNTRIES.name() + ";" )
                    .all().stream() )
            .map( Country::new ); }
}
