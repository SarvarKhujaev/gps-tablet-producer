package com.ssd.mvd.gpstabletsservice.inspectors;

import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests.PatrulActivityRequest;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests.PatrulLoginRequest;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.request.AndroidVersionUpdate;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.request.TaskDetailsRequest;
import com.ssd.mvd.gpstabletsservice.request.TaskTimingRequest;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.entity.Point;

import com.datastax.driver.core.Row;
import static java.lang.Math.cos;
import static java.lang.Math.*;
import java.util.*;
import java.util.function.Consumer;

public class DataValidateInspector extends TimeInspector {
    private static final DataValidateInspector INSTANCE = new DataValidateInspector();

    public static DataValidateInspector getInstance () {
        return INSTANCE;
    }

    public boolean objectIsNotNull (
            final Object o
    ) {
        return o != null;
    }

    protected boolean checkSosTable ( final UUID patrulId ) {
        return this.objectIsNotNull(
                CassandraDataControl
                        .getInstance()
                        .getRowFromTabletsKeyspace(
                                CassandraTables.SOS_TABLE,
                                "patrulUUID",
                                patrulId.toString()
                        )
        );
    }

    public Boolean checkObject ( final Point point ) {
        return this.objectIsNotNull( point.getLatitude() )
                && this.objectIsNotNull( point.getLongitude() );
    }

    public Boolean checkObject ( final Patrul patrul ) {
        return patrul.getPatrulTaskInfo().getTaskId().equals( "null" )
                && patrul.getPatrulUniqueValues().getUuidOfEscort() == null
                && patrul.getPatrulUniqueValues().getUuidForPatrulCar() == null
                && patrul.getPatrulUniqueValues().getUuidForEscortCar() == null
                && patrul.getPatrulCarInfo().getCarNumber().equals( "null" )
                && patrul.getPatrulTaskInfo().getTaskTypes().isFree();
    }

    public Boolean checkObject ( final TaskDetailsRequest request ) {
        return this.objectIsNotNull( request )
                && this.objectIsNotNull( request.getId() )
                && this.objectIsNotNull( request.getTaskTypes() )
                && this.objectIsNotNull( request.getPatrulUUID() );
    }

    public Boolean checkObject ( final PatrulLoginRequest patrulLoginRequest ) {
        return this.objectIsNotNull( patrulLoginRequest.getLogin() )
                && this.objectIsNotNull( patrulLoginRequest.getPassword() )
                && this.objectIsNotNull( patrulLoginRequest.getSimCardNumber() );
    }

    public Boolean checkObject ( final AndroidVersionUpdate androidVersionUpdate ) {
        return this.objectIsNotNull( androidVersionUpdate.getVersion() )
                && this.objectIsNotNull( androidVersionUpdate.getLink() );
    }

    public Boolean checkObject ( final PatrulActivityRequest patrulActivityRequest ) {
        return this.objectIsNotNull( patrulActivityRequest.getStartDate() )
                && this.objectIsNotNull( patrulActivityRequest.getEndDate() );
    }

    protected Boolean filterPatrul (
            final Row row,
            final Map< String, String > params,
            final List< String > policeTypes,
            final Integer value ) {
        return value == 1 ? ( !params.containsKey( "regionId" )
                || row.getLong( "regionId" ) == Long.parseLong( params.get( "regionId" ) ) )
                && ( !params.containsKey( "policeType" ) || policeTypes.contains( row.getString( "policeType" ) ) )
                : ( !params.containsKey( "policeType" ) || policeTypes.contains( row.getString( "policeType" ) ) )
                && row.getLong( "regionId" ) == Long.parseLong( params.get( "regionId" ) )
                && ( !params.containsKey( "districtId" )
                || row.getLong( "districtId" ) == Long.parseLong( params.get( "districtId" ) ) )
                && switch ( Status.valueOf( params.get( "status" ) ) ) {
                    // активные патрульные
                    case ACTIVE -> !this.checkPatrulActivity( row.getUUID( "uuid" ) )
                            && super.getTimeDifference( row.getTimestamp( "lastActiveDate" ).toInstant(), 1 ) <= 24;

                    // не активные патрульные
                    case IN_ACTIVE -> !this.checkPatrulActivity( row.getUUID( "uuid" ) )
                            && super
                            .getTimeDifference( row.getTimestamp( "lastActiveDate" ).toInstant(), 1 ) > 24;

                    // патрульные которые которые никогда не заходили
                    case FORCE -> this.checkPatrulActivity( row.getUUID( "uuid" ) );

                    // патрульные которые которые заходили хотя бы раз
                    default -> !this.checkPatrulActivity( row.getUUID( "uuid" ) );
        }; }

