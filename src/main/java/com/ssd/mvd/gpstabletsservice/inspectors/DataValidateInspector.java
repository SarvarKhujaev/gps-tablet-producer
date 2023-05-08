package com.ssd.mvd.gpstabletsservice.inspectors;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ModelForCar;
import com.ssd.mvd.gpstabletsservice.tuple.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.request.PatrulActivityRequest;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.request.AndroidVersionUpdate;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.Pinpp;
import com.ssd.mvd.gpstabletsservice.request.PatrulLoginRequest;
import com.ssd.mvd.gpstabletsservice.request.TaskTimingRequest;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.task.sos_task.PatrulSos;
import com.ssd.mvd.gpstabletsservice.tuple.PolygonForEscort;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.ssd.mvd.gpstabletsservice.controller.Point;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.datastax.driver.core.Row;

import java.util.function.BiPredicate;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.Objects;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static java.lang.Math.cos;
import static java.lang.Math.*;

@lombok.Data
public class DataValidateInspector extends Archive {
    private static final DataValidateInspector INSTANCE = new DataValidateInspector();

    public static DataValidateInspector getInstance () { return INSTANCE; }

    private final Predicate< Object > checkParam = Objects::nonNull;

    private final BiFunction< Object, Integer, String > concatNames = ( o, integer ) -> integer == 0
            ? String.join( " ",
            ( ( Pinpp ) o ).getName(),
                    ( ( Pinpp ) o ).getSurname(),
                    ( ( Pinpp ) o ).getPatronym() )
            : String.join( " ",
            ( (ModelForCar) o ).getModel(),
                    ( (ModelForCar) o ).getVehicleType(),
                    ( (ModelForCar) o ).getColor() );

    private final BiPredicate< Status, Status > checkEquality = ( o, b ) -> o.compareTo( b ) == 0;

    private final BiPredicate< Object, Integer > checkRequest = ( o, value ) -> switch ( value ) {
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
                    .getGetTimeDifferenceInHours()
                    .apply( ( (Date) o ).toInstant() ) ) >= 24;
            case 6 -> o != null && ( ( List< ? > ) o ).size() > 0;
            case 7 -> ( (AndroidVersionUpdate) o ).getVersion() != null && ( (AndroidVersionUpdate) o ).getLink() != null;
            case 8 -> ( (TaskTimingRequest) o ).getStartDate() != null && ( (TaskTimingRequest) o ).getEndDate() != null;
            default -> ( (PatrulLoginRequest) o ).getLogin() != null
                    && ( (PatrulLoginRequest) o ).getPassword() != null
                    && ( (PatrulLoginRequest) o ).getSimCardNumber() != null; };

    private final Function< Integer, Integer > checkDifference = integer -> integer > 0 && integer < 100 ? integer : 10;

    private final BiPredicate< Double, Double > checkDistance = ( distance, patrulDistance ) -> patrulDistance <= distance;

    private final BiPredicate< Row, PatrulActivityRequest > checkTabletUsage = ( row, request ) ->
            row.getTimestamp( "startedToUse" ).before( request.getEndDate() )
            && row.getTimestamp( "startedToUse" ).after( request.getStartDate() );

    private final BiPredicate< TaskTimingRequest, Row > checkTaskTimingRequest = ( request, row ) ->
            request.getEndDate() == null
            || request.getStartDate() == null
            || row.getTimestamp( "dateofcoming" ).after( request.getStartDate() )
            && row.getTimestamp( "dateofcoming").before( request.getEndDate() );

    private final BiPredicate< TaskTimingRequest, Row > checkTaskType = (request, row ) ->
            request.getTaskType() == null
            || request.getTaskType().size() == 0
            || request.getTaskType().contains( TaskTypes.valueOf( row.getString( "tasktypes" ) ) );

    private final BiPredicate< String, String > checkTable = ( id, tableName ) -> CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + tableName
                    + " where id = '" + id + "';" ).one() != null;

    // определяет тип таска
    private final Function< String, CassandraTables > findTable = id -> {
            if ( this.getCheckTable().test( id, CassandraTables.FACEPERSON.name() ) ) return CassandraTables.FACEPERSON;
            else if ( this.getCheckTable().test( id, CassandraTables.EVENTBODY.name() ) ) return CassandraTables.EVENTBODY;
            else return CassandraTables.EVENTFACE; };

    private final Predicate< UUID > checkSosTable = patrulUUID -> CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.SOS_TABLE.name()
                    + " WHERE patrulUUID = " + patrulUUID + ";" )
            .one() == null;

    // по статусу определяет какой параметр обновлять
    private final Function< Status, String > defineNecessaryTable = status -> switch ( status ) {
            case ATTACHED -> "attachedSosList";
            case CANCEL -> "cancelledSosList";
            case CREATED -> "sentSosList";
            default -> "acceptedSosList"; };

    private final Predicate< PolygonForEscort > checkPolygonForEscort = polygon ->
            CassandraDataControlForEscort
                    .getInstance()
                    .getSession()
                    .execute( "SELECT * FROM "
                            + CassandraTables.ESCORT.name() + "."
                            + CassandraTables.POLYGON_FOR_ESCORT.name()
                            + " where uuid = " + polygon.getUuid() + ";" ).one() != null;

    private final Predicate< String > checkTracker = trackerId -> CassandraDataControl
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

    private final Predicate< String > checkCarNumber = carNumber -> CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.ESCORT.name() + "."
                    + CassandraTables.TUPLE_OF_CAR.name() +
                    " where gosnumber = '" + carNumber + "';" ).one() == null
            && CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.CARS.name() +
                    " where gosnumber = '" + carNumber + "';" ).one() == null;

    private final Predicate< Row > checkPatrulStatus = row -> row.getDouble( "latitude" ) > 0 && row.getDouble( "longitude" ) > 0;

    private static final Double p = PI / 180;

    private final BiFunction< Point, Patrul, Double > calculate = ( first, second ) ->
            12742 * asin( sqrt( 0.5 - cos( ( second.getLatitude() - first.getLatitude() ) * p ) / 2
                    + cos( first.getLatitude() * p ) * cos( second.getLatitude() * p )
                    * ( 1 - cos( ( second.getLongitude() - first.getLongitude() ) * p ) ) / 2 ) ) * 1000;
}