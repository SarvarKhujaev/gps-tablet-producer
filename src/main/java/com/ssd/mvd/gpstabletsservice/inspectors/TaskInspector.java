package com.ssd.mvd.gpstabletsservice.inspectors;

import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.text.MessageFormat;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.ssd.mvd.gpstabletsservice.database.*;
import com.ssd.mvd.gpstabletsservice.task.card.*;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.tuple.EscortTuple;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.SerDes;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.entity.responseForAndroid.*;
import com.ssd.mvd.gpstabletsservice.constants.CassandraCommands;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.interfaces.TaskCommonMethods;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.*;
import com.ssd.mvd.gpstabletsservice.subscribers.CustomSubscriber;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.entity.notifications.Notification;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.TaskTimingStatistics;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests.PatrulActivityRequest;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PatrulTimeConsumedToArriveToTaskLocation;

/*
отвечает за работу со всеми задачами
и операциями связанными с ними
*/
public final class TaskInspector extends SerDes {
    private final static TaskInspector taskInspector = new TaskInspector();

    public static TaskInspector getInstance () {
        return taskInspector;
    }

    /*
        функция заполняет форму для уведомления андроида и фронта,
        заполняет краткое содержание задачи,
        обновляет данные патрульного,
        сохраняет все в БД
        и отправляет уведомление в Кафку
    */
    private Patrul updatePatrulAndTaskInfoAndGenerateNotification (
            final Patrul patrul,
            final Status status,
            final TaskCommonMethods taskCommonMethods
    ) {
        // отправляем в Кафку уведомление
        KafkaDataControl
                .getKafkaDataControl()
                .getWriteNotificationToKafka()
                .accept( CassandraDataControlForTasks // сохраняем саму задачу в БД
                        .getInstance()
                        .updateTaskPatrulAndNotificationAfterChange(
                                patrul, // обновляем статус патрульного после каждого изменения
                                Notification.generate( patrul, status, taskCommonMethods ),
                                taskCommonMethods
                        ) );

        return patrul;
    }

    /*
    после завершения задачи, сохраняем данные об общем расходе времени на выполнение
     */
    private final BiConsumer< Patrul, TaskTypes > updateTotalTimeConsumption = ( patrul, taskTypes ) -> {
            CassandraDataControlForTasks
                    .getInstance()
                    .updateTotalTimeConsumption
                    .accept(
                            patrul,
                            super.getTimeDifference(
                                    patrul.getPatrulDateData().getTaskDate().toInstant(),
                                    0
                            )
                    );

            // сохраняем ID и тип задачи в список патрульного
            patrul.getPatrulTaskInfo().saveNewTaskInTheMapOfCompletedTasks( taskTypes );
    };

    /*
    обрабатываем данные о передвижении патрульного пока он шел на задание
     */
    private final BiFunction< Patrul, TaskTypes, PatrulTimeConsumedToArriveToTaskLocation > saveTaskTiming = ( patrul, taskTypes ) -> {
            final PatrulTimeConsumedToArriveToTaskLocation patrulStatus =
                    PatrulTimeConsumedToArriveToTaskLocation.generate( patrul );

            // для начала сохраняем данные о том, сколько патрульный
            // потратил времени и какой маршрут он прошел пока не достиг локации задачи
            CassandraDataControl
                    .getInstance()
                    .getHistoricalPositionOfPatrulUntilArriveness
                    .apply( PatrulActivityRequest
                            .builder()
                            .endDate( super.newDate() )
                            .startDate( patrul.getPatrulDateData().getTaskDate() )
                            .patrulUUID( patrul.getPassportNumber() )
                            .build() )
                    .map( positionInfos -> TaskTimingStatistics.generate(
                            patrul,
                            taskTypes,
                            patrulStatus,
                            positionInfos ) )
                    .subscribe( new CustomSubscriber<>(
                            CassandraDataControlForTasks
                                    .getInstance()
                                    .saveTaskTimeStatistics
                    ) );

            return patrulStatus;
    };

