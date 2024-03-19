package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.interfaces.ServiceCommonMethods;
import com.ssd.mvd.gpstabletsservice.subscribers.CustomSubscriber;
import com.ssd.mvd.gpstabletsservice.constants.CassandraFunctions;
import com.ssd.mvd.gpstabletsservice.constants.CassandraCommands;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskInspector;
import com.ssd.mvd.gpstabletsservice.tuple.PolygonForEscort;
import com.ssd.mvd.gpstabletsservice.tuple.TupleTotalData;
import com.ssd.mvd.gpstabletsservice.tuple.EscortTuple;
import com.ssd.mvd.gpstabletsservice.tuple.TupleOfCar;
import com.ssd.mvd.gpstabletsservice.constants.Errors;
import com.ssd.mvd.gpstabletsservice.entity.Country;
import com.ssd.mvd.gpstabletsservice.entity.Data;

import com.datastax.driver.core.Session;
import com.datastax.driver.core.Row;
import java.text.MessageFormat;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.Optional;

import java.util.UUID;
import java.util.Map;

@lombok.Data
public final class CassandraDataControlForEscort extends CassandraConverter implements ServiceCommonMethods {
    private final Session session = CassandraDataControl.getInstance().getSession();

    private static CassandraDataControlForEscort INSTANCE = new CassandraDataControlForEscort();

    public static CassandraDataControlForEscort getInstance () {
        return INSTANCE != null ? INSTANCE : ( INSTANCE = new CassandraDataControlForEscort() );
    }

    private CassandraDataControlForEscort () {
        super.logging( this.getClass() );
    }

    /*
    возвращает ROW из БД для любой таблицы внутри Escort
    */
    public Row getRowFromEscortKeyspace (
            // название таблицы внутри Tablets
            final CassandraTables cassandraTableName,
            // название колонки
            final String columnName,
            // параметр по которому введется поиск
            final String paramName
    ) {
        return this.getSession().execute(
                MessageFormat.format(
                        """
                        {0} {1}.{2} WHERE {3} = {4};
                        """,
                        CassandraCommands.SELECT_ALL,
                        CassandraTables.ESCORT,
                        cassandraTableName,
                        columnName,
                        paramName
                )
        ).one();
    }

    private final Function< String, Mono< EscortTuple > > getCurrentTupleOfEscort = id -> super.convert(
            new EscortTuple(
                    this.getRowFromEscortKeyspace( CassandraTables.TUPLE_OF_ESCORT, "id", id ) ) );

