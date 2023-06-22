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

import java.util.function.BiPredicate;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Function;

import com.datastax.driver.core.Row;
import static java.lang.Math.cos;
import static java.lang.Math.*;

import java.util.Objects;
import java.util.Date;
import java.util.UUID;
import java.util.List;

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

            default -> String.valueOf( o ).replaceAll( "'", "" ); };

    public final BiPredicate< Object, Integer > checkRequest = ( o, value ) -> switch ( value ) {
            case 1 -> ( (Point) o ).getLatitude() != null && ( (Point) o ).getLongitude() != null;
            case 2 -> ( (PatrulActivityRequest) o ).getStartDate() != null && ( (PatrulActivityRequest) o ).getEndDate() != null;
            case 3 -> ( (Patrul) o ).getTaskId().equals( "null" )
                    && ( (Patrul) o ).getUuidOfEscort() == null
                    && ( (Patrul) o ).getUuidForPatrulCar() == null
                    && ( (Patrul) o ).getUuidForEscortCar() == null
                    && ( (Patrul) o ).getCarNumber().equals( "null" )
                    && ( (Patrul) o ).getTaskTypes().compareTo( TaskTypes.FREE ) == 0;
            case 4 -> ( (PatrulSos) o ).getPatrulStatuses() != null && ( (PatrulSos) o ).getPatrulStatuses().size() > 19;
            case 5 -> Math.abs( TimeInspector
                    .getInspector()
                    .getGetTimeDifference()
                    .apply( ( (Date) o ).toInstant(), 1 ) ) >= 24;
            case 6 -> o != null && ( ( List< ? > ) o ).size() > 0;
            case 7 -> ( (AndroidVersionUpdate) o ).getVersion() != null && ( (AndroidVersionUpdate) o ).getLink() != null;
            case 8 -> ( (TaskTimingRequest) o ).getStartDate() != null && ( (TaskTimingRequest) o ).getEndDate() != null;
            case 9 -> this.checkParam.test( o ) && this.checkParam.test( ( (DataInfo) o ).getData() );
            default -> ( (PatrulLoginRequest) o ).getLogin() != null
                    && ( (PatrulLoginRequest) o ).getPassword() != null
                    && ( (PatrulLoginRequest) o ).getSimCardNumber() != null; };

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
            || request.getTaskType().size() == 0
            || request.getTaskType().contains( TaskTypes.valueOf( row.getString( "tasktypes" ) ) );

    protected final Predicate< UUID > checkSosTable = patrulUUID -> CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.SOS_TABLE.name()
                    + " WHERE patrulUUID = " + patrulUUID + ";" )
            .one() == null;

    protected final BiPredicate< String, String > checkTable = ( id, tableName ) -> CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS + "."
                    + tableName
                    + " WHERE id = '" + id + "';" ).one() != null;

    // определяет тип таска
    protected final Function< String, CassandraTables > findTable = id -> {
            if ( this.checkTable.test( id, CassandraTables.FACEPERSON.name() ) ) return CassandraTables.FACEPERSON;
            else if ( this.checkTable.test( id, CassandraTables.EVENTBODY.name() ) ) return CassandraTables.EVENTBODY;
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

    protected final Predicate< Row > checkPatrulLocation = row -> row.getDouble( "latitude" ) > 0 && row.getDouble( "longitude" ) > 0;

    private static final Double p = PI / 180;

    protected final BiFunction< Point, Patrul, Double > calculate = ( first, second ) ->
            12742 * asin( sqrt( 0.5 - cos( ( second.getLatitude() - first.getLatitude() ) * p ) / 2
                    + cos( first.getLatitude() * p ) * cos( second.getLatitude() * p )
                    * ( 1 - cos( ( second.getLongitude() - first.getLongitude() ) * p ) ) / 2 ) ) * 1000;
}