    /*
    меняем статус задачи и патрульного
    обновляем их данные
    в зависимости от статуса, выполняем разные алгоритмы работы с данными
    */
    public Patrul changeTaskStatus (
            final Patrul patrul,
            final Status status,
            final TaskCommonMethods taskCommonMethods
    ) {
        patrul.getPatrulTaskInfo().setStatus( status );

        switch ( status ) {
            case CANCEL, FINISHED -> {
                if ( status.isFinished() ) {
                    /*
                    если Завершен, то добавлемя в список выполненных задач патрульного ID задачи
                    */
                    this.updateTotalTimeConsumption.accept( patrul, taskCommonMethods.getTaskCommonParams().getTaskTypes() );
                }
                /*
                    в ином случае, убираем ID патрульного из списка в задаче
                */
                else {
                    taskCommonMethods.remove( patrul, taskCommonMethods.getTaskCommonParams() );
                }

                taskCommonMethods.update();

                /*
                отсоединяем патрульного от задачи
                обнуляем его TaskId, TaskType, TaskDate
                */
                patrul.getPatrulTaskInfo().unlinkPatrulFromTask();
            }

            /*
            сохраянем дату когда патрульный принял задачу
            */
            case ACCEPTED -> patrul.getPatrulDateData().update( 1 );

            /*
            когда патрульный добрался до пункта назначения
            то сохраняем данные о его передвижениях
            и времени которое он потратил
            */
            case ARRIVED -> taskCommonMethods.update(
                    patrul,
                    this.saveTaskTiming.apply( patrul, taskCommonMethods.getTaskCommonParams().getTaskTypes() ),
                    taskCommonMethods.getTaskCommonParams()
            );

            /*
            в случае когда патрульному назначают задачу,
            то обновляем данные о местоположении задания
            и его ID
            */
            case ATTACHED -> patrul.update(
                    taskCommonMethods.getTaskCommonParams().getTaskTypes(),
                    taskCommonMethods.getLatitude(),
                    taskCommonMethods.getLongitude(),
                    taskCommonMethods.getTaskCommonParams().getUuid().toString()
            );
        }

        /*
        если патрульного не отменили от задачи,
        то обновляем данные патрульного в списке патрульных самой задачи
        */
        if ( !status.isCanceled() ) {
            taskCommonMethods.update( patrul, taskCommonMethods.getTaskCommonParams() );
        }

        /*
        обновляем данные патрульного, самой задачи и генерируем уведомление
        */
        return this.updatePatrulAndTaskInfoAndGenerateNotification(
                patrul,
                status,
                taskCommonMethods
        );
    }

