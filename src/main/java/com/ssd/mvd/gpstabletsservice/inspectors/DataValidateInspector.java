package com.ssd.mvd.gpstabletsservice.inspectors;

import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests.PatrulActivityRequest;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests.PatrulLoginRequest;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.DataInfo;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ModelForCar;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.request.AndroidVersionUpdate;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.Pinpp;
import com.ssd.mvd.gpstabletsservice.request.TaskTimingRequest;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.task.sos_task.PatrulSos;
import com.ssd.mvd.gpstabletsservice.tuple.PolygonForEscort;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Point;

import java.nio.charset.StandardCharsets;
import java.util.function.BiPredicate;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.*;

import com.datastax.driver.core.Row;
import static java.lang.Math.cos;
import static java.lang.Math.*;

public class DataValidateInspector extends Archive {
    private static final DataValidateInspector INSTANCE = new DataValidateInspector();

    public static DataValidateInspector getInstance () { return INSTANCE; }

    public final Predicate< Object > checkParam = Objects::nonNull;

    protected final BiPredicate< Status, Status > checkEquality = ( o, b ) -> o.compareTo( b ) == 0;

    public final BiFunction< Object, Integer, String > concatNames = ( o, integer ) -> switch ( integer ) {
            case 0 -> String.join( " ",
                    ( ( Pinpp ) o ).getName(),
                    ( ( Pinpp ) o ).getSurname(),
                    ( ( Pinpp ) o ).getPatronym() );

            case 1 -> String.join( " ",
                    ( (ModelForCar) o ).getModel(),
                    ( (ModelForCar) o ).getVehicleType(),
                    ( (ModelForCar) o ).getColor() );

            case 2 -> String.join( "", String.valueOf( o ).split( "[.]" ) );

            case 4 -> Base64
                    .getEncoder()
                    .encodeToString( String.join( "@",
                            ( (Patrul) o ).getUuid().toString(),
                                    ( (Patrul) o ).getPassportNumber(),
                                    ( (Patrul) o ).getPassword(),
                                    ( (Patrul) o ).getSimCardNumber(),
                                    super.getGenerateToken().get() )
                            .getBytes( StandardCharsets.UTF_8 ) );

            case 5 -> String.join( " ", ( (Patrul) o ).getName(), ( (Patrul) o ).getSurname(), ( (Patrul) o ).getFatherName() );

            default -> String.valueOf( o ).replaceAll( "'", "" ); };

    public final BiPredicate< Object, Integer > checkRequest = ( o, value ) -> switch ( value ) {
            case 1 -> this.checkParam.test( ( (Point) o ).getLatitude() )
                    && this.checkParam.test( ( (Point) o ).getLongitude() );
            case 2 -> this.checkParam.test( ( (PatrulActivityRequest) o ).getStartDate() )
                    && this.checkParam.test( ( (PatrulActivityRequest) o ).getEndDate() );
            case 3 -> ( (Patrul) o ).getTaskId().equals( "null" )
                    && ( (Patrul) o ).getUuidOfEscort() == null
                    && ( (Patrul) o ).getUuidForPatrulCar() == null
                    && ( (Patrul) o ).getUuidForEscortCar() == null
                    && ( (Patrul) o ).getCarNumber().equals( "null" )
                    && ( (Patrul) o ).getTaskTypes().compareTo( TaskTypes.FREE ) == 0;
            case 4 -> this.checkParam.test( ( (PatrulSos) o ).getPatrulStatuses() )
                    && ( (PatrulSos) o ).getPatrulStatuses().size() > 19;
            case 5 -> Math.abs( TimeInspector
                    .getInspector()
                    .getGetTimeDifference()
                    .apply( ( (Date) o ).toInstant(), 1 ) ) >= 24;
            case 6 -> o != null && !( ( List< ? > ) o ).isEmpty();
            case 7 -> this.checkParam.test( ( (AndroidVersionUpdate) o ).getVersion() )
                    && this.checkParam.test( ( (AndroidVersionUpdate) o ).getLink() );
            case 8 -> this.checkParam.test( ( (TaskTimingRequest) o ).getStartDate() )
                    && this.checkParam.test( ( (TaskTimingRequest) o ).getEndDate() );
            case 9 -> this.checkParam.test( o ) && this.checkParam.test( ( (DataInfo) o ).getCadaster() );
            case 10 -> o != null && !( ( Set< ? > ) o ).isEmpty();
            default -> this.checkParam.test( ( (PatrulLoginRequest) o ).getLogin() )
                    && this.checkParam.test( ( (PatrulLoginRequest) o ).getPassword() )
                    && this.checkParam.test( ( (PatrulLoginRequest) o ).getSimCardNumber() ); };

