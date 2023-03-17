package com.ssd.mvd.gpstabletsservice.inspectors;

import com.ssd.mvd.gpstabletsservice.tuple.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import com.ssd.mvd.gpstabletsservice.request.TaskTimingRequest;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.task.sos_task.PatrulSos;
import com.ssd.mvd.gpstabletsservice.tuple.PolygonForEscort;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.database.Archive;
import com.datastax.driver.core.Row;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Function;
import java.util.Objects;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@lombok.Data
public class DataValidateInspector extends Archive {
    private final Predicate< Object > checkParam = Objects::nonNull;

    public final Predicate< List< ? > > checkList = list -> list != null && list.size() > 0;

    public final Function< Date, Boolean > checkTime = date -> Math.abs(
            TimeInspector
                    .getInspector()
                    .getGetTimeDifferenceInHours()
                    .apply( date.toInstant() ) ) >= 24;

    private final BiFunction< TaskTimingRequest, Row, Boolean > checkRequest = ( request, row ) ->
            request.getEndDate() == null
            || request.getStartDate() == null
            || row.getTimestamp( "dateofcoming" ).after( request.getStartDate() )
            && row.getTimestamp( "dateofcoming").before( request.getEndDate() );

    private final BiFunction< TaskTimingRequest, Row, Boolean > checkTaskType = ( request, row ) ->
            request.getTaskType() == null
            || request.getTaskType().size() == 0
            || request.getTaskType().contains( TaskTypes.valueOf( row.getString( "tasktypes" ) ) );

    private final BiFunction< String, String, Boolean > checkTable = ( id, tableName ) -> CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + tableName
                    + " where id = '" + id + "';" ).one() != null;

    // определяет тип таска
    private final Function< String, CassandraTables > findTable = id -> {
        if ( this.getCheckTable().apply( id, CassandraTables.FACEPERSON.name() ) ) return CassandraTables.FACEPERSON;
        else if ( this.getCheckTable().apply( id, CassandraTables.EVENTBODY.name() ) ) return CassandraTables.EVENTBODY;
        else return CassandraTables.EVENTFACE; };

    private final Predicate<UUID> checkSosTable = patrulUUID -> CassandraDataControl
            .getInstance()
            .getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.SOS_TABLE.name()
                    + " WHERE patrulUUID = " + patrulUUID + ";" )
            .one() == null;



    // по статусу определяет какой параметр обновлять
    private final Function<Status, String > defineNecessaryTable = status -> switch ( status ) {
        case ATTACHED -> "attachedSosList";
        case CANCEL -> "cancelledSosList";
        case CREATED -> "sentSosList";
        default -> "acceptedSosList"; };

    private final Predicate< PatrulSos > checkPatrulSos = patrulSos ->
            patrulSos.getPatrulStatuses() != null
            && patrulSos.getPatrulStatuses().size() > 19;

    private final Predicate<PolygonForEscort> checkPolygonForEscort = polygon ->
            CassandraDataControlForEscort
                    .getInstance()
                    .getSession()
                    .execute( "SELECT * FROM "
                            + CassandraTables.ESCORT.name() + "."
                            + CassandraTables.POLYGON_FOR_ESCORT.name()
                            + " where uuid = " + polygon.getUuid() + ";" ).one() != null;
}
