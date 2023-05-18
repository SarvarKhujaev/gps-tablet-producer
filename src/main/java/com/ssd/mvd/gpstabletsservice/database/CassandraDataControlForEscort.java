package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.subscribers.CustomSubscriber;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskInspector;
import com.ssd.mvd.gpstabletsservice.tuple.PolygonForEscort;
import com.ssd.mvd.gpstabletsservice.tuple.TupleTotalData;
import com.ssd.mvd.gpstabletsservice.tuple.EscortTuple;
import com.ssd.mvd.gpstabletsservice.tuple.TupleOfCar;
import com.ssd.mvd.gpstabletsservice.constants.Errors;
import com.ssd.mvd.gpstabletsservice.entity.Country;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Consumer;

import java.util.UUID;
import java.util.Map;

@lombok.Data
public final class CassandraDataControlForEscort extends CassandraConverter {
    private final Session session = CassandraDataControl.getInstance().getSession();
    private final Cluster cluster = CassandraDataControl.getInstance().getCluster();

    private static CassandraDataControlForEscort INSTANCE = new CassandraDataControlForEscort();

    public static CassandraDataControlForEscort getInstance () { return INSTANCE != null ? INSTANCE : ( INSTANCE = new CassandraDataControlForEscort() ); }

    private CassandraDataControlForEscort () {
        this.getSession().execute( "CREATE KEYSPACE IF NOT EXISTS "
                + CassandraTables.ESCORT +
                " WITH REPLICATION = {" +
                "'class' : 'NetworkTopologyStrategy'," +
                "'datacenter1':3 }" +
                "AND DURABLE_WRITES = false;" );

        this.getSession().execute("CREATE TYPE IF NOT EXISTS "
                + CassandraTables.ESCORT + "."
                + CassandraTables.POINTS_ENTITY
                + "( lat double, "
                + "lng double, " +
                "pointId uuid, " +
                "pointName text );" );

        this.getSession().execute("CREATE TYPE IF NOT EXISTS "
                + CassandraTables.ESCORT + "."
                + CassandraTables.POLYGON_ENTITY
                + "( lat double, lng double );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.ESCORT + "."
                + CassandraTables.POLYGON_FOR_ESCORT
                + "( uuid uuid PRIMARY KEY, " +
                "uuidOfEscort uuid, " +
                "name text, " +
                "totalTime int, " +
                "routeIndex int, " +
                "totalDistance int, " +
                "pointsList list< frozen < "
                + CassandraTables.POINTS_ENTITY + " > >, " +
                "latlngs list< frozen < "
                + CassandraTables.POLYGON_ENTITY + " > > );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.ESCORT + "."
                + CassandraTables.TUPLE_OF_ESCORT +
                " ( uuid uuid PRIMARY KEY, " +
                " countries text, " +
                " uuidOfPolygon uuid, " +
                " tupleOfCarsList list< uuid >," +
                " patrulList list< uuid > );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.ESCORT + "."
                + CassandraTables.COUNTRIES
                + super.convertClassToCassandra.apply( Country.class )
                + ", PRIMARY KEY ( uuid ) );" );

        super.logging( "CassandraDataControlForEscort is ready" ); }

