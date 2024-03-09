package com.ssd.mvd.gpstabletsservice.database;

import java.util.*;
import java.util.function.*;
import java.text.MessageFormat;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;

import com.ssd.mvd.gpstabletsservice.task.card.*;
import com.ssd.mvd.gpstabletsservice.constants.*;
import com.ssd.mvd.gpstabletsservice.entity.Point;
import com.ssd.mvd.gpstabletsservice.task.sos_task.*;
import com.ssd.mvd.gpstabletsservice.request.SosRequest;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.SerDes;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.request.TaskTimingRequest;
import com.ssd.mvd.gpstabletsservice.request.TaskDetailsRequest;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.inspectors.TaskCommonParams;
import com.ssd.mvd.gpstabletsservice.controller.UnirestController;
import com.ssd.mvd.gpstabletsservice.subscribers.CustomSubscriber;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.entity.notifications.Notification;
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

public final class CassandraDataControlForTasks extends SerDes {
    private final Session session = CassandraDataControl.getInstance().getSession();

    private static CassandraDataControlForTasks INSTANCE = new CassandraDataControlForTasks();

    public static CassandraDataControlForTasks getInstance () {
        return INSTANCE != null ? INSTANCE : ( INSTANCE = new CassandraDataControlForTasks() );
    }

    public Session getSession() {
        return this.session;
    }

    private CassandraDataControlForTasks () {
        super.logging( this.getClass().getName() + " was created" );
    }