    /*
    меняем статус задачи и патрульного
    обновляем их данные
    в зависимости от статуса, выполняем разные алгоритмы работы с данными
    */
    public Patrul changeTaskStatus (
            final Patrul patrul,
            final Status status,
            final EscortTuple escortTuple
    ) {
        final StringBuilder stringBuilder = super.newStringBuilder();

        patrul.getPatrulTaskInfo().setStatus( status );
        switch ( status ) {
            /*
            в случае когда патрульному назначают задачу,
            то обновляем данные о местоположении задания
            и его ID
            */
            case ATTACHED -> {
                patrul.getPatrulTaskInfo().setTaskTypes( TaskTypes.ESCORT );
                patrul.getPatrulTaskInfo().setTaskId( escortTuple.getUuid().toString() );
            }

            /*
            срабатывает когда патрульного либо отменили от задачи
            или же он ее завершил
            */
            case CANCEL, FINISHED -> {
                /*
                проверяем статус задачи
                Отменен или Завершен
                */
                if ( status.isFinished() ) {
                    /*
                    если Завершен, то добавлемя в список выполненных задач патрульного ID задачи
                    */
                    patrul.getPatrulTaskInfo().saveNewTaskInTheMapOfCompletedTasks( ESCORT );
                } else {
                    /*
                    в ином случае, убираем ID патрульного из списка в задаче
                    */
                    escortTuple.getPatrulList().remove( patrul.getUuid() );
                }

                CassandraDataControlForEscort
                        .getInstance()
                        .getGetCurrentTupleOfCar()
                        .apply( patrul.getPatrulUniqueValues().getUuidForEscortCar() )
                        .subscribe( new CustomSubscriber<>(
                                tuple -> stringBuilder.append(
                                        MessageFormat.format(
                                                """
                                                {0} {1}.{2}
                                                SET uuidOfPatrul = {3}
                                                WHERE uuid = {4};
                                                """,
                                                CassandraCommands.UPDATE,

                                                CassandraTables.ESCORT,
                                                CassandraTables.TUPLE_OF_CAR,

                                                tuple.getUuidOfPatrul(),
                                                tuple.getUuid()
                                        )
                                )
                        ) );

                /*
                отсоединяем Эскорт от патрульного
                */
                patrul.getPatrulUniqueValues().unlinkFromEscortCar();
                /*
                отсоединяем патрульного от Эскорт
                */
                patrul.getPatrulTaskInfo().unlinkPatrulFromTask();
            }
            /*
            сохраянем дату когда патрульный принял задачу
            */
            case ACCEPTED -> patrul.getPatrulDateData().update( 1 );
        }

        /*
        обновляем данные о локации, статусе, ID задачи и несоклько других значений
        связанных с задачами
        */
        CassandraDataControl
                .getInstance()
                .updatePatrulAfterTask
                .accept( patrul, stringBuilder );

        return patrul;
    }

