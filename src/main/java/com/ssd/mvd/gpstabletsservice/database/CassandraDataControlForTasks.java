package com.ssd.mvd.gpstabletsservice.database;

import java.util.*;
import java.util.function.*;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import com.ssd.mvd.gpstabletsservice.task.card.*;
import com.ssd.mvd.gpstabletsservice.entity.Point;
import com.ssd.mvd.gpstabletsservice.task.sos_task.*;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.request.SosRequest;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.SerDes;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.request.TaskTimingRequest;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.request.TaskDetailsRequest;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.controller.UnirestController;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.*;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.TaskDetails;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.TaskTotalData;
import com.ssd.mvd.gpstabletsservice.entity.notifications.SosNotification;
import com.ssd.mvd.gpstabletsservice.entity.responseForAndroid.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.entity.responseForAndroid.CardDetails;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.TaskTimingStatistics;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.entity.notifications.SosNotificationForAndroid;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.TaskTimingStatisticsList;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;

@lombok.Data
public final class CassandraDataControlForTasks extends SerDes {
    private final Session session = CassandraDataControl.getInstance().getSession();
    private final Cluster cluster = CassandraDataControl.getInstance().getCluster();

    private static CassandraDataControlForTasks INSTANCE = new CassandraDataControlForTasks();

    public static CassandraDataControlForTasks getInstance () { return INSTANCE != null ? INSTANCE : ( INSTANCE = new CassandraDataControlForTasks() ); }

    private CassandraDataControlForTasks () {
        this.getSession().execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS + "."
                + CassandraTables.CARTOTALDATA
                + "( gosnumber text PRIMARY KEY, "
                + "cameraImage text, "
                + "violationsInformationsList list< frozen <"
                + CassandraTables.VIOLATION_LIST_TYPE + "> >, "
                + "object text );" );