    private final Function< String, Mono< ApiResponseModel > > deleteTupleOfEscort = id ->
            this.getGetCurrentTupleOfEscort().apply( id )
                    .flatMap( escortTuple -> {
                        final Optional< EscortTuple > optional = Optional.ofNullable( escortTuple );

                        final StringBuilder stringBuilder = super.newStringBuilder();

                        // если у Эскорта есть закрепленные патрульные, то освобождаем их всех
                        optional.filter( escortTuple1 -> super.isCollectionNotEmpty( escortTuple.getPatrulList() ) )
                                .ifPresent( escortTuple1 -> {
                                    final StringBuilder patrulUuids = super.newStringBuilder( "" );

                                    super.analyze(
                                            escortTuple.getPatrulList(),
                                            uuid -> patrulUuids.append( uuid ).append( ", " )
                                    );

                                    stringBuilder.append(
                                            MessageFormat.format(
                                                    """
                                                    {0} {1}.{2}
                                                    SET uuidForEscortCar = {3},
                                                    uuidOfEscort = {3}
                                                    WHERE uuid in ( {4} );
                                                    """,
                                                    CassandraCommands.UPDATE,
                                                    CassandraTables.TABLETS,
                                                    CassandraTables.PATRULS,
                                                    null,
                                                    patrulUuids.substring( 0, patrulUuids.length() - 2 )
                                            )
                                    );
                                } );

                        // также освобождаем все машины эскорта
                        optional.filter( escortTuple1 -> super.isCollectionNotEmpty( escortTuple.getTupleOfCarsList() ) )
                                .ifPresent( escortTuple1 -> {
                                    final StringBuilder tupleCars = super.newStringBuilder( "" );

                                    super.analyze(
                                            escortTuple.getTupleOfCarsList(),
                                            uuid -> this.getGetCurrentTupleOfCar().apply( uuid )
                                                    .subscribe(
                                                            new CustomSubscriber<>(
                                                                    tupleOfCar1 -> tupleCars.append(
                                                                            MessageFormat.format(
                                                                                    """
                                                                                    {0} {1}.{2}
                                                                                    SET uuidOfEscort = {3},
                                                                                    uuidOfPatrul = {3}
                                                                                    WHERE uuid = {4} AND trackerid = {5};
                                                                                    """,
                                                                                    CassandraCommands.UPDATE,
                                                                                    CassandraTables.ESCORT,
                                                                                    CassandraTables.TUPLE_OF_CAR,
                                                                                    null,
                                                                                    uuid,
                                                                                    super.joinWithAstrix( tupleOfCar1.getTrackerId() )
                                                                            )
                                                                    )
                                                            )
                                                    )
                                    );

                                    stringBuilder.append( tupleCars );
                                } );

                        stringBuilder.append(
                                MessageFormat.format(
                                        """
                                        {0} {1}.{2}
                                        WHERE id = {3};
                                        """,
                                        CassandraCommands.DELETE,
                                        CassandraTables.ESCORT,
                                        CassandraTables.TUPLE_OF_ESCORT,
                                        UUID.fromString( id ) )
                        ).append( CassandraCommands.APPLY_BATCH );

                        return super.function(
                                Map.of( "message", id + " was successfully deleted",
                                        "success", this.getSession().execute( stringBuilder.toString() ).wasApplied() ) );
                    } );

    private final Consumer< EscortTuple > linkPatrulWithEscortCar = escortTuple ->
            Flux.range( 0, escortTuple.getPatrulList().size() )
                    .parallel( escortTuple.getPatrulList().size() )
                    .runOn( Schedulers.parallel() )
                    .map( integer -> {
                        final Patrul patrul = Patrul.empty().generate(
                                CassandraDataControl
                                        .getInstance()
                                        .getRowFromTabletsKeyspace(
                                                CassandraTables.PATRULS,
                                                "uuid",
                                                escortTuple.getPatrulList().get( integer ).toString()
                                        )
                        );

                        patrul.linkWithEscortCar(
                                escortTuple.getUuid(),
                                escortTuple.getTupleOfCarsList().get( integer )
                        );

                        TaskInspector
                                .getInstance()
                                .changeTaskStatus( patrul, com.ssd.mvd.gpstabletsservice.constants.Status.ATTACHED, escortTuple )
                                .getPatrulUniqueValues()
                                .setUuidOfEscort( escortTuple.getUuid() );

                        return MessageFormat.format(
                                """
                                {0} {1}.{2}
                                SET uuidOfPatrul = {3}, uuidOfEscort = {4}
                                WHERE uuid = {5};
                                """,
                                CassandraCommands.UPDATE,

                                CassandraTables.ESCORT,
                                CassandraTables.TUPLE_OF_CAR,

                                patrul.getUuid(),
                                escortTuple.getUuid(),
                                patrul.getPatrulUniqueValues().getUuidForEscortCar()
                        );
                    } )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .collectList()
                    .subscribe( new CustomSubscriber<>(
                            strings -> {
                                final StringBuilder stringBuilder = super.newStringBuilder();

                                super.analyze(
                                        strings,
                                        stringBuilder::append
                                );

                                stringBuilder.append( CassandraCommands.APPLY_BATCH );

                                this.getSession().execute( stringBuilder.toString() );
                            }
                    ) );