    /*
    находим все завершенные задачи патрульного
    принимаем пагинацию как дом опцию
    */
    public Mono< ApiResponseModel > getListOfCompletedTasksOfPatrul (
            final Patrul patrul,
            final Integer page,
            final Integer size
    ) {
        return Flux.fromStream( patrul.getPatrulTaskInfo().getListOfTasks().keySet().stream() )
                .skip( Long.valueOf( page ) * Long.valueOf( size ) ) // выполняем пагинацию
                .take( size )
                .parallel( super.checkDifference( size ) )
                .runOn( Schedulers.parallel() )
                .flatMap( key -> switch ( TaskTypes.valueOf( patrul.getPatrulTaskInfo().getListOfTasks().get( key ) ) ) {
                        case CARD_102 -> CassandraDataControlForTasks
                                .getInstance()
                                .getTask
                                .apply( key )
                                .map( row -> super.deserialize( row.getString( "object" ), Card.class ) )
                                .map( card -> FinishedTask
                                        .builder()
                                        .taskTypes( CARD_102 )
                                        .task( card.getFabula() )
                                        .createdDate( card.getCreated_date().getTime() )
                                        .cardDetails( new CardDetails( card, patrul, "ru" ) )
                                        .reportForCard(
                                                card
                                                    .getTaskCommonParams()
                                                    .getReportForCardList()
                                                    .get(
                                                            this.getReportIndex.apply(
                                                                card.getTaskCommonParams().getReportForCardList(),
                                                                patrul.getUuid()
                                                            )
                                                    )
                                        )
                                        .totalTimeConsumption(
                                                card
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .containsKey( patrul.getPassportNumber() )
                                                        ? card
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .get( patrul.getPassportNumber() )
                                                        .getTotalTimeConsumption()
                                                        : 0
                                        ).build() );

                        case FIND_FACE_CAR -> CassandraDataControlForTasks
                                .getInstance()
                                .getTask
                                .apply( key )
                                .map( row -> super.deserialize( row.getString("object" ), CarEvent.class ) )
                                .map( carEvent -> FinishedTask
                                        .builder()
                                        .taskTypes( FIND_FACE_CAR )
                                        .task( carEvent.getName() )
                                        .createdDate( super.convertTimeToLong( carEvent.getCreated_date() ) )
                                        .cardDetails( CardDetails.from( CarDetails.from( carEvent ) ) )
                                        .reportForCard(
                                                carEvent
                                                        .getTaskCommonParams()
                                                        .getReportForCardList()
                                                        .get(
                                                                this.getReportIndex.apply(
                                                                        carEvent.getTaskCommonParams().getReportForCardList(),
                                                                        patrul.getUuid()
                                                                )
                                                        )
                                        ).totalTimeConsumption(
                                                carEvent
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .containsKey( patrul.getPassportNumber() )
                                                        ? carEvent
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .get( patrul.getPassportNumber() )
                                                        .getTotalTimeConsumption()
                                                        : 0
                                        ).build()
                                );

                        case FIND_FACE_PERSON -> CassandraDataControlForTasks
                                .getInstance()
                                .getTask
                                .apply( key )
                                .map( row -> super.deserialize( row.getString("object" ), FaceEvent.class ) )
                                .map( faceEvent -> FinishedTask
                                        .builder()
                                        .taskTypes( FIND_FACE_PERSON )
                                        .task( faceEvent.getName() )
                                        .cardDetails( CardDetails.from( PersonDetails.from( faceEvent ) ) )
                                        .createdDate( super.convertTimeToLong( faceEvent.getCreated_date() ) )
                                        .reportForCard(
                                                faceEvent
                                                        .getTaskCommonParams()
                                                        .getReportForCardList()
                                                        .get(
                                                                this.getReportIndex.apply(
                                                                        faceEvent.getTaskCommonParams().getReportForCardList(),
                                                                        patrul.getUuid()
                                                                )
                                                        )
                                        ).totalTimeConsumption(
                                                faceEvent
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .containsKey( patrul.getPassportNumber() )
                                                        ? faceEvent
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .get( patrul.getPassportNumber() )
                                                        .getTotalTimeConsumption()
                                                        : 0
                                        ).build() );

                        case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                                .getInstance()
                                .getTask
                                .apply( key )
                                .map( row -> super.deserialize( row.getString("object" ), EventCar.class ) )
                                .map( eventCar -> FinishedTask
                                        .builder()
                                        .task( eventCar.getId() )
                                        .taskTypes( FIND_FACE_EVENT_CAR )
                                        .createdDate( eventCar.getCreated_date().getTime() )
                                        .cardDetails( CardDetails.from( CarDetails.from( eventCar ) ) )
                                        .reportForCard(
                                                eventCar
                                                        .getTaskCommonParams()
                                                        .getReportForCardList()
                                                        .get(
                                                                this.getReportIndex.apply(
                                                                        eventCar.getTaskCommonParams().getReportForCardList(),
                                                                        patrul.getUuid()
                                                                )
                                                        )
                                        ).totalTimeConsumption(
                                                eventCar
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .containsKey( patrul.getPassportNumber() )
                                                        ? eventCar
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .get( patrul.getPassportNumber() )
                                                        .getTotalTimeConsumption()
                                                        : 0
                                        ).build() );

                        case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                                .getInstance()
                                .getTask
                                .apply( key )
                                .map( row -> super.deserialize( row.getString("object" ), EventBody.class ) )
                                .map( eventBody -> FinishedTask
                                        .builder()
                                        .task( eventBody.getId() )
                                        .taskTypes( FIND_FACE_EVENT_BODY )
                                        .createdDate( eventBody.getCreated_date().getTime() )
                                        .cardDetails( CardDetails.from( PersonDetails.from( eventBody ) ) )
                                        .reportForCard(
                                                eventBody
                                                        .getTaskCommonParams()
                                                        .getReportForCardList()
                                                        .get(
                                                                this.getReportIndex.apply(
                                                                        eventBody.getTaskCommonParams().getReportForCardList(),
                                                                        patrul.getUuid()
                                                                )
                                                        )
                                        ).totalTimeConsumption(
                                                eventBody
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .containsKey( patrul.getPassportNumber() )
                                                        ? eventBody
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .get( patrul.getPassportNumber() )
                                                        .getTotalTimeConsumption()
                                                        : 0
                                        ).build() );

                        case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                                .getInstance()
                                .getTask
                                .apply( key )
                                .map( row -> super.deserialize( row.getString( "object" ), EventFace.class ) )
                                .map( eventFace -> FinishedTask
                                        .builder()
                                        .task( eventFace.getId() )
                                        .taskTypes( FIND_FACE_EVENT_FACE )
                                        .createdDate( eventFace.getCreated_date().getTime() )
                                        .cardDetails( CardDetails.from( PersonDetails.from( eventFace ) ) )
                                        .reportForCard(
                                                eventFace
                                                        .getTaskCommonParams()
                                                        .getReportForCardList()
                                                        .get(
                                                                this.getReportIndex.apply(
                                                                        eventFace.getTaskCommonParams().getReportForCardList(),
                                                                        patrul.getUuid()
                                                                )
                                                        )
                                        ).totalTimeConsumption(
                                                eventFace
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .containsKey( patrul.getPassportNumber() )
                                                        ? eventFace
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .get( patrul.getPassportNumber() )
                                                        .getTotalTimeConsumption()
                                                        : 0
                                        ).build() );

                        default -> CassandraDataControlForTasks
                                .getInstance()
                                .getTask
                                .apply( key )
                                .map( row -> super.deserialize( row.getString("object" ), SelfEmploymentTask.class ) )
                                .map( selfEmploymentTask -> FinishedTask
                                        .builder()
                                        .taskTypes( SELF_EMPLOYMENT )
                                        .task( selfEmploymentTask.getDescription() )
                                        .createdDate( selfEmploymentTask.getIncidentDate().getTime() )
                                        .cardDetails( new CardDetails( selfEmploymentTask, "ru", patrul ) )
                                        .totalTimeConsumption(
                                                selfEmploymentTask
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .containsKey( patrul.getPassportNumber() )
                                                        ? selfEmploymentTask
                                                        .getTaskCommonParams()
                                                        .getPatrulStatuses()
                                                        .get( patrul.getPassportNumber() )
                                                        .getTotalTimeConsumption()
                                                        : 0
                                        ).reportForCard(
                                                selfEmploymentTask
                                                        .getTaskCommonParams()
                                                        .getReportForCardList()
                                                        .get(
                                                                this.getReportIndex.apply(
                                                                        selfEmploymentTask.getTaskCommonParams().getReportForCardList(),
                                                                        patrul.getUuid()
                                                                )
                                                        )
                                        ).build() );
                } )
                .sequential()
                .publishOn( Schedulers.single() )
                .collectList()
                .flatMap( finishedTasks -> super.function(
                        Map.of(
                                "message", "Your list of tasks",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data.from( finishedTasks )
                        )
                ) );
    }