    private final Function< String, Mono< EscortTuple > > getCurrentTupleOfEscort = id -> super.convert(
            new EscortTuple( this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.ESCORT + "."
                    + CassandraTables.TUPLE_OF_ESCORT
                    + " WHERE id = " + UUID.fromString( id ) + ";" ).one() ) );

    private final Function< String, Mono< ApiResponseModel > > deleteTupleOfEscort = id ->
            this.getGetCurrentTupleOfEscort().apply( id )
                    .flatMap( escortTuple -> {
                        if ( super.checkRequest.test( escortTuple.getPatrulList(), 6 ) )
                            escortTuple
                                    .getPatrulList()
                                    .parallelStream()
                                    .forEach( uuid -> this.getSession().execute(
                                            "UPDATE " + CassandraTables.TABLETS +
                                                    "." + CassandraTables.PATRULS +
                                                    " SET uuidOfEscort = " + null
                                                    + ", uuidForEscortCar = " + null
                                                    + " WHERE uuid = " + uuid + ";" ) );

                        if ( super.checkRequest.test( escortTuple.getTupleOfCarsList(), 6 ) )
                            escortTuple
                                    .getTupleOfCarsList()
                                    .parallelStream()
                                    .forEach( uuid -> this.getGetCurrentTupleOfCar()
                                            .apply( uuid )
                                            .subscribe( tupleOfCar1 -> this.getSession().execute(
                                                    "UPDATE " +
                                                            CassandraTables.ESCORT + "."
                                                            + CassandraTables.TUPLE_OF_CAR +
                                                            " SET uuidOfEscort = " + null
                                                            + ", uuidOfPatrul = " + null
                                                            + " WHERE uuid = " + uuid +
                                                            " AND trackerid = '" + tupleOfCar1.getTrackerId() + "';" ) ) );

                        return super.getFunction().apply(
                                Map.of( "message", id + " was successfully deleted",
                                        "success", this.getSession()
                                                .execute( "DELETE FROM "
                                                        + CassandraTables.ESCORT + "."
                                                        + CassandraTables.TUPLE_OF_ESCORT
                                                        + " WHERE id = " + UUID.fromString( id ) + ";" )
                                                .wasApplied() ) ); } );

    private final Consumer< EscortTuple > linkPatrulWithEscortCar = escortTuple ->
            Flux.range( 0, escortTuple.getPatrulList().size() )
                    .parallel( escortTuple.getPatrulList().size() )
                    .runOn( Schedulers.parallel() )
                    .flatMap( integer -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( escortTuple.getPatrulList().get( integer ) )
                            .flatMap( patrul -> {
                                patrul.setUuidOfEscort( escortTuple.getUuid() );
                                patrul.setUuidForEscortCar( escortTuple.getTupleOfCarsList().get( integer ) );
                                TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED, escortTuple );
                                return this.getGetCurrentTupleOfCar().apply( patrul.getUuidForEscortCar() )
                                        .flatMap( tupleOfCar -> {
                                            tupleOfCar.setUuidOfPatrul( patrul.getUuid() );
                                            tupleOfCar.setUuidOfEscort( escortTuple.getUuid() );
                                            return this.getUpdateTupleOfCar().apply( tupleOfCar ); } ); } ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .subscribe( new CustomSubscriber( 0 ) );

    private final Function< EscortTuple, Flux< ApiResponseModel > > saveEscortTuple = escortTuple -> {
            if ( super.checkParam.test( escortTuple.getUuidOfPolygon() ) )
                this.getGetCurrentPolygonForEscort()
                        .apply( escortTuple.getUuidOfPolygon().toString() )
                        .subscribe( polygonForEscort1 -> this.getSession().execute ( "UPDATE "
                                        + CassandraTables.ESCORT + "."
                                        + CassandraTables.POLYGON_FOR_ESCORT
                                        + " SET uuidOfEscort = " + escortTuple.getUuid()
                                        + " WHERE uuid = " + polygonForEscort1.getUuid() + ";" ) );

            this.getLinkPatrulWithEscortCar().accept( escortTuple );
            return this.getSession().execute( "INSERT INTO "
                            + CassandraTables.ESCORT + "."
                            + CassandraTables.TUPLE_OF_ESCORT
                            + "( id," +
                            " countries," +
                            " uuidOfPolygon," +
                            " tupleOfCarsList," +
                            " patrulList ) VALUES ("
                            + escortTuple.getUuid() + ", '"
                            + escortTuple.getCountries() + "', "
                            + escortTuple.getUuidOfPolygon() + ", "
                            + super.convertListToCassandra.apply( escortTuple.getTupleOfCarsList() ) + ", "
                            + super.convertListToCassandra.apply( escortTuple.getPatrulList() ) + " ) IF NOT EXISTS;" )
                    .wasApplied()
                    ? Flux.just( ApiResponseModel
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
                    .build() ); };

    private final Function< EscortTuple, Mono< ApiResponseModel > > updateEscortTuple = escortTuple -> {
            if ( super.checkParam.test( escortTuple.getUuidOfPolygon() ) ) this.getGetCurrentTupleOfEscort()
                    .apply( escortTuple.getUuid().toString() )
                    .subscribe( escortTuple1 -> {
                        if ( super.checkParam.test( escortTuple1.getUuidOfPolygon() ) ) this.getSession().execute (
                                "UPDATE "
                                        + CassandraTables.ESCORT + "."
                                        + CassandraTables.POLYGON_FOR_ESCORT
                                        + " SET uuidOfEscort = " + null
                                        + " WHERE uuid = " + escortTuple1.getUuidOfPolygon() + ";" ); } );

            this.getLinkPatrulWithEscortCar().accept( escortTuple );
            return this.getSession().execute( "INSERT INTO "
                            + CassandraTables.ESCORT + "."
                            + CassandraTables.TUPLE_OF_ESCORT
                            + "( id," +
                            " countries," +
                            " uuidOfPolygon," +
                            " tupleOfCarsList," +
                            " patrulList ) VALUES ("
                            + escortTuple.getUuid() + ", '"
                            + escortTuple.getCountries() + "', "
                            + escortTuple.getUuidOfPolygon() + ", "
                            + super.convertListToCassandra.apply( escortTuple.getTupleOfCarsList() ) + ", "
                            + super.convertListToCassandra.apply( escortTuple.getPatrulList() ) + " );" )
                    .wasApplied()
                    ? super.getFunction().apply( Map.of( "message", escortTuple.getUuid() + " was updated successfully" ) )
                    : super.getFunction().apply(
                            Map.of( "message", escortTuple.getUuid() + " does not exists",
                                    "code", 201,
                                    "success", false ) ); };

    private final Function<PolygonForEscort, Mono< ApiResponseModel > > savePolygonForEscort = polygon ->
            this.getSession().execute( "INSERT INTO "
                            + CassandraTables.ESCORT + "."
                            + CassandraTables.POLYGON_FOR_ESCORT
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

                            + super.convertListOfPointsToCassandra.apply( polygon.getPointsList() ) + ", "

                            + super.convertListOfPointsToCassandra.apply( polygon.getLatlngs() )
                            + ") IF NOT EXISTS;" )
                    .wasApplied()
                    ? super.getFunction().apply(
                            Map.of( "message", "Polygon was saved",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .type( polygon.getUuid().toString() )
                                    .build() ) )
                    : super.getFunction().apply(
                            Map.of( "message", "This polygon has already been saved",
                            "code", 201,
                            "success", false ) );

    private final Function< String, Mono< PolygonForEscort > > getCurrentPolygonForEscort = id -> super.convert(
            new PolygonForEscort( this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.ESCORT + "."
                    + CassandraTables.POLYGON_FOR_ESCORT
                    + " WHERE uuid = " + UUID.fromString( id ) + ";" ).one() ) );

    private final Function< String, Mono< ApiResponseModel > > deletePolygonForEscort = id ->
            this.getGetCurrentPolygonForEscort().apply( id )
                    .flatMap( polygonForEscort -> polygonForEscort.getUuidOfEscort() == null
                            ? super.getFunction().apply(
                            Map.of( "message", id + " was removed successfully",
                                    "success", this.getSession().execute( "DELETE FROM "
                                                    + CassandraTables.ESCORT + "."
                                                    + CassandraTables.POLYGON_FOR_ESCORT
                                                    + " WHERE uuid = " + UUID.fromString( id ) + ";" )
                                            .wasApplied() ) )
                            : super.getFunction().apply(
                            Map.of( "message", id + " cannot be removed. cause it is linked to Escort",
                                    "code", 201,
                                    "success", false ) ) );

    private final Function< PolygonForEscort, Mono< ApiResponseModel > > updatePolygonForEscort = polygon ->
            this.checkPolygonForEscort.test( polygon )
                    ? this.getSession().execute( "INSERT INTO "
                            + CassandraTables.ESCORT + "."
                            + CassandraTables.POLYGON_FOR_ESCORT
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

                            + super.convertListOfPointsToCassandra.apply( polygon.getPointsList() ) + ", "
                            + super.convertListOfPointsToCassandra.apply( polygon.getLatlngs() ) + ");" )
                    .wasApplied()
                    ? super.getFunction().apply(
                            Map.of( "message", "Polygon was updated successfully",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .type( polygon.getUuid().toString() )
                                            .build() ) )
                    : super.getFunction().apply(
                            Map.of( "message", "This polygon has already been saved",
                                    "code", 201,
                                    "success", false ) )
                    : super.getFunction().apply(
                            Map.of( "message", "This polygon does not exists. so u cannot update it",
                                    "code", 201,
                                    "success", false ) );

    private final Consumer<TupleOfCar> unlinkTupleOfCarFromPatrul = tupleOfCar ->
            this.getSession().execute(
                    "UPDATE "
                            + CassandraTables.ESCORT + "."
                            + CassandraTables.TUPLE_OF_CAR
                            + " SET uuidOfPatrul = " + tupleOfCar.getUuidOfPatrul()
                            + " WHERE uuid = " + tupleOfCar.getUuid() + ";" );

    private final Function< TupleOfCar, Mono< ApiResponseModel > > updateTupleOfCar = tupleOfCar ->
            this.getSession().execute( "INSERT INTO "
                            + CassandraTables.ESCORT + "."
                            + CassandraTables.TUPLE_OF_CAR
                            + super.getALlNames.apply( TupleOfCar.class )
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
                    ? super.getFunction().apply(
                            Map.of( "message", "Car" + tupleOfCar.getGosNumber() + " was updated successfully" ) )
                    : super.getFunction().apply(
                            Map.of( "message", "This car does not exists",
                                    "code", 201,
                                    "success", false ) );

    private final BiFunction< String, TupleTotalData, Mono< TupleTotalData > > getTupleTotalData = (uuid, tupleTotalData ) ->
            this.getGetCurrentTupleOfEscort().apply( uuid )
                    .map( escortTuple -> {
                        if ( super.checkParam.test( escortTuple.getUuidOfPolygon() ) )
                            this.getGetCurrentPolygonForEscort()
                                    .apply( escortTuple.getUuidOfPolygon().toString() )
                                    .subscribe( new CustomSubscriber( 6, tupleTotalData ) );

                        escortTuple
                                .getPatrulList()
                                .parallelStream()
                                .forEach( uuid1 -> CassandraDataControl
                                        .getInstance()
                                        .getGetPatrulByUUID()
                                        .apply( uuid1 )
                                        .subscribe( new CustomSubscriber( 7, tupleTotalData ) ) );

                        escortTuple
                                .getTupleOfCarsList()
                                .parallelStream()
                                .forEach( uuid1 -> CassandraDataControlForEscort
                                        .getInstance()
                                        .getGetCurrentTupleOfCar()
                                        .apply( uuid1 )
                                        .subscribe( new CustomSubscriber( 8, tupleTotalData ) ) );
                        return tupleTotalData; } );

    private final Function< UUID, Mono< TupleOfCar > > getCurrentTupleOfCar = uuid -> super.convert(
            new TupleOfCar( this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.ESCORT + "."
                    + CassandraTables.TUPLE_OF_CAR
                    + " WHERE uuid = " + uuid + ";" ).one() ) );

    private final Function< String, Mono< Country > > getCurrentCountry = countryName -> super.convert(
            new Country( this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.ESCORT + "."
                    + CassandraTables.COUNTRIES
                    + " WHERE uuid = " + UUID.fromString( countryName ) + ";" ).one() ) );

    private final Function< String, Mono< ApiResponseModel > > deleteCountry = countryName -> {
            this.getSession().execute ( "DELETE FROM "
                    + CassandraTables.ESCORT + "."
                    + CassandraTables.COUNTRIES
                    + " WHERE uuid = " + UUID.fromString( countryName ) + ";" );
            return super.getFunction().apply( Map.of( "message", countryName + " bazadan ochirildi" ) ); };

    private final Function< Country, Mono< ApiResponseModel > > saveNewCountry = country -> this.getSession().execute(
            "INSERT INTO "
                    + CassandraTables.ESCORT + "."
                    + CassandraTables.COUNTRIES
                    + super.getALlNames.apply( Country.class )
                    + " VALUES("
                    + country.getUuid() + ", '"
                    + ( country.getFlag() != null && country.getFlag().length() > 0
                    ? country.getFlag() : Errors.DATA_NOT_FOUND ) + "', '"
                    + super.concatNames.apply( country.getSymbol().toUpperCase(), 2 ) + "', '"
                    + super.concatNames.apply( country.getCountryNameEn().toUpperCase(), 2 ) + "', '"
                    + super.concatNames.apply( country.getCountryNameUz().toUpperCase(), 2 ) + "', '"
                    + super.concatNames.apply( country.getCountryNameRu().toUpperCase(), 2 )
                    + "') IF NOT EXISTS;" )
            .wasApplied()
            ? super.getFunction().apply( Map.of( "message", "Yangi davlat bazaga qoshildi" ) )
            : super.getFunction().apply(
                    Map.of( "message", "This country has already been inserted",
                            "code", 201,
                            "success", false ) );

    private final Function< Country, Mono< ApiResponseModel > > update = country -> this.getSession().execute(
            "UPDATE "
                    + CassandraTables.ESCORT + "."
                    + CassandraTables.COUNTRIES +
                    " SET countryNameUz = '" + super.concatNames.apply( country.getCountryNameUz().toUpperCase(), 2 ) + "', " +
                    "countryNameRu = '" + super.concatNames.apply( country.getCountryNameRu().toUpperCase(), 2 ) + "', " +
                    "flag = '" + country.getFlag() + "', " +
                    "symbol = '" + super.concatNames.apply( country.getSymbol().toUpperCase(), 2 ) +
                    " countryNameEn = '" + super.concatNames.apply( country.getCountryNameEn().toUpperCase(), 2 ) +
                    "' WHERE uuid = " + country.getUuid() + " IF EXISTS;" )
            .wasApplied()
            ? super.getFunction().apply( Map.of( "message", country.getCountryNameEn() + " muvaffaqiyatli yangilandi" ) )
            : super.getFunction().apply(
                    Map.of( "message", "This country has not been inserted yet",
                            "code", 201,
                            "success", false ) );
}