        this.getSession().execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS + "."
                + CassandraTables.TASKS_STORAGE_TABLE
                + "( uuid uuid PRIMARY KEY, id text, tasktype text, object text );" );

        this.getSession().execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS + "."
                + CassandraTables.ACTIVE_TASK
                + "( id text PRIMARY KEY, object text );" );

        this.getSession().execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS + "."
                + CassandraTables.PATRUL_SOS_TABLE
                + super.convertClassToCassandra.apply( PatrulSos.class )
                + ", status text, " +
                "patrulStatuses map< uuid, text >, PRIMARY KEY ( uuid ) );" );

        this.getSession().execute( "CREATE TABLE IF NOT EXISTS " +
                CassandraTables.TABLETS + "." +
                CassandraTables.TASKS_TIMING_TABLE +
                " ( taskId text, " +
                "patrulUUID uuid, " +
                "totalTimeConsumption bigint, " +
                "timeWastedToArrive bigint, " +
                "dateOfComing timestamp, " +
                "status text, " +
                "taskTypes text, " +
                "inTime boolean, " +
                "positionInfoList list< frozen< " +
                CassandraTables.POSITION_INFO + " >  >, " +
                "PRIMARY KEY( (taskId), patrulUUID ) );" );

        this.getSession().execute( "CREATE INDEX IF NOT EXISTS task_id_index ON "
                + CassandraTables.TABLETS + "."
                + CassandraTables.TASKS_TIMING_TABLE + "( taskId )" );

        this.getSession().execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS + "."
                + CassandraTables.PATRUL_SOS_LIST
                + "( patrulUUID uuid PRIMARY KEY, " +
                "sentSosList set< uuid >, " + // список отправленных сосов
                "attachedSosList set< uuid >, " + // список закрепленных
                "cancelledSosList set< uuid >, " + // список закрепленных
                "acceptedSosList set< uuid > );" ); // список принятых

        super.logging( "Starting CassandraDataControl for tasks" ); }

    private final Function< String, List< ViolationsInformation > > getViolationsInformationList = gosnumber -> {
            final Row row = this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.CARTOTALDATA
                    + " WHERE gosnumber = '" + gosnumber + "';" ).one();
            return super.checkParam.test( row ) ? row.getList( "violationsInformationsList", ViolationsInformation.class ) : new ArrayList<>(); };

    private final Function< String, Mono< ApiResponseModel > > getWarningCarDetails = gosnumber -> super.getFunction().apply(
            Map.of( "message", "Warning car details",
                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                            .builder()
                            .data( new CardDetails( super.deserialize(
                                    this.getSession().execute( "SELECT * FROM "
                                                    + CassandraTables.TABLETS + "."
                                                    + CassandraTables.CARTOTALDATA
                                                    + " WHERE gosnumber = '" + gosnumber + "';" )
                                            .one()
                                            .getString( "object" ), CarTotalData.class ) ) )
                            .build() ) );

    // возвращает запись из БД для конкретной задачи
    private final Function< String, Mono< Row > > getTask = uuid ->
            Optional.ofNullable( this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.TASKS_STORAGE_TABLE
                    + " WHERE uuid = " + UUID.fromString( uuid ) + ";" ).one() )
                    .map( super::convert )
                    .orElseGet( Mono::empty );

    private final Consumer< String > deleteActiveTask = id -> this.getSession().execute(
            "DELETE FROM " + CassandraTables.TABLETS + "." + CassandraTables.ACTIVE_TASK + " WHERE id = '" + id + "';" );

    // созраняет и обновляет все задачи
    public void saveTask ( final UUID uuid,
                           final String id,
                           final TaskTypes taskTypes,
                           final Object clazz ) {
            this.getSession().execute( "INSERT INTO "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.TASKS_STORAGE_TABLE
                    + "(uuid, id, tasktype, object) VALUES ("
                    + uuid + ", '"
                    + id + "', '"
                    + taskTypes + "', '"
                    + super.serialize( clazz ) + "');" ); }

    private final Function< CarTotalData, Boolean > saveCarTotalData = carTotalData ->
            this.getSession().execute( "INSERT INTO "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.CARTOTALDATA
                    + "( gosnumber, cameraImage, violationsInformationsList, object ) VALUES('"
                    + carTotalData.getGosNumber() + "', '"
                    + carTotalData.getCameraImage() + "', "
                    + super.convertListOfPointsToCassandra.apply( carTotalData
                            .getViolationsList()
                            .getViolationsInformationsList() ) + ", '"
                    + super.serialize( carTotalData ) + "');" )
                    .wasApplied();

    private final Consumer< ActiveTask > saveActiveTask = activeTask ->
            this.getSession().execute( "INSERT INTO "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.ACTIVE_TASK
                    + "(id, object) VALUES ('"
                    + activeTask.getTaskId() + "', '"
                    + super.serialize( activeTask ) + "');" );

    // если патрульному отменили задание то нужно удалить запись о времени затраченное на задачу
    private final Function< Patrul, UUID > deleteRowFromTaskTimingTable = patrul -> {
            Optional.ofNullable( patrul )
                    .filter( patrul1 -> super.checkParam.test( patrul.getTaskId() ) )
                    .ifPresent( patrul1 -> this.getSession().execute( "DELETE FROM "
                            + CassandraTables.TABLETS + "."
                            + CassandraTables.TASKS_TIMING_TABLE
                            + " WHERE taskId = '" + patrul.getTaskId()
                            + "' AND patruluuid = " + patrul.getUuid() + " IF EXISTS;" ) );
            return patrul.getUuid(); };

    // обновляет время которое патрульный полностью потратил на выполнение задания
    // если патрульный завершил то обновляем общее время выполнения
    private final BiConsumer< Patrul, Long > updateTotalTimeConsumption = ( patrul, timeConsumption ) -> {
            super.logging( "timeConsumption: " + timeConsumption );
            this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.TASKS_TIMING_TABLE
                    + " SET totaltimeconsumption = " + timeConsumption
                    + " WHERE taskId = '" + patrul.getTaskId()
                    + "' AND patruluuid = " + patrul.getUuid() + ";" ); };

    private final Function< TaskTimingStatistics, Boolean > saveTaskTimeStatistics = taskTimingStatistics ->
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
                    ( super.checkParam.test( taskTimingStatistics.getDateOfComing() )
                            ? taskTimingStatistics.getDateOfComing().toInstant()
                            : new Date().toInstant() ) + "', '" +
                    taskTimingStatistics.getStatus() + "', '" +
                    taskTimingStatistics.getTaskTypes() + "', " +
                    taskTimingStatistics.getInTime() + ", " +
                    super.convertListOfPointsToCassandra.apply( taskTimingStatistics.getPositionInfoList() ) + ");" )
                    .wasApplied();

    private final Function< TaskTimingRequest, Mono< TaskTimingStatisticsList > > getTaskTimingStatistics = request ->
            Flux.just( new TaskTimingStatisticsList() )
                    .flatMap( taskTimingStatisticsList -> CassandraDataControl
                            .getInstance()
                            .getGetAllEntities()
                            .apply( CassandraTables.TABLETS, CassandraTables.TASKS_TIMING_TABLE )
                            .filter( row -> super.checkParam.test( row.getTimestamp( "dateofcoming" ) ) )
                            .filter( row -> super.checkTaskTimingRequest.test( request, row ) )
                            .filter( row -> super.checkTaskType.test( request, row ) )
                            .flatMap( row -> CassandraDataControl
                                    .getInstance()
                                    .getGetPatrulByUUID()
                                    .apply( row.getUUID( "patrulUUID" ) )
                                    .map( patrul -> new TaskTimingStatistics( row, patrul ) ) )
                            .sequential()
                            .publishOn( Schedulers.single() )
                            .collectList()
                            .map( taskTimingStatisticsList1 -> {
                                taskTimingStatisticsList1
                                        .forEach( taskTimingStatistics1 -> {
                                            switch ( taskTimingStatistics1.getStatus() ) {
                                                case LATE -> taskTimingStatisticsList.getListLate().add( taskTimingStatistics1 );
                                                case IN_TIME -> taskTimingStatisticsList.getListInTime().add( taskTimingStatistics1 );
                                                default -> taskTimingStatisticsList.getListDidNotArrived().add( taskTimingStatistics1 ); } } );
                                return taskTimingStatisticsList; } ) )
                    .single();

    // возвращает список точек локаций, где был патрульной пока не дашел до точки назначения
    private final BiFunction< String, UUID, TaskTotalData > getTaskTimingInfo = ( taskId, patrulUUID ) ->
            new TaskTotalData( this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.TASKS_TIMING_TABLE
                    + " WHERE taskid = '" + taskId + "'"
                    + " AND patruluuid = " + patrulUUID + ";" ).one() );

    private final Function< TaskDetailsRequest, Mono< TaskDetails > > getTaskDetails = taskDetailsRequest -> switch ( taskDetailsRequest.getTaskTypes() ) {
            case CARD_102 -> this.getGetTask().apply( taskDetailsRequest.getId() ) // получаем запись из базы
                    .map( row -> super.deserialize( row.getString( "object" ), Card.class ) ) // конвертируем в нужный формат
                    .map( card -> new TaskDetails(
                            card,
                            taskDetailsRequest.getPatrulUUID(),
                            CARD_102,
                            this.getGetTaskTimingInfo().apply( card.getCardId().toString(), taskDetailsRequest.getPatrulUUID() ),
                            card.getReportForCardList() ) );

            case FIND_FACE_CAR -> super.checkTable.test( taskDetailsRequest.getId(), CassandraTables.FACECAR.name() )
                    ? this.getGetTask().apply( taskDetailsRequest.getId() )
                    .map( row -> super.deserialize( row.getString("object" ), CarEvent.class ) )
                    .map( carEvent -> new TaskDetails(
                            carEvent,
                            taskDetailsRequest.getPatrulUUID(),
                            FIND_FACE_CAR,
                            this.getGetTaskTimingInfo().apply( carEvent.getId(), taskDetailsRequest.getPatrulUUID() ),
                            carEvent.getReportForCardList() ) )
                    : this.getGetTask().apply( taskDetailsRequest.getId() )
                    .map( row -> super.deserialize( row.getString("object" ), EventCar.class ) )
                    .map( eventCar -> new TaskDetails(
                            eventCar,
                            taskDetailsRequest.getPatrulUUID(),
                            FIND_FACE_EVENT_CAR,
                            this.getGetTaskTimingInfo().apply( eventCar.getId(), taskDetailsRequest.getPatrulUUID() ),
                            eventCar.getReportForCardList() ) );

            case FIND_FACE_PERSON -> switch ( super.findTable.apply( taskDetailsRequest.getId() ) ) {
                case FACEPERSON -> this.getGetTask().apply( taskDetailsRequest.getId() )
                        .map( row -> super.deserialize( row.getString("object" ), FaceEvent.class ) )
                        .map( faceEvent -> new TaskDetails(
                                faceEvent,
                                taskDetailsRequest.getPatrulUUID(),
                                FIND_FACE_PERSON,
                                this.getGetTaskTimingInfo().apply( faceEvent.getId(), taskDetailsRequest.getPatrulUUID() ),
                                faceEvent.getReportForCardList() ) );

                case EVENTBODY -> this.getGetTask().apply( taskDetailsRequest.getId() )
                    .map( row -> super.deserialize( row.getString("object" ), EventBody.class ) )
                        .map( eventBody -> new TaskDetails(
                                eventBody,
                                taskDetailsRequest.getPatrulUUID(),
                                FIND_FACE_EVENT_BODY,
                                this.getGetTaskTimingInfo().apply( eventBody.getId(), taskDetailsRequest.getPatrulUUID() ),
                                eventBody.getReportForCardList() ) );

                default -> this.getGetTask().apply( taskDetailsRequest.getId() )
                    .map( row -> super.deserialize( row.getString( "object" ), EventFace.class ) )
                        .map( eventFace -> new TaskDetails(
                                eventFace,
                                taskDetailsRequest.getPatrulUUID(),
                                FIND_FACE_EVENT_FACE,
                                this.getGetTaskTimingInfo().apply( eventFace.getId(), taskDetailsRequest.getPatrulUUID() ),
                                eventFace.getReportForCardList() ) ); };

            default -> this.getGetTask().apply( taskDetailsRequest.getId() )
                    .map( row -> super.deserialize( row.getString("object" ), SelfEmploymentTask.class ) )
                    .map( selfEmploymentTask -> new TaskDetails(
                            selfEmploymentTask,
                            taskDetailsRequest.getPatrulUUID(),
                            SELF_EMPLOYMENT,
                            this.getGetTaskTimingInfo().apply( selfEmploymentTask.getUuid().toString(), taskDetailsRequest.getPatrulUUID() ),
                            selfEmploymentTask.getReportForCards() ) ); };

    // сохраняет сос сигнал от патрульного
    private final Function< PatrulSos, Mono< ApiResponseModel > > saveSos = patrulSos -> this.getSession().execute(
            "INSERT INTO "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.SOS_TABLE
                    + "( sosWasSendDate, patruluuid, longitude, latitude )"
                    + " VALUES ('"
                    + new Date().toInstant() + "', "
                    + patrulSos.getPatrulUUID() + ", "
                    + patrulSos.getLongitude() + ", "
                    + patrulSos.getLatitude() + ") IF NOT EXISTS;" )
            .wasApplied()
            ? super.getFunction().apply(
                    Map.of( "message", KafkaDataControl // sending message to Kafka
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
            : super.getFunction().apply(
                    Map.of( "message", "Sos was deleted successfully",
                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                            .builder()
                            .data( Status.IN_ACTIVE )
                            .build() ) )
            .map( status -> { // если патрульный уже отправлял сигнал ранее, то этот сигнал будет удален
                KafkaDataControl // sending message to Kafka
                        .getInstance()
                        .getWriteSosNotificationToKafka()
                        .apply( SosNotification
                                .builder()
                                .status( Status.CANCEL ) // отправляем уведомление фронту
                                .patrulUUID( patrulSos.getPatrulUUID() )
                                .build() );
                this.getSession().execute( "DELETE FROM "
                        + CassandraTables.TABLETS + "."
                        + CassandraTables.SOS_TABLE
                        + " WHERE patrulUUID = " + patrulSos.getPatrulUUID() + ";" );
                return status; } );

    // связывает патрульного с сос сигналом
    private final BiConsumer< UUID, UUID > updatePatrulSos = ( uuid, uuidOfPatrul ) ->
            this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.PATRULS
                    + " SET sos_id = " + uuid
                    + " WHERE uuid = " + uuidOfPatrul + ";" );

    private void updatePatrulSosList ( final UUID sosUUID,
                                       final UUID patrulUUID,
                                       final Status status ) {
            super.convert( super.defineNecessaryTable.apply( status ) )
                    .subscribe( param -> this.getSession().execute( "UPDATE "
                            + CassandraTables.TABLETS + "."
                            + CassandraTables.PATRUL_SOS_LIST
                            + " SET " + param + " = " + param + " + {" + sosUUID + "}"
                            + " WHERE patrulUUID = " + patrulUUID + ";" ) ); }

    private final Consumer< PatrulSos > save = patrulSos ->
            Optional.ofNullable( patrulSos )
                    .filter( patrulSos1 -> super.checkRequest.test( patrulSos1, 4 ) )
                    .ifPresent( patrulSos1 -> this.getSession().execute( "INSERT INTO "
                            + CassandraTables.TABLETS + "."
                            + CassandraTables.PATRUL_SOS_TABLE
                            + super.getALlNames.apply( PatrulSos.class )
                            + " VALUES ("
                            + patrulSos.getUuid() + ", "
                            + patrulSos.getPatrulUUID() + ", '"

                            + patrulSos.getAddress() + "', '"

                            + new Date().toInstant() + "', '"
                            + new Date().toInstant() + "', "

                            + patrulSos.getLatitude() + ", "
                            + patrulSos.getLongitude() + ", '"

                            + Status.CREATED + "', "
                            + super.convertSosMapToCassandra.apply( patrulSos.getPatrulStatuses() ) + " ) IF NOT EXISTS;" ) );

    private final Function< PatrulSos, Mono< ApiResponseModel > > savePatrulSos = patrulSos ->
            CassandraDataControl
                    .getInstance()
                    .getGetPatrulByUUID()
                    .apply( patrulSos.getPatrulUUID() )
                    .flatMap( patrul -> this.getSaveSos().apply( patrulSos )
                            .flatMap( apiResponseModel -> Optional.ofNullable( apiResponseModel )
                                    .filter( apiResponseModel1 -> super.checkEquality.test(
                                            Status.valueOf( apiResponseModel
                                                    .getData()
                                                    .getData()
                                                    .toString() ),
                                            Status.ACTIVE ) )
                                    .map( apiResponseModel1 -> {
                                        patrulSos.setPatrulStatuses( new HashMap<>() );
                                        //обновляем список сигналов которые отправлял патрульный
                                        this.updatePatrulSosList( patrulSos.getUuid(), patrul.getUuid(), Status.CREATED );
                                        // закрепояем этот сос сигнал за тем кто отправил его
                                        this.getUpdatePatrulSos().accept( patrulSos.getUuid(), patrulSos.getPatrulUUID() );
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
                                                        .apply( new Point( patrulSos.getLatitude(), patrulSos.getLongitude() ),
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
                                                        .publishOn( Schedulers.single() ), apiResponseModel ); } )
                                    .orElseGet( () -> {
                                        final PatrulSos patrulSos1 = this.getCurrentPatrulSos.apply( patrul.getSos_id() );
                                        this.getUpdatePatrulSos().accept( null, patrul.getUuid() );

                                        // меняем статус сигнала на выолнено
                                        this.getSession().execute( "UPDATE "
                                                + CassandraTables.TABLETS + "."
                                                + CassandraTables.PATRUL_SOS_TABLE
                                                + " SET status = '" + Status.FINISHED + "',"
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
                                                        .map( patrul1 -> {
                                                            if ( super.checkParam.test( patrul1.getSos_id() )
                                                                    && patrul1.getSos_id().compareTo( patrulSos1.getUuid() ) == 0 )
                                                                this.getUpdatePatrulSos().accept( null, patrul1.getUuid() );
                                                            return new SosNotificationForAndroid(
                                                                    patrulSos1,
                                                                    patrul,
                                                                    Status.IN_ACTIVE,
                                                                    patrul1.getPassportNumber() ); } )
                                                        .sequential()
                                                        .publishOn( Schedulers.single() ), apiResponseModel ); } ) ) );

    // используется в случае когда патрульный либо принимает сигнал либо отказывается
    private final Function< SosRequest, Mono< ApiResponseModel > > updatePatrulStatusInSosTable = sosRequest -> {
            // добавляем данный сос сигнал в список
            this.updatePatrulSosList( sosRequest.getSosUUID(), sosRequest.getPatrulUUID(), sosRequest.getStatus() );
            // если патрульный подтвердил данный сигнал то связымаем его с ним
            if ( super.checkEquality.test( sosRequest.getStatus(), Status.ACCEPTED ) ) {
                this.getUpdatePatrulSos().accept( sosRequest.getSosUUID(), sosRequest.getPatrulUUID() );
                // меняем статус сос сигнала на принятый
                this.getSession().execute( "UPDATE "
                        + CassandraTables.TABLETS + "."
                        + CassandraTables.PATRUL_SOS_TABLE
                        + " SET status = '" + Status.ACCEPTED + "'"
                        + " WHERE uuid = " + sosRequest.getSosUUID() + " IF EXISTS;" );
                KafkaDataControl
                        .getInstance()
                        .getWriteSosNotificationToKafka()
                        .apply( SosNotification
                                .builder()
                                .patrulUUID( this.getGetCurrentPatrulSos().apply( sosRequest.getSosUUID() ).getPatrulUUID() )
                                .status( Status.ACCEPTED )
                                .build() ); }
            this.getSession().execute( "UPDATE "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.PATRUL_SOS_TABLE
                    + " SET patrulStatuses [" + sosRequest.getPatrulUUID() + "] = '"
                    + ( super.checkEquality.test( sosRequest.getStatus(), Status.CANCEL )
                    ? Status.ATTACHED : sosRequest.getStatus() )
                    + "' WHERE uuid = " + sosRequest.getSosUUID() + ";" );
            return super.getFunction().apply( Map.of( "message", "You have changed status of sos task" ) ); };

    private final Function< UUID, PatrulSos > getCurrentPatrulSos = uuid -> new PatrulSos(
            this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.PATRUL_SOS_TABLE
                    + " WHERE uuid = " + uuid + ";" ).one() );

    // проверяет не завершен ли сос сигнал
    private final Predicate< UUID > checkSosWasFinished = uuid ->
            Optional.ofNullable( this.getSession().execute( "SELECT * FROM "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.PATRUL_SOS_TABLE
                    + " WHERE uuid = " + uuid + ";" ).one() )
                    .filter( row -> super.checkParam.test( row )
                            && super.checkEquality.test( Status.valueOf( row.getString( "status" ) ), Status.CREATED ) )
                    .isPresent();

    // возвращает все сос сигналы для конкретного патрульного
    private final Function< UUID, Mono< ApiResponseModel > > getAllSosForCurrentPatrul = patrulUUID -> Flux.fromStream(
            this.getSession().execute( "SELECT * FROM "
                            + CassandraTables.TABLETS + "."
                            + CassandraTables.PATRUL_SOS_LIST
                            + " WHERE patruluuid = " + patrulUUID + ";" )
                    .one()
                    .getSet( "attachedsoslist", UUID.class )
                    .stream()
                    .parallel() )
            .parallel( patrulUUID.toString().length() )
            .runOn( Schedulers.parallel() )
            .filter( this.getCheckSosWasFinished() )
            .map( this.getGetCurrentPatrulSos() )
            .flatMap( patrulSos -> CassandraDataControl
                    .getInstance()
                    .getGetPatrulByUUID()
                    .apply( patrulSos.getPatrulUUID() )
                    .map( patrul -> new SosTotalData(
                            patrulSos,
                            patrulSos.getPatrulStatuses().get( patrulUUID ),
                            new SosNotificationForAndroid(
                                    patrulSos,
                                    patrul,
                                    Status.CREATED,
                                    patrul.getPassportNumber() ) ) ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .collectList()
            .flatMap( sosTotalDataList -> super.getFunction().apply(
                    Map.of( "message", "Your list of sos signals",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( sosTotalDataList )
                                    .build() ) ) );

    // создает список различных сос сигналов для нового патрульного
    private final Consumer< UUID > createRowInPatrulSosListTable = uuid -> this.getSession().execute(
            "INSERT INTO "
                    + CassandraTables.TABLETS + "."
                    + CassandraTables.PATRUL_SOS_LIST
                    + " ( patruluuid, " +
                    "sentSosList, " +
                    "attachedSosList, " +
                    "cancelledSosList, " +
                    "acceptedSosList ) VALUES ( " + uuid + ", {}, {}, {}, {} ) IF NOT EXISTS;" );

    private final Function< TaskDetailsRequest, Mono< ActiveTask > > getActiveTask = taskDetailsRequest -> switch ( taskDetailsRequest.getTaskTypes() ) {
            case CARD_102 -> this.getGetTask().apply( taskDetailsRequest.getId() )
                    .map( row -> super.deserialize( row.getString( "object" ), Card.class ) )
                    .map( card -> new ActiveTask(
                            card,
                            card.getUUID().toString(),
                            card.getCardId().toString(),
                            card.getStatus(),
                            CARD_102,
                            card.getPatruls() ) );

            case FIND_FACE_CAR -> this.getGetTask().apply( taskDetailsRequest.getId() )
                    .map( row -> super.deserialize( row.getString("object" ), CarEvent.class ) )
                    .map( carEvent -> new ActiveTask(
                            carEvent,
                            carEvent.getUUID().toString(),
                            carEvent.getId(),
                            carEvent.getStatus(),
                            FIND_FACE_CAR,
                            carEvent.getPatruls() ) );

            case FIND_FACE_PERSON -> this.getGetTask().apply( taskDetailsRequest.getId() )
                    .map( row -> super.deserialize( row.getString("object" ), FaceEvent.class ) )
                    .map( faceEvent -> new ActiveTask(
                            faceEvent,
                            faceEvent.getUUID().toString(),
                            faceEvent.getId(),
                            faceEvent.getStatus(),
                            FIND_FACE_PERSON,
                            faceEvent.getPatruls() ) );

            case FIND_FACE_EVENT_CAR -> this.getGetTask().apply( taskDetailsRequest.getId() )
                    .map( row -> super.deserialize( row.getString("object" ), EventCar.class ) )
                    .map( eventCar -> new ActiveTask(
                            eventCar,
                            eventCar.getUUID().toString(),
                            eventCar.getId(),
                            eventCar.getStatus(),
                            FIND_FACE_EVENT_CAR,
                            eventCar.getPatruls() ) );

            case FIND_FACE_EVENT_BODY -> this.getGetTask().apply( taskDetailsRequest.getId() )
                    .map( row -> super.deserialize( row.getString("object" ), EventBody.class ) )
                    .map( eventBody -> new ActiveTask(
                            eventBody,
                            eventBody.getUUID().toString(),
                            eventBody.getId(),
                            eventBody.getStatus(),
                            FIND_FACE_EVENT_BODY,
                            eventBody.getPatruls() ) );

            case FIND_FACE_EVENT_FACE -> this.getGetTask().apply( taskDetailsRequest.getId() )
                    .map( row -> super.deserialize( row.getString( "object" ), EventFace.class ) )
                    .map( eventFace -> new ActiveTask(
                            eventFace,
                            eventFace.getUUID().toString(),
                            eventFace.getId(),
                            eventFace.getStatus(),
                            FIND_FACE_EVENT_FACE,
                            eventFace.getPatruls() ) );

            default -> this.getGetTask().apply( taskDetailsRequest.getId() )
                    .map( row -> super.deserialize( row.getString("object" ), SelfEmploymentTask.class ) )
                    .map( selfEmploymentTask -> new ActiveTask(
                            selfEmploymentTask,
                            selfEmploymentTask.getUuid().toString(),
                            selfEmploymentTask.getUuid().toString(),
                            selfEmploymentTask.getTaskStatus(),
                            SELF_EMPLOYMENT,
                            selfEmploymentTask.getPatruls() ) ); };
}