    /*
    по ID патрульного находим индекс его рапорта в списке
    если его там нет, то возвращаем 0
     */
    public final BiFunction< List< ReportForCard >, UUID, Integer > getReportIndex = ( reportForCardList, uuid ) -> {
            for ( int i = 0; i < reportForCardList.size(); i++ ) {
                if ( reportForCardList.get( i )
                        .getUuidOfPatrul()
                        .compareTo( uuid ) == 0 ) {
                    return i;
                }
            }

            return 0;
    };

    /*
    сохраняет рапорт от патрульного
    отсоединяет его от задачи
    обновляет БД
    */
    public final BiFunction< Patrul, ReportForCard, Mono< ApiResponseModel > > saveReportForTask = ( patrul, reportForCard ) ->
            CassandraDataControlForTasks
                    .getInstance()
                    .getTask
                    .apply( patrul.getPatrulTaskInfo().getTaskId() )
                    .flatMap( row -> super.function(
                            Map.of(
                                    "message", super.getMessage(
                                            this.changeTaskStatus(
                                                    patrul,
                                                    FINISHED,
                                                    super.deserialize(
                                                            row.getString( "object" ),
                                                            TaskCommonMethods.class
                                                    ).update( reportForCard )
                                            )
                                    )
                            )
                    ) );

