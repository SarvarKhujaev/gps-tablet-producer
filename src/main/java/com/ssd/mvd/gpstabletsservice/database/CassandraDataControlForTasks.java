package com.ssd.mvd.gpstabletsservice.database;

import lombok.Data;
import java.util.*;
import java.util.function.*;
import java.util.logging.Logger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import com.ssd.mvd.gpstabletsservice.task.card.*;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.task.sos_task.*;
import com.ssd.mvd.gpstabletsservice.controller.Point;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.request.TaskTimingRequest;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.controller.UnirestController;
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

        this.getSession().execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.TASKS_STORAGE_TABLE.name()
                + "( uuid uuid PRIMARY KEY, id text, tasktype text, object text );" );

        this.getSession().execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.ACTIVE_TASK.name()
                + "( id text PRIMARY KEY, object text );" );

        this.getSession().execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRUL_SOS_TABLE.name()
                + CassandraConverter
                .getInstance()
                .convertClassToCassandra( PatrulSos.class )
                + ", status text, " +
                "patrulStatuses map< uuid, text >, PRIMARY KEY ( uuid ) );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS " +
                CassandraTables.TABLETS.name() + "." +
                CassandraTables.TASKS_TIMING_TABLE.name() +
                " ( taskId text, " +
                "patrulUUID uuid, " +
                "totalTimeConsumption bigint, " +
                "timeWastedToArrive bigint, " +
                "dateOfComing timestamp, " +
                "status text, " +
                "taskTypes text, " +
                "inTime boolean, " +
                "positionInfoList list< frozen< " +
                CassandraTables.POSITION_INFO.name() + " >  >, " +
                "PRIMARY KEY( (taskId), patrulUUID ) );" );

        this.getSession().execute( "CREATE INDEX IF NOT EXISTS task_id_index ON "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.TASKS_TIMING_TABLE.name() + "( taskId )" );

        this.getSession().execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRUL_SOS_LIST.name()
                + "( patrulUUID uuid PRIMARY KEY, " +
                "sentSosList set< uuid >, " + // список отправленных сосов
                "attachedSosList set< uuid >, " + // список закрепленных
                "cancelledSosList set< uuid >, " + // список закрепленных
                "acceptedSosList set< uuid > );" ); // список принятых

        this.logger.info("Starting CassandraDataControl for tasks" ); }

    private final Function< String, List< ViolationsInformation > > getViolationsInformationList = gosnumber -> {
        Row row = this.getSession().execute( "SELECT * FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.CARTOTALDATA.name()
                        + " WHERE gosnumber = '" + gosnumber + "';" ).one();
        return row != null ? row.getList( "violationsInformationsList", ViolationsInformation.class ) : new ArrayList<>(); };

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
                                                    .one()
                                                    .getString( "object" ) ) ) )
                            .build() ) );

    private final Function< UUID, Row > getRow = uuid -> this.getSession().execute( "SELECT * FROM "
            + CassandraTables.TABLETS.name() + "."
            + CassandraTables.TASKS_STORAGE_TABLE.name()
            + " where uuid = " + uuid + ";" ).one();

    private final Function< UUID, Mono< SelfEmploymentTask > > getSelfEmploymentTask = id -> {
        Row row = this.getRow.apply( id );
        return row != null
                ? Mono.just( SerDes
                        .getSerDes()
                        .deserializeSelfEmploymentTask( row.getString( "object" ) ) )
                : Mono.empty(); };

    private final Function< String, Mono< FaceEvent > > getFaceEvents = id -> {
        Row row = this.getRow.apply( UUID.fromString( id ) );
        return row != null
                ? Mono.justOrEmpty( SerDes
                .getSerDes()
                .deserializeFaceEvents ( row.getString( "object" ) ) )
                : Mono.empty(); };

    private final Function< String, Mono< EventBody > > getEventBody = id -> {
        Row row = this.getRow.apply( UUID.fromString( id ) );
        return row != null
                ? Mono.justOrEmpty( SerDes
                        .getSerDes()
                        .deserializeEventBody( row.getString( "object" ) ) )
                : Mono.empty(); };

    private final Function< String, Mono< EventFace > > getEventFace = id -> {
        Row row = this.getRow.apply( UUID.fromString( id ) );
        return row != null
                ? Mono.just( SerDes
                    .getSerDes()
                    .deserializeEventFace( row.getString( "object" ) ) )
                : Mono.empty(); };

    private final Function< String, Mono< CarEvent > > getCarEvents = id -> {
        Row row = this.getRow.apply( UUID.fromString( id ) );
        return row != null
                ? Mono.just( SerDes
                .getSerDes()
                .deserializeCarEvents( row.getString( "object" ) ) )
                : Mono.empty(); };

    private final Function< String, Mono< EventCar > > getEventCar = id -> {
        Row row = this.getRow.apply( UUID.fromString( id ) );
        return row != null
                ? Mono.just( SerDes
                    .getSerDes()
                    .deserializeEventCar( row.getString( "object" ) ) )
                : Mono.empty(); };

    private final Function< String, Mono< Card > > getCard102 = id -> {
        Row row = this.getRow.apply( UUID.fromString( id ) );
        return row != null
                ? Mono.just( SerDes
                .getSerDes()
                .deserializeCard( row.getString( "object" ) ) )
                : Mono.empty(); };

    private final Supplier< Flux< CarTotalData > > getAllCarTotalData = () -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.CARTOTALDATA.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .map( row -> SerDes
                    .getSerDes()
                    .deserializeCarTotalData( row.getString( "object" ) ) )
            .sequential()
            .publishOn( Schedulers.single() );

    private final Supplier< Flux< ActiveTask > > getActiveTasks = () -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.ACTIVE_TASK.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .map( row -> SerDes
                    .getSerDes()
                    .deserializeActiveTask( row.getString( "object" ) ) )
            .sequential()
            .publishOn( Schedulers.single() );

    private final Consumer< String > deleteActiveTask = id -> this.getSession()
            .execute( "DELETE FROM "
            + CassandraTables.TABLETS.name() + "."
            + CassandraTables.ACTIVE_TASK.name()
            + " WHERE id = '" + id + "';" );

    public void saveTask ( UUID uuid, String id, TaskTypes taskTypes, Object clazz ) {
        this.getSession().execute( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.TASKS_STORAGE_TABLE.name()
                + "(uuid, id, tasktype, object) VALUES ("
                + uuid + ", '"
                + id + "', '"
                + taskTypes + "', '"
                + SerDes.getSerDes().serialize( clazz ) + "');" ); }

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

    private final Consumer< ActiveTask > saveActiveTask = ( activeTask ) ->
            this.getSession().execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.ACTIVE_TASK.name()
                    + "(id, object) VALUES ('"
                    + activeTask.getTaskId() + "', '"
                    + SerDes.getSerDes().serialize( activeTask ) + "');" );

    // если патрульному отменили задание то нужно удалить запись
    private final Consumer< Patrul > deleteRowFromTaskTimingTable = patrul -> {
        if ( patrul.getTaskId() != null ) this.getSession().execute( "DELETE FROM "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.TASKS_TIMING_TABLE.name()
                + " WHERE taskId = '" + patrul.getTaskId()
                + "' AND patruluuid = " + patrul.getUuid() + " IF EXISTS;" ); };

    // обновляет время которое патрульный полностью потратил на выполнение задания
    // если патрульный завершил то обновляем общее время выполнения
    private final BiFunction< Patrul, Long, Boolean > updateTotalTimeConsumption = ( patrul, timeConsumption ) ->
            this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.TASKS_TIMING_TABLE.name()
                    + " SET totaltimeconsumption = " + timeConsumption
                    + " WHERE taskId = '" + patrul.getTaskId()
                    + "' AND patruluuid = " + patrul.getUuid() + ";" )
                    .wasApplied();

    private final Consumer< TaskTimingStatistics > saveTaskTimeStatistics = taskTimingStatistics ->
            this.getSession().execute( "INSERT INTO " +
                    CassandraTables.TABLETS + "." +
                    CassandraTables.TASKS_TIMING_TABLE +
                    " ( taskId, " +
                    "patrulUUID, " +
                    "totalTimeConsumption, " +
                    "timeWastedToArrive, " +
                    "dateOfComing, " +
                    "status, " +
                    "taskTypes, " +
                    "inTime, " +
                    "positionInfoList ) VALUES( '" +
                    taskTimingStatistics.getTaskId() + "', " +
                    taskTimingStatistics.getPatrulUUID() + ", " +
                    Math.abs( taskTimingStatistics.getTotalTimeConsumption() ) + ", " +
                    Math.abs( taskTimingStatistics.getTimeWastedToArrive() ) + ", '" +
                    ( taskTimingStatistics.getDateOfComing() != null
                            ? taskTimingStatistics.getDateOfComing().toInstant()
                            : new Date().toInstant() ) + "', '" +
                    taskTimingStatistics.getStatus() + "', '" +
                    taskTimingStatistics.getTaskTypes() + "', " +
                    taskTimingStatistics.getInTime() + ", " +
                    CassandraConverter
                            .getInstance()
                            .convertListOfPointsToCassandra( taskTimingStatistics.getPositionInfoList() ) + ");" );

    private final Function< TaskTimingRequest, Mono< TaskTimingStatisticsList > > getTaskTimingStatistics = request ->
        Flux.just( new TaskTimingStatisticsList() )
                .flatMap( taskTimingStatisticsList ->
                        Flux.fromStream( this.getSession()
                                .execute( "SELECT * FROM "
                                        + CassandraTables.TABLETS.name() + "."
                                        + CassandraTables.TASKS_TIMING_TABLE.name() + ";" )
                                .all()
                                .stream()
                                .parallel() )
                        .parallel()
                        .runOn( Schedulers.parallel() )
                        .filter( row -> row.getTimestamp( "dateofcoming" ) != null )
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
                        .map( TaskTimingStatistics::new )
                        .sequential()
                        .publishOn( Schedulers.single() )
                        .collectList()
                        .map( taskTimingStatisticsList1 -> {
                            taskTimingStatisticsList1
                                    .parallelStream()
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
                            return taskTimingStatisticsList; } ) )
                .single();

    // возвращает список точек локаций, где был патрульной пока не дашел до точки назначения
    private final BiFunction< String, UUID, TaskTotalData > getPositionInfoList = ( taskId, patrulUUID ) ->
            new TaskTotalData( this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.TASKS_TIMING_TABLE.name()
                    + " WHERE taskid = '" + taskId + "'"
                    + " AND patruluuid = " + patrulUUID + ";" ).one() );

    private final BiFunction< String, String, Boolean > checkTable = ( id, tableName ) -> this.getSession()
                .execute( "SELECT * FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + tableName
                        + " where id = '" + id + "';" ).one() != null;

    // определяет тип таска
    private final Function< String, CassandraTables > findTable = id -> {
        if ( this.getCheckTable().apply( id, CassandraTables.FACEPERSON.name() ) ) return CassandraTables.FACEPERSON;
        else if ( this.getCheckTable().apply( id, CassandraTables.EVENTBODY.name() ) ) return CassandraTables.EVENTBODY;
        else return CassandraTables.EVENTFACE; };

    private final Function< TaskDetailsRequest, Mono< TaskDetails > > getTaskDetails = taskDetailsRequest ->
            switch ( taskDetailsRequest.getTaskTypes() ) {
                case CARD_102 -> this.getCard102
                        .apply( taskDetailsRequest.getId() )
                        .map( card -> new TaskDetails(
                                card,
                                taskDetailsRequest.getPatrulUUID(),
                                this.getGetPositionInfoList()
                                        .apply( card.getCardId().toString(),
                                                taskDetailsRequest.getPatrulUUID() ) ) );

                case FIND_FACE_CAR -> this.getCheckTable().apply( taskDetailsRequest.getId(), CassandraTables.FACECAR.name() )
                        ? this.getCarEvents
                        .apply( taskDetailsRequest.getId() )
                        .map( carEvent -> new TaskDetails(
                                carEvent,
                                taskDetailsRequest.getPatrulUUID(),
                                this.getGetPositionInfoList()
                                        .apply( carEvent.getId(),
                                                taskDetailsRequest.getPatrulUUID() ) ) )
                        : this.getEventCar
                        .apply( taskDetailsRequest.getId() )
                        .map( eventCar -> new TaskDetails(
                                eventCar,
                                taskDetailsRequest.getPatrulUUID(),
                                this.getGetPositionInfoList()
                                        .apply( eventCar.getId(),
                                                taskDetailsRequest.getPatrulUUID() ) ) );

                case FIND_FACE_PERSON -> switch ( this.getFindTable().apply( taskDetailsRequest.getId() ) ) {
                    case FACEPERSON -> this.getFaceEvents
                            .apply( taskDetailsRequest.getId() )
                            .map( faceEvent -> new TaskDetails(
                                    faceEvent,
                                    taskDetailsRequest.getPatrulUUID(),
                                    this.getGetPositionInfoList()
                                            .apply( faceEvent.getId(),
                                                    taskDetailsRequest.getPatrulUUID() ) ) );

                    case EVENTBODY -> this.getEventBody
                            .apply( taskDetailsRequest.getId() )
                            .map( eventBody -> new TaskDetails(
                                    eventBody,
                                    taskDetailsRequest.getPatrulUUID(),
                                    this.getGetPositionInfoList()
                                            .apply( eventBody.getId(),
                                                    taskDetailsRequest.getPatrulUUID() ) ) );

                    default -> this.getEventFace
                            .apply( taskDetailsRequest.getId() )
                            .map( eventFace -> new TaskDetails(
                                    eventFace,
                                    taskDetailsRequest.getPatrulUUID(),
                                    this.getGetPositionInfoList()
                                            .apply( eventFace.getId(),
                                                    taskDetailsRequest.getPatrulUUID() ) ) ); };
                default -> this.getSelfEmploymentTask
                        .apply( UUID.fromString( taskDetailsRequest.getId() ) )
                        .map( selfEmploymentTask -> new TaskDetails(
                                selfEmploymentTask,
                                taskDetailsRequest.getPatrulUUID(),
                                this.getGetPositionInfoList()
                                        .apply( selfEmploymentTask.getUuid().toString(),
                                                taskDetailsRequest.getPatrulUUID() ) ) ); };

    private final Predicate< UUID > checkSosTable = patrulUUID -> this.getSession()
            .execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.SOS_TABLE.name()
                    + " WHERE patrulUUID = " + patrulUUID + ";" )
            .one() == null;

    private final Function< PatrulSos, Mono< ApiResponseModel > > saveSos = patrulSos -> this.getSession()
            .execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.SOS_TABLE.name()
                    + "( sosWasSendDate, patruluuid, longitude, latitude )"
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
                    "message", KafkaDataControl // sending message to Kafka
                            .getInstance()
                            .getWriteSosNotificationToKafka()
                            .apply( SosNotification
                                    .builder()
                                    .status( Status.CREATED )
                                    .patrulUUID( patrulSos.getPatrulUUID() )
                                    .build() ),
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
                KafkaDataControl // sending message to Kafka
                        .getInstance()
                        .getWriteSosNotificationToKafka()
                        .apply( SosNotification
                                .builder()
                                .status( Status.CANCEL )
                                .patrulUUID( patrulSos.getPatrulUUID() )
                                .build() );
                this.getSession().execute( "DELETE FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.SOS_TABLE.name()
                        + " WHERE patrulUUID = " + patrulSos.getPatrulUUID() + ";" );
                return status; } );

    private final Supplier< Flux< PatrulSos > > getAllSos = () -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRUL_SOS_TABLE.name() + ";" )
                    .all()
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .filter( row -> Status.valueOf( row.getString( "status" ) ).compareTo( Status.FINISHED ) != 0 )
            .flatMap( row -> Mono.just( new PatrulSos( row ) ) )
            .sequential()
            .publishOn( Schedulers.single() );

    // связывает патрульного с сос сигналом
    private final BiFunction< UUID, UUID, Boolean > updatePatrulSos = ( uuid, uuidOfPatrul ) ->
            this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRULS.name()
                    + " SET sos_id = " + uuid
                    + " WHERE uuid = " + uuidOfPatrul + " IF EXISTS;" )
                    .wasApplied();

    // по статусу определяет какой параметр обновлять
    private final Function< Status, String > defineNecessaryTable = status -> switch ( status ) {
                case ATTACHED -> "attachedSosList";
                case CANCEL -> "cancelledSosList";
                case CREATED -> "sentSosList";
                default -> "acceptedSosList"; };

    private void updatePatrulSosList ( UUID sosUUID, UUID patrulUUID, Status status ) {
        Mono.just( this.defineNecessaryTable.apply( status ) )
                .subscribe( param -> this.getSession()
                        .execute( "UPDATE "
                                + CassandraTables.TABLETS.name() + "."
                                + CassandraTables.PATRUL_SOS_LIST.name()
                                + " SET " + param + " = " + param + " + {" + sosUUID + "}"
                                + " WHERE patrulUUID = " + patrulUUID + ";" ) ); }

    private final Consumer< PatrulSos > save = patrulSos1 -> {
        if ( patrulSos1.getPatrulStatuses() != null
                && patrulSos1.getPatrulStatuses().size() > 19 )
            this.getSession().execute( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRUL_SOS_TABLE.name()
                    + CassandraConverter
                    .getInstance()
                    .getALlNames( PatrulSos.class )
                    + " VALUES ("
                    + patrulSos1.getUuid() + ", "
                    + patrulSos1.getPatrulUUID() + ", '"

                    + patrulSos1.getAddress() + "', '"

                    + new Date().toInstant() + "', '"
                    + new Date().toInstant() + "', "

                    + patrulSos1.getLatitude() + ", "
                    + patrulSos1.getLongitude() + ", '"

                    + Status.CREATED.name() + "', "
                    + CassandraConverter
                    .getInstance()
                    .convertSosMapToCassandra( patrulSos1.getPatrulStatuses() ) + " ) IF NOT EXISTS;" ); };

    private final Function< PatrulSos, Mono< ApiResponseModel > > savePatrulSos = patrulSos ->
            CassandraDataControl
                    .getInstance()
                    .getGetPatrulByUUID()
                    .apply( patrulSos.getPatrulUUID() )
                    .flatMap( patrul -> this.getSaveSos().apply( patrulSos )
                            .flatMap( apiResponseModel -> {
                                if ( Status.valueOf( apiResponseModel
                                                .getData()
                                                .getData()
                                                .toString() )
                                        .compareTo( Status.ACTIVE ) == 0 ) {
                                    patrulSos.setPatrulStatuses( new HashMap<>() );
                                    //обновляем список сигналов которые отправлял патрульный
                                    this.updatePatrulSosList( patrulSos.getUuid(), patrul.getUuid(), Status.CREATED );
                                    // закрепояем этот сос сигнал за тем кто отправил его
                                    this.getUpdatePatrulSos().apply( patrulSos.getUuid(), patrulSos.getPatrulUUID() );
                                    // сохраняем адрес сигнала
                                    patrulSos.setAddress( UnirestController
                                            .getInstance()
                                            .getGetAddressByLocation()
                                            .apply( patrulSos.getLatitude(), patrulSos.getLongitude() )
                                            .replaceAll( "'", "`" ) );

                                    return KafkaDataControl
                                            .getInstance()
                                            .getSave()
                                            .apply( CassandraDataControl
                                                    .getInstance()
                                                    .getFindTheClosestPatrulsForSos()
                                                    .apply( new Point(
                                                                    patrulSos.getLatitude(),
                                                                    patrulSos.getLongitude() ),
                                                            patrul.getUuid() )
                                                    .parallel( 20 )
                                                    .runOn( Schedulers.parallel() )
                                                    .map( patrul1 -> {
                                                        this.updatePatrulSosList( patrulSos.getUuid(), patrul1.getUuid(), Status.ATTACHED );
                                                        patrulSos.getPatrulStatuses().put( patrul1.getUuid(), Status.ATTACHED.name() );
                                                        this.getSave().accept( patrulSos );
                                                        return new SosNotificationForAndroid(
                                                                patrulSos,
                                                                patrul,
                                                                Status.ACTIVE,
                                                                patrul1.getPassportNumber() ); } )
                                                    .sequential()
                                                    .publishOn( Schedulers.single() ), apiResponseModel ); }
                                else {
                                    final PatrulSos patrulSos1 = this.getCurrentPatrulSos.apply( patrul.getSos_id() );
                                    this.getUpdatePatrulSos().apply( null, patrul.getUuid() );

                                    // меняем статус сигнала на выолнено
                                    this.getSession().execute( "UPDATE "
                                            + CassandraTables.TABLETS.name() + "."
                                            + CassandraTables.PATRUL_SOS_TABLE.name()
                                            + " SET status = '" + Status.FINISHED.name() + "',"
                                            + " sosWasClosed = '" + new Date().toInstant() + "'"
                                            + " WHERE uuid = " + patrulSos1.getUuid() + " IF EXISTS;" );
                                    return KafkaDataControl
                                            .getInstance()
                                            .getSave()
                                            .apply( Flux.fromStream( patrulSos1
                                                            .getPatrulStatuses()
                                                            .keySet()
                                                            .stream()
                                                            .parallel() )
                                                    .parallel()
                                                    .runOn( Schedulers.parallel() )
                                                    .flatMap( CassandraDataControl
                                                            .getInstance()
                                                            .getGetPatrulByUUID() )
                                                    .flatMap( patrul1 -> {
                                                        if ( patrul1.getSos_id() != null
                                                                && patrul1.getSos_id().compareTo( patrulSos1.getUuid() ) == 0 )
                                                            this.getUpdatePatrulSos().apply( null, patrul1.getUuid() );
                                                        return Mono.just( new SosNotificationForAndroid(
                                                                patrulSos1,
                                                                patrul,
                                                                Status.IN_ACTIVE,
                                                                patrul1.getPassportNumber() ) ); } )
                                                    .sequential()
                                                    .publishOn( Schedulers.single() ), apiResponseModel ); } } ) );

    // используется в случае когда патрульный либо принимает сигнал либо отказывается
    private final Function< SosRequest, Mono< ApiResponseModel > > updatePatrulStatusInSosTable = sosRequest -> {
        // добавляем данный сос сигнал в список
        this.updatePatrulSosList( sosRequest.getSosUUID(),
                sosRequest.getPatrulUUID(),
                sosRequest.getStatus() );
        // если патрульный подтвердил данный сигнал то связымаем его с ним
        if ( sosRequest.getStatus().compareTo( Status.ACCEPTED ) == 0 ) {
            this.getUpdatePatrulSos().apply( sosRequest.getSosUUID(), sosRequest.getPatrulUUID() );
            // меняем статус сос сигнала на принятый
            this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRUL_SOS_TABLE.name()
                    + " SET status = '" + Status.ACCEPTED.name() + "'"
                    + " WHERE uuid = " + sosRequest.getSosUUID() + " IF EXISTS;" );
            KafkaDataControl
                    .getInstance()
                    .getWriteSosNotificationToKafka()
                    .apply( SosNotification
                            .builder()
                            .patrulUUID( this.getCurrentPatrulSos.apply( sosRequest.getSosUUID() ).getPatrulUUID() )
                            .status( Status.ACCEPTED )
                            .build() ); }
        this.getSession().execute( "UPDATE "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.PATRUL_SOS_TABLE.name()
                + " SET patrulStatuses [" + sosRequest.getPatrulUUID() + "] = '"
                + ( sosRequest.getStatus().compareTo( Status.CANCEL ) == 0
                ? Status.ATTACHED : sosRequest.getStatus() )
                + "' WHERE uuid = " + sosRequest.getSosUUID() + ";" );
        return Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", "You have changed status of sos task" ) ); };

    private final Function< UUID, PatrulSos > getCurrentPatrulSos = uuid -> new PatrulSos(
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRUL_SOS_TABLE.name()
                    + " WHERE uuid = " + uuid + ";" ).one() );

    private final Predicate< UUID > checkSosWasFinished = uuid -> {
        Row row = this.getSession()
                .execute( "SELECT * FROM "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.PATRUL_SOS_TABLE.name()
                        + " WHERE uuid = " + uuid + ";" )
                .one();
        return row != null && Status.valueOf( row.getString( "status" ) ).compareTo( Status.CREATED ) == 0; };

    // возвращает все сос сигналы для конкретного патрульного
    private final Function< UUID, Mono< ApiResponseModel > > getAllSosForCurrentPatrul = patrulUUID -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.PATRUL_SOS_LIST.name()
                    + " WHERE patruluuid = " + patrulUUID + ";" )
                    .one()
                    .getSet( "attachedsoslist", UUID.class )
                    .stream()
                    .parallel() )
            .parallel()
            .runOn( Schedulers.parallel() )
            .filter( this.checkSosWasFinished )
            .map( this.getCurrentPatrulSos )
            .flatMap( patrulSos -> CassandraDataControl
                    .getInstance()
                    .getGetPatrulByUUID()
                    .apply( patrulSos.getPatrulUUID() )
                    .map( patrul -> new SosTotalData( patrulSos,
                            patrulSos.getPatrulStatuses().get( patrulUUID ),
                            new SosNotificationForAndroid(
                                    patrulSos,
                                    patrul,
                                    Status.CREATED,
                                    patrul.getPassportNumber() ) ) ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .collectList()
            .flatMap( sosTotalDataList -> Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of( "message", "Your list of sos signals",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( sosTotalDataList )
                                    .build() ) ) );

    // создает список различных сос сигналов лоя нового патрульного
    private final Consumer< UUID > createRowInPatrulSosListTable = uuid -> this.getSession()
            .execute( "INSERT INTO "
            + CassandraTables.TABLETS.name() + "."
            + CassandraTables.PATRUL_SOS_LIST.name()
            + " ( patruluuid, " +
            "sentSosList, " +
            "attachedSosList, " +
            "cancelledSosList, " +
            "acceptedSosList ) VALUES ( " + uuid + ", {}, {}, {}, {} ) IF NOT EXISTS;" );

    private final Function< TaskDetailsRequest, Mono< ActiveTask > > getActiveTask = taskDetailsRequest ->
            switch ( taskDetailsRequest.getTaskTypes() ) {
                case CARD_102 -> this.getCard102
                        .apply( taskDetailsRequest.getId() )
                        .map( ActiveTask::new );

                case FIND_FACE_CAR -> this.getCarEvents
                        .apply( taskDetailsRequest.getId() )
                        .map( ActiveTask::new );

                case FIND_FACE_PERSON -> this.getFaceEvents
                        .apply( taskDetailsRequest.getId() )
                        .map( ActiveTask::new );

                case FIND_FACE_EVENT_CAR -> this.getEventCar
                        .apply( taskDetailsRequest.getId() )
                        .map( ActiveTask::new );

                case FIND_FACE_EVENT_BODY -> this.getEventBody
                        .apply( taskDetailsRequest.getId() )
                        .map( ActiveTask::new );

                case FIND_FACE_EVENT_FACE -> this.getEventFace
                        .apply( taskDetailsRequest.getId() )
                        .map( ActiveTask::new );

                default -> this.getSelfEmploymentTask
                        .apply( UUID.fromString( taskDetailsRequest.getId() ) )
                        .map( ActiveTask::new ); };
}