    public Boolean filterPatrul ( final Row row,
                                     final Map< String, String > params,
                                     final List< String > policeTypes,
                                     final Integer value ) {
        return value == 1 ? ( !params.containsKey( "regionId" ) || row.getLong( "regionId" ) == Long.parseLong( params.get( "regionId" ) ) )
                && ( !params.containsKey( "policeType" ) || policeTypes.contains( row.getString( "policeType" ) ) )
                : ( !params.containsKey( "policeType" ) || policeTypes.contains( row.getString( "policeType" ) ) )
                && row.getLong( "regionId" ) == Long.parseLong( params.get( "regionId" ) )
                && ( !params.containsKey( "districtId" ) || row.getLong( "districtId" ) == Long.parseLong( params.get( "districtId" ) ) )
                && switch ( Status.valueOf( params.get( "status" ) ) ) {
                    // активные патрульные
                    case ACTIVE -> !this.checkPatrulActivity.test( row.getUUID( "uuid" ) )
                            && TimeInspector
                            .getInspector()
                            .getGetTimeDifference()
                            .apply( row.getTimestamp( "lastActiveDate" ).toInstant(), 1 ) <= 24;

                    // не активные патрульные
                    case IN_ACTIVE -> !this.checkPatrulActivity.test( row.getUUID( "uuid" ) )
                            && TimeInspector
                            .getInspector()
                            .getGetTimeDifference()
                            .apply( row.getTimestamp( "lastActiveDate" ).toInstant(), 1 ) > 24;

                    // патрульные которые которые никогда не заходили
                    case FORCE -> this.checkPatrulActivity.test( row.getUUID( "uuid" ) );

                    // патрульные которые которые заходили хотя бы раз
                    default -> !this.checkPatrulActivity.test( row.getUUID( "uuid" ) ); }; }

    protected final Function< Integer, Integer > checkDifference = integer -> integer > 0 && integer < 100 ? integer : 10;

    public final BiPredicate< Double, Double > checkDistance = ( distance, patrulDistance ) -> patrulDistance <= distance;

    protected final BiPredicate< Row, PatrulActivityRequest > checkTabletUsage = ( row, request ) ->
            row.getTimestamp( "startedToUse" ).before( request.getEndDate() )
            && row.getTimestamp( "startedToUse" ).after( request.getStartDate() );

    protected final BiPredicate< TaskTimingRequest, Row > checkTaskTimingRequest = ( request, row ) ->
            request.getEndDate() == null
            || request.getStartDate() == null
            || row.getTimestamp( "dateofcoming" ).after( request.getStartDate() )
            && row.getTimestamp( "dateofcoming").before( request.getEndDate() );

    protected final BiPredicate< TaskTimingRequest, Row > checkTaskType = ( request, row ) ->
            request.getTaskType() == null
            || request.getTaskType().isEmpty()
            || request.getTaskType().contains( TaskTypes.valueOf( row.getString( "tasktypes" ) ) );

    protected final Predicate< UUID > checkSosTable = patrulUUID -> CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.SOS_TABLE.name()
                    + " WHERE patrulUUID = " + patrulUUID + ";" )
            .one() == null;

    protected final BiPredicate< String, CassandraTables > checkTable = ( id, tableName ) -> CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS + "."
                    + tableName
                    + " WHERE id = '" + id + "';" ).one() != null;

    // определяет тип таска
    protected final Function< String, CassandraTables > findTable = id -> {
            if ( this.checkTable.test( id, CassandraTables.FACEPERSON ) ) return CassandraTables.FACEPERSON;
            else if ( this.checkTable.test( id, CassandraTables.EVENTBODY ) ) return CassandraTables.EVENTBODY;
            else return CassandraTables.EVENTFACE; };

    // по статусу определяет какой параметр обновлять
    protected final Function< Status, String > defineNecessaryTable = status -> switch ( status ) {
            case ATTACHED -> "attachedSosList";
            case CANCEL -> "cancelledSosList";
            case CREATED -> "sentSosList";
            default -> "acceptedSosList"; };

    protected final Predicate< PolygonForEscort > checkPolygonForEscort = polygon ->
            CassandraDataControlForEscort
                    .getInstance()
                    .getSession()
                    .execute( "SELECT * FROM "
                            + CassandraTables.ESCORT + "."
                            + CassandraTables.POLYGON_FOR_ESCORT
                            + " where uuid = " + polygon.getUuid() + ";" ).one() != null;

    protected final Predicate< String > checkTracker = trackerId -> CassandraDataControl
            .getInstance()
            .getSession()
            .execute ( "SELECT * FROM "
                    + CassandraTables.ESCORT + "."
                    + CassandraTables.TRACKERSID
                    + " WHERE trackersId = '" + trackerId + "';" ).one() == null
            && CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TRACKERS + "."
                    + CassandraTables.TRACKERSID
                    + " WHERE trackersId = '" + trackerId + "';" ).one() == null;

    protected final Predicate< String > checkCarNumber = carNumber -> CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.ESCORT + "."
                    + CassandraTables.TUPLE_OF_CAR +
                    " WHERE gosnumber = '" + carNumber + "';" ).one() == null
            && CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.CARS +
                    " WHERE gosnumber = '" + carNumber + "';" ).one() == null;

    public final Predicate< UUID > checkPatrulActivity = uuid -> CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.TABLETS_USAGE_TABLE
                    + " WHERE uuidofpatrul = " + uuid + ";" )
            .one() == null;

    protected final Predicate< Row > checkPatrulLocation = row -> row.getDouble( "latitude" ) > 0 && row.getDouble( "longitude" ) > 0;

    private static final Double p = PI / 180;

    protected final BiFunction< Point, Patrul, Double > calculate = ( first, second ) ->
            12742 * asin( sqrt( 0.5 - cos( ( second.getLatitude() - first.getLatitude() ) * p ) / 2
                    + cos( first.getLatitude() * p ) * cos( second.getLatitude() * p )
                    * ( 1 - cos( ( second.getLongitude() - first.getLongitude() ) * p ) ) / 2 ) ) * 1000;
}