    /*
    функция меняет статус патрульного
    */
    public final BiFunction< Patrul, Status, Mono< ApiResponseModel > > changeTaskStatus = ( patrul, status ) ->
            patrul.getPatrulTaskInfo().getTaskTypes().compareTo( ESCORT ) == 0
                    ? CassandraDataControlForEscort
                    .getInstance()
                    .getGetCurrentTupleOfEscort()
                    .apply( patrul.getPatrulTaskInfo().getTaskId() )
                    .flatMap( escortTuple -> super.function(
                            Map.of( "message", this.getMessage(
                                            this.changeTaskStatus( patrul, status, escortTuple ),
                                            status ),
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .updatePatrulStatus
                                            .apply( patrul, status ) ) ) )
                    : CassandraDataControlForTasks
                    .getInstance()
                    .getTask
                    .apply( patrul.getPatrulTaskInfo().getTaskId() )
                    .flatMap( row -> super.function(
                            Map.of(
                                    "message", super.getMessage(
                                            this.changeTaskStatus(
                                                    patrul,
                                                    status,
                                                    super.deserialize( row.getString( "object" ), TaskCommonMethods.class )
                                            ),
                                            status
                                    ), "success", CassandraDataControl
                                            .getInstance()
                                            .updatePatrulStatus
                                            .apply( patrul, status )
                            )
                    ) );

