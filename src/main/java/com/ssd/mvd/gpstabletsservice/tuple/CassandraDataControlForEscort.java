package com.ssd.mvd.gpstabletsservice.tuple;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.database.CassandraConverter;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.entity.TaskInspector;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.entity.Country;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Row;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import java.util.logging.Logger;
import java.util.Locale;
import java.util.List;
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
                + CassandraTables.COUNTRIES.name() +
            "( countryNameEN text PRIMARY KEY, " +
            "countryNameUz text, " +
            "countryNameRu text, " +
            "symbol text );" );

        CassandraConverter
                .getInstance()
                .registerCodecForPolygonEntity( CassandraTables.ESCORT.name(),
                        CassandraTables.POLYGON_ENTITY.name() );

        CassandraConverter
                .getInstance()
                .registerCodecForPointsList( CassandraTables.ESCORT.name(),
                        CassandraTables.POINTS_ENTITY.name() );

        this.logger.info( "CassandraDataControlForEscort is ready" ); }

    private final Supplier< Flux< EscortTuple > > getAllTupleOfEscort = () -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.ESCORT.name() + "."
                            + CassandraTables.TUPLE_OF_ESCORT.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .flatMap( row -> Mono.just( new EscortTuple( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() );

    private final Function< String, Mono< EscortTuple > > getCurrentTupleOfEscort = id -> {
        Row row = this.getSession().execute( "SELECT * FROM "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.TUPLE_OF_ESCORT.name()
                + " where id = " + UUID.fromString( id ) + ";" ).one();
        return Mono.justOrEmpty( row != null ? new EscortTuple( row ) : null ); };

    private final Predicate< List< UUID > > checkUUIDList = uuids ->
            uuids != null
            && !uuids.isEmpty()
            && uuids.size() > 0;

    private final Function< String, Mono< ApiResponseModel > > deleteTupleOfEscort = id ->
            this.getGetCurrentTupleOfEscort()
                .apply( id )
                .flatMap( escortTuple -> {
            if ( this.checkUUIDList.test( escortTuple.getPatrulList() ) )
                escortTuple
                        .getPatrulList()
                        .parallelStream()
                        .parallel()
                        .forEach( uuid -> this.getSession().execute(
                                "UPDATE " + CassandraTables.TABLETS.name() +
                                        "." + CassandraTables.PATRULS +
                                        " SET uuidOfEscort = " + null
                                        + ", uuidForEscortCar = " + null
                                        + " where uuid = " + uuid + ";" ) );

            if ( this.checkUUIDList.test( escortTuple.getTupleOfCarsList() ) )
                escortTuple
                        .getTupleOfCarsList()
                        .parallelStream()
                        .parallel()
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

            return Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of(
                            "message", id + " was successfully deleted",
                            "success", this.getSession()
                                    .execute( "DELETE FROM "
                                            + CassandraTables.ESCORT.name() + "."
                                            + CassandraTables.TUPLE_OF_ESCORT.name()
                                            + " where id = " + UUID.fromString( id ) + ";" )
                                    .wasApplied() ) ); } );

    private final Consumer< EscortTuple > linkPatrulWithEscortCar = escortTuple ->
            Flux.range( 0, escortTuple.getPatrulList().size() )
                    .subscribe( integer -> CassandraDataControl
                        .getInstance()
                        .getGetPatrulByUUID()
                        .apply( escortTuple.getPatrulList().get( integer ) )
                        .subscribe( patrul -> {
                            patrul.setUuidOfEscort( escortTuple.getUuid() );
                            patrul.setUuidForEscortCar( escortTuple.getTupleOfCarsList().get( integer ) );
                            CassandraDataControlForEscort
                                    .getInstance()
                                    .getGetCurrentTupleOfCar()
                                    .apply( patrul.getUuidForEscortCar() )
                                    .subscribe( tupleOfCar -> {
                                        tupleOfCar.setUuidOfPatrul( patrul.getUuid() );
                                        tupleOfCar.setUuidOfEscort( escortTuple.getUuid() );
                                        CassandraDataControlForEscort
                                                .getInstance()
                                                .getUpdateTupleOfcar()
                                                .apply( tupleOfCar )
                                                .subscribe(); } );
                            TaskInspector
                                    .getInstance()
                                    .changeTaskStatus( patrul,
                                            com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED,
                                            escortTuple ); } ) );

    private final Function< EscortTuple, Flux< ApiResponseModel > > saveEscortTuple = escortTuple -> {
        if ( escortTuple.getUuidOfPolygon() != null )
            this.getCurrentPolygonForEscort
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
                    + CassandraConverter
                    .getInstance()
                    .convertListToCassandra( escortTuple.getTupleOfCarsList() ) + ", "
                    + CassandraConverter
                    .getInstance()
                    .convertListToCassandra( escortTuple.getPatrulList() )
                    + " ) IF NOT EXISTS;" )
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
        if ( escortTuple.getUuidOfPolygon() == null ) this.getGetCurrentTupleOfEscort()
                .apply( escortTuple.getUuid().toString() )
                .subscribe( escortTuple1 -> {
                    if ( escortTuple1.getUuidOfPolygon() != null ) this.getSession()
                            .execute ( "UPDATE "
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
                        "success", false ) ); };

    private final Function< PolygonForEscort, Mono< ApiResponseModel > > savePolygonForEscort = polygon ->
            this.getSession()
                    .execute( "INSERT INTO "
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
                    "success", false ) );

    private final Function< String, Mono< PolygonForEscort > > getCurrentPolygonForEscort = id -> {
        Row row = this.getSession().execute( "SELECT * FROM "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.POLYGON_FOR_ESCORT.name()
                + " where uuid = " + UUID.fromString( id ) + ";" ).one();
        return Mono.justOrEmpty( row != null ? new PolygonForEscort( row ) : null ); };

    private final Function< String, Mono< ApiResponseModel > > deletePolygonForEscort = id ->
            this.getCurrentPolygonForEscort
            .apply( id )
            .flatMap( polygonForEscort -> polygonForEscort.getUuidOfEscort() == null
                    ? Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of( "message", id + " was removed successfully",
                            "success", this.getSession().execute( "DELETE FROM "
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
                            "success", false ) ) );

    private final Predicate< PolygonForEscort > checkPolygonForEscort = polygon -> this.getSession().execute( "SELECT * FROM "
            + CassandraTables.ESCORT.name() + "."
            + CassandraTables.POLYGON_FOR_ESCORT.name()
            + " where uuid = " + polygon.getUuid() + ";" ).one() != null;

    private final Function< PolygonForEscort, Mono< ApiResponseModel > > updatePolygonForEscort = polygon ->
            this.checkPolygonForEscort.test( polygon )
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
                            "success", false ) );

    private final Supplier< Flux< PolygonForEscort > > getAllPolygonForEscort = () -> Flux.fromStream(
            this.getSession()
                    .execute( "SELECT * FROM "
                            + CassandraTables.ESCORT.name() + "."
                            + CassandraTables.POLYGON_FOR_ESCORT.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .flatMap( row -> Mono.just( new PolygonForEscort( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() );

    private final Function< TupleOfCar, Mono< ApiResponseModel > > updateTupleOfcar = tupleOfCar ->
            this.getSession()
                    .execute( "INSERT INTO "
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
                    "success", false ) );

    private final Function< String, Mono< TupleTotalData > > getTupleTotalData = uuid -> {
        TupleTotalData tupleTotalData = new TupleTotalData();
        return this.getGetCurrentTupleOfEscort()
                .apply( uuid )
                .flatMap( escortTuple -> {
                    if ( escortTuple.getUuidOfPolygon() != null )
                        this.getGetCurrentPolygonForEscort()
                                .apply( escortTuple.getUuidOfPolygon().toString() )
                                .subscribe( tupleTotalData::setPolygonForEscort );

                    escortTuple.getPatrulList().forEach( uuid1 -> CassandraDataControl
                            .getInstance()
                            .getGetPatrulByUUID()
                            .apply( uuid1 )
                            .subscribe( patrul -> tupleTotalData.getPatrulList().add( patrul ) ) );

                    escortTuple.getTupleOfCarsList().forEach( uuid1 -> CassandraDataControlForEscort
                            .getInstance()
                            .getGetCurrentTupleOfCar()
                            .apply( uuid1 )
                            .subscribe( escortTuple1 -> tupleTotalData.getTupleOfCarList().add( escortTuple1 ) ) );
                    return Mono.just( tupleTotalData ); } ); };

    private final Function< UUID, Mono< TupleOfCar > > getCurrentTupleOfCar = uuid -> Mono.just(
            this.getSession().execute( "SELECT * FROM "
                        + CassandraTables.ESCORT.name() + "."
                        + CassandraTables.TUPLE_OF_CAR.name()
                        + " where uuid = " + uuid + ";" ).one() )
            .map( TupleOfCar::new );

    private final Function< String, Mono< Country > > getCurrentCountry = countryName -> {
        Row row = this.getSession().execute( "SELECT * FROM "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.COUNTRIES.name()
                + " WHERE countryNameEn = '" + countryName + "';" ).one();
        return Mono.justOrEmpty( row != null ? new Country( row ) : null ); };

    private final Function< String, Mono< ApiResponseModel > > deleteCountry = countryName -> {
        this.getSession().execute ( "DELETE FROM "
                + CassandraTables.ESCORT.name() + "."
                + CassandraTables.COUNTRIES.name()
                + " where countryNameEN = '" + countryName + "';" );
        return Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", countryName + " bazadan ochirildi" ) ); };

    private final Function< Country, Mono< ApiResponseModel > > saveNewCountry = country -> this.getSession()
            .execute( "INSERT INTO "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.COUNTRIES.name() +
                    "( countryNameEN, " +
                    "countryNameUz, " +
                    "countryNameRu, " +
                    "symbol ) VALUES('"
                    + country.getCountryNameEn()
                    .toUpperCase( Locale.ROOT )
                    .replaceAll( "'", "" ) + "', '"
                    + country.getCountryNameUz()
                    .toUpperCase( Locale.ROOT )
                    .replaceAll( "'", "" ) + "', '"
                    + country.getCountryNameRu()
                    .toUpperCase( Locale.ROOT )
                    .replaceAll( "'", "" ) + "', '"
                    + country.getSymbol()
                    .toUpperCase( Locale.ROOT )
                    .replaceAll( "'", "" )
                    + "') IF NOT EXISTS;" )
            .wasApplied()
            ? Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", "Yangi davlat bazaga qoshildi" ) )
            : Archive
            .getArchive()
            .getFunction()
            .apply( Map.of(
                    "message", "This country has already been inserted",
                    "code", 201,
                    "success", false ) );

    private final Function< Country, Mono< ApiResponseModel > > update = country -> this.getSession()
            .execute( "UPDATE "
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
                    + "' IF EXISTS;" )
            .wasApplied()
            ? Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", country.getCountryNameEn() + " muvaffaqiyatli yangilandi" ) )
            : Archive
            .getArchive()
            .getFunction()
            .apply( Map.of(
                    "message", "This country has not been inserted yet",
                    "code", 201,
                    "success", false ) );

    private final Supplier< Flux< Country > > getAllCountries = () -> Flux.fromStream (
            this.getSession()
                    .execute( "SELECT * FROM "
                        + CassandraTables.ESCORT.name() + "."
                        + CassandraTables.COUNTRIES.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .flatMap( row -> Mono.just( new Country( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() );
}