    /*
    возвращает ROW из БД для любой таблицы внутри TABLETS
    */
    private Row getRowFromTaskKeyspace (
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

    public final Function< String, List< ViolationsInformation > > getViolationsInformationList = gosnumber ->
            Optional.ofNullable(
                    this.getRowFromTaskKeyspace(
                            CassandraTables.CARTOTALDATA,
                            "gosnumber",
                            super.joinWithAstrix( gosnumber ) ) )
                    .map( row -> row.getList( "violationsInformationsList", ViolationsInformation.class ) )
                    .orElseGet( super::emptyList );

    public final Function< String, Mono< ApiResponseModel > > getWarningCarDetails = gosnumber -> super.function(
            Map.of( "message", "Warning car details",
                    "data", com.ssd.mvd.gpstabletsservice.entity.Data.from(
                            new CardDetails(
                                    super.deserialize(
                                            this.getRowFromTaskKeyspace(
                                                    CassandraTables.CARTOTALDATA,
                                                    "gosnumber",
                                                    super.joinWithAstrix( gosnumber )
                                            ).getString( "object" ),
                                    CarTotalData.class ) ) ) ) );

    // возвращает запись из БД для конкретной задачи
    public final Function< String, Mono< Row > > getTask = uuid ->
            super.convert(
                    this.getRowFromTaskKeyspace(
                            CassandraTables.TASKS_STORAGE_TABLE,
                            "uuid",
                            uuid )
            );

    public final Consumer< String > deleteActiveTask = id -> this.getSession().execute(
            MessageFormat.format(
                    """
                    {0} {1}.{2} WHERE id = {3};
                    """,
                    CassandraCommands.DELETE,
                    CassandraTables.TABLETS,
                    CassandraTables.ACTIVE_TASK,
                    id
            )
    );

    /*
    сохраняет уведомление, обновляет данные патрульного и задачи
    */
    public Notification updateTaskPatrulAndNotificationAfterChange (
            final TaskCommonParams taskCommonParams,
            final Object clazz,
            final Patrul patrul,
            final Notification notification
    ) {
        final StringBuilder stringBuilder = super.newStringBuilder();

        /*
        проверяем что задача еще не завершена
        */
        if ( taskCommonParams.isNotFinished() ) {
            /*
            генерируем и сохраняем свежие данные о задаче и патрульном
            */
            stringBuilder.append(
                    CassandraDataControlForTasks
                            .getInstance()
                            .saveActiveTask
                            .apply( ActiveTask.generate(
                                    clazz,
                                    patrul.getPatrulTaskInfo().getStatus(),
                                    taskCommonParams ) )
            );
        }

        this.getSession().execute(
                MessageFormat.format(
                        """
                        {0}
                        {1}
                        {2}
                        {3}
                        {4};
                        """,
                        /*
                        запускаем BATCH
                        */
                        stringBuilder.toString(),

                        /*
                        обновляем данные самой задачи
                        */
                        MessageFormat.format(
                                """
                                {0} {1}.{2}
                                (uuid, id, tasktype, object)
                                VALUES ( {3}, {4}, {5}, {6} );
                                """,
                                CassandraCommands.INSERT_INTO,
                                CassandraTables.TABLETS,
                                CassandraTables.TASKS_STORAGE_TABLE,

                                taskCommonParams.getUuid(),

                                super.joinWithAstrix( taskCommonParams.getTaskId() ),
                                super.joinWithAstrix( taskCommonParams.getTaskTypes() ),
                                super.joinWithAstrix( super.serialize( clazz ) )
                        ),

                        /*
                        обновляем данные патрульного
                        */
                        MessageFormat.format(
                                """
                                {0} {1}.{2}
                                SET status = {3},
                                taskTypes = {4},
                                taskId = {5},
                                uuidOfEscort = {6},
                                uuidForEscortCar = {7},
                                longitudeOfTask = {8,number,#},
                                latitudeOfTask = {9,number,#},
                                taskDate = {10},
                                listOfTasks = {11}
                                WHERE uuid = {12};
                                """,
                                CassandraCommands.UPDATE,
                                CassandraTables.TABLETS,
                                CassandraTables.PATRULS,

                                super.joinWithAstrix( patrul.getPatrulTaskInfo().getStatus() ),
                                super.joinWithAstrix( patrul.getPatrulTaskInfo().getTaskTypes() ),
                                super.joinWithAstrix( patrul.getPatrulTaskInfo().getTaskId() ),

                                patrul.getPatrulUniqueValues().getUuidOfEscort(),
                                patrul.getPatrulUniqueValues().getUuidForEscortCar(),
                                patrul.getPatrulLocationData().getLongitudeOfTask(),
                                patrul.getPatrulLocationData().getLatitudeOfTask(),

                                super.joinWithAstrix(
                                        ( super.objectIsNotNull( patrul.getPatrulDateData().getTaskDate() )
                                                ? patrul.getPatrulDateData().getTaskDate()
                                                : super.newDate() )
                                ),
                                super.convertMapToCassandra.apply( patrul.getPatrulTaskInfo().getListOfTasks() ),
                                patrul.getUuid()
                        ),
                        /*
                        добавляем новое уведомление в БД
                        */
                        MessageFormat.format(
                                """
                                {0} {1}.{2} {3}
                                VALUES ( {4}, {5}, {6}, {7}, {8}, {9}, {10}, {11}, {12}, {13}, {14}, {15}, {16}, {17}, {18} );
                                """,
                                CassandraCommands.INSERT_INTO,
                                CassandraTables.TABLETS,
                                CassandraTables.NOTIFICATION,
                                super.getALlParamsNamesForClass.apply( Notification.class ),

                                super.joinWithAstrix( notification.getId() ),
                                super.joinWithAstrix( notification.getType() ),
                                super.joinWithAstrix( notification.getTitle() ),
                                super.joinWithAstrix( notification.getAddress() ),
                                super.joinWithAstrix( notification.getCarNumber() ),
                                super.joinWithAstrix( notification.getPoliceType() ),
                                super.joinWithAstrix( notification.getNsfOfPatrul() ),
                                super.joinWithAstrix( notification.getPassportSeries() ),

                                notification.getLongitudeOfTask(),
                                notification.getLongitudeOfTask(),

                                super.joinWithAstrix( notification.getStatus() ),
                                super.joinWithAstrix( notification.getTaskStatus() ),
                                false,
                                super.joinWithAstrix( notification.getTaskTypes() ),
                                CassandraFunctions.TO_TIMESTAMP.formatted( CassandraFunctions.NOW )
                        ),
                        /*
                        завершаем BATCH
                        */
                        CassandraCommands.APPLY_BATCH
                )
        );

        return notification;
    }

    public final Function< CarTotalData, Boolean > saveCarTotalData = carTotalData ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            ( gosnumber, cameraImage, violationsInformationsList, object )
                            VALUES( {3}, {4}, {5}, {6} );
                            """,
                            CassandraCommands.INSERT_INTO,
                            CassandraTables.TABLETS,
                            CassandraTables.CARTOTALDATA,

                            super.joinWithAstrix( carTotalData.getGosNumber() ),
                            super.joinWithAstrix( carTotalData.getCameraImage() ),

                            super.convertListOfPointsToCassandra.apply(
                                    carTotalData
                                            .getViolationsList()
                                            .getViolationsInformationsList() ),
                            super.joinWithAstrix( super.serialize( carTotalData ) ) ) )
                    .wasApplied();

    private final Function< ActiveTask, String > saveActiveTask = activeTask ->
            MessageFormat.format(
                    """
                    {0} {1}.{2}
                    ( id, object )
                    VALUES ( {3}, {4} );
                    """,
                    CassandraCommands.INSERT_INTO,
                    CassandraTables.TABLETS,
                    CassandraTables.ACTIVE_TASK,

                    super.joinWithAstrix( activeTask.getTaskId() ),
                    super.joinWithAstrix( super.serialize( activeTask ) )
            );

    // если патрульному отменили задание то нужно удалить запись о времени затраченное на задачу
    public final Function< Patrul, UUID > deleteRowFromTaskTimingTable = patrul -> {
            Optional.ofNullable( patrul )
                    .filter( patrul1 -> super.objectIsNotNull( patrul.getPatrulTaskInfo().getTaskId() ) )
                    .ifPresent( patrul1 -> this.getSession().execute(
                            MessageFormat.format(
                                    """
                                    {0} {1}.{2} WHERE taskId = {3} AND patruluuid = {4} {5};
                                    """,
                                    CassandraCommands.DELETE,
                                    CassandraTables.TABLETS,
                                    CassandraTables.TASKS_TIMING_TABLE,

                                    super.joinWithAstrix( patrul.getPatrulTaskInfo().getTaskId() ),
                                    patrul.getUuid(),
                                    CassandraCommands.IF_EXISTS
                            )
                    ) );

            return patrul.getUuid();
    };

    /*
        обновляет время которое патрульный полностью потратил на выполнение задания
        если патрульный завершил то обновляем общее время выполнения
    */
    public final BiConsumer< Patrul, Long > updateTotalTimeConsumption = ( patrul, timeConsumption ) ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            SET totaltimeconsumption = {3,number,#}
                            WHERE taskId = {4} AND patruluuid = {5};
                            """,
                            CassandraCommands.UPDATE,
                            CassandraTables.TABLETS,
                            CassandraTables.TASKS_TIMING_TABLE,
                            timeConsumption,
                            super.joinWithAstrix( patrul.getPatrulTaskInfo().getTaskId() ),
                            patrul.getUuid()
                    )
            );

    public final Consumer< TaskTimingStatistics > saveTaskTimeStatistics = taskTimingStatistics ->
            this.getSession().execute(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            ( taskId, patrulUUID, totalTimeConsumption, timeWastedToArrive,
                            dateOfComing, status, taskTypes, inTime, positionInfoList )
                            VALUES( {3}, {4}, {5,number,#}, {6,number,#}, {7}, {8}, {9}, {10}, {11} );
                            """,
                            CassandraCommands.INSERT_INTO,
                            CassandraTables.TABLETS,
                            CassandraTables.TASKS_TIMING_TABLE,

                            super.joinWithAstrix( taskTimingStatistics.getTaskId() ),
                            taskTimingStatistics.getPatrulUUID(),

                            Math.abs( taskTimingStatistics.getTotalTimeConsumption() ),
                            Math.abs( taskTimingStatistics.getTimeWastedToArrive() ),

                            super.joinWithAstrix(
                                    ( super.objectIsNotNull( taskTimingStatistics.getDateOfComing() )
                                            ? taskTimingStatistics.getDateOfComing()
                                            : super.newDate() )
                            ),
                            super.joinWithAstrix( taskTimingStatistics.getStatus() ),
                            super.joinWithAstrix( taskTimingStatistics.getTaskTypes() ),

                            taskTimingStatistics.getInTime(),
                            super.convertListOfPointsToCassandra.apply( taskTimingStatistics.getPositionInfoList() ) ) );

    public final Function< TaskTimingRequest, Mono< TaskTimingStatisticsList > > getTaskTimingStatistics = request ->
            Flux.just( TaskTimingStatisticsList.empty() )
                    .flatMap( taskTimingStatisticsList -> CassandraDataControl
                            .getInstance()
                            .getAllEntities
                            .apply( CassandraTables.TABLETS, CassandraTables.TASKS_TIMING_TABLE )
                            .filter( row -> super.objectIsNotNull( row.getTimestamp( "dateofcoming" ) )
                                    && super.checkTaskTimingRequest( request, row )
                                    && super.checkTaskType( request, row ) )
                            .flatMap( row -> CassandraDataControl
                                    .getInstance()
                                    .getPatrulByUUID
                                    .apply( row.getUUID( "patrulUUID" ) )
                                    .map( patrul -> TaskTimingStatistics.generate( row, patrul ) ) )
                            .sequential()
                            .publishOn( Schedulers.single() )
                            .collectList()
                            .map( taskTimingStatisticsList1 -> {
                                super.analyze(
                                        taskTimingStatisticsList1,
                                        taskTimingStatistics1 -> {
                                            switch ( taskTimingStatistics1.getStatus() ) {
                                                case LATE -> taskTimingStatisticsList.getListLate().add( taskTimingStatistics1 );
                                                case IN_TIME -> taskTimingStatisticsList.getListInTime().add( taskTimingStatistics1 );
                                                default -> taskTimingStatisticsList.getListDidNotArrived().add( taskTimingStatistics1 );
                                            }
                                        }
                                );
                                return taskTimingStatisticsList;
                            } ) )
                    .single();

    // возвращает список точек локаций, где был патрульной пока не дашел до точки назначения
    private final BiFunction< String, UUID, TaskTotalData > getPatrulLocationHistoryBeforeArrived = ( taskId, patrulUUID ) ->
            new TaskTotalData(
                    this.getSession().execute(
                            MessageFormat.format(
                                    """
                                    {0} {1}.{2}
                                    WHERE taskid = {3} AND patruluuid = {4};
                                    """,
                                    CassandraCommands.SELECT_ALL,
                                    CassandraTables.TABLETS,
                                    CassandraTables.TASKS_TIMING_TABLE,
                                    super.joinWithAstrix( taskId ),
                                    patrulUUID
                            )
                    ).one()
            );

    // определяет тип задачи
    private CassandraTables findTable (
            final String id
    ) {
        if ( this.checkTable( id, CassandraTables.FACEPERSON ) ) {
            return CassandraTables.FACEPERSON;
        } else if ( this.checkTable( id, CassandraTables.EVENTBODY ) ) {
            return CassandraTables.EVENTBODY;
        } else {
            return CassandraTables.EVENTFACE;
        }
    }

    public final Function< TaskDetailsRequest, Mono< TaskDetails > > getTaskDetails = taskDetailsRequest ->
            this.getTask.apply( taskDetailsRequest.getId() )
                    .flatMap( row -> switch ( taskDetailsRequest.getTaskTypes() ) {
                case CARD_102 -> super.convert( super.deserialize( row.getString( "object" ), Card.class ) )
                        .map( card -> TaskDetails.generate(
                                card,
                                taskDetailsRequest.getPatrulUUID(),
                                this.getPatrulLocationHistoryBeforeArrived.apply(
                                        card.getTaskCommonParams().getUuid().toString(),
                                        taskDetailsRequest.getPatrulUUID() ),
                                card.getTaskCommonParams() ) );

                case FIND_FACE_CAR -> super.checkTable( taskDetailsRequest.getId(), CassandraTables.FACECAR )
                        ? super.convert( super.deserialize( row.getString("object" ), CarEvent.class ) )
                        .map( carEvent -> TaskDetails.generate(
                                carEvent,
                                taskDetailsRequest.getPatrulUUID(),
                                this.getPatrulLocationHistoryBeforeArrived.apply(
                                        carEvent.getTaskCommonParams().getUuid().toString(),
                                        taskDetailsRequest.getPatrulUUID() ),
                                carEvent.getTaskCommonParams() ) )
                        : super.convert( super.deserialize( row.getString("object" ), EventCar.class ) )
                        .map( eventCar -> TaskDetails.generate(
                                eventCar,
                                taskDetailsRequest.getPatrulUUID(),
                                this.getPatrulLocationHistoryBeforeArrived.apply(
                                        eventCar.getTaskCommonParams().getUuid().toString(),
                                        taskDetailsRequest.getPatrulUUID() ),
                                eventCar.getTaskCommonParams() ) );

                case FIND_FACE_PERSON -> switch ( this.findTable( taskDetailsRequest.getId() ) ) {
                    case FACEPERSON -> super.convert( super.deserialize( row.getString("object" ), FaceEvent.class ) )
                            .map( faceEvent -> TaskDetails.generate(
                                    faceEvent,
                                    taskDetailsRequest.getPatrulUUID(),
                                    this.getPatrulLocationHistoryBeforeArrived.apply(
                                            faceEvent.getTaskCommonParams().getUuid().toString(),
                                            taskDetailsRequest.getPatrulUUID() ),
                                    faceEvent.getTaskCommonParams() ) );

                    case EVENTBODY -> super.convert( super.deserialize( row.getString("object" ), EventBody.class ) )
                            .map( eventBody -> TaskDetails.generate(
                                    eventBody,
                                    taskDetailsRequest.getPatrulUUID(),
                                    this.getPatrulLocationHistoryBeforeArrived.apply(
                                            eventBody.getTaskCommonParams().getUuid().toString(),
                                            taskDetailsRequest.getPatrulUUID() ),
                                    eventBody.getTaskCommonParams() ) );

                    default -> super.convert( super.deserialize( row.getString( "object" ), EventFace.class ) )
                            .map( eventFace -> TaskDetails.generate(
                                    eventFace,
                                    taskDetailsRequest.getPatrulUUID(),
                                    this.getPatrulLocationHistoryBeforeArrived.apply(
                                            eventFace.getTaskCommonParams().getUuid().toString(),
                                            taskDetailsRequest.getPatrulUUID() ),
                                    eventFace.getTaskCommonParams() ) );
                };

                default -> super.convert( super.deserialize( row.getString("object" ), SelfEmploymentTask.class ) )
                        .map( selfEmploymentTask -> TaskDetails.generate(
                                selfEmploymentTask,
                                taskDetailsRequest.getPatrulUUID(),
                                this.getPatrulLocationHistoryBeforeArrived.apply(
                                        selfEmploymentTask.getTaskCommonParams().getUuid().toString(),
                                        taskDetailsRequest.getPatrulUUID() ),
                                selfEmploymentTask.getTaskCommonParams() ) );
            } );

    // сохраняет сос сигнал от патрульного
    private final Function< PatrulSos, Mono< ApiResponseModel > > saveSos = patrulSos -> this.getSession().execute(
            MessageFormat.format(
                    """
                    {0} {1}.{2}
                    ( sosWasSendDate, patruluuid, longitude, latitude )
                    VALUES ( {3}, {4}, {5}, {6} ) {7};
                    """,
                    CassandraCommands.INSERT_INTO,
                    CassandraTables.TABLETS,
                    CassandraTables.SOS_TABLE,

                    CassandraFunctions.TO_TIMESTAMP.formatted( CassandraFunctions.NOW ),

                    patrulSos.getPatrulUUID(),
                    patrulSos.getLongitude(),
                    patrulSos.getLatitude(),
                    CassandraCommands.IF_NOT_EXISTS ) )
            .wasApplied()
            ? super.function(
                    Map.of( "message", KafkaDataControl // sending message to Kafka
                            .getKafkaDataControl()
                            .getWriteSosNotificationToKafka()
                            .apply( SosNotification
                                    .builder()
                                    .status( Status.CREATED )
                                    .patrulUUID( patrulSos.getPatrulUUID() )
                                    .build() ),
                    "data", com.ssd.mvd.gpstabletsservice.entity.Data.from( Status.ACTIVE ) ) )
            : super.function(
                    Map.of( "message", "Sos was deleted successfully",
                    "data", com.ssd.mvd.gpstabletsservice.entity.Data.from( Status.IN_ACTIVE ) ) )
            .map( status -> { // если патрульный уже отправлял сигнал ранее, то этот сигнал будет удален
                KafkaDataControl // sending message to Kafka
                        .getKafkaDataControl()
                        .getWriteSosNotificationToKafka()
                        .apply( SosNotification
                                .builder()
                                .status( Status.CANCEL ) // отправляем уведомление фронту
                                .patrulUUID( patrulSos.getPatrulUUID() )
                                .build() );

                this.getSession().execute(
                        MessageFormat.format(
                                """
                                {0} {1}.{2} WHERE patrulUUID = {3};
                                """,
                                CassandraCommands.DELETE,
                                CassandraTables.TABLETS,
                                CassandraTables.SOS_TABLE,
                                patrulSos.getPatrulUUID() ) );

                return status;
            } );

    // связывает патрульного с сос сигналом
    private final BiFunction< UUID, UUID, String > updatePatrulSos = ( uuid, uuidOfPatrul ) ->
            MessageFormat.format(
                    """
                    {0} {1}.{2}
                    SET patrulUniqueValues.sos_id = {3}
                    WHERE uuid = {4};
                    """,
                    CassandraCommands.UPDATE,
                    CassandraTables.TABLETS,
                    CassandraTables.PATRULS,
                    uuid,
                    uuidOfPatrul
            );

    // по статусу определяет какой параметр обновлять
    private final Function< Status, String > defineNecessaryTable = status -> switch ( status ) {
        case ATTACHED -> "attachedSosList";
        case CANCEL -> "cancelledSosList";
        case CREATED -> "sentSosList";
        default -> "acceptedSosList";
    };

    /*
    обновляем данные о самом СОС сигнале,
    в зависимости от статуса, обновляется разные SET
     */
    private String updatePatrulSosList (
            final UUID sosUUID,
            final UUID patrulUUID,
            final Status status ) {
        return MessageFormat.format(
                """
                {0} {1}.{2}
                SET {3} = {3} + {4}
                WHERE patrulUUID = {5};
                """,
                CassandraCommands.UPDATE,
                CassandraTables.TABLETS,
                CassandraTables.PATRUL_SOS_LIST,

                this.defineNecessaryTable.apply( status ),
                super.joinTextWithCorrectCollectionEnding(
                        sosUUID.toString(),
                        CassandraDataTypes.MAP
                ),
                patrulUUID
        );
    }

    private final Function< PatrulSos, String > save = patrulSos ->
            MessageFormat.format(
                    """
                    {0} {1}.{2} {3}
                    VALUES ( {4}, {5}, {6}, {7}, {7}, {8}, {9}, {10}, {11} );
                    """,
                    CassandraCommands.INSERT_INTO,
                    CassandraTables.TABLETS,
                    CassandraTables.PATRUL_SOS_TABLE,

                    super.getALlParamsNamesForClass.apply( PatrulSos.class ),
                    CassandraFunctions.UUID,
                    patrulSos.getPatrulUUID(),

                    super.joinWithAstrix( patrulSos.getAddress() ),

                    CassandraFunctions.TO_TIMESTAMP.formatted( CassandraFunctions.NOW ),
                    patrulSos.getLatitude(),
                    patrulSos.getLongitude(),

                    super.joinWithAstrix( Status.CREATED ),
                    super.convertSosMapToCassandra.apply( patrulSos.getPatrulStatuses() )
            );

    public final Function< PatrulSos, Mono< ApiResponseModel > > savePatrulSos = patrulSos ->
            CassandraDataControl
                    .getInstance()
                    .getPatrulByUUID
                    .apply( patrulSos.getPatrulUUID() )
                    .flatMap( patrul -> this.saveSos.apply( patrulSos )
                            .flatMap( apiResponseModel -> Optional.ofNullable( apiResponseModel )
                                    /*
                                        проверяем создан ли сос сигнал в первый раз или патрульный
                                        отправляет его второй раз чтобы отменить
                                    */
                                    .filter( apiResponseModel1 -> Status.valueOf(
                                            apiResponseModel
                                                    .getData()
                                                    .getData()
                                                    .toString() ).isActive() )
                                    .map( apiResponseModel1 -> {
                                        final StringBuilder stringBuilder = super.newStringBuilder();

                                        /*
                                        создаем Мар для хранения данныз о патрульных,
                                        которые были прикреплены к этому сигналу
                                        */
                                        patrulSos.setPatrulStatuses( super.newMap() );

                                        // обновляем список сигналов которые отправлял патрульный
                                        stringBuilder.append(
                                                this.updatePatrulSosList( patrulSos.getUuid(), patrul.getUuid(), Status.CREATED )
                                        );

                                        // закрепляем этот сос сигнал за тем кто отправил его
                                        stringBuilder.append(
                                                this.updatePatrulSos.apply( patrulSos.getUuid(), patrulSos.getPatrulUUID() )
                                        );

                                        // сохраняем адрес сигнала
                                        patrulSos.setAddress( super.removeAllDotes(
                                                UnirestController
                                                        .getInstance()
                                                        .getAddressByLocation
                                                        .apply( patrulSos.getLatitude(), patrulSos.getLongitude() ) ) );

                                        final Flux< SosNotificationForAndroid > sosNotificationForAndroidFlux =
                                                CassandraDataControl
                                                        .getInstance()
                                                        .findTheClosestPatrulsForSos
                                                        .apply( Point.from( patrulSos ), patrul.getUuid() )
                                                        .parallel( 20 )
                                                        .runOn( Schedulers.parallel() )
                                                        .map( patrul1 -> {
                                                            /*
                                                            обновляем данные СОС сигнала,
                                                            прикрепляем всех патрульных, которые находятся
                                                            рядом с локацием СОС сигнала
                                                            */
                                                            stringBuilder.append(
                                                                    this.updatePatrulSosList( patrulSos.getUuid(), patrul1.getUuid(), Status.ATTACHED )
                                                            );

                                                            /*
                                                            также сохраняем статусы патрульных
                                                            ATTACHED -> патрульный только получил сигнал
                                                            */
                                                            patrulSos.getPatrulStatuses().put( patrul1.getUuid(), Status.ATTACHED.name() );

                                                            /*
                                                            сохраняем СОС сигнал
                                                             */
                                                            stringBuilder.append( this.save.apply( patrulSos ) );

                                                            /*
                                                            генерируем СОС уведомление для андроида
                                                             */
                                                            return new SosNotificationForAndroid(
                                                                    patrulSos,
                                                                    patrul,
                                                                    Status.ACTIVE );
                                                        } )
                                                        .sequential()
                                                        .publishOn( Schedulers.single() );

                                        /*
                                        сохраняем финальный BATCH
                                        */
                                        this.getSession().execute(
                                                stringBuilder.append( CassandraCommands.APPLY_BATCH ).toString()
                                        );

                                        /*
                                        отправляем конечные СОС уведомления андроиду через Кафку
                                        */
                                        return KafkaDataControl
                                                .getKafkaDataControl()
                                                .getSendSosNotificationsToAndroid()
                                                .apply( sosNotificationForAndroidFlux, apiResponseModel );
                                    } )
                                    /*
                                    в случае если патрульный отправляет сигнал уже второй раз,
                                    то отменяем сигнал
                                    */
                                    .orElseGet( () -> {
                                        final PatrulSos patrulSos1 = this.getCurrentPatrulSos.apply( patrul.getPatrulUniqueValues().getSos_id() );

                                        final StringBuilder stringBuilder = super.newStringBuilder();

                                        stringBuilder.append(
                                                this.updatePatrulSos.apply( null, patrul.getUuid() )
                                        );

                                        /*
                                        меняем статус сигнала на выполнено,
                                        */
                                        stringBuilder.append(
                                                MessageFormat.format(
                                                        """
                                                        {0} {1}.{2}
                                                        SET status = {3}, -- меняем статус сигнала на выполнено
                                                        sosWasClosed = {4} -- дата закрытия сигнала
                                                        WHERE uuid = {5};
                                                        """,
                                                        CassandraCommands.UPDATE,
                                                        CassandraTables.TABLETS,
                                                        CassandraTables.PATRUL_SOS_TABLE,
                                                        super.joinWithAstrix( Status.FINISHED ),
                                                        CassandraFunctions.TO_TIMESTAMP.formatted( CassandraFunctions.NOW ),
                                                        patrulSos1.getUuid()
                                                )
                                        );

                                        final Flux< SosNotificationForAndroid > sosNotificationForAndroidFlux =
                                                Flux.fromStream(
                                                        patrulSos1
                                                                .getPatrulStatuses()
                                                                .keySet()
                                                                .stream() )
                                                        .parallel()
                                                        .runOn( Schedulers.parallel() )
                                                        .flatMap( CassandraDataControl.getInstance().getPatrulByUUID )
                                                        .map( patrul1 -> {
                                                            if ( super.objectIsNotNull( patrul1.getPatrulUniqueValues().getSos_id() )
                                                                    && patrul1
                                                                    .getPatrulUniqueValues()
                                                                    .getSos_id()
                                                                    .compareTo( patrulSos1.getUuid() ) == 0 ) {
                                                                stringBuilder.append(
                                                                        this.updatePatrulSos.apply( null, patrul1.getUuid() )
                                                                );
                                                            }

                                                            return new SosNotificationForAndroid(
                                                                    patrulSos1,
                                                                    patrul,
                                                                    Status.IN_ACTIVE );
                                                        } )
                                                        .sequential()
                                                        .publishOn( Schedulers.single() );

                                        this.getSession().execute(
                                                stringBuilder.append( CassandraCommands.APPLY_BATCH ).toString()
                                        );

                                        return KafkaDataControl
                                                .getKafkaDataControl()
                                                .getSendSosNotificationsToAndroid()
                                                .apply( sosNotificationForAndroidFlux, apiResponseModel );
                                    } ) ) );

    // используется в случае когда патрульный либо принимает сигнал, либо отказывается
    public final Function< SosRequest, Mono< ApiResponseModel > > updatePatrulStatusInSosTable = sosRequest -> {
            final StringBuilder stringBuilder = super.newStringBuilder();

            // добавляем данный сос сигнал в список
            stringBuilder.append(
                    this.updatePatrulSosList( sosRequest.getSosUUID(), sosRequest.getPatrulUUID(), sosRequest.getStatus() )
            );

            // если патрульный подтвердил данный сигнал, то связымаем его с ним
            if ( sosRequest.getStatus().isAccepted() ) {
                stringBuilder.append(
                        this.updatePatrulSos.apply( sosRequest.getSosUUID(), sosRequest.getPatrulUUID() )
                );

                // меняем статус сос сигнала на принятый
                stringBuilder.append(
                        MessageFormat.format(
                                """
                                {0} {1}.{2}
                                SET status = {3}
                                WHERE uuid = {4};
                                """,
                                CassandraCommands.UPDATE,
                                CassandraTables.TABLETS,
                                CassandraTables.PATRUL_SOS_TABLE,
                                super.joinWithAstrix( Status.ACCEPTED ),
                                sosRequest.getSosUUID()
                        )
                );

                /*
                отправляем уведомления что патрульный принял со сигнал фронту
                 */
                KafkaDataControl
                        .getKafkaDataControl()
                        .getWriteSosNotificationToKafka()
                        .apply( SosNotification
                                .builder()
                                .patrulUUID( this.getCurrentPatrulSos.apply( sosRequest.getSosUUID() ).getPatrulUUID() )
                                .status( Status.ACCEPTED )
                                .build() );
            }

            stringBuilder.append(
                    MessageFormat.format(
                            """
                            {0} {1}.{2}
                            SET patrulStatuses[ {3} ] = {4} WHERE uuid = {5};
                            """,
                            CassandraCommands.UPDATE,
                            CassandraTables.TABLETS,
                            CassandraTables.PATRUL_SOS_TABLE,
                            sosRequest.getPatrulUUID(),
                            super.joinWithAstrix( ( sosRequest.getStatus().isCanceled() ? Status.ATTACHED : sosRequest.getStatus() ) ),
                            sosRequest.getSosUUID()
                    )
            );

            this.getSession().execute(
                    stringBuilder.append( CassandraCommands.APPLY_BATCH ).toString()
            );

            return super.function( Map.of( "message", "You have changed status of sos task" ) );
    };

    private final Function< UUID, PatrulSos > getCurrentPatrulSos = uuid -> new PatrulSos(
            this.getRowFromTaskKeyspace(
                    CassandraTables.PATRUL_SOS_TABLE,
                    "uuid",
                    uuid.toString()
            )
    );

    // проверяет не завершен ли сос сигнал
    private final Predicate< UUID > checkSosWasFinished = uuid ->
            Optional.ofNullable(
                    this.getRowFromTaskKeyspace(
                            CassandraTables.PATRUL_SOS_TABLE,
                            "uuid",
                            uuid.toString() ) )
                    .filter( row -> super.objectIsNotNull( row )
                            && ( Status.valueOf( row.getString( "status" ) ).isCreated()
                            || Status.valueOf( row.getString( "status" ) ).isAccepted() ) )
                    .isPresent();

    // возвращает все сос сигналы для конкретного патрульного
    public final Function< UUID, Mono< ApiResponseModel > > getAllSosForCurrentPatrul = patrulUUID -> Flux.fromStream(
            this.getRowFromTaskKeyspace(
                    CassandraTables.PATRUL_SOS_LIST,
                    "patruluuid",
                    patrulUUID.toString() )
            .getSet( "attachedsoslist", UUID.class )
            .stream() )
            .parallel( patrulUUID.toString().length() )
            .runOn( Schedulers.parallel() )
            .filter( this.checkSosWasFinished )
            .map( this.getCurrentPatrulSos )
            .map( patrulSos -> new SosTotalData(
                    patrulSos,
                    patrulSos.getPatrulStatuses().get( patrulUUID ),
                    new SosNotificationForAndroid(
                            patrulSos,
                            CassandraDataControl
                                    .getInstance()
                                    .getRowFromTabletsKeyspace(
                                            CassandraTables.PATRULS,
                                            "uuid",
                                            patrulSos.getPatrulUUID().toString() ) ) ) )
            .sequential()
            .publishOn( Schedulers.single() )
            .collectList()
            .flatMap( sosTotalDataList -> super.function(
                    Map.of( "message", "Your list of sos signals",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data.from( sosTotalDataList ) ) ) );

    /*
    находит все СОС сигналы, привязанные к патрульному
     */
    public final Function< Patrul, StringBuilder > deletePatrulSosSignals = patrul -> {
        /*
        проверяем что сам патрульный не отправлял сос сигнал
         */
        if ( super.objectIsNotNull( patrul.getPatrulUniqueValues().getSos_id() ) ) {
            /*
            если да, то отменяем этот сигнал
            */
            this.savePatrulSos.apply( this.getCurrentPatrulSos.apply( patrul.getPatrulUniqueValues().getSos_id() ) );
        }

        final StringBuilder stringBuilder = super.newStringBuilder(
                MessageFormat.format(
                        """
                        {0} {1}.{2}
                        WHERE patrulUUID = {3};
                        """,
                        CassandraCommands.DELETE,
                        CassandraTables.TABLETS,
                        CassandraTables.SOS_TABLE,
                        patrul.getUuid()
                )
        );

        /*
            удалеям данные патрульного из всех СОС сигналов,
            к которым был прикреплен этот патрульный
        */
        super.analyze(
                this.getRowFromTaskKeyspace(
                                CassandraTables.PATRUL_SOS_LIST,
                                "patruluuid",
                                patrul.getUuid().toString() )
                        .getSet( "attachedsoslist", UUID.class ),
                uuid -> stringBuilder.append(
                        MessageFormat.format(
                                """
                                {0} {1}.{2}
                                SET patrulStatuses = patrulStatuses - [ {3} ]
                                WHERE uuid = {4};
                                """,
                                CassandraCommands.UPDATE,
                                CassandraTables.TABLETS,
                                CassandraTables.PATRUL_SOS_TABLE,
                                patrul.getUuid(),
                                uuid
                        )
                )
        );

        return stringBuilder.append(
                MessageFormat.format(
                        """
                        {0} {1}.{2} WHERE patrulUUID = {3};
                        """,
                        CassandraCommands.DELETE,
                        CassandraTables.TABLETS,
                        CassandraTables.PATRUL_SOS_TABLE,
                        patrul.getUuid()
                )
        );
    };

    public final Function< TaskDetailsRequest, Mono< ActiveTask > > getActiveTask = taskDetailsRequest ->
            this.getTask.apply( taskDetailsRequest.getId() )
                    .flatMap( row -> switch ( taskDetailsRequest.getTaskTypes() ) {
                    case CARD_102 -> super.convert(
                            super.deserialize( row.getString( "object" ), Card.class ) )
                            .map( card -> ActiveTask.generate( card, card.getTaskCommonParams() ) );

                    case FIND_FACE_CAR -> super.convert(
                            super.deserialize( row.getString("object" ), CarEvent.class ) )
                            .map( carEvent -> ActiveTask.generate( carEvent, carEvent.getTaskCommonParams() ) );

                    case FIND_FACE_PERSON -> super.convert(
                            super.deserialize( row.getString("object" ), FaceEvent.class ) )
                            .map( faceEvent -> ActiveTask.generate( faceEvent, faceEvent.getTaskCommonParams() ) );

                    case FIND_FACE_EVENT_CAR -> super.convert(
                            super.deserialize( row.getString("object" ), EventCar.class ) )
                            .map( eventCar -> ActiveTask.generate( eventCar, eventCar.getTaskCommonParams() ) );

                    case FIND_FACE_EVENT_BODY -> super.convert(
                            super.deserialize( row.getString("object" ), EventBody.class ) )
                            .map( eventBody -> ActiveTask.generate( eventBody, eventBody.getTaskCommonParams() ) );

                    case FIND_FACE_EVENT_FACE -> super.convert(
                            super.deserialize( row.getString( "object" ), EventFace.class ) )
                            .map( eventFace -> ActiveTask.generate( eventFace, eventFace.getTaskCommonParams() ) );

                    default -> super.convert(
                            super.deserialize( row.getString("object" ), SelfEmploymentTask.class ) )
                            .map( selfEmploymentTask -> ActiveTask.generate(
                                    selfEmploymentTask,
                                    selfEmploymentTask.getTaskCommonParams() ) );
            } );

    /*
    после того, как отсоединили патрульного от задачи,
    обновляем данные самой задачи в БД
    */
    private final BiFunction< TaskCommonParams, Object, String > updateTaskInfoAfterPatrulDeleting = ( taskCommonParams, clazz ) ->
            MessageFormat.format(
                    """
                    {0} {1}.{2}
                    ( uuid, id, tasktype, object )
                    VALUES ( {3}, {4}, {5}, {6} );
                    """,
                    CassandraCommands.INSERT_INTO,
                    CassandraTables.TABLETS,
                    CassandraTables.TASKS_STORAGE_TABLE,
                    taskCommonParams.getUuid(),
                    super.joinWithAstrix( taskCommonParams.getTaskId() ),
                    super.joinWithAstrix( taskCommonParams.getTaskTypes() ),
                    super.joinWithAstrix( super.serialize( clazz ) )
            );

    /*
    используется в случае когда патурльного удаляют и нам нужно отсоединить его от весх
    выполненных задач
    */
    public final Function< Patrul, StringBuilder > unlinkPatrulFromAllCompletedTasks = patrul -> {
        final StringBuilder stringBuilder = super.newStringBuilder();
        super.analyze(
                patrul.getPatrulTaskInfo().getListOfTasks(),
                ( key, value ) -> {
                    switch ( TaskTypes.valueOf( value ) ) {
                        case CARD_102 -> this.getTask.apply( key )
                                .map( row -> super.deserialize( row.getString( "object" ), Card.class ) )
                                .subscribe( new CustomSubscriber<>(
                                        card -> stringBuilder.append(
                                                this.updateTaskInfoAfterPatrulDeleting.apply(
                                                        card
                                                            .getTaskCommonParams()
                                                            .unlinkTaskInfoFromPatrul( patrul ),
                                                        card )
                                            )
                                ) );

                        case FIND_FACE_EVENT_BODY -> this.getTask.apply( key )
                                .map( row -> super.deserialize( row.getString( "object" ), EventBody.class ) )
                                .subscribe( new CustomSubscriber<>(
                                        eventBody -> stringBuilder.append(
                                                this.updateTaskInfoAfterPatrulDeleting.apply(
                                                        eventBody
                                                                .getTaskCommonParams()
                                                                .unlinkTaskInfoFromPatrul( patrul ),
                                                        eventBody )
                                            )
                                ) );

                        case FIND_FACE_EVENT_FACE -> this.getTask.apply( key )
                                .map( row -> super.deserialize( row.getString( "object" ), EventFace.class ) )
                                .subscribe( new CustomSubscriber<>(
                                        eventFace -> stringBuilder.append(
                                                this.updateTaskInfoAfterPatrulDeleting.apply(
                                                        eventFace
                                                                .getTaskCommonParams()
                                                                .unlinkTaskInfoFromPatrul( patrul ),
                                                        eventFace )
                                            )
                                ) );

                        case FIND_FACE_EVENT_CAR -> this.getTask.apply( key )
                                .map( row -> super.deserialize( row.getString( "object" ), EventCar.class ) )
                                .subscribe( new CustomSubscriber<>(
                                        eventCar -> stringBuilder.append(
                                                this.updateTaskInfoAfterPatrulDeleting.apply(
                                                        eventCar
                                                                .getTaskCommonParams()
                                                                .unlinkTaskInfoFromPatrul( patrul ),
                                                        eventCar )
                                            )
                                ) );

                        case FIND_FACE_CAR -> this.getTask.apply( key )
                                .map( row -> super.deserialize( row.getString( "object" ), CarEvent.class ) )
                                .subscribe( new CustomSubscriber<>(
                                        carEvent -> stringBuilder.append(
                                                this.updateTaskInfoAfterPatrulDeleting.apply(
                                                        carEvent
                                                                .getTaskCommonParams()
                                                                .unlinkTaskInfoFromPatrul( patrul ),
                                                        carEvent )
                                            )
                                ) );

                        case FIND_FACE_PERSON -> this.getTask.apply( key )
                                .map( row -> super.deserialize( row.getString( "object" ), FaceEvent.class ) )
                                .subscribe( new CustomSubscriber<>(
                                        faceEvent -> stringBuilder.append(
                                                this.updateTaskInfoAfterPatrulDeleting.apply(
                                                        faceEvent
                                                                .getTaskCommonParams()
                                                                .unlinkTaskInfoFromPatrul( patrul ),
                                                        faceEvent )
                                            )
                                ) );

                        default -> this.getTask.apply( key )
                                .map( row -> super.deserialize( row.getString( "object" ), SelfEmploymentTask.class ) )
                                .subscribe( new CustomSubscriber<>(
                                        selfEmploymentTask -> stringBuilder.append(
                                                this.updateTaskInfoAfterPatrulDeleting.apply(
                                                        selfEmploymentTask
                                                                .getTaskCommonParams()
                                                                .unlinkTaskInfoFromPatrul( patrul ),
                                                        selfEmploymentTask )
                                            )
                                ) );
                    }
                }
        );

        return stringBuilder;
    };
}
