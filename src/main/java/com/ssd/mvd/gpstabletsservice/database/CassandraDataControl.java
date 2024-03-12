package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests.PatrulActivityRequest;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests.PatrulImageRequest;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests.PatrulLoginRequest;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PositionInfo;
import com.ssd.mvd.gpstabletsservice.entity.notifications.Notification;
import com.ssd.mvd.gpstabletsservice.interfaces.ServiceCommonMethods;
import com.ssd.mvd.gpstabletsservice.subscribers.CustomSubscriber;
import com.ssd.mvd.gpstabletsservice.controller.UnirestController;
import com.ssd.mvd.gpstabletsservice.request.AndroidVersionUpdate;
import com.ssd.mvd.gpstabletsservice.entity.polygons.PolygonType;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskInspector;
import com.ssd.mvd.gpstabletsservice.entity.polygons.Polygon;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.*;
import com.ssd.mvd.gpstabletsservice.entity.Point;
import com.ssd.mvd.gpstabletsservice.constants.*;
import com.ssd.mvd.gpstabletsservice.entity.*;

import com.datastax.driver.core.*;

import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.text.MessageFormat;
import static java.lang.Math.*;
import java.util.function.*;
import java.time.Duration;
import java.util.*;

public class CassandraDataControl extends CassandraConverter implements ServiceCommonMethods {
    private final Cluster cluster;
    private final Session session;

    private static CassandraDataControl INSTANCE;

    private final CodecRegistry codecRegistry = new CodecRegistry();

    public static CassandraDataControl getInstance () {
        return INSTANCE != null ? INSTANCE : ( INSTANCE = new CassandraDataControl() );
    }

    public CodecRegistry getCodecRegistry() {
        return this.codecRegistry;
    }

    public Cluster getCluster() {
        return this.cluster;
    }

    public Session getSession() {
        return this.session;
    }

    private CassandraDataControl () {
        final SocketOptions options = new SocketOptions();
        options.setConnectTimeoutMillis( 30000 );
        options.setReadTimeoutMillis( 300000 );
        options.setTcpNoDelay( true );
        options.setKeepAlive( true );

        this.session = ( this.cluster = Cluster
                .builder()
                .withClusterName( "Test Cluster" )
                .withPort( 9042 )
                .addContactPoint( "localhost" )
                .withProtocolVersion( ProtocolVersion.V4 )
                .withCodecRegistry( this.getCodecRegistry() )
                .withRetryPolicy( CustomRetryPolicy.generate( 3, 3, 2 ) )
                .withQueryOptions( new QueryOptions()
                        .setDefaultIdempotence( true )
                        .setConsistencyLevel( ConsistencyLevel.ONE ) )
                .withSocketOptions( options )
                .build() )
                .connect();

        super.logging( this.getClass() );

        /*
        создаем, регистрируем и сохраняем все таблицы, типы и кодеки
        */
        CassandraTablesAndTypesRegister.generate( this.getSession(), this.getCluster(), this.getCodecRegistry() );
    }