    private final Function< EscortTuple, Flux< ApiResponseModel > > saveEscortTuple = escortTuple -> {
        final StringBuilder stringBuilder = super.newStringBuilder();

        /*
        проверяем что эскорта не привязан к полигону
        */
        if ( super.objectIsNotNull( escortTuple.getUuidOfPolygon() ) ) {
            /*
            если да, то обновляем данные самого полигона
            */
            this.getGetCurrentPolygonForEscort()
                    .apply( escortTuple.getUuidOfPolygon().toString() )
                    .subscribe( new CustomSubscriber<>(
                            polygonForEscort1 -> stringBuilder.append(
                                    MessageFormat.format(
                                            """
                                            {0} {1}.{2}
                                            SET uuidOfEscort = {3}
                                            WHERE uuid = {4};
                                            """,
                                            CassandraCommands.UPDATE,

                                            CassandraTables.ESCORT,
                                            CassandraTables.POLYGON_FOR_ESCORT,

                                            escortTuple.getUuid(),
                                            polygonForEscort1.getUuid()
                                    )
                            )
                    ) );
        }

        this.getLinkPatrulWithEscortCar().accept( escortTuple );

        stringBuilder.append(
                MessageFormat.format(
                        """
                        {0} {1}.{2}
                        ( id, countries, uuidOfPolygon, tupleOfCarsList, patrulList )
                        VALUES ( {3}, {4}, {5}, {6}, {7} );
                        """,
                        CassandraCommands.INSERT_INTO,

                        CassandraTables.ESCORT,
                        CassandraTables.TUPLE_OF_ESCORT,

                        CassandraFunctions.UUID,

                        super.joinWithAstrix( escortTuple.getCountries() ),
                        escortTuple.getUuidOfPolygon(),

                        super.convertListToCassandra.apply( escortTuple.getTupleOfCarsList() ),
                        super.convertListToCassandra.apply( escortTuple.getPatrulList() )
                )
        ).append( CassandraCommands.APPLY_BATCH );

        return this.getSession().execute( stringBuilder.toString() ).wasApplied()
                ? Flux.from( super.function( Map.of( "message", "Tuple was successfully created" ) ) )
                : Flux.from( super.errorResponse( "Such a tuple has already been created" ) );
    };

    private final Function< EscortTuple, Mono< ApiResponseModel > > updateEscortTuple = escortTuple ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            ( id, countries, uuidOfPolygon, tupleOfCarsList, patrulList )
                            VALUES ( {3}, {4}, {5}, {6}, {7} ) {8};
                            """,
                            CassandraCommands.INSERT_INTO,

                            CassandraTables.ESCORT,
                            CassandraTables.TUPLE_OF_ESCORT,

                            escortTuple.getUuid(),
                            super.joinWithAstrix( escortTuple.getCountries() ),
                            escortTuple.getUuidOfPolygon(),

                            super.convertListToCassandra.apply( escortTuple.getTupleOfCarsList() ),
                            super.convertListToCassandra.apply( escortTuple.getPatrulList() ),

                            CassandraCommands.IF_EXISTS
                    ) )
                    .wasApplied()
                    ? super.function( Map.of( "message", escortTuple.getUuid() + " was updated successfully" ) )
                    : super.errorResponse( "This car does not exist, choose another one" );

    private final Function< PolygonForEscort, Mono< ApiResponseModel > > savePolygonForEscort = polygon ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            ( uuid, uuidOfEscort, name, totalTime, routeIndex, totalDistance, pointsList, latlngs )
                            VALUES ( {3}, {4}, {5}, {6,number,#}, {7}, {8,number,#}, {9}, {10} );
                            """,
                            CassandraCommands.INSERT_INTO,

                            CassandraTables.ESCORT,
                            CassandraTables.POLYGON_FOR_ESCORT,

                            CassandraFunctions.UUID,

                            polygon.getUuidOfEscort(),

                            super.joinWithAstrix( polygon.getName() ),
                            polygon.getTotalTime(),
                            polygon.getRouteIndex(),
                            polygon.getTotalDistance(),