    /*
    по запросу проверяет какая задача дана конкретному патрульному
    после чего возвращает краткое ( ACTIVE_TASK ), полное ( CARD_DETAILS )
    или же по дефолту убирает патрульного из задачи
    */
    public final BiFunction< Patrul, TaskTypes, Mono< ApiResponseModel > > getTaskData = ( patrul, taskTypes ) ->
            switch ( patrul.getPatrulTaskInfo().getTaskTypes() ) {
                case CARD_102 -> CassandraDataControlForTasks
                        .getInstance()
                        .getTask
                        .apply( patrul.getPatrulTaskInfo().getTaskId() )
                        .map( row -> super.deserialize( row.getString( "object" ), TaskCommonMethods.class ) )
                        .flatMap( taskCommonMethods -> super.function( switch ( taskTypes ) {
                            case CARD_DETAILS -> Map.of(
                                    "message", super.taskDetailsMessage,
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data.from(
                                            new CardDetails( (Card) taskCommonMethods, patrul, "ru" ),
                                            CARD_102.name()
                                    )
                            );

                            case ACTIVE_TASK -> Map.of(
                                    "message", super.getMessage( CARD_102 ),
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data.from(
                                            ActiveTask.generate(
                                                    taskCommonMethods,
                                                    patrul.getPatrulTaskInfo().getStatus()
                                            )
                                    )
                            );

                            default -> Map.of(
                                    "message", super.getMessage(
                                            this.changeTaskStatus( patrul, CANCEL, taskCommonMethods ),
                                            taskCommonMethods.getTaskCommonParams()
                                    )
                            );
                        } ) );

                case FIND_FACE_EVENT_BODY, FIND_FACE_EVENT_FACE, FIND_FACE_PERSON -> CassandraDataControlForTasks
                        .getInstance()
                        .getTask
                        .apply( patrul.getPatrulTaskInfo().getTaskId() )
                        .map( row -> super.deserialize( row.getString("object" ), TaskCommonMethods.class ) )
                        .flatMap( taskCommonMethods -> super.function( switch ( taskTypes ) {
                            case CARD_DETAILS -> Map.of(
                                    "message", super.taskDetailsMessage,
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data.from(
                                            CardDetails.from( PersonDetails.from( taskCommonMethods ) ),
                                            taskCommonMethods.getTaskCommonParams().getTaskTypes().name()
                                    )
                            );

                            case ACTIVE_TASK -> Map.of( "message", super.getMessage(
                                            taskCommonMethods.getTaskCommonParams().getTaskTypes()
                                    ),
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data.from(
                                            ActiveTask.generate(
                                                    taskCommonMethods,
                                                    patrul.getPatrulTaskInfo().getStatus()
                                            )
                                    )
                            );

                            default -> Map.of(
                                    "message", super.getMessage(
                                            this.changeTaskStatus( patrul, CANCEL, taskCommonMethods ),
                                            taskCommonMethods.getTaskCommonParams()
                                    )
                            );
                        } ) );

                case FIND_FACE_EVENT_CAR, FIND_FACE_CAR -> CassandraDataControlForTasks
                        .getInstance()
                        .getTask
                        .apply( patrul.getPatrulTaskInfo().getTaskId() )
                        .map( row -> super.deserialize( row.getString("object" ), TaskCommonMethods.class ) )
                        .flatMap( taskCommonMethods -> super.function( switch ( taskTypes ) {
                            case CARD_DETAILS -> Map.of(
                                    "message", super.taskDetailsMessage,
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data.from(
                                            CardDetails.from( CarDetails.from( taskCommonMethods ) ),
                                            taskCommonMethods.getTaskCommonParams().getTaskTypes().name()
                                    )
                            );

                            case ACTIVE_TASK -> Map.of(
                                    "message", super.getMessage( taskCommonMethods.getTaskCommonParams().getTaskTypes() ),
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data.from(
                                            ActiveTask.generate(
                                                    taskCommonMethods,
                                                    patrul.getPatrulTaskInfo().getStatus()
                                            ), taskCommonMethods.getTaskCommonParams().getTaskTypes().name()
                                    )
                            );

                            default -> Map.of( "message", super.getMessage(
                                    this.changeTaskStatus( patrul, CANCEL, taskCommonMethods ),
                                    taskCommonMethods.getTaskCommonParams() ) );
                        } ) );

                case ESCORT -> CassandraDataControlForEscort
                        .getInstance()
                        .getGetCurrentTupleOfEscort()
                        .apply( patrul.getPatrulTaskInfo().getTaskId() )
                        .flatMap( escortTuple -> CassandraDataControlForEscort
                                .getInstance()
                                .getGetCurrentTupleOfCar()
                                .apply( escortTuple.getTupleOfCarsList().get(
                                        escortTuple
                                                .getPatrulList()
                                                .indexOf( patrul.getUuid() ) ) )
                                .flatMap( tupleOfCar -> super.function(
                                        Map.of(
                                                "message", super.taskDetailsMessage,
                                                "data", com.ssd.mvd.gpstabletsservice.entity.Data.from(
                                                        tupleOfCar,
                                                        ESCORT.name()
                                                )
                                        )
                                ) ) );

                case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                        .getInstance()
                        .getTask
                        .apply( patrul.getPatrulTaskInfo().getTaskId() )
                        .map( row -> super.deserialize( row.getString("object" ), SelfEmploymentTask.class ) )
                        .flatMap( selfEmploymentTask -> super.function( switch ( taskTypes ) {
                            case CARD_DETAILS -> Map.of(
                                    "message", super.taskDetailsMessage,
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data.from( patrul , SELF_EMPLOYMENT.name() )
                            );

                            case ACTIVE_TASK -> Map.of(
                                    "message", super.getMessage( SELF_EMPLOYMENT ),
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data.from(
                                            ActiveTask.generate(
                                                    selfEmploymentTask,
                                                    patrul.getPatrulTaskInfo().getStatus()
                                            ),
                                            SELF_EMPLOYMENT.name()
                                    )
                            );

                            default -> Map.of(
                                    "message", super.getMessage(
                                            this.changeTaskStatus( patrul, CANCEL, selfEmploymentTask ),
                                            selfEmploymentTask.getTaskCommonParams()
                                    )
                            );
                        } ) );

                default -> super.function(
                        Map.of(
                                "message", "U have no any Task",
                                "code", 201,
                                "success", false
                        )
                );
    };
}