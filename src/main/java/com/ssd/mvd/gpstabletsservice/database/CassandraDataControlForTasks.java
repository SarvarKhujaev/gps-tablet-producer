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

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.ResultSetFuture;

import com.ssd.mvd.gpstabletsservice.task.card.*;
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

    public static CassandraDataControlForTasks getInstance() { return cassandraDataControl != null ? cassandraDataControl
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

    private final Function< String, List< ViolationsInformation > > getViolationsInformationList = gosnumber -> this.getSession()
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
            SerDes.getSerDes().deserializeFaceEvents (
                    this.getSession().execute( "SELECT * FROM "
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
                    .deserializeEventFace( this.getSession().execute( "SELECT * FROM "
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

    public void addValue ( Card card ) { this.getSession()
            .executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                    + TaskTypes.CARD_102
                + "(id, object) VALUES ('"
                + card.getCardId() + "', '"
                + SerDes.getSerDes().serialize( card ) + "');" ); }

    public Boolean addValue ( EventCar eventCar ) { return this.getSession()
            .executeAsync( "INSERT INTO "
            + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.EVENTCAR.name()
            + "( id, camera, matched, date, confidence, object ) VALUES('"
            + eventCar.getId() + "', "
            + eventCar.getCamera() + ", "
            + eventCar.getMatched() + ", '"
            + eventCar.getCreated_date().toInstant() + "', "
            + eventCar.getConfidence() + ", '"
            + SerDes.getSerDes().serialize( eventCar ) + "');" ).isDone(); }

    public Boolean addValue ( EventFace eventFace ) { return this.getSession()
            .executeAsync( "INSERT INTO "
            + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.EVENTFACE.name()
            + "( id, camera, matched, date, confidence, object ) VALUES('"
            + eventFace.getId() + "', "
            + eventFace.getCamera() + ", "
            + eventFace.getMatched() + ", '"
            + eventFace.getCreated_date().toInstant() + "', "
            + eventFace.getConfidence() + ", '"
            + SerDes.getSerDes().serialize( eventFace ) + "');" ).isDone(); }

    public Boolean addValue ( EventBody eventBody ) { return this.getSession()
            .executeAsync( "INSERT INTO "
            + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.EVENTBODY.name()
            + "( id, camera, matched, date, confidence, object ) VALUES('"
            + eventBody.getId() + "', "
            + eventBody.getCamera() + ", "
            + eventBody.getMatched() + ", '"
            + eventBody.getCreated_date().toInstant() + "', "
            + eventBody.getConfidence() + ", '"
            + SerDes.getSerDes().serialize( eventBody ) + "');" ).isDone(); }

    public Boolean addValue ( CarTotalData carTotalData ) { return this.getSession()
            .execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.CARTOTALDATA.name()
                    + "( gosnumber, cameraImage, violationsInformationsList, object ) VALUES('"
                    + carTotalData.getGosNumber() + "', '"
                    + carTotalData.getCameraImage() + "', "
                    + CassandraConverter
                    .getInstance()
                    .convertListOfPointsToCassandra( carTotalData
                            .getViolationsList()
                            .getViolationsInformationsList() )
                    + ", '" + SerDes.getSerDes().serialize( carTotalData ) + "');" ).wasApplied(); }

    public ResultSetFuture addValue ( CarEvent carEvents ) { return this.getSession()
            .executeAsync( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.FACECAR.name()
                    + "(id, object) VALUES ('"
                    + carEvents.getId() + "', '"
                    + SerDes.getSerDes().serialize( carEvents ) + "');" ); }

    public ResultSetFuture addValue ( FaceEvent faceEvents ) {
        if ( faceEvents.getCreated_date() == null ) faceEvents.setCreated_date( new Date().toString() );
        return this.getSession().executeAsync( "INSERT INTO "
            + CassandraTables.TABLETS.name() + "."
                + CassandraTables.FACEPERSON.name()
            + "(id, object) VALUES ('"
            + faceEvents.getId() + "', '"
            + SerDes.getSerDes().serialize( faceEvents ) + "');" ); }

    public void addValue ( String id, ActiveTask activeTask ) { this.getSession()
            .executeAsync( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.ACTIVE_TASK.name()
                    + "(id, object) VALUES ('"
                    + id + "', '"
                    + SerDes.getSerDes().serialize( activeTask ) + "');" ); }

    public Boolean addValue ( SelfEmploymentTask selfEmploymentTask ) { return this.getSession()
            .executeAsync( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.SELFEMPLOYMENT.name() +
                    " ( id, object ) VALUES("
                    + selfEmploymentTask.getUuid() + ", '"
                    + SerDes.getSerDes().serialize( selfEmploymentTask )
                    + "');" ).isDone(); }

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

    private final Function< TaskTimingRequest, Flux< TaskTimingStatistics > > getTaskTimingStatistics = request -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.TABLETS.name() + "."
                            + CassandraTables.TASK_TIMING_TABLE.name() + ";" )
                            .all().stream() )
            .filter( row -> request.getEndDate() == null
                    || request.getStartDate() == null
                    || row.getTimestamp( "dateofcoming" )
                    .after( request.getStartDate() )
                    && row.getTimestamp( "dateofcoming")
                    .before(request.getEndDate() ) )
            .filter( row -> request.getTaskType() == null
                    || request.getTaskType().size() <= 0
                    || request.getTaskType()
                    .contains( TaskTypes.valueOf( row.getString( "tasktypes" ) ) ) )
            .map( TaskTimingStatistics::new );

    public Mono< List< PositionInfo > > getPositionInfoList ( String taskId ) {
        return Mono.justOrEmpty( this.getSession()
                .execute( "SELECT positionInfoList FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.TASK_TIMING_TABLE.name()
                        + " WHERE taskid = '" + taskId + "';" )
                .one().getList( "positionInfoList", PositionInfo.class ) ); }

    private Boolean checkTable ( String id, String tableName ) {
        return this.getSession()
                .execute( "SELECT * FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + tableName
                        + " where id = '" + id + "';" ).one() != null; }

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
}