                            super.convertListOfPointsToCassandra.apply( polygon.getPointsList() ),
                            super.convertListOfPointsToCassandra.apply( polygon.getLatlngs() ) ) )
                    .wasApplied()
                    ? super.function(
                            Map.of(
                                    "message", "Polygon was saved",
                                    "data", Data.from( polygon.getUuid().toString() )
                            )
                    )
                    : super.errorResponse( "This polygon has already been saved" );

    private final Function< String, Mono< PolygonForEscort > > getCurrentPolygonForEscort = id -> super.convert(
            new PolygonForEscort(
                    this.getRowFromEscortKeyspace( CassandraTables.POLYGON_FOR_ESCORT, "uuid", id ) ) );

    private final Function< String, Mono< ApiResponseModel > > deletePolygonForEscort = id ->
            this.getGetCurrentPolygonForEscort().apply( id )
                    .flatMap( polygonForEscort -> !super.objectIsNotNull( polygonForEscort.getUuidOfEscort() )
                            ? super.function(
                                    Map.of( "message", id + " was removed successfully",
                                            "success", this.getSession().execute(
                                                            MessageFormat.format(
                                                                    """
                                                                    {0} {1}.{2} WHERE uuid = {3};
                                                                    """,
                                                                    CassandraCommands.DELETE,

                                                                    CassandraTables.ESCORT,
                                                                    CassandraTables.POLYGON_FOR_ESCORT,

                                                                    id
                                                            )
                                                    )
                                            .wasApplied() ) )
                            : super.errorResponse( "polygon cannot be removed. cause it is linked to Escort" ) );

    private final Function< PolygonForEscort, Mono< ApiResponseModel > > updatePolygonForEscort = polygon ->
            super.objectIsNotNull(
                    this.getRowFromEscortKeyspace(
                            CassandraTables.POLYGON_FOR_ESCORT,
                            "uuid",
                            polygon.getUuid().toString() ) )
                    ? this.getSession().execute(
                            MessageFormat.format(
                                    """
                                    {0} {1}.{2}
                                    ( uuid, uuidOfEscort, name, totalTime, routeIndex, totalDistance, pointsList, latlngs )
                                    VALUES ( {3}, {4}, {5}, {6,number,#}, {7}, {8,number,#}, {9}, {10} );
                                    """,
                                    CassandraCommands.INSERT_INTO,

                                    CassandraTables.ESCORT,
                                    CassandraTables.POLYGON_FOR_ESCORT,

                                    polygon.getUuid(),
                                    polygon.getUuidOfEscort(),

                                    super.joinWithAstrix( polygon.getName() ),

                                    polygon.getTotalTime(),
                                    polygon.getRouteIndex(),
                                    polygon.getTotalDistance(),

                                    super.convertListOfPointsToCassandra.apply( polygon.getPointsList() ),
                                    super.convertListOfPointsToCassandra.apply( polygon.getLatlngs() ) ) )
                    .wasApplied()
                    ? super.function(
                            Map.of( "message", "Polygon was updated successfully",
                                    "data", Data.from( polygon.getUuid().toString() ) ) )
                    : super.errorResponse( "This polygon has already been saved" )
                    : super.errorResponse( "This polygon does not exists. so u cannot update it" );

    private final Function< TupleOfCar, Mono< ApiResponseModel > > updateTupleOfCar = tupleOfCar ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2} {3}
                            VALUES( {4}, {5}, {6}, {7}, {8}, {9}, {10}, {11}, {12}, {13}, {14} );
                            """,
                            CassandraCommands.INSERT_INTO,

                            CassandraTables.ESCORT,
                            CassandraTables.TUPLE_OF_CAR,

                            super.getALlParamsNamesForClass.apply( TupleOfCar.class ),

                            CassandraFunctions.UUID,

                            tupleOfCar.getUuidOfEscort(),
                            tupleOfCar.getUuidOfPatrul(),

                            super.joinWithAstrix( tupleOfCar.getCarModel() ),
                            super.joinWithAstrix( tupleOfCar.getGosNumber() ),
                            super.joinWithAstrix( tupleOfCar.getTrackerId() ),
                            super.joinWithAstrix( tupleOfCar.getNsfOfPatrul() ),
                            super.joinWithAstrix( tupleOfCar.getSimCardNumber() ),

                            tupleOfCar.getLatitude(),
                            tupleOfCar.getLongitude(),
                            tupleOfCar.getAverageFuelConsumption()
                    ) )
                    .wasApplied()
                    ? super.function( Map.of( "message", "Car " + tupleOfCar.getGosNumber() + " was updated successfully" ) )
                    : super.errorResponse( "This car does not exists" );

    private final Function< TupleOfCar, Mono< ApiResponseModel > > updateEscortAfterDeleting = tupleOfCar -> {
            final EscortTuple escortTuple = new EscortTuple(
                    this.getRowFromEscortKeyspace(
                            CassandraTables.TUPLE_OF_ESCORT,
                            "id",
                            tupleOfCar.getUuidOfEscort().toString() ) );

            escortTuple.removePatrulAndCar( tupleOfCar );

            final StringBuilder stringBuilder = super.newStringBuilder();

            stringBuilder.append(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            SET tupleOfCarsList = {3}, patrulList = {4};
                            """,
                            CassandraCommands.UPDATE,

                            CassandraTables.ESCORT,
                            CassandraTables.TUPLE_OF_ESCORT,

                            super.convertListToCassandra.apply( escortTuple.getTupleOfCarsList() ),
                            super.convertListToCassandra.apply( escortTuple.getPatrulList() )
                    )
            );

            final Patrul patrul = Patrul.empty().generate(
                    CassandraDataControl
                            .getInstance()
                            .getRowFromTabletsKeyspace(
                                    CassandraTables.PATRULS,
                                    "uuid",
                                    tupleOfCar.getUuidOfPatrul().toString()
                            )
            );

            patrul.getPatrulUniqueValues().unlinkFromEscortCar();

            stringBuilder.append(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            SET uuidForEscortCar = {3},
                            uuidOfEscort = {3}
                            WHERE uuid = {4};
                            """,
                            CassandraCommands.UPDATE,

                            CassandraTables.TABLETS,
                            CassandraTables.PATRULS,

                            null,
                            patrul.getUuid()
                    )
            );

            tupleOfCar.unlinkFromEscortCarAndPatrul();

            stringBuilder.append(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            SET uuidOfPatrul = {3},
                            uuidOfEscort = {3}
                            WHERE uuid = {4};
                            """,
                            CassandraCommands.UPDATE,

                            CassandraTables.ESCORT,
                            CassandraTables.TUPLE_OF_CAR,

                            null,
                            tupleOfCar.getUuid()
                    )
            );

            return this.getSession().execute(
                    stringBuilder.append( CassandraCommands.APPLY_BATCH ).toString()
            ).wasApplied()
                    ? super.function(
                            Map.of( "message", "Car" + tupleOfCar.getGosNumber() + " was updated successfully" ) )
                    : super.errorResponse( "This car does not exists" );
    };

    private final BiFunction< String, TupleTotalData, Mono< TupleTotalData > > getTupleTotalData = ( uuid, tupleTotalData ) ->
            this.getGetCurrentTupleOfEscort().apply( uuid )
                    .map( escortTuple -> {
                        /*
                        проверяем есть ли у эскорта полигон
                        */
                        if ( super.objectIsNotNull( escortTuple.getUuidOfPolygon() ) ) {
                            tupleTotalData.setPolygonForEscort(
                                    new PolygonForEscort(
                                            this.getRowFromEscortKeyspace(
                                                    CassandraTables.POLYGON_FOR_ESCORT,
                                                    "uuid",
                                                    escortTuple.getUuidOfPolygon().toString()
                                            )
                                    )
                            );
                        }

                        /*
                        сохраняем все данные патрульных прикрепленных к эскорту
                        */
                        super.analyze(
                                escortTuple.getPatrulList(),
                                uuid1 -> tupleTotalData.getPatrulList().add(
                                        Patrul.empty().generate(
                                                CassandraDataControl
                                                    .getInstance()
                                                    .getRowFromTabletsKeyspace(
                                                            CassandraTables.PATRULS,
                                                            "uuid",
                                                            uuid1.toString()
                                                    )
                                        )
                                )
                        );

                        /*
                        сохраняем все данные машин прикрепленных к эскорту
                        */
                        super.analyze(
                                escortTuple.getTupleOfCarsList(),
                                uuid1 -> tupleTotalData.getTupleOfCarList().add(
                                        new TupleOfCar(
                                                this.getRowFromEscortKeyspace(
                                                        CassandraTables.TUPLE_OF_CAR,
                                                        "uuid",
                                                        uuid1.toString()
                                                )
                                        )
                                )
                        );

                        return tupleTotalData;
                    } );

    private final Function< UUID, Mono< TupleOfCar > > getCurrentTupleOfCar = uuid -> super.convert(
            new TupleOfCar(
                    this.getRowFromEscortKeyspace( CassandraTables.TUPLE_OF_CAR, "uuid", uuid.toString() )
            )
    );

    private final Function< String, Mono< Country > > getCurrentCountry = uuid -> super.convert(
            new Country(
                    this.getRowFromEscortKeyspace( CassandraTables.COUNTRIES, "uuid", uuid )
            )
    );

    private final Function< String, Mono< ApiResponseModel > > deleteCountry = countryId -> {
            this.getSession().execute (
                    MessageFormat.format(
                            """
                            {0} {1}.{2} WHERE uuid = {3};
                            """,
                            CassandraCommands.DELETE,

                            CassandraTables.ESCORT,
                            CassandraTables.COUNTRIES,

                            countryId
                    )
            );

            return super.function( Map.of( "message", countryId + " bazadan ochirildi" ) );
    };

    private final Function< Country, Mono< ApiResponseModel > > saveNewCountry = country -> this.getSession().execute(
            MessageFormat.format(
                    """
                    {0} {1}.{2} {3}
                    VALUES( {4}, {5}, {6}, {7}, {8}, {9} );
                    """,
                    CassandraCommands.INSERT_INTO,

                    CassandraTables.ESCORT,
                    CassandraTables.COUNTRIES,

                    super.convertClassToCassandra.apply( Country.class ),

                    CassandraFunctions.UUID,

                    super.joinWithAstrix(
                            ( !country.getFlag().isBlank()
                                    ? country.getFlag()
                                    : Errors.DATA_NOT_FOUND )
                    ),
                    super.joinWithAstrix( country.getSymbol().toUpperCase() ),
                    super.joinWithAstrix( country.getCountryNameEn().toUpperCase() ),
                    super.joinWithAstrix( country.getCountryNameUz().toUpperCase() ),
                    super.joinWithAstrix( country.getCountryNameRu().toUpperCase() ) ) )
            .wasApplied()
            ? super.function( Map.of( "message", "Yangi davlat bazaga qoshildi" ) )
            : super.errorResponse( "This country has already been inserted" );

    private final Function< Country, Mono< ApiResponseModel > > update = country -> this.getSession().execute(
            MessageFormat.format(
                    """
                    {0} {1}.{2} {3}
                    VALUES( {4}, {5}, {6}, {7}, {8}, {9} );
                    """,
                    CassandraCommands.INSERT_INTO,

                    CassandraTables.ESCORT,
                    CassandraTables.COUNTRIES,

                    super.getALlParamsNamesForClass.apply( Country.class ),

                    country.getUuid(),
                    super.joinWithAstrix( country.getFlag() ),
                    super.joinWithAstrix( country.getSymbol().toUpperCase() ),
                    super.joinWithAstrix( country.getCountryNameEn().toUpperCase() ),
                    super.joinWithAstrix( country.getCountryNameUz().toUpperCase() ),
                    super.joinWithAstrix( country.getCountryNameRu().toUpperCase() ) ) )
            .wasApplied()
            ? super.function( Map.of( "message", country.getCountryNameEn() + " muvaffaqiyatli yangilandi" ) )
            : super.errorResponse( "This country has not been inserted yet" );

    @Override
    public void close () {
        INSTANCE = null;
        this.session.close();
        super.logging( this );
    }
}