    protected int checkDifference ( final int integer ) {
        return integer > 0 && integer < 100 ? integer : 10;
    }

    protected boolean checkTabletUsage (
            final Row row,
            final PatrulActivityRequest request
    ) {
        return row.getTimestamp( "startedToUse" ).before( request.getEndDate() )
                && row.getTimestamp( "startedToUse" ).after( request.getStartDate() );
    }

    protected boolean checkTaskTimingRequest (
            final TaskTimingRequest request,
            final Row row
    ) {
        return request.getEndDate() == null
                || request.getStartDate() == null
                || row.getTimestamp( "dateofcoming" ).after( request.getStartDate() )
                && row.getTimestamp( "dateofcoming").before( request.getEndDate() );
    }

    protected boolean checkTaskType (
            final TaskTimingRequest request,
            final Row row
    ) {
        return request.getTaskType() == null
                || request.getTaskType().isEmpty()
                || request.getTaskType().contains( TaskTypes.valueOf( row.getString( "tasktypes" ) ) );
    }

    protected boolean checkTable (
            final String id,
            final CassandraTables tableName
    ) {
        return CassandraDataControl
                .getInstance()
                .getRowFromTabletsKeyspace(
                        tableName,
                        "id",
                        id ) != null;
    }

    protected boolean checkTracker (
            final String trackerId
    ) {
        return CassandraDataControlForEscort
                .getInstance()
                .getRowFromEscortKeyspace(
                        CassandraTables.TRACKERSID,
                        "trackersId",
                        trackerId
                ) == null
                && CassandraDataControl
                .getInstance()
                .getSession()
                .execute( "SELECT * FROM "
                        + CassandraTables.TRACKERS + "."
                        + CassandraTables.TRACKERSID
                        + " WHERE trackersId = '" + trackerId + "';" ).one() == null;
    }

    protected boolean checkCarNumber (
            final String carNumber
    ) {
        return CassandraDataControlForEscort
                .getInstance()
                .getRowFromEscortKeyspace(
                        CassandraTables.TUPLE_OF_CAR,
                        "gosnumber",
                        carNumber
                ) == null
                && CassandraDataControl
                .getInstance()
                .getRowFromTabletsKeyspace(
                        CassandraTables.CARS,
                        "gosnumber",
                        carNumber
                ) == null;
    }

    public boolean checkPatrulActivity (
            final UUID uuid
    ) {
        return CassandraDataControl
                .getInstance()
                .getRowFromTabletsKeyspace(
                        CassandraTables.TABLETS_USAGE_TABLE,
                        "uuidofpatrul",
                        uuid.toString()
                ) == null;
    }

    protected boolean checkPatrulLocation (
            final Row row
    ) {
        return row.getDouble( "latitude" ) > 0 && row.getDouble( "longitude" ) > 0;
    }

    private static final Double p = PI / 180;

    protected double calculate (
            final Point first,
            final Patrul second
    ) {
        return 12742 * asin( sqrt( 0.5 - cos( ( second.getPatrulLocationData().getLatitude() - first.getLatitude() ) * p ) / 2
                + cos( first.getLatitude() * p ) * cos( second.getPatrulLocationData().getLatitude() * p )
                * ( 1 - cos( ( second.getPatrulLocationData().getLongitude() - first.getLongitude() ) * p ) ) / 2 ) ) * 1000;
    }

    /*
    принимает запись из БД
    проверяет что запись не пуста
    и заполняет параметры объекта по заданной логике
    */
    protected <T> void checkAndSetParams (
            final T object,
            final Consumer< T > customConsumer
    ) {
        Optional.ofNullable( object ).ifPresent( customConsumer );
    }
}