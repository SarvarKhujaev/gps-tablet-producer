package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.database.CassandraConverter;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskInspector;
import com.ssd.mvd.gpstabletsservice.constants.Errors;
import com.ssd.mvd.gpstabletsservice.entity.Country;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Row;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;

import java.util.Locale;
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
                + CassandraTables.ESCORT.name() +
                " WITH REPLICATION = {" +
                "'class' : 'NetworkTopologyStrategy'," +
                "'datacenter1':3 }" +
                "AND DURABLE_WRITES = false;" );

        this.getSession().execute("CREATE TYPE IF NOT EXISTS "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.POINTS_ENTITY.name()
                + "( lat double, "
                + "lng double, " +
                "pointId uuid, " +
                "pointName text );" );

        this.getSession().execute("CREATE TYPE IF NOT EXISTS "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.POLYGON_ENTITY.name()
                + "( lat double,"
                + "lng double );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.POLYGON_FOR_ESCORT.name()
                + "( uuid uuid PRIMARY KEY, " +
                "uuidOfEscort uuid, " +
                "name text, " +
                "totalTime int, " +
                "routeIndex int, " +
                "totalDistance int, " +
                "pointsList list< frozen < "
                + CassandraTables.POINTS_ENTITY.name() + " > >, " +
                "latlngs list< frozen < "
                + CassandraTables.POLYGON_ENTITY.name() + " > > );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.TUPLE_OF_ESCORT.name() +
                " ( uuid uuid PRIMARY KEY, " +
                " countries text, " +
                " uuidOfPolygon uuid, " +
                " tupleOfCarsList list< uuid >," +
                " patrulList list< uuid > );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.COUNTRIES.name()
                + super.convertClassToCassandra( Country.class )
                + ", PRIMARY KEY ( uuid ) );" );

        super.logging( "CassandraDataControlForEscort is ready" ); }

    private final Function< String, Mono< EscortTuple > > getCurrentTupleOfEscort = id -> {
            final Row row = this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.TUPLE_OF_ESCORT.name()
                    + " where id = " + UUID.fromString( id ) + ";" ).one();
            return Mono.justOrEmpty( super.getCheckParam().test( row ) ? new EscortTuple( row ) : null ); };

    private final Function< String, Mono< ApiResponseModel > > deleteTupleOfEscort = id ->
            this.getGetCurrentTupleOfEscort().apply( id )
                    .flatMap( escortTuple -> {
                        if ( super.getCheckRequest().test( escortTuple.getPatrulList(), 6 ) )
                            escortTuple
                                    .getPatrulList()
                                    .parallelStream()
                                    .forEach( uuid -> this.getSession().execute(
                                            "UPDATE " + CassandraTables.TABLETS.name() +
                                                    "." + CassandraTables.PATRULS +
                                                    " SET uuidOfEscort = " + null
                                                    + ", uuidForEscortCar = " + null
                                                    + " where uuid = " + uuid + ";" ) );

                        if ( super.getCheckRequest().test( escortTuple.getTupleOfCarsList(), 6 ) )
                            escortTuple
                                    .getTupleOfCarsList()
                                    .parallelStream()
                                    .forEach( uuid -> this.getGetCurrentTupleOfCar()
                                            .apply( uuid )
                                            .subscribe( tupleOfCar1 -> this.getSession().execute(
                                                    "UPDATE " +
                                                            CassandraTables.ESCORT.name() + "."
                                                            + CassandraTables.TUPLE_OF_CAR.name() +
                                                            " SET uuidOfEscort = " + null
                                                            + ", uuidOfPatrul = " + null
                                                            + " where uuid = " + uuid +
                                                            " and trackerid = '" + tupleOfCar1.getTrackerId() + "';" ) ) );

                        return super.getFunction().apply(
                                Map.of( "message", id + " was successfully deleted",
                                        "success", this.getSession()
                                                .execute( "DELETE FROM "
                                                        + CassandraTables.ESCORT.name() + "."
                                                        + CassandraTables.TUPLE_OF_ESCORT.name()
                                                        + " where id = " + UUID.fromString( id ) + ";" )
                                                .wasApplied() ) ); } );

    private final Consumer< EscortTuple > linkPatrulWithEscortCar = escortTuple ->
            Flux.range( 0, escortTuple.getPatrulList().size() )
                    .parallel( escortTuple.getPatrulList().size() )
                    .runOn( Schedulers.parallel() )
                    .map( integer -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( escortTuple.getPatrulList().get( integer ) )
                            .subscribe( patrul -> {
                                patrul.setUuidOfEscort( escortTuple.getUuid() );
                                patrul.setUuidForEscortCar( escortTuple.getTupleOfCarsList().get( integer ) );
                                this.getGetCurrentTupleOfCar()
                                        .apply( patrul.getUuidForEscortCar() )
                                        .subscribe( tupleOfCar -> {
                                            tupleOfCar.setUuidOfPatrul( patrul.getUuid() );
                                            tupleOfCar.setUuidOfEscort( escortTuple.getUuid() );
                                            this.getUpdateTupleOfCar()
                                                    .apply( tupleOfCar )
                                                    .subscribe(); } );
                                TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul,
                                                com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                                escortTuple ); } ) )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .subscribe();

    private final Function< EscortTuple, Flux< ApiResponseModel > > saveEscortTuple = escortTuple -> {
            if ( super.getCheckParam().test( escortTuple.getUuidOfPolygon() ) )
                this.getGetCurrentPolygonForEscort()
                        .apply( escortTuple.getUuidOfPolygon().toString() )
                        .subscribe( polygonForEscort1 -> this.getSession()
                                .execute ( "UPDATE "
                                        + CassandraTables.ESCORT.name() + "."
                                        + CassandraTables.POLYGON_FOR_ESCORT.name()
                                        + " SET uuidOfEscort = " + escortTuple.getUuid()
                                        + " WHERE uuid = " + polygonForEscort1.getUuid() + ";" ) );

            this.getLinkPatrulWithEscortCar().accept( escortTuple );
            return this.getSession().execute( "INSERT INTO "
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
                        + super.convertListToCassandra( escortTuple.getTupleOfCarsList() ) + ", "
                        + super.convertListToCassandra( escortTuple.getPatrulList() ) + " ) IF NOT EXISTS;" )
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
            if ( super.getCheckParam().test( escortTuple.getUuidOfPolygon() ) ) this.getGetCurrentTupleOfEscort()
                    .apply( escortTuple.getUuid().toString() )
                    .subscribe( escortTuple1 -> {
                        if ( super.getCheckParam().test( escortTuple1.getUuidOfPolygon() ) ) this.getSession().execute (
                                "UPDATE "
                                        + CassandraTables.ESCORT.name() + "."
                                        + CassandraTables.POLYGON_FOR_ESCORT.name()
                                        + " SET uuidOfEscort = " + null
                                        + " WHERE uuid = " + escortTuple1.getUuidOfPolygon() + ";" ); } );

            this.getLinkPatrulWithEscortCar().accept( escortTuple );
            return this.getSession().execute( "INSERT INTO "
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
                            + super.convertListToCassandra( escortTuple.getTupleOfCarsList() ) + ", "
                            + super.convertListToCassandra( escortTuple.getPatrulList() ) + " );" )
                    .wasApplied()
                    ? super.getFunction().apply( Map.of( "message", escortTuple.getUuid() + " was updated successfully" ) )
                    : super.getFunction().apply(
                            Map.of( "message", escortTuple.getUuid() + " does not exists",
                            "code", 201,
                            "success", false ) ); };

    private final Function< PolygonForEscort, Mono< ApiResponseModel > > savePolygonForEscort = polygon ->
            this.getSession().execute( "INSERT INTO "
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

                    + super.convertListOfPointsToCassandra( polygon.getPointsList() ) + ", "

                    + super.convertListOfPointsToCassandra( polygon.getLatlngs() )
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

    private final Function< String, Mono< PolygonForEscort > > getCurrentPolygonForEscort = id -> {
            final Row row = this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.POLYGON_FOR_ESCORT.name()
                    + " where uuid = " + UUID.fromString( id ) + ";" ).one();
            return Mono.justOrEmpty( super.getCheckParam().test( row ) ? new PolygonForEscort( row ) : null ); };

    private final Function< String, Mono< ApiResponseModel > > deletePolygonForEscort = id ->
            this.getGetCurrentPolygonForEscort().apply( id )
                    .flatMap( polygonForEscort -> polygonForEscort.getUuidOfEscort() == null
                            ? super.getFunction().apply(
                                    Map.of( "message", id + " was removed successfully",
                                            "success", this.getSession().execute( "DELETE FROM "
                                                    + CassandraTables.ESCORT.name() + "."
                                                    + CassandraTables.POLYGON_FOR_ESCORT.name()
                                                    + " where uuid = " + UUID.fromString( id ) + ";" )
                                            .wasApplied() ) )
                            : super.getFunction().apply(
                                    Map.of( "message", id + " cannot be removed. cause it is linked to Escort",
                                    "code", 201,
                                    "success", false ) ) );

    private final Function< PolygonForEscort, Mono< ApiResponseModel > > updatePolygonForEscort = polygon ->
            this.getCheckPolygonForEscort().test( polygon )
                    ? this.getSession().execute( "INSERT INTO "
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

                            + super.convertListOfPointsToCassandra( polygon.getPointsList() ) + ", "
                            + super.convertListOfPointsToCassandra( polygon.getLatlngs() ) + ");" )
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

    private final Function< TupleOfCar, Mono< ApiResponseModel > > updateTupleOfCar = tupleOfCar ->
            this.getSession().execute( "INSERT INTO "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.TUPLE_OF_CAR.name()
                    + super.getALlNames( TupleOfCar.class )
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
                    ? super.getFunction().apply( Map.of( "message", "Car" + tupleOfCar.getGosNumber() + " was updated successfully" ) )
                    : super.getFunction().apply(
                            Map.of( "message", "This car does not exists",
                            "code", 201,
                            "success", false ) );

    private final Function< String, Mono< TupleTotalData > > getTupleTotalData = uuid -> {
            final TupleTotalData tupleTotalData = new TupleTotalData();
            return this.getGetCurrentTupleOfEscort().apply( uuid )
                    .map( escortTuple -> {
                        if ( super.getCheckParam().test( escortTuple.getUuidOfPolygon() ) )
                            this.getGetCurrentPolygonForEscort()
                                    .apply( escortTuple.getUuidOfPolygon().toString() )
                                    .subscribe( tupleTotalData::setPolygonForEscort );

                        escortTuple
                                .getPatrulList()
                                .parallelStream()
                                .forEach( uuid1 -> CassandraDataControl
                                .getInstance()
                                .getGetPatrulByUUID()
                                .apply( uuid1 )
                                .subscribe( patrul -> tupleTotalData.getPatrulList().add( patrul ) ) );

                        escortTuple
                                .getTupleOfCarsList()
                                .parallelStream()
                                .forEach( uuid1 -> CassandraDataControlForEscort
                                        .getInstance()
                                        .getGetCurrentTupleOfCar()
                                        .apply( uuid1 )
                                        .subscribe( escortTuple1 -> tupleTotalData.getTupleOfCarList().add( escortTuple1 ) ) );
                        return tupleTotalData; } ); };

    private final Function< UUID, Mono< TupleOfCar > > getCurrentTupleOfCar = uuid -> Mono.just(
            new TupleOfCar( this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.TUPLE_OF_CAR.name()
                    + " where uuid = " + uuid + ";" ).one() ) );

    private final Function< String, Mono< Country > > getCurrentCountry = countryName -> {
        final Row row = this.getSession().execute( "SELECT * FROM "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.COUNTRIES.name()
                + " WHERE uuid = " + UUID.fromString( countryName ) + ";" ).one();
        return Mono.justOrEmpty( super.getCheckParam().test( row ) ? new Country( row ) : null ); };

    private final Function< String, Mono< ApiResponseModel > > deleteCountry = countryName -> {
            this.getSession().execute ( "DELETE FROM "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.COUNTRIES.name()
                    + " where uuid = " + UUID.fromString( countryName ) + ";" );
            return super.getFunction().apply( Map.of( "message", countryName + " bazadan ochirildi" ) ); };

    private final Function< Country, Mono< ApiResponseModel > > saveNewCountry = country -> this.getSession().execute(
            "INSERT INTO "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.COUNTRIES.name()
                    + super.getALlNames( Country.class )
                    + " VALUES("
                    + country.getUuid() + ", '"
                    + ( country.getFlag() != null && country.getFlag().length() > 0
                    ? country.getFlag(): Errors.DATA_NOT_FOUND.name() ) + "', '"
                    + country.getSymbol().toUpperCase( Locale.ROOT ).replaceAll( "'", "" ) + "', '"
                    + country.getCountryNameEn().toUpperCase( Locale.ROOT ).replaceAll( "'", "" ) + "', '"
                    + country.getCountryNameUz().toUpperCase( Locale.ROOT ).replaceAll( "'", "" ) + "', '"
                    + country.getCountryNameRu().toUpperCase( Locale.ROOT ).replaceAll( "'", "" )
                    + "') IF NOT EXISTS;" )
            .wasApplied()
            ? super.getFunction().apply( Map.of( "message", "Yangi davlat bazaga qoshildi" ) )
            : super.getFunction().apply(
                    Map.of( "message", "This country has already been inserted",
                    "code", 201,
                    "success", false ) );

    private final Function< Country, Mono< ApiResponseModel > > update = country -> this.getSession().execute(
            "UPDATE "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.COUNTRIES.name() +
                    " SET countryNameUz = '" + country.getCountryNameUz().toUpperCase( Locale.ROOT ).replaceAll( "'", "" ) + "', " +
                    "countryNameRu = '" + country.getCountryNameRu().toUpperCase( Locale.ROOT ).replaceAll( "'", "" ) + "', " +
                    "flag = '" + country.getFlag() + "', " +
                    "symbol = '" + country.getSymbol().toUpperCase( Locale.ROOT ).replaceAll( "'", "" ) +
                    " countryNameEn = '" + country.getCountryNameEn().toUpperCase( Locale.ROOT ).replaceAll( "'", "" ) +
                    "' WHERE uuid = " + country.getUuid() + " IF EXISTS;" )
            .wasApplied()
            ? super.getFunction().apply( Map.of( "message", country.getCountryNameEn() + " muvaffaqiyatli yangilandi" ) )
            : super.getFunction().apply(
                    Map.of( "message", "This country has not been inserted yet",
                    "code", 201,
                    "success", false ) );
}