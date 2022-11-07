package com.ssd.mvd.gpstabletsservice.database;

import lombok.Data;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import java.util.logging.Logger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.Predicate;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import com.ssd.mvd.gpstabletsservice.task.card.*;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.request.TaskTimingRequest;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;

@Data
public class CassandraDataControlForTasks {
    private final Session session = CassandraDataControl.getInstance().getSession();
    private final Cluster cluster = CassandraDataControl.getInstance().getCluster();

    private final Logger logger = Logger.getLogger( CassandraDataControl.class.toString() );
    private static CassandraDataControlForTasks cassandraDataControl = new CassandraDataControlForTasks();

    public static CassandraDataControlForTasks getInstance() { return cassandraDataControl != null
            ? cassandraDataControl
            : ( cassandraDataControl = new CassandraDataControlForTasks() ); }

    private CassandraDataControlForTasks () {
        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.CARTOTALDATA.name()
                + "( gosnumber text PRIMARY KEY, "
                + "cameraImage text, "
                + "violationsInformationsList list< frozen <"
                + CassandraTables.VIOLATION_LIST_TYPE.name() + "> >, "
                + "object text );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.EVENTBODY.name()
                + "( id text PRIMARY KEY, object text );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.EVENTFACE.name()
                + "( id text PRIMARY KEY, object text );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.EVENTCAR.name()
                + "( id text PRIMARY KEY, object text );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.FACECAR.name()
                + "( id text PRIMARY KEY, object text );" );

        this.getSession().execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.SELFEMPLOYMENT.name()
                + "( id uuid PRIMARY KEY, object text );" );

        this.getSession().execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.FACEPERSON.name()
                + "( id text PRIMARY KEY, object text );" );

        this.getSession().execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + TaskTypes.CARD_102
                + "( id text PRIMARY KEY, object text );" );

        this.getSession().execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.ACTIVE_TASK.name()
                + "( id text PRIMARY KEY, object text );" );

        this.getSession().execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.SOS_TABLE.name()
                + CassandraConverter
                .getInstance()
                .convertClassToCassandra( PatrulSos.class )
                + ", PRIMARY KEY ( patrulUUID ) );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS " +
                CassandraTables.TABLETS.name() + "." +
                CassandraTables.TASK_TIMING_TABLE.name() +
                " ( taskId text, " +
                "patrulUUID uuid, " +
                "totalTimeConsumption bigint, " +
                "dateOfComing timestamp, " +
                "status text, " +
                "taskTypes text, " +
                "inTime boolean, " +
                "positionInfoList list< frozen< " +
                CassandraTables.POSITION_INFO.name() + " >  >, " +
                "PRIMARY KEY( (dateOfComing), taskId ) );" );

        this.getSession().execute( "CREATE INDEX IF NOT EXISTS task_id_index ON "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.TASK_TIMING_TABLE.name() + "( taskId )" );

        this.logger.info("Starting CassandraDataControl for tasks" ); }

    private final Function< String, List< ViolationsInformation > > getViolationsInformationList = gosnumber ->
            this.getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.CARTOTALDATA.name()
                    + " WHERE gosnumber = '" + gosnumber + "';" )
            .one()
            .getList( "violationsInformationsList", ViolationsInformation.class );

    private final Function< String, Mono< ApiResponseModel > > getWarningCarDetails = gosnumber -> Archive
            .getArchive()
            .getFunction()
            .apply( Map.of( "message", "Warning car details",
                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                            .builder()
                            .data( new CardDetails( SerDes
                                    .getSerDes()
                                    .deserializeCarTotalData(
                                            this.getSession().execute( "SELECT * FROM "
                                                            + CassandraTables.TABLETS.name() + "."
                                                            + CassandraTables.CARTOTALDATA.name()
                                                            + " WHERE gosnumber = '" + gosnumber + "';" )
                                                    .one().getString( "object" ) ) ) )
                            .build() ) );

    private final Function< UUID, Mono< SelfEmploymentTask > > getSelfEmploymentTask = id -> Mono.just(
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.SELFEMPLOYMENT.name()
                    + " where id = " + id + ";" ).one() )
            .map( row -> SerDes
                    .getSerDes()
                    .deserializeSelfEmploymentTask( row.getString( "object" ) ) );

    private final Function< String, Mono< FaceEvent > > getFaceEvents = id -> Mono.justOrEmpty(
            SerDes
                .getSerDes()
                .deserializeFaceEvents ( this.getSession()
                        .execute( "SELECT * FROM "
                                + CassandraTables.TABLETS.name() + "."
                                + CassandraTables.FACEPERSON.name()
                                + " where id = '" + id + "';" )
                        .one().getString( "object" ) ) );

    private final Function< String, Mono< EventBody > > getEventBody = id -> Mono.justOrEmpty(
            SerDes
                .getSerDes()
                .deserializeEventBody( this.getSession()
                        .execute( "SELECT * FROM "
                                + CassandraTables.TABLETS.name() + "."
                                + CassandraTables.EVENTBODY.name()
                                + " where id = '" + id + "';" )
                        .one().getString( "object" ) ) );

    private final Function< String, Mono< EventFace > > getEventFace = id -> Mono.just(
            SerDes
                .getSerDes()
                .deserializeEventFace( this.getSession()
                        .execute( "SELECT * FROM "
                                + CassandraTables.TABLETS.name() + "."
                                + CassandraTables.EVENTFACE.name()
                                + " where id = '" + id + "';" )
                        .one().getString( "object" ) ) );

    private final Function< String, Mono< CarEvent > > getCarEvents = id -> {
        Row row = this.getSession().execute( "SELECT * FROM "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.FACECAR.name()
                + " where id = '" + id + "';" ).one();
        return row != null ? Mono.just( SerDes
                .getSerDes()
                .deserializeCarEvents( row.getString( "object" ) ) )
                : Mono.empty(); };

    private final Function< String, Mono< EventCar > > getEventCar = id -> {
        Row row = this.getSession().execute( "SELECT * FROM "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.EVENTCAR.name()
                + " where id = '" + id + "';" ).one();
        return row != null ? Mono.just(
                SerDes
                        .getSerDes()
                        .deserializeEventCar(
                                row.getString( "object" ) ) ) : Mono.empty(); };

    private final Function< String, Mono< Card > > getCard102 = id -> {
        Row row = this.getSession().execute( "SELECT * FROM "
                + CassandraTables.TABLETS.name() + "."
                + TaskTypes.CARD_102
                + " where id = '" + id + "';" ).one();
        return row != null ? Mono.just( SerDes
                .getSerDes()
                .deserializeCard(
                        row.getString( "object" ) ) ) : Mono.empty(); };

    private final Supplier< Flux< CarTotalData > > getAllCarTotalData = () -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.CARTOTALDATA.name() + ";" )
                    .all().stream() )
            .map( row -> SerDes
                    .getSerDes()
                    .deserializeCarTotalData( row.getString( "object" ) ) );

    private final Supplier< Flux< ActiveTask > > getActiveTasks = () -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.ACTIVE_TASK.name() + ";" )
                    .all().stream() )
            .map( row -> SerDes
                    .getSerDes()
                    .deserializeActiveTask( row.getString( "object" ) ) );

    private final Consumer< String > remove = id -> this.getSession()
            .execute( "DELETE FROM "
            + CassandraTables.TABLETS.name() + "."
            + CassandraTables.ACTIVE_TASK.name()
            + " WHERE id = '" + id + "';" );

    private final Consumer< Card > saveCard102 = card -> this.getSession()
            .executeAsync( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + TaskTypes.CARD_102
                    + "(id, object) VALUES ('"
                    + card.getCardId() + "', '"
                    + SerDes.getSerDes().serialize( card ) + "');" );

    private final Consumer< EventCar > saveEventCar = eventCar -> this.getSession()
            .executeAsync( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.EVENTCAR.name()
                    + "( id, camera, matched, date, confidence, object ) VALUES('"
                    + eventCar.getId() + "', "
                    + eventCar.getCamera() + ", "
                    + eventCar.getMatched() + ", '"
                    + eventCar.getCreated_date().toInstant() + "', "
                    + eventCar.getConfidence() + ", '"
                    + SerDes.getSerDes().serialize( eventCar ) + "');" );

    private final Consumer< EventFace > saveEventFace = eventFace -> this.getSession()
            .executeAsync( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.EVENTFACE.name()
                    + "( id, camera, matched, date, confidence, object ) VALUES('"
                    + eventFace.getId() + "', "
                    + eventFace.getCamera() + ", "
                    + eventFace.getMatched() + ", '"
                    + eventFace.getCreated_date().toInstant() + "', "
                    + eventFace.getConfidence() + ", '"
                    + SerDes.getSerDes().serialize( eventFace ) + "');" );

    private final Consumer< EventBody > saveEventBody = eventBody -> this.getSession()
            .executeAsync( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.EVENTBODY.name()
                    + "( id, camera, matched, date, confidence, object ) VALUES('"
                    + eventBody.getId() + "', "
                    + eventBody.getCamera() + ", "
                    + eventBody.getMatched() + ", '"
                    + eventBody.getCreated_date().toInstant() + "', "
                    + eventBody.getConfidence() + ", '"
                    + SerDes.getSerDes().serialize( eventBody ) + "');" );

    private final Function< CarTotalData, Boolean > saveCarTotalData = carTotalData ->
            this.getSession().execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.CARTOTALDATA.name()
                    + "( gosnumber, cameraImage, violationsInformationsList, object ) VALUES('"
                    + carTotalData.getGosNumber() + "', '"
                    + carTotalData.getCameraImage() + "', "
                    + CassandraConverter
                    .getInstance()
                    .convertListOfPointsToCassandra( carTotalData
                            .getViolationsList()
                            .getViolationsInformationsList() ) + ", '"
                    + SerDes
                            .getSerDes()
                            .serialize( carTotalData ) + "');" )
                    .wasApplied();

    private final Consumer< CarEvent > saveCarEvent = carEvents -> this.getSession()
            .executeAsync( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.FACECAR.name()
                    + "(id, object) VALUES ('"
                    + carEvents.getId() + "', '"
                    + SerDes.getSerDes().serialize( carEvents ) + "');" );

    private final Consumer< FaceEvent > saveFaceEvent = faceEvents -> {
        if ( faceEvents.getCreated_date() == null ) faceEvents.setCreated_date( new Date().toString() );
        this.getSession().executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.FACEPERSON.name()
                + "(id, object) VALUES ('"
                + faceEvents.getId() + "', '"
                + SerDes.getSerDes().serialize( faceEvents ) + "');" ); };

    private final Consumer< SelfEmploymentTask > saveSelfEmploymentTask = selfEmploymentTask -> this.getSession()
            .executeAsync( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.SELFEMPLOYMENT.name() +
                    " ( id, object ) VALUES("
                    + selfEmploymentTask.getUuid() + ", '"
                    + SerDes.getSerDes().serialize( selfEmploymentTask ) + "');" );

    public void addValue ( String id, ActiveTask activeTask ) { this.getSession()
            .executeAsync( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.ACTIVE_TASK.name()
                    + "(id, object) VALUES ('"
                    + id + "', '"
                    + SerDes.getSerDes().serialize( activeTask ) + "');" ); }

    private final Consumer< TaskTimingStatistics > saveTaskTimeStatistics = taskTimingStatistics -> this.getSession()
            .execute( "INSERT INTO " +
                    CassandraTables.TABLETS + "." +
                    CassandraTables.TASK_TIMING_TABLE +
                    " ( taskId, " +
                    "patrulUUID, " +
                    "totalTimeConsumption, " +
                    "dateOfComing, " +
                    "status, " +
                    "taskTypes, " +
                    "inTime, " +
                    "positionInfoList ) VALUES( '" +
                    taskTimingStatistics.getTaskId() + "', " +
                    taskTimingStatistics.getPatrulUUID() + ", " +
                    Math.abs( taskTimingStatistics.getTotalTimeConsumption() ) + ", '" +
                    taskTimingStatistics.getDateOfComing().toInstant() + "', '" +
                    taskTimingStatistics.getStatus() + "', '" +
                    taskTimingStatistics.getTaskTypes() + "', " +
                    taskTimingStatistics.getInTime() + ", " +
                    CassandraConverter
                            .getInstance()
                            .convertListOfPointsToCassandra( taskTimingStatistics.getPositionInfoList() ) + ");" );

    private final Function< TaskTimingRequest, Mono< TaskTimingStatisticsList > > getTaskTimingStatistics = request -> {
        TaskTimingStatisticsList taskTimingStatisticsList = new TaskTimingStatisticsList();
        return Flux.fromStream( this.getSession()
                    .execute( "SELECT * FROM "
                                + CassandraTables.TABLETS.name() + "."
                                + CassandraTables.TASK_TIMING_TABLE.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
                .parallel()
                .runOn( Schedulers.parallel() )
                .filter( row -> request.getEndDate() == null
                        || request.getStartDate() == null
                        || row.getTimestamp( "dateofcoming" )
                        .after( request.getStartDate() )
                        && row.getTimestamp( "dateofcoming")
                        .before( request.getEndDate() ) )
                .filter( row -> request.getTaskType() == null
                        || request.getTaskType().size() == 0
                        || request.getTaskType()
                        .contains( TaskTypes.valueOf( row.getString( "tasktypes" ) ) ) )
                .flatMap( row -> Mono.just( new TaskTimingStatistics( row ) ) )
                .sequential()
                .publishOn( Schedulers.single() )
                .collectList()
                .map( taskTimingStatisticsList1 -> {
                    taskTimingStatisticsList1
                            .parallelStream()
                            .parallel()
                            .forEach( taskTimingStatistics1 -> {
                                switch ( taskTimingStatistics1.getStatus() ) {
                                    case LATE -> taskTimingStatisticsList
                                            .getListLate()
                                            .add( taskTimingStatistics1 );
                                    case IN_TIME -> taskTimingStatisticsList
                                            .getListInTime()
                                            .add( taskTimingStatistics1 );
                                    default -> taskTimingStatisticsList
                                            .getListDidNotArrived()
                                            .add( taskTimingStatistics1 ); } } );
                    return taskTimingStatisticsList; } ); };

    // возвращает список точек локаций, где был патрульной пока не дашел до точки назначения
    private final Function< String, Mono< List< PositionInfo > > > getPositionInfoList = taskId -> Mono.justOrEmpty(
            this.getSession()
                    .execute( "SELECT positionInfoList FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.TASK_TIMING_TABLE.name()
                        + " WHERE taskid = '" + taskId + "';" )
            .one().getList( "positionInfoList", PositionInfo.class ) );

    private Boolean checkTable ( String id, String tableName ) {
        return this.getSession()
                .execute( "SELECT * FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + tableName
                        + " where id = '" + id + "';" ).one() != null; }

    // определяет тип таска
    private CassandraTables findTable ( String id ) {
        if ( this.checkTable( id, CassandraTables.FACEPERSON.name() ) ) return CassandraTables.FACEPERSON;
        else if ( this.checkTable( id, CassandraTables.EVENTBODY.name() ) ) return CassandraTables.EVENTBODY;
        else return CassandraTables.EVENTFACE; }

    private final Function< TaskDetailsRequest, Mono< TaskDetails > > getTaskDetails = taskDetailsRequest ->
            switch ( taskDetailsRequest.getTaskTypes() ) {
                case CARD_102 -> this.getCard102
                        .apply( taskDetailsRequest.getId() )
                        .map( card -> new TaskDetails( card, taskDetailsRequest.getPatrulUUID() ) );

                case FIND_FACE_CAR -> this.checkTable( taskDetailsRequest.getId(), CassandraTables.FACECAR.name() )
                        ? this.getCarEvents
                        .apply( taskDetailsRequest.getId() )
                        .map( carEvent -> new TaskDetails( carEvent, taskDetailsRequest.getPatrulUUID() ) )
                        : this.getEventCar
                        .apply( taskDetailsRequest.getId() )
                        .map( eventCar -> new TaskDetails( eventCar, taskDetailsRequest.getPatrulUUID() ) );

                case FIND_FACE_PERSON -> switch ( this.findTable( taskDetailsRequest.getId() ) ) {
                    case FACEPERSON -> this.getFaceEvents
                            .apply( taskDetailsRequest.getId() )
                            .map( faceEvent -> new TaskDetails( faceEvent, taskDetailsRequest.getPatrulUUID() ) );

                    case EVENTBODY -> this.getEventFace
                            .apply( taskDetailsRequest.getId() )
                            .map( eventFace -> new TaskDetails( eventFace, taskDetailsRequest.getPatrulUUID() ) );

                    default -> this.getEventBody
                            .apply( taskDetailsRequest.getId() )
                            .map( eventFace -> new TaskDetails( eventFace, taskDetailsRequest.getPatrulUUID() ) ); };
                default -> this.getSelfEmploymentTask
                        .apply( UUID.fromString( taskDetailsRequest.getId() ) )
                        .map( selfEmploymentTask -> new TaskDetails( selfEmploymentTask,
                                taskDetailsRequest.getPatrulUUID() ) ); };

    private final Predicate< UUID > checkSosTable = patrulUUID -> this.getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.SOS_TABLE.name()
                    + " WHERE patrulUUID = "
                    + patrulUUID + ";" ).one() == null;

    private final Function< PatrulSos, Mono< ApiResponseModel > > saveSos = patrulSos -> this.getSession()
            .execute( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.SOS_TABLE.name()
                + CassandraConverter
                .getInstance()
                .getALlNames( PatrulSos.class )
                + " VALUES ('"
                + new Date().toInstant() + "', "
                + patrulSos.getPatrulUUID() + ", "
                + patrulSos.getLongitude() + ", "
                + patrulSos.getLatitude() + ") IF NOT EXISTS;" )
                .wasApplied()
                ? Archive
            .getArchive()
            .getFunction()
            .apply( Map.of(
                    "message", "Sos was saved successfully",
                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                            .builder()
                            .data( Status.ACTIVE )
                            .build() ) )
            : Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of(
                            "message", "Sos was deleted successfully",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( Status.IN_ACTIVE )
                                    .build() ) )
            .map( status -> {
                this.getSession().execute( "DELETE FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.SOS_TABLE.name()
                        + " WHERE patrulUUID = " + patrulSos.getPatrulUUID() + ";" );
                return status; } );

    private final Supplier< Flux< PatrulSos > > getAllSos = () -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.SOS_TABLE.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .flatMap( row -> Mono.just( new PatrulSos( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() );
}