    /*
    возвращает ROW из БД для любой таблицы внутри TABLETS
    */
    public Row getRowFromTabletsKeyspace (
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
                        CassandraTables.TABLETS,
                        cassandraTableName,
                        columnName,
                        paramName
                )
        ).one();
    }

    /*
    по запросу находит исторические данные о передвижениях патрульного
    за выбранные отрезок времени
    */
    public final Function< PatrulActivityRequest, Mono< List< PositionInfo > > > getHistoricalPositionOfPatrulUntilArriveness = request ->
            super.convertValuesToParallelFlux(
                    this.getSession().execute(
                            MessageFormat.format(
                                    """
                                    {0} {1}.{2}
                                    WHERE {3} = {4} AND date >= {5} AND date <= {6};
                                    """,
                                    CassandraCommands.SELECT_ALL,
                                    CassandraTables.GPSTABLETS,
                                    CassandraTables.TABLETS_LOCATION_TABLE,
                                    "userId",
                                    request.getPatrulUUID(),
                                    super.joinWithAstrix( request.getStartDate() ),
                                    super.joinWithAstrix( request.getEndDate() )
                            )
                    ),
                    (int) Math.abs(
                            Duration.between(
                                    request.getStartDate().toInstant(),
                                    request.getEndDate().toInstant() ).toDays()
                    )
            ).map( row -> PositionInfo.empty().generate( row ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .collectList();

    public final Function< PoliceType, Mono< ApiResponseModel > > updatePoliceType = policeType -> {
            final StringBuilder stringBuilder = super.newStringBuilder();

            this.getAllEntities.apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
                    .filter( row -> row.getString( "policeType" ).compareTo( policeType.getPoliceType() ) == 0 )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .subscribe(  new CustomSubscriber<>(
                            row -> stringBuilder.append(
                                    MessageFormat.format(
                                            """
                                            {0} {1}.{2}
                                            SET policeType = {3}
                                            WHERE uuid = {4};
                                            """,
                                            CassandraCommands.UPDATE,
                                            CassandraTables.TABLETS,
                                            CassandraTables.PATRULS,
                                            super.joinWithAstrix( policeType.getPoliceType() ),
                                            row.getUUID( "uuid" )
                                    )
                            )
                    )  );

            stringBuilder.append(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            SET policeType = {3}, icon = {4}, icon2 = {5}
                            WHERE uuid = {6};
                            """,
                            CassandraCommands.UPDATE,
                            CassandraTables.TABLETS,
                            CassandraTables.POLICE_TYPE,

                            super.joinWithAstrix( policeType.getPoliceType() ),
                            super.joinWithAstrix( policeType.getIcon() ),
                            super.joinWithAstrix( policeType.getIcon2() ),

                            policeType.getUuid()
                    )
            );

            return this.getSession().execute(
                        stringBuilder.append( CassandraCommands.APPLY_BATCH ).toString()
                    ).wasApplied()
                    ? super.function( Map.of( "message", super.getSuccessMessage( "PoliceType", "updated" ) ) )
                    : super.errorResponse( super.getFailMessage( "PoliceType" ) )
                    .doOnError( this::close );
    };

    public final Function< PoliceType, Mono< ApiResponseModel > > savePoliceType = policeType -> this.getAllEntities
            .apply( CassandraTables.TABLETS, CassandraTables.POLICE_TYPE )
            .filter( row -> row.getString( "PoliceType" ).compareTo( policeType.getPoliceType() ) == 0 )
            .sequential()
            .publishOn( Schedulers.single() )
            .count()
            .flatMap( aLong -> aLong == 0
                    ? this.getSession().execute(
                            MessageFormat.format(
                                    """
                                    {0} {1}.{2} {3}
                                    VALUES( {4}, {5}, {6}, {7} );
                                    """,
                                    CassandraCommands.INSERT_INTO,
                                    CassandraTables.TABLETS,
                                    CassandraTables.POLICE_TYPE,

                                    super.getALlParamsNamesForClass.apply( PoliceType.class ),
                                    policeType.getUuid(),

                                    super.joinWithAstrix( policeType.getIcon() ),
                                    super.joinWithAstrix( policeType.getIcon2() ),
                                    super.joinWithAstrix( policeType.getPoliceType() ) ) )
                    .wasApplied()
                    ? super.function( Map.of( "message", super.getSuccessMessage( "PoliceType", "saved" ) ) )
                    : super.errorResponse( super.getFailMessage( "PoliceType" ) )
                    : super.errorResponse( super.getFailMessage( "PoliceType" ) ) )
            .doOnError( this::close);

    public final BiFunction< AtlasLustra, Boolean, Mono< ApiResponseModel > > saveLustra = ( atlasLustra, check ) ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2} {3}
                            VALUES( {4}, {5}, {6}, {7} ) {8};
                            """,
                            CassandraCommands.INSERT_INTO,
                            CassandraTables.TABLETS,
                            CassandraTables.LUSTRA,

                            super.getALlParamsNamesForClass.apply( AtlasLustra.class ),
                            CassandraFunctions.UUID,

                            super.joinWithAstrix( atlasLustra.getLustraName() ),
                            super.joinWithAstrix( atlasLustra.getCarGosNumber() ),
                            super.convertListOfPointsToCassandra.apply( atlasLustra.getCameraLists() ),
                            ( check ? CassandraCommands.IF_NOT_EXISTS : CassandraCommands.IF_EXISTS ) ) )
                    .wasApplied()
                    ? super.function( Map.of( "message", super.getSuccessMessage( "Lustra", "saved" ) ) )
                    : super.errorResponse( super.getFailMessage( "Lustra" ) )
                    .doOnError( this::close);

    public final Function< PolygonType, Mono< ApiResponseModel > > savePolygonType = polygonType ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2} {3}
                            VALUES( {4}, {5} );
                            """,
                            CassandraCommands.INSERT_INTO,
                            CassandraTables.TABLETS,
                            CassandraTables.POLYGON_TYPE,
                            super.getALlParamsNamesForClass.apply( PolygonType.class ),
                            CassandraFunctions.UUID,
                            super.joinWithAstrix( polygonType.getName() ) ) )
                    .wasApplied()
                    ? super.function( Map.of( "message", super.getSuccessMessage( "PolygonType", "saved" ) ) )
                    : super.errorResponse( super.getFailMessage( "PolygonType" ) )
                    .doOnError( this::close);

    public final Function< PolygonType, Mono< ApiResponseModel > > updatePolygonType = polygonType ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            SET name = {3}
                            WHERE uuid = {4} {5};
                            """,
                            CassandraCommands.UPDATE,
                            CassandraTables.TABLETS,
                            CassandraTables.POLYGON_TYPE,

                            super.joinWithAstrix( polygonType.getName() ),
                            polygonType.getUuid(),
                            CassandraCommands.IF_EXISTS ) )
                    .wasApplied()
                    ? super.function( Map.of( "message", super.getSuccessMessage( "PolygonType", "updated" ) ) )
                    : super.errorResponse( super.getFailMessage( "PolygonType" ) )
                    .doOnError( this::close);

    public final Function< UUID, Mono< PolygonType > > getPolygonTypeByUUID = uuid -> super.convert(
            new PolygonType(
                    this.getRowFromTabletsKeyspace(
                            CassandraTables.POLYGON_TYPE,
                            "uuid",
                            uuid.toString() ) ) )
            .doOnError( this::close);

    public final Function< Polygon, Mono< ApiResponseModel > > updatePolygon = polygon -> this.getSession().execute(
            MessageFormat.format(
                    """
                    {0} {1}.{2} {3}
                    VALUES ( {4}, {5}, {6,number,#}, {7,number,#}, {8,number,#}, {9}, {10}, {11}, {12}, {13} );
                    """,
                    CassandraCommands.INSERT_INTO,
                    CassandraTables.TABLETS,
                    CassandraTables.POLYGON,

                    super.getALlParamsNamesForClass.apply( Polygon.class ),

                    polygon.getUuid(),
                    polygon.getOrgan(),
                    polygon.getRegionId(),
                    polygon.getMahallaId(),
                    polygon.getDistrictId(),

                    super.joinWithAstrix( polygon.getName() ),
                    super.joinWithAstrix( polygon.getColor() ),

                    super.convertClassToCassandraTable.apply( polygon.getPolygonType() ),
                    super.convertListToCassandra.apply( polygon.getPatrulList() ),
                    super.convertListOfPointsToCassandra.apply( polygon.getLatlngs() ) ) )
            .wasApplied()
            ? super.function( Map.of( "message", super.getSuccessMessage( "Polygon", "updated" ) ) )
            : super.errorResponse( super.getFailMessage( "Polygon" ) )
            .doOnError( this::close);

    public final Function< Polygon, Mono< ApiResponseModel > > savePolygon = polygon -> this.getSession().execute(
            MessageFormat.format(
                    """
                    {0} {1}.{2} {3}
                    VALUES ( {4}, {5}, {6,number,#}, {7,number,#}, {8,number,#}, {9}, {10}, {11}, {12}, {13} );
                    """,
                    CassandraCommands.INSERT_INTO,
                    CassandraTables.TABLETS,
                    CassandraTables.POLYGON,

                    super.getALlParamsNamesForClass.apply( Polygon.class ),
                    CassandraFunctions.UUID,

                    polygon.getOrgan(),
                    polygon.getRegionId(),
                    polygon.getMahallaId(),
                    polygon.getDistrictId(),

                    super.joinWithAstrix( polygon.getName() ),
                    super.joinWithAstrix( polygon.getColor() ),

                    super.convertClassToCassandraTable.apply( polygon.getPolygonType() ),
                    super.convertListToCassandra.apply( polygon.getPatrulList() ),
                    super.convertListOfPointsToCassandra.apply( polygon.getLatlngs() ) ) )
            .wasApplied()
            ? super.function( Map.of( "message", super.getSuccessMessage( "Polygon", "saved" ) ) )
            : super.errorResponse( super.getFailMessage( "Polygon" ) )
            .doOnError( this::close);

    public final Function< UUID, Mono< Polygon > > getPolygonByUUID = uuid -> super.convert(
            new Polygon(
                    this.getRowFromTabletsKeyspace(
                            CassandraTables.POLYGON,
                            "uuid",
                            uuid.toString() ) ) );

    public final Function< UUID, Mono< ReqCar > > getCarByUUID = uuid -> super.convert(
            new ReqCar(
                    this.getRowFromTabletsKeyspace(
                            CassandraTables.CARS,
                            "uuid",
                            uuid.toString()
                    )
            )
    );

    public final Function< String, Mono< ApiResponseModel > > deleteCar = gosno ->
            this.getCarByUUID.apply( UUID.fromString( gosno ) )
                    .flatMap( reqCar -> {
                        if ( reqCar.getPatrulPassportSeries() == null
                                && reqCar.getPatrulPassportSeries().equals( "null" ) ) {
                            this.getSession().execute(
                                    MessageFormat.format(
                                            """
                                            {0} {1} {2} {3};
                                            """,
                                            CassandraCommands.BEGIN_BATCH,

                                            MessageFormat.format(
                                                    """
                                                    {0} {1}.{2} WHERE uuid = {3};
                                                    """,
                                                    CassandraCommands.DELETE,

                                                    CassandraTables.TABLETS,
                                                    CassandraTables.CARS,

                                                    gosno
                                            ),

                                            MessageFormat.format(
                                                    """
                                                    {0} {1}.{2} WHERE trackersId = {3};
                                                    """,
                                                    CassandraCommands.DELETE,

                                                    CassandraTables.TRACKERS,
                                                    CassandraTables.TRACKERSID,

                                                    super.joinWithAstrix( reqCar.getTrackerId() )
                                            ),

                                            CassandraCommands.APPLY_BATCH
                                    )
                            );

                            return super.convert(
                                    ApiResponseModel
                                            .builder()
                                            .status( com.ssd.mvd.gpstabletsservice.response.Status
                                                    .builder()
                                                    .message( super.getSuccessMessage( "Car", "deleted" ) )
                                                    .build() )
                                            .build()
                            );
                        }
                        else {
                            return super.errorResponse( "This car is linked to patrul" );
                        }
                    } )
                    .doOnError( this::close );

    public final Function< ReqCar, Mono< ApiResponseModel > > updateCar = reqCar ->
            this.getCarByUUID.apply( reqCar.getUuid() )
                    .flatMap( reqCar1 -> {
                        if ( Optional.of( reqCar )
                                .filter( reqCar2 -> !reqCar.getTrackerId().equals( reqCar1.getTrackerId() )
                                        && !super.checkTracker( reqCar.getTrackerId() ) )
                                .isPresent() ) {
                            return super.errorResponse( super.getFailMessage( reqCar.getTrackerId() ) );
                        }

                        final StringBuilder stringBuilder = super.newStringBuilder();

                        if ( Optional.of( reqCar )
                                .filter( reqCar2 -> !reqCar.getPatrulPassportSeries().equals( reqCar1.getPatrulPassportSeries() ) )
                                .isPresent() ) {
                            stringBuilder.append(
                                    MessageFormat.format(
                                            """
                                            {0} {1}
                                            """,
                                            MessageFormat.format(
                                                    """
                                                    {0} {1}.{2}
                                                    SET carnumber = {3},
                                                    uuidForPatrulCar = {4},
                                                    cartype = {5}
                                                    WHERE uuid = {6};
                                                    """,
                                                    CassandraCommands.UPDATE,
                                                    CassandraTables.TABLETS,
                                                    CassandraTables.PATRULS,
                                                    super.joinWithAstrix( reqCar.getGosNumber() ),
                                                    reqCar.getUuid(),
                                                    super.joinWithAstrix( reqCar.getVehicleType() ),
                                                    this.getRowFromTabletsKeyspace(
                                                                    CassandraTables.PATRULS,
                                                                    "passportNumber",
                                                                    super.joinWithAstrix( reqCar1.getPatrulPassportSeries() ) )
                                                            .getUUID( "uuid" )
                                            ),

                                            MessageFormat.format(
                                                    """
                                                    {0} {1}.{2}
                                                    SET carnumber = {3},
                                                    uuidForPatrulCar = {4},
                                                    cartype = {5}
                                                    WHERE uuid = {6};
                                                    """,
                                                    CassandraCommands.UPDATE,
                                                    CassandraTables.TABLETS,
                                                    CassandraTables.PATRULS,
                                                    super.joinWithAstrix( reqCar.getGosNumber() ),
                                                    reqCar.getUuid(),
                                                    super.joinWithAstrix( reqCar.getVehicleType() ),
                                                    this.getRowFromTabletsKeyspace(
                                                                    CassandraTables.PATRULS,
                                                                    "passportNumber",
                                                                    super.joinWithAstrix( reqCar.getPatrulPassportSeries() ) )
                                                            .getUUID( "uuid" )
                                            )
                                    )
                            );
                        }

                        stringBuilder.append(
                                MessageFormat.format(
                                        """
                                        {0} {1}.{2} {3}
                                        VALUES ( {4}, {5}, {6}, {7}, {8}, {9}, {10}, {11}, {12}, {13}, {14}, {15}, {16} );
                                        """,
                                        CassandraCommands.INSERT_INTO,
                                        CassandraTables.TABLETS,
                                        CassandraTables.CARS,

                                        super.getALlParamsNamesForClass.apply( ReqCar.class ),
                                        reqCar.getUuid(),
                                        reqCar.getLustraId(),

                                        super.joinWithAstrix( reqCar.getGosNumber() ),
                                        super.joinWithAstrix( reqCar.getTrackerId() ),
                                        super.joinWithAstrix( reqCar.getVehicleType() ),
                                        super.joinWithAstrix( reqCar.getCarImageLink() ),
                                        super.joinWithAstrix( reqCar.getPatrulPassportSeries() ),

                                        reqCar.getSideNumber(),
                                        reqCar.getSimCardNumber(),
                                        reqCar.getLatitude(),
                                        reqCar.getLongitude(),
                                        reqCar.getAverageFuelSize(),
                                        reqCar.getAverageFuelConsumption()
                                )
                        );

                        return this.getSession().execute(
                                    stringBuilder.append( CassandraCommands.APPLY_BATCH ).toString()
                                ).wasApplied()
                                ? super.function( Map.of( "message", super.getSuccessMessage( "Car", "saved" ) ) )
                                : super.errorResponse( super.getFailMessage( "Car" ) );
                    } );

    public final Function< ReqCar, Mono< ApiResponseModel > > saveCar = reqCar ->
            super.checkTracker( reqCar.getTrackerId() )
                    && super.checkCarNumber( reqCar.getGosNumber() )
                    ? this.getSession().execute(
                            MessageFormat.format(
                                    """
                                    {0} {1} {2} {3}
                                    """,
                                    CassandraCommands.BEGIN_BATCH,

                                    /*
                                    обновляем данные патрульного чтобы связать его с машиной
                                     */
                                    MessageFormat.format(
                                            """
                                            {0} {1}.{2}
                                            SET carNumber = {3},
                                            carType = {4},
                                            uuidForPatrulCar = {5}
                                            WHERE uuid = {6};
                                            """,
                                            CassandraCommands.UPDATE,
                                            CassandraTables.TABLETS,
                                            CassandraTables.PATRULS,

                                            super.joinWithAstrix( reqCar.getGosNumber() ),
                                            super.joinWithAstrix( reqCar.getVehicleType() ),

                                            reqCar.getUuid(),
                                            reqCar.getPatrulId()
                                    ),

                                    /*
                                    сохраняем данные самой машины
                                    */
                                    MessageFormat.format(
                                            """
                                            {0} {1}.{2} {3}
                                            VALUES ( {4}, {5}, {6}, {7}, {8}, {9}, {10}, {11}, {12}, {13}, {14}, {15}, {16} );
                                            """,
                                            CassandraCommands.INSERT_INTO,
                                            CassandraTables.TABLETS,
                                            CassandraTables.CARS,

                                            super.getALlParamsNamesForClass.apply( ReqCar.class ),
                                            CassandraFunctions.UUID,

                                            reqCar.getLustraId(),

                                            super.joinWithAstrix( reqCar.getGosNumber() ),
                                            super.joinWithAstrix( reqCar.getTrackerId() ),
                                            super.joinWithAstrix( reqCar.getVehicleType() ),
                                            super.joinWithAstrix( reqCar.getCarImageLink() ),
                                            super.joinWithAstrix( reqCar.getPatrulPassportSeries() ),

                                            reqCar.getSideNumber(),
                                            reqCar.getSimCardNumber(),
                                            reqCar.getLatitude(),
                                            reqCar.getLongitude(),
                                            reqCar.getAverageFuelSize(),
                                            reqCar.getAverageFuelConsumption()
                                    ),
                                    CassandraCommands.APPLY_BATCH
                            )
                    ).wasApplied()
                    ? super.function(
                            Map.of( "message", super.getSuccessMessage( "Car", "saved" ) ) )
                    : super.errorResponse( super.getFailMessage( "Car" ) )
                    : super.errorResponse( super.getFailMessage( "trackerId" ) )
                    .doOnError( this::close);

    public final Function< UUID, Mono< Patrul > > getPatrulByUUID = uuid -> super.convert(
            Patrul.empty().generate( this.getRowFromTabletsKeyspace( CassandraTables.PATRULS, "uuid", uuid.toString() ) ) );

    // обновляет время последней активности патрульного
    public final BiConsumer< Patrul, StringBuilder > updatePatrulAfterTask = ( patrul, query ) ->
            this.getSession().execute(
                    query.append(
                            MessageFormat.format(
                                    """
                                    {0} {1}.{2}
                                    SET patrulTaskInfo = {3},
                                    patrulUniqueValues = {4},
                                    patrulLocationData = {5},
                                    patrulDateData = {6}
                                    WHERE uuid = {7};
                                    """,
                                    CassandraCommands.UPDATE,
                                    CassandraTables.TABLETS,
                                    CassandraTables.PATRULS,

                                    super.convertClassToCassandraTable.apply( patrul.getPatrulTaskInfo() ),
                                    super.convertClassToCassandraTable.apply( patrul.getPatrulUniqueValues() ),
                                    super.convertClassToCassandraTable.apply( patrul.getPatrulLocationData() ),
                                    super.convertClassToCassandraTable.apply( patrul.getPatrulDateData() ),
                                    patrul.getUuid()
                            )
                    ).append( CassandraCommands.APPLY_BATCH ).toString()
            );

    // обновляет время последней активности патрульного
    public final Consumer< Patrul > updatePatrulActivity = patrul ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            SET lastActiveDate = {3}, totalActivityTime = {4,number,#}
                            WHERE uuid = {5};
                            """,
                            CassandraCommands.UPDATE,
                            CassandraTables.TABLETS,
                            CassandraTables.PATRULS,

                            CassandraFunctions.TO_TIMESTAMP.formatted( CassandraFunctions.NOW ),
                            Math.abs( patrul.getTotalActivityTime()
                                    + super.getTimeDifference(
                                            patrul
                                                    .getPatrulDateData()
                                                    .getLastActiveDate()
                                                    .toInstant(), 3 ) ),
                            patrul.getUuid()
                    )
            );

    public final Function< Patrul, Mono< ApiResponseModel > > updatePatrul = patrul -> {
            final Optional< Row > rowOptional = Optional.ofNullable(
                    this.getRowFromTabletsKeyspace(
                            CassandraTables.PATRULS,
                            "passportNumber",
                            super.joinWithAstrix( patrul.getPassportNumber() )
                    )
            );

            if ( rowOptional.isEmpty() ) {
                return super.errorResponse( super.getFailMessage( "patrul" ) );
            }

            if ( rowOptional.get().getUUID( "uuid" ).compareTo( patrul.getUuid() ) == 0 ) {
                return this.getSession().execute(
                        MessageFormat.format(
                                    """
                                    {0} {1}.{2} {3}
                                    {4} ( {5}, {6,number,#}, {7}, {8},
                                    {9}, {10}, {11}, {12}, {13}, {14}, {15},
                                    {16}, {17}, {18}, {19}, {20}, {21}, {22}, {23}, {24}, {25} ),
                                    """,
                                    CassandraCommands.INSERT_INTO,
                                    CassandraTables.TABLETS,
                                    CassandraTables.PATRULS,
                                    super.getALlParamsNamesForClass.apply( Patrul.class ),

                                    "VALUES",
                                    patrul.getUuid(),
                                    patrul.getTotalActivityTime(),

                                    patrul.getInPolygon(),
                                    patrul.getTuplePermission(),

                                    super.joinWithAstrix( patrul.getRank() ),
                                    super.joinWithAstrix( patrul.getEmail() ),
                                    super.joinWithAstrix( patrul.getOrganName() ),
                                    super.joinWithAstrix( patrul.getPoliceType() ),
                                    super.joinWithAstrix( patrul.getDateOfBirth() ),
                                    super.joinWithAstrix( patrul.getPassportNumber() ),
                                    super.joinWithAstrix( patrul.getPatrulImageLink() ),

                                    super.convertClassToCassandraTable.apply( patrul.getPatrulFIOData() ),
                                    super.convertClassToCassandraTable.apply( patrul.getPatrulCarInfo() ),
                                    super.convertClassToCassandraTable.apply( patrul.getPatrulDateData() ),
                                    super.convertClassToCassandraTable.apply( patrul.getPatrulAuthData() ),
                                    super.convertClassToCassandraTable.apply( patrul.getPatrulTaskInfo() ),
                                    super.convertClassToCassandraTable.apply( patrul.getPatrulTokenInfo() ),
                                    super.convertClassToCassandraTable.apply( patrul.getPatrulRegionData() ),
                                    super.convertClassToCassandraTable.apply( patrul.getPatrulLocationData() ),
                                    super.convertClassToCassandraTable.apply( patrul.getPatrulUniqueValues() ),
                                    super.convertClassToCassandraTable.apply( patrul.getPatrulMobileAppInfo() ) ) )
                        .wasApplied()
                        ? super.function( Map.of( "message", super.getSuccessMessage( "Patrul", "updated" ) ) )
                        : super.errorResponse( super.getFailMessage( "Patrul" ) );
            }
            else {
                return super.errorResponse( super.getFailMessage( "Patrul" ) );
            }
    };

    // обновляет фото или номер патрульного
    public final BiFunction< PatrulImageRequest, Integer, Mono< ApiResponseModel > > updatePatrulImage = ( request, integer ) ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            SET {3} = {4}
                            WHERE uuid = {5};
                            """,
                            CassandraCommands.UPDATE,
                            CassandraTables.TABLETS,
                            CassandraTables.PATRULS,
                            ( integer == 0 ? "patrulImageLink" : "patrulMobileAppInfo.phoneNumber" ),
                            request.getNewImage(),
                            request.getPatrulUUID() ) )
                    .wasApplied()
                    ? super.function( Map.of( "message", super.getSuccessMessage( "Image", "updated" ) ) )
                    : super.convert( super.errorResponse() );

    public final Function< UUID, Mono< ApiResponseModel > > deletePatrul = uuid -> this.getPatrulByUUID.apply( uuid )
            .flatMap( patrul -> {
                if ( super.checkObject( patrul ) ) {
                    this.getSession().execute(
                            MessageFormat.format(
                                    """
                                    {0} {1} {2} {3} {4} {5}
                                    """,
                                    /*
                                    отсоединиям патрульного от весх выполненных задач
                                    */
                                    CassandraDataControlForTasks
                                            .getInstance()
                                            .unlinkPatrulFromAllCompletedTasks
                                            .apply( patrul )
                                            .toString(),

                                    /*
                                    удаляем данные из таблицы с логинами
                                    */
                                    MessageFormat.format(
                                            """
                                            {0} {1}.{2}
                                            WHERE login = {3} AND uuid = {4};
                                            """,
                                            CassandraCommands.DELETE,
                                            CassandraTables.TABLETS,
                                            CassandraTables.PATRULS_LOGIN_TABLE,
                                            patrul.getPatrulAuthData().getLogin(),
                                            patrul.getUuid()
                                    ),

                                    /*
                                    удаляем данные из таблицы с патрульными
                                    */
                                    MessageFormat.format(
                                            """
                                            {0} {1}.{2}
                                            WHERE uuid = {3};
                                            """,
                                            CassandraCommands.DELETE,
                                            CassandraTables.TABLETS,
                                            CassandraTables.PATRULS,
                                            patrul.getUuid()
                                    ),

                                    /*
                                    удаляем данные о передвижениях патрульного
                                    */
                                    MessageFormat.format(
                                            """
                                            {0} {1}.{2} WHERE userId = {3};
                                            """,
                                            CassandraCommands.DELETE,
                                            CassandraTables.GPSTABLETS,
                                            CassandraTables.TABLETS_LOCATION_TABLE,
                                            patrul.getPassportNumber()
                                    ),

                                    /*
                                    удаляем данные о сос сигналах патрульного
                                    */
                                    CassandraDataControlForTasks
                                            .getInstance()
                                            .deletePatrulSosSignals
                                            .apply( patrul )
                                            .toString(),

                                    CassandraCommands.APPLY_BATCH
                            )
                    );

                    return super.function( Map.of( "message", super.getSuccessMessage( "Patrul", "deleted" ) ) );
                }

                else {
                    return super.errorResponse( super.getFailMessage( "Patrul" ) );
                }
            } )
            .doOnError( this::close);

    public final Function< Patrul, Mono< ApiResponseModel > > savePatrul = patrul -> {
            /*
            проверяем что такой серии паспорта не существует
            */
            if ( !super.objectIsNotNull(
                    this.getRowFromTabletsKeyspace(
                            CassandraTables.PATRULS,
                            "passportNumber",
                            super.joinWithAstrix( patrul.getPassportNumber() ) ) )
            ) {
                /*
                присваиваем объектам начальные значения по деволту
                */
                patrul.setDefaultValuesInTheBeginning();

                /*
                проверяем что такого логина не существует
                */
                if ( this.checkLogin
                        .apply( super.joinWithAstrix( patrul.getPatrulAuthData().getLogin() ) )
                        .isPresent() ) {
                    return super.errorResponse( super.getFailMessage( "Login" ) );
                }

                this.getSession().execute(
                        MessageFormat.format(
                                """
                                {0}
                                {1}
                                {2}
                                {3}
                                {4}
                                """,
                                CassandraCommands.BEGIN_BATCH,

                                /*
                                сохраняем данные логина патрульного
                                */
                                MessageFormat.format(
                                        """
                                        {0} {1}.{2}
                                        ( login, password, uuid ) -- сохраняем данные логина патрульного
                                        VALUES( {3}, {4}, {5} );
                                        """,
                                        CassandraCommands.INSERT_INTO,
                                        CassandraTables.TABLETS,
                                        CassandraTables.PATRULS_LOGIN_TABLE,

                                        super.joinWithAstrix( patrul.getPatrulAuthData().getLogin() ),
                                        super.joinWithAstrix( patrul.getPatrulAuthData().getPassword() ),
                                        patrul.getUuid()
                                ),

                                /*
                                создаем запись для патрульного в таблице для СОС сигналов
                                */
                                MessageFormat.format(
                                        """
                                        {0} {1}.{2}
                                        ( patruluuid, sentSosList, attachedSosList, cancelledSosList, acceptedSosList )
                                        VALUES ( {3}, {4}, {4}, {4}, {4} );
                                        """,
                                        CassandraCommands.INSERT_INTO,
                                        CassandraTables.TABLETS,
                                        CassandraTables.PATRUL_SOS_LIST,
                                        patrul.getUuid(),
                                        "{}"
                                ),

                                /*
                                сохраняем сами данные патрульного
                                */
                                MessageFormat.format(
                                        """
                                        {0} {1}.{2} {3}
                                        {4} ( {5}, {6}, {7}, {8},
                                        {9}, {10}, {11}, {12}, {13}, {14}, {15},
                                        {16}, {17}, {18}, {19}, {20}, {21}, {22}, {23}, {24}, {25} );
                                        """,
                                        CassandraCommands.INSERT_INTO,
                                        CassandraTables.TABLETS,
                                        CassandraTables.PATRULS,
                                        super.getALlParamsNamesForClass.apply( Patrul.class ),

                                        "VALUES",
                                        patrul.getUuid(),
                                        patrul.getTotalActivityTime(),

                                        patrul.getInPolygon(),
                                        patrul.getTuplePermission(),

                                        super.joinWithAstrix( patrul.getRank() ),
                                        super.joinWithAstrix( patrul.getEmail() ),
                                        super.joinWithAstrix( patrul.getOrganName() ),
                                        super.joinWithAstrix( patrul.getPoliceType() ),
                                        super.joinWithAstrix( patrul.getDateOfBirth() ),
                                        super.joinWithAstrix( patrul.getPassportNumber() ),
                                        super.joinWithAstrix( patrul.getPatrulImageLink() ),

                                        super.convertClassToCassandraTable.apply( patrul.getPatrulFIOData() ),
                                        super.convertClassToCassandraTable.apply(
                                                super.objectIsNotNull( patrul.getPatrulCarInfo() )
                                                        ? patrul.getPatrulCarInfo()
                                                        : PatrulCarInfo.empty()
                                        ),
                                        super.convertClassToCassandraTable.apply( patrul.getPatrulDateData() ),
                                        super.convertClassToCassandraTable.apply( patrul.getPatrulAuthData() ),
                                        super.convertClassToCassandraTable.apply( patrul.getPatrulTaskInfo() ),
                                        super.convertClassToCassandraTable.apply( patrul.getPatrulTokenInfo() ),
                                        super.convertClassToCassandraTable.apply( patrul.getPatrulRegionData() ),
                                        super.convertClassToCassandraTable.apply( patrul.getPatrulLocationData() ),
                                        super.convertClassToCassandraTable.apply( patrul.getPatrulUniqueValues() ),
                                        super.convertClassToCassandraTable.apply( patrul.getPatrulMobileAppInfo() )
                                ),

                                CassandraCommands.APPLY_BATCH
                        )
                );

                return super.function( Map.of( "message", super.getSuccessMessage( "Patrul", "saved" ) ) );
            }
            else {
                return super.errorResponse( super.getFailMessage( "Login" ) );
            }
    };

    public final Function< String, Mono< Polygon > > getPolygonForPatrul = id -> super.convert(
            new Polygon(
                    this.getRowFromTabletsKeyspace(
                            CassandraTables.POLYGON_FOR_PATRUl,
                            "uuid",
                            id
                    )
            ) );

    public final Function< String, Mono< ApiResponseModel > > deletePolygonForPatrul = id -> this.getPolygonForPatrul
            .apply( id )
            .map( polygon1 -> {
                super.analyze(
                        polygon1.getPatrulList(),
                        uuid -> this.getSession().executeAsync(
                                MessageFormat.format(
                                        """
                                        {0} {1}.{2}
                                        SET inPolygon = {3}
                                        WHERE uuid = {4};
                                        """,
                                        CassandraCommands.UPDATE,
                                        CassandraTables.TABLETS,
                                        CassandraTables.PATRULS,
                                        false,
                                        uuid
                                )
                        )
                );
                return polygon1;
            } )
            .flatMap( polygon1 -> {
                this.getSession().execute(
                        MessageFormat.format(
                                """
                                {0} {1}.{2} WHERE uuid = {3};
                                """,
                                CassandraCommands.DELETE,
                                CassandraTables.TABLETS,
                                CassandraTables.POLYGON_FOR_PATRUl,
                                id
                        )
                );
                return super.function( Map.of( "message", "Polygon " + id + " successfully deleted" ) );
            } );

    public final Function< Polygon, Mono< ApiResponseModel > > addPolygonForPatrul = polygon ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2} {3}
                            VALUES ( {4}, {5}, {6,number,#}, {7,number,#}, {8,number,#}, {9}, {10}, {11}, {12}, {13} );
                            """,
                            CassandraCommands.INSERT_INTO,
                            CassandraTables.TABLETS,
                            CassandraTables.POLYGON_FOR_PATRUl,

                            super.getALlParamsNamesForClass.apply( Polygon.class ),

                            CassandraFunctions.UUID,
                            polygon.getOrgan(),

                            polygon.getRegionId(),
                            polygon.getMahallaId(),
                            polygon.getDistrictId(),

                            super.joinWithAstrix( polygon.getName() ),
                            super.joinWithAstrix( ( polygon.getColor() == null ? "Qizil" : polygon.getColor() ) ),

                            super.convertClassToCassandraTable.apply( polygon.getPolygonType() ),
                            super.convertListToCassandra.apply( polygon.getPatrulList() ),
                            super.convertListOfPointsToCassandra.apply( polygon.getLatlngs() ) ) )
            .wasApplied()
            ? super.function( Map.of( "message", super.getSuccessMessage( "Polygon", "saved" ) ) )
            : super.errorResponse( "This polygon has already been created" )
            .doOnError( this::close);

    public final Function< Polygon, Mono< ApiResponseModel > > updatePolygonForPatrul = polygon -> this.getPolygonForPatrul
            .apply( polygon.getUuid().toString() )
            .map( polygon1 -> {
                polygon.getPatrulList().addAll( polygon1.getPatrulList() );

                super.analyze(
                        polygon.getPatrulList(),
                        uuid -> this.getSession().executeAsync(
                                MessageFormat.format(
                                        """
                                        {0} {1}.{2}
                                        SET inPolygon = {3}
                                        WHERE uuid = {4};
                                        """,
                                        CassandraCommands.UPDATE,
                                        CassandraTables.TABLETS,
                                        CassandraTables.PATRULS,
                                        true,
                                        uuid
                                )
                        ) );

                return polygon;
            } )
            .flatMap( polygon1 -> this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2} {3}
                            VALUES ( {4}, {5}, {6,number,#}, {7,number,#}, {8,number,#}, {9}, {10}, {11}, {12}, {13} );
                            """,
                            CassandraCommands.INSERT_INTO,
                            CassandraTables.TABLETS,
                            CassandraTables.POLYGON_FOR_PATRUl,
                            super.getALlParamsNamesForClass.apply( Polygon.class ),

                            polygon.getUuid(),
                            polygon.getOrgan(),

                            polygon.getRegionId(),
                            polygon.getMahallaId(),
                            polygon.getDistrictId(),

                            super.joinWithAstrix( polygon.getName() ),
                            super.joinWithAstrix( polygon.getColor() ),

                            super.convertClassToCassandraTable.apply( polygon.getPolygonType() ),
                            super.convertListToCassandra.apply( polygon.getPatrulList() ),
                            super.convertListOfPointsToCassandra.apply( polygon.getLatlngs() ) ) )
                    .wasApplied()
                    ? super.function( Map.of( "message", super.getSuccessMessage( "Polygon", "updated" ) ) )
                    : super.errorResponse( "This polygon has already been created" )
                    .doOnError( this::close) );

    public final Function< PatrulActivityRequest, Mono< PatrulActivityStatistics > > getPatrulStatistics = request ->
            this.getPatrulByUUID.apply( UUID.fromString( request.getPatrulUUID() ) )
                    .flatMap( patrul -> super.convertValuesToParallelFlux(
                            this.getSession().execute(
                                    MessageFormat.format(
                                            """
                                            {0} {1}.{2}
                                            WHERE uuid = {3} {4};
                                            """,
                                            CassandraCommands.SELECT_ALL,
                                            CassandraTables.TABLETS,
                                            CassandraTables.PATRULS_STATUS_TABLE,

                                            patrul.getUuid(),
                                            ( super.objectIsNotNull( request.getEndDate() )
                                                    && super.objectIsNotNull( request.getStartDate() )
                                                    ? " AND date >= " + super.joinWithAstrix( request.getStartDate() )
                                                    + " AND date <= " + super.joinWithAstrix( request.getEndDate() )
                                                    : "" )
                                    )
                                ),
                                (int) Math.abs(
                                        Duration.between(
                                            request.getStartDate().toInstant(),
                                            request.getEndDate().toInstant()
                                        ).toDays()
                                )
                            ).filter( row -> Status.valueOf( row.getString( "status" ) ).isLogout() )
                            .map( row -> row.getLong( "totalActivityTime" ) )
                            .sequential()
                            .publishOn( Schedulers.single() )
                            .collectList()
                            .map( longs -> PatrulActivityStatistics
                                    .builder()
                                    .dateList( longs )
                                    .patrul( patrul )
                                    .build() ) );

    public final Function< ScheduleForPolygonPatrul, Mono< ApiResponseModel > > addPatrulToPolygon =
            scheduleForPolygonPatrul -> this.getPolygonForPatrul.apply( scheduleForPolygonPatrul.getUuid() )
                    .flatMap( polygon -> super.convertValuesToParallelFlux(
                                    scheduleForPolygonPatrul
                                            .getPatrulUUIDs()
                                            .stream(),
                                    scheduleForPolygonPatrul.getPatrulUUIDs().size()
                            ).flatMap( this.getPatrulByUUID )
                            .map( patrul -> {
                                this.getSession().executeAsync(
                                        MessageFormat.format(
                                                """
                                                {0} {1}.{2}
                                                SET inPolygon = {3}
                                                WHERE uuid = {4};
                                                """,
                                                CassandraCommands.UPDATE,
                                                CassandraTables.TABLETS,
                                                CassandraTables.PATRULS,
                                                true,
                                                patrul.getUuid()
                                        )
                                );

                                return patrul.getUuid();
                            } )
                            .sequential()
                            .publishOn( Schedulers.single() )
                            .collectList()
                            .flatMap( uuidList -> this.updatePolygonForPatrul.apply( polygon.setPatrulList( uuidList ) ) ) );

    public final Supplier< Flux< Notification > > getUnreadNotifications = () -> super.convertValuesToParallelFlux(
                this.getSession().execute(
                        MessageFormat.format(
                                """
                                {0} {1}.{2} WHERE wasread = {3};
                                """,
                                CassandraCommands.SELECT_ALL,
                                CassandraTables.TABLETS,
                                CassandraTables.NOTIFICATION,
                                false
                        )
                )
            ).map( Notification::generate )
            .sequential()
            .publishOn( Schedulers.single() );

    public final Supplier< Mono< Long > > getUnreadNotificationQuantity = () -> super.convert(
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2} WHERE wasread ={3};
                            """,
                            CassandraCommands.SELECT_ALL.replaceAll( "[*]", "COUNT(*) as quantity" ),
                            CassandraTables.TABLETS,
                            CassandraTables.NOTIFICATION,
                            false
                    ) )
                    .one()
                    .getLong( "quantity" )
    );

    public final Function< UUID, Mono< ApiResponseModel > > setNotificationAsRead = uuid ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            SET wasRead = {3}
                            WHERE uuid = {4};
                            """,
                            CassandraCommands.UPDATE,

                            CassandraTables.TABLETS,
                            CassandraTables.NOTIFICATION,

                            false,
                            uuid
                    ) )
                    .wasApplied()
                    ? super.function( Map.of( "message", "Notification " + uuid + " was updated successfully" ) )
                    : super.errorResponse( "Notification was not updated" );

    public Mono< ApiResponseModel > deleteRow (
            final String table,
            final String param,
            final String id
    ) {
        this.getSession().execute (
                MessageFormat.format(
                        """
                        {0} {1}.{2} WHERE {3} = {4};
                        """,
                        CassandraCommands.DELETE,
                        CassandraTables.TABLETS,

                        table,
                        param,
                        id
                )
        );

        return super.function( Map.of( "message", "Deleting has been finished successfully" ) );
    }

    public final BiFunction< Point, Integer, Flux< Patrul > > findTheClosestPatruls = ( point, integer ) ->
            this.getAllEntities.apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
                    .filter( row -> super.checkPatrulLocation( row )
                            && ( integer != 1 || Status.valueOf( row.getString( "status" ) ).compareTo( Status.FREE ) == 0
                            && TaskTypes.valueOf( row.getString( "taskTypes" ) ).compareTo( TaskTypes.FREE ) == 0 ) )
                    .map( Patrul::new )
                    .map( patrul -> {
                        patrul.getPatrulLocationData().setDistance( super.calculate( point, patrul ) );
                        return patrul;
                    } )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .sort( Comparator.comparing( patrul -> patrul.getPatrulLocationData().getDistance() ) );

    public final BiFunction< Point, UUID, Flux< Patrul > > findTheClosestPatrulsForSos = ( point, uuid ) ->
            this.getAllEntities
                    .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
                    .filter( row -> super.checkPatrulLocation( row )
                            && row.getUUID( "uuid" ).compareTo( uuid ) != 0
                            && row.getUUID( "uuidOfEscort" ) == null )
                    .map( Patrul::new )
                    .map( patrul -> {
                        patrul.getPatrulLocationData().setDistance( super.calculate( point, patrul ) );
                        return patrul;
                    } )
                    .sequential()
                    .publishOn( Schedulers.single() )
                    .sort( Comparator.comparing( patrul -> patrul.getPatrulLocationData().getDistance() ) )
                    .take( 20 );

    public final BiFunction< Patrul, Status, Boolean > updatePatrulStatus = ( patrul, status ) -> {
        final String message = super.getMessage( status );

        return this.getSession().executeAsync(
                MessageFormat.format(
                        """
                        {0} {1}.{2}
                        ( uuid, date, status, message, totalActivityTime )
                        VALUES( {3}, {4}, {5}, {6}, {7,number,#} );
                        """,
                        CassandraCommands.INSERT_INTO,
                        CassandraTables.TABLETS,
                        CassandraTables.PATRULS_STATUS_TABLE,
                        patrul.getUuid(),
                        CassandraFunctions.TO_TIMESTAMP.formatted( CassandraFunctions.NOW ),
                        super.joinWithAstrix( status ),
                        super.joinWithAstrix( message ),
                        patrul.getTotalActivityTime()
                )
        ).isDone();
    };

    /*
    находим записи использования устройства
    по ID патрульного и номеру сим карты устройства
    */
    private final Function< Patrul, TabletUsage > checkTableUsage = patrul ->
            Optional.ofNullable(
                    this.getSession().execute(
                            MessageFormat.format(
                                    """
                                    {0} {1}.{2}
                                    WHERE uuidOfPatrul = {3} AND simCardNumber = {4};
                                    """,
                                    CassandraCommands.SELECT_ALL,
                                    CassandraTables.TABLETS,
                                    CassandraTables.TABLETS_USAGE_TABLE,
                                    patrul.getUuid(),
                                    super.joinWithAstrix( patrul.getPatrulMobileAppInfo().getSimCardNumber() )
                            ) ).one() )
                    .map( TabletUsage::generate )
                    .orElse( null );

    private final BiFunction< Patrul, Status, String > updateStatus = ( patrul, status ) ->
            Optional.ofNullable(
                    this.getSession().execute(
                            MessageFormat.format(
                                """
                                    {0} {1}.{2}
                                    WHERE uuidOfPatrul = {3} AND simCardNumber = {4};
                                    """,
                                    CassandraCommands.SELECT_ALL,
                                    CassandraTables.TABLETS,
                                    CassandraTables.TABLETS_USAGE_TABLE,
                                    patrul.getUuid(),
                                    super.joinWithAstrix( patrul.getPatrulMobileAppInfo().getSimCardNumber() ) ) )
                                    .one() )
                    .map( row -> MessageFormat.format(
                                    """
                                    {0} {1}.{2}
                                    SET lastActiveDate = {3}, {4}
                                    WHERE uuidOfPatrul = {5} AND simCardNumber = {6};
                                    """,
                                    CassandraCommands.UPDATE,
                                    CassandraTables.TABLETS,
                                    CassandraTables.TABLETS_USAGE_TABLE,
                                    CassandraFunctions.TO_TIMESTAMP.formatted( CassandraFunctions.NOW ),
                                    ( status.isLogout()
                                            ? ", totalActivityTime = "
                                            + abs( super.getTimeDifference(
                                                    row.getTimestamp( "startedToUse" ).toInstant(), 0 ) )
                                            : "" ),
                                    patrul.getUuid(),
                                    super.joinWithAstrix( row.getString( "simCardNumber" ) ) ) )
                    .orElse( "" );

    private final Function< String, Optional< Row > > checkLogin = login ->
            Optional.ofNullable(
                    this.getRowFromTabletsKeyspace(
                            CassandraTables.PATRULS_LOGIN_TABLE,
                            "login",
                            login
                    )
            );

    public final Function< PatrulLoginRequest, Mono< ApiResponseModel > > login = patrulLoginRequest ->
            // проверяем логин через базу
            this.checkLogin.apply( super.joinWithAstrix( patrulLoginRequest.getLogin() ) )
                    // если такой логин есть идем дальше
                    .map( row -> {
                        final Patrul patrul = Patrul.empty().generate(
                                this.getRowFromTabletsKeyspace(
                                        CassandraTables.PATRULS,
                                        "uuid",
                                        row.getUUID( "uuid" ).toString()
                                )
                        );

                        /*
                        проверяем првильность введенного пароля
                        */
                        if ( patrul
                                .getPatrulAuthData()
                                .getPassword()
                                .compareTo( patrulLoginRequest.getPassword() ) == 0 ) {
                            /*
                            обновляем дату начала работы
                            */
                            patrul.getPatrulDateData().update( 0 );

                            final StringBuilder stringBuilder = super.newStringBuilder();

                            /*
                            если пользователь поменял устройство,
                            то мы можем проверить это по номеру сим карты,
                            в этом случае меняем эти параметры
                            */
                            if ( super.objectIsNotNull( patrul.getPatrulMobileAppInfo() )
                                    && !patrul.getPatrulMobileAppInfo().getSimCardNumber().isBlank()
                                    && !patrul
                                    .getPatrulMobileAppInfo()
                                    .getSimCardNumber()
                                    .equals( "null" )
                                    && !patrul
                                    .getPatrulMobileAppInfo()
                                    .getSimCardNumber()
                                    .equals( patrulLoginRequest.getSimCardNumber() ) ) {
                                stringBuilder.append( this.updateStatus.apply( patrul, LOGOUT ) );
                            }

                            patrul.update( patrulLoginRequest.getSimCardNumber() );

                            final long time = patrul.getTotalActivityTime()
                                    + super.getTimeDifference(
                                            patrul
                                                    .getPatrulDateData()
                                                    .getLastActiveDate()
                                                    .toInstant(), 3 );

                            stringBuilder.append(
                                    MessageFormat.format(
                                            """
                                             {0} {1}.{2}
                                             SET patrulDateData.startedToWorkDate = {3},
                                             patrulMobileAppInfo.simCardNumber = {4},
                                             patrulTokenInfo.tokenForLogin = {5},
                                             patrulDateData.lastActiveDate = {6},
                                             totalActivityTime = {7,number,#}
                                             WHERE uuid = {8};
                                             """,
                                            CassandraCommands.UPDATE,
                                            CassandraTables.TABLETS,
                                            CassandraTables.PATRULS,
                                            super.joinWithAstrix(
                                                    patrul
                                                            .getPatrulDateData()
                                                            .getStartedToWorkDate() ),
                                            super.joinWithAstrix( patrul.getPatrulMobileAppInfo().getSimCardNumber() ),
                                            super.joinWithAstrix( patrul.getPatrulTokenInfo().getTokenForLogin() ),
                                            CassandraFunctions.TO_TIMESTAMP.formatted( CassandraFunctions.NOW ),
                                            time,
                                            patrul.getUuid()
                                    )
                            );

                            /*
                            находим записи использования данного устройства
                            по ID патрульного и номеру сим карты устройства
                            */
                            final Optional< TabletUsage > optional =
                                    Optional.ofNullable( this.checkTableUsage.apply( patrul ) );

                            /*
                            если пользователь впервые использует устройство,
                            то создаем первую запись
                            */
                            if ( optional.isEmpty() ) {
                                final TabletUsage tabletUsage = TabletUsage.generate( patrul );
                                stringBuilder.append(
                                        MessageFormat.format(
                                                """
                                                {0} {1}.{2} {3}
                                                VALUES ( {4}, {5}, {6}, {7}, {8,number,#} );
                                                """,
                                                CassandraCommands.INSERT_INTO,
                                                CassandraTables.TABLETS,
                                                CassandraTables.TABLETS_USAGE_TABLE,
                                                super.getALlParamsNamesForClass.apply( TabletUsage.class ),
                                                super.joinWithAstrix( tabletUsage.getStartedToUse() ),
                                                super.joinWithAstrix( tabletUsage.getLastActiveDate() ),
                                                tabletUsage.getUuidOfPatrul(),
                                                super.joinWithAstrix( tabletUsage.getSimCardNumber() ),
                                                tabletUsage.getTotalActivityTime() )
                                );
                            }

                            /*
                            а иначе обновляем время использования даннаго устройтсва
                            */
                            else {
                                stringBuilder.append(
                                        MessageFormat.format(
                                                """
                                                {0} {1}.{2}
                                                SET lastActiveDate = {3}
                                                WHERE uuidOfPatrul = {4}
                                                AND simCardNumber = {5};
                                                """,
                                                CassandraCommands.UPDATE,
                                                CassandraTables.TABLETS,
                                                CassandraTables.TABLETS_USAGE_TABLE,
                                                CassandraFunctions.TO_TIMESTAMP.formatted( CassandraFunctions.NOW ),
                                                patrul.getUuid(),
                                                super.joinWithAstrix( patrul.getPatrulMobileAppInfo().getSimCardNumber() ) )
                                );
                            }

                            stringBuilder.append(
                                    MessageFormat.format(
                                            """
                                            {0} {1}.{2}
                                            ( uuid, date, status, message, totalActivityTime )
                                            VALUES( {3}, {4}, {5}, {6}, {7,number,#} );
                                            """,
                                            CassandraCommands.INSERT_INTO,
                                            CassandraTables.TABLETS,
                                            CassandraTables.PATRULS_STATUS_TABLE,
                                            patrul.getUuid(),
                                            CassandraFunctions.TO_TIMESTAMP.formatted( CassandraFunctions.NOW ),
                                            super.joinWithAstrix( LOGIN ),
                                            super.joinWithAstrix( super.getMessage( LOGIN ) ),
                                            patrul.getTotalActivityTime()
                                    )
                            ).append( CassandraCommands.APPLY_BATCH );

                            return super.function(
                                    Map.of( "message", "Authentication successfully passed",
                                            "success", this.getSession().execute( stringBuilder.toString() ),
                                            "data",  Data.from( patrul, patrul.getUuid().toString() ) ) );
                        }

                        else {
                            return super.errorResponse( "Wrong Login or password" );
                        }
                    } )
                    .orElseGet( () -> super.errorResponse( "Wrong Login or password" ) );

    public final BiFunction< String, Status, Mono< ApiResponseModel > > changeStatus = ( token, status ) -> this.getPatrulByUUID
            .apply( super.decode( token ) )
            .flatMap( patrul -> {
                final StringBuilder stringBuilder = super.newStringBuilder();

                stringBuilder.append( this.updateStatus.apply( patrul, status ) );
                stringBuilder.append(
                        MessageFormat.format(
                                """
                                {0} {1}.{2}
                                SET lastActiveDate = {3}, totalActivityTime = {4,number,#}
                                WHERE uuid = {5};
                                """,
                                CassandraCommands.UPDATE,
                                CassandraTables.TABLETS,
                                CassandraTables.PATRULS,
                                CassandraFunctions.TO_TIMESTAMP.formatted( CassandraFunctions.NOW ),
                                Math.abs( patrul.getTotalActivityTime()
                                        + super.getTimeDifference(
                                                patrul
                                                    .getPatrulDateData()
                                                    .getLastActiveDate()
                                                    .toInstant(), 3 ) ),
                                patrul.getUuid()
                        )
                );

                if ( status.isStartToWork() ) {
                    patrul.getPatrulDateData().update( 0 ); // registration of time every day

                    stringBuilder.append(
                            MessageFormat.format(
                                    """
                                    {0} {1}.{2}
                                    SET startedToWorkDate = {3}
                                    WHERE uuid = {4};
                                    """,
                                    CassandraCommands.UPDATE,
                                    CassandraTables.TABLETS,
                                    CassandraTables.PATRULS,
                                    super.joinWithAstrix( patrul.getPatrulDateData().getStartedToWorkDate() ),
                                    patrul.getUuid()
                            )
                    ).append(
                            MessageFormat.format(
                                    """
                                    {0} {1}.{2}
                                    ( uuid, date, status, message, totalActivityTime )
                                    VALUES( {3}, {4}, {5}, {6}, {7,number,#} );
                                    """,
                                    CassandraCommands.INSERT_INTO,
                                    CassandraTables.TABLETS,
                                    CassandraTables.PATRULS_STATUS_TABLE,
                                    patrul.getUuid(),
                                    CassandraFunctions.TO_TIMESTAMP.formatted( CassandraFunctions.NOW ),
                                    status,
                                    super.getMessage( START_TO_WORK ),
                                    patrul.getTotalActivityTime()
                            )
                    ).append( CassandraCommands.APPLY_BATCH );

                    return super.function(
                            Map.of( "message", "Patrul: " + START_TO_WORK,
                                    "success", this.getSession().execute( stringBuilder.toString() ) ) );
                }

                else if ( status.isArrived() ) {
                    /*
                    проверяем что патрульный добрался до пункта назначения менее чем за 24 часа
                    */
                    if ( super.checkPatrulCameInTime( patrul.getPatrulDateData().getTaskDate() ) ) {
                        /*
                        если нет, то убираем его от задачи
                        */
                        stringBuilder.append( this.updateStatus.apply( patrul, CANCEL ) );

                        this.getSession().execute( stringBuilder.toString() );

                        /*
                        и возвращаем сообщение
                        */
                        return TaskInspector
                                .getInstance()
                                .getTaskData
                                .apply( patrul, TaskTypes.FREE )
                                .flatMap( apiResponseModel -> super.errorResponse(
                                        "You were removed from task, due to fact that u r late for more then 24 hours" ) );
                    } else {
                        return TaskInspector
                                .getInstance()
                                .changeTaskStatus
                                .apply( patrul, ARRIVED );
                    }
                }

                else if ( status.isLogout() ) {
                    patrul.update();
                    return this.updatePatrul.apply( patrul )
                            .flatMap( aBoolean -> super.function(
                                    Map.of( "message", "See you soon my darling )))",
                                            "success", this.updatePatrulStatus.apply( patrul, LOGOUT ) ) ) );
                }

                else if ( status.isAccepted() ) {
                    return TaskInspector
                            .getInstance()
                            .changeTaskStatus
                            .apply( patrul, ACCEPTED );
                }

                else {
                    return super.function(
                            Map.of( "message", "Patrul: " + status,
                                    "success", this.updatePatrulStatus.apply( patrul, status ) ) );
                }
            } );

    public final Function< String, Mono< ApiResponseModel > > checkToken = token -> this.getPatrulByUUID
            .apply( super.decode( token ) )
            .flatMap( patrul -> super.function(
                    Map.of( "message", patrul.getUuid().toString(),
                            "success", this.updatePatrulStatus.apply( patrul, Status.LOGIN ),
                            "data", Data.from( patrul ) ) ) );

    public final BiFunction< UUID, PatrulActivityRequest, Mono< List< TabletUsage > > > getAllUsedTablets = ( uuid, request ) ->
            super.convertValuesToParallelFlux(
                    this.getSession().execute(
                            MessageFormat.format(
                                    """
                                    {0} {1}.{2} WHERE uuidOfPatrul = {3};
                                    """,
                                    CassandraCommands.SELECT_ALL,
                                    CassandraTables.TABLETS,
                                    CassandraTables.TABLETS_USAGE_TABLE,
                                    uuid
                            )
                    ),
                    super.checkDifference( uuid.toString().length() )
            ).filter( row -> !super.checkObject( request ) || super.checkTabletUsage( row, request ) )
            .map( TabletUsage::generate )
            .sequential()
            .publishOn( Schedulers.single() )
            .collectList();

    public final Consumer< String > addAllPatrulsToChatService = token -> this.getAllEntities
            .apply( CassandraTables.TABLETS, CassandraTables.PATRULS )
            .map( Patrul::new )
            .sequential()
            .publishOn( Schedulers.single() )
            .collectList()
            .subscribe( new CustomSubscriber<>(
                    patruls -> super.analyze(
                            patruls,
                            patrul -> {
                                patrul.getPatrulTokenInfo().setSpecialToken( token );
                                UnirestController
                                        .getInstance()
                                        .addUser
                                        .accept( patrul );
                            }
                    )
            ) );

    /*
    возвращает список патрульных которые макс близко к камере
     */
    public final Function< Point, Mono< PatrulInRadiusList > > getPatrulInRadiusList = point ->
            this.findTheClosestPatruls
                    .apply( point, 2 )
                    .collectList()
                    .map( PatrulInRadiusList::new );

    // проверяет последнюю версию андроид приложения
    public final Function< String, Mono< ApiResponseModel > > checkVersionForAndroid = version -> {
            final Row row = this.getRowFromTabletsKeyspace(
                    CassandraTables.ANDROID_VERSION_CONTROL_TABLE,
                    "id",
                    super.joinWithAstrix( "id" )
            );

            byte check = 0;
            final String[] fromAndroid = version.contains( "DEBUG" )
                    ? ( version.split( "-DEBUG" )[ 0 ] ).split( "[.]" )
                    : version.split( "[.]" );

            final String[] versionInDb = row.getString( "version" ).split( "[.]" );
            while ( check < fromAndroid.length && Integer.parseInt( fromAndroid[ check ] ) >= Integer.parseInt( versionInDb[ check ] ) ) check++;

            return check == fromAndroid.length
                    ? super.function(
                            Map.of( "message", "you have the last version",
                                "data", Data.from( AndroidVersionUpdate.generate( row, LAST ) ) ) )
                    : super.function(
                            Map.of( "message", "you have to update to last version",
                                    "data", Data.from( AndroidVersionUpdate.generate( row, FORCE ) ) ) );
    };

    // обновляет последнюю версию андроид приложения
    public final Function< AndroidVersionUpdate, Mono< ApiResponseModel > > saveLastVersion = androidVersionUpdate ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            SET version = {3}, link = {4}
                            WHERE id = 'id';
                            """,
                            CassandraCommands.UPDATE,
                            CassandraTables.TABLETS,
                            CassandraTables.ANDROID_VERSION_CONTROL_TABLE,
                            androidVersionUpdate.getVersion(),
                            androidVersionUpdate.getLink() ) )
                    .wasApplied()
                    ? super.function( Map.of( "message", "Last version was saved" ) )
                    : super.errorResponse( "Error during the saving of version" );

    /*
    возвращает последнюю версию приложения
    */
    public final Supplier< Mono< ApiResponseModel > > getLastVersion = () -> super.function(
            Map.of( "message", "you have to update to last version",
                    "data", com.ssd.mvd.gpstabletsservice.entity.Data.from(
                            AndroidVersionUpdate.generate(
                                    this.getRowFromTabletsKeyspace(
                                            CassandraTables.ANDROID_VERSION_CONTROL_TABLE,
                                            "id",
                                            "id"
                                    ),
                                    LAST ) ) ) );

    public final Function< PatrulActivityRequest, Mono< ApiResponseModel > > getTabletUsageStatistics = patrulActivityRequest ->
            super.convert( new TabletUsageStatistics() )
                    .flatMap( tabletUsageStatistics -> {
                        if ( !super.checkObject( patrulActivityRequest ) ) {
                            /*
                            получаем первый день года
                            */
                            final Date startOfYear = super.getYearStartOrEnd( true );

                            /*
                            получаем конец года
                            */
                            final Date endOfYear = super.getYearStartOrEnd( false );

                            tabletUsageStatistics.setMap();
                            return super.convertValuesToParallelFlux(
                                    this.getSession().execute(
                                            MessageFormat.format(
                                                    """
                                                    {0} {1}.{2}
                                                    WHERE uuidofpatrul = {3};
                                                    """,
                                                    CassandraCommands.SELECT_ALL,
                                                    CassandraTables.TABLETS,
                                                    CassandraTables.TABLETS_USAGE_TABLE,
                                                    patrulActivityRequest.getPatrulUUID() )
                                    ) )
                                    .filter( row -> row.getTimestamp( "startedtouse" ).after( startOfYear )
                                            && row.getTimestamp( "startedtouse" ).before( endOfYear ) )
                                    .map( row -> tabletUsageStatistics.update(
                                            row.getTimestamp( "startedtouse" ),
                                            row.getLong( "totalactivitytime" ),
                                            true ) )
                                    .sequential()
                                    .publishOn( Schedulers.single() )
                                    .collectList()
                                    .map( longs -> {
                                        final List< TabletUsageData > tabletUsageDataList = super.newList();

                                        super.analyze(
                                                tabletUsageStatistics.getTabletUsageStatisticsForYear(),
                                                ( key, value ) -> tabletUsageDataList.add( TabletUsageData.generate( value, key.toString() ) )
                                        );

                                        return ApiResponseModel.generate(
                                                Data.from( tabletUsageDataList, tabletUsageStatistics.getTotalCount() )
                                        );
                                    } );
                        }

                        else {
                            return super.convertValuesToParallelFlux(
                                    this.getSession().execute(
                                            MessageFormat.format(
                                                    """
                                                    {0} {1}.{2} WHERE uuidofpatrul = {3};
                                                    """,
                                                    CassandraCommands.SELECT_ALL,
                                                    CassandraTables.TABLETS,
                                                    CassandraTables.TABLETS_USAGE_TABLE,
                                                    patrulActivityRequest.getPatrulUUID()
                                            )
                                    ),
                                    (int) Math.abs( Duration.between(
                                            patrulActivityRequest.getStartDate().toInstant(),
                                            patrulActivityRequest.getEndDate().toInstant() ).toDays() )
                                    ).filter( row -> row.getTimestamp( "startedtouse" ).after( patrulActivityRequest.getStartDate() )
                                            && row.getTimestamp( "startedtouse" ).before( patrulActivityRequest.getEndDate() ) )
                                    .map( row -> tabletUsageStatistics.update(
                                            row.getTimestamp( "startedtouse" ),
                                            row.getLong( "totalactivitytime" ),
                                            false ) )
                                    .sequential()
                                    .publishOn( Schedulers.single() )
                                    .collectList()
                                    .map( longs -> {
                                        final List< TabletUsageData > tabletUsageDataList = super.newList();

                                        super.analyze(
                                                tabletUsageStatistics.getTabletUsageStatisticsForEachDay(),
                                                ( key, value ) -> tabletUsageDataList.add(
                                                        TabletUsageData.generate( value, key.toString() ) )
                                        );

                                        return ApiResponseModel.generate(
                                                Data.from( tabletUsageDataList, tabletUsageStatistics.getTotalCount() )
                                        );
                                    } );
                        }
                    } );

    public final BiFunction< CassandraTables, CassandraTables, ParallelFlux< Row >> getAllEntities =
            ( keyspace, table ) -> super.convertValuesToParallelFlux(
                    this.getSession().execute(
                            MessageFormat.format(
                                    """
                                    {0} {1}.{2};
                                    """,
                                    CassandraCommands.SELECT_ALL,
                                    keyspace,
                                    table
                            )
                    ),
                    Math.abs( keyspace.name().length() + table.name().length() )
            );

    @Override
    public void close( final Throwable throwable ) {
        super.logging( throwable );
        super.logging( this );
        this.close();
    }

    @Override
    public void close() {
        INSTANCE = null;
        super.logging( this );
        this.getCluster().close();
        this.getSession().close();
    }
}