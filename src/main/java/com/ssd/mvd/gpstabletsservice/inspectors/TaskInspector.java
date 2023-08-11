package com.ssd.mvd.gpstabletsservice.inspectors;

import java.util.Map;
import java.util.List;
import java.util.UUID;
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
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import com.ssd.mvd.gpstabletsservice.entity.responseForAndroid.*;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.subscribers.CustomSubscriber;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.*;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.entity.notifications.Notification;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.TaskTimingStatistics;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests.PatrulActivityRequest;

public final class TaskInspector extends SerDes {
    private final static TaskInspector taskInspector = new TaskInspector();

    public static TaskInspector getInstance () { return taskInspector; }

    // функция заполняет форму для уведомления для андроида и фронта
    private Patrul saveNotification (
            final UUID uuid,
            final String taskId,
            final Patrul patrul,
            final Object object,
            final Status status,
            final TaskTypes taskTypes ) {
            CassandraDataControlForTasks // сохраняем саму задачу в БД
                    .getInstance()
                    .saveTask( uuid, taskId, taskTypes, object );

            CassandraDataControl
                    .getInstance()
                    .getUpdatePatrulAfterTask() // обновляем статус патрульного после каждого изменения
                    .accept( patrul );

            KafkaDataControl // отправляем в Кафку уведомление
                    .getInstance()
                    .getWriteNotificationToKafka()
                    .accept( CassandraDataControl
                            .getInstance()
                            .getSaveNotification()
                            .apply( new Notification(
                                    patrul,
                                    status,
                                    object,
                                    switch ( status ) {
                                        case ACCEPTED -> patrul.getName() + " " + ACCEPTED + " his task: " + patrul.getTaskId() + " " + patrul.getTaskTypes() + " at: ";
                                        case ARRIVED -> patrul.getName() + " " + ARRIVED + " : " + patrul.getTaskTypes() + " task location at: ";
                                        case ATTACHED -> patrul.getName() + " got new task: " + patrul.getTaskId() + " " + patrul.getTaskTypes();
                                        case FINISHED -> patrul.getName() + " completed his task at: ";
                                        default -> patrul.getName() + " has been canceled from task at: "; }, // составляем сообщение для уведомления
                                    taskTypes ) ) );
            return patrul; }

    // после завершения задачи, сохраняем данные об общем расходе времени на выполнение
    private final BiConsumer< Patrul, TaskTypes > updateTotalTimeConsumption = ( patrul, taskTypes ) -> {
            CassandraDataControlForTasks
                    .getInstance()
                    .getUpdateTotalTimeConsumption()
                    .accept( patrul, TimeInspector
                            .getInspector()
                            .getGetTimeDifference()
                            .apply( patrul.getTaskDate().toInstant(), 0 ) );
            // сохраняем ID и тип задачи в список патрульного
            patrul.update( taskTypes ); };

    // обрабатываем данные о передвижении патрульного пока он шел на задание
    private final BiFunction< Patrul, TaskTypes, PatrulStatus > saveTaskTiming = ( patrul, taskTypes ) -> {
            final PatrulStatus patrulStatus = new PatrulStatus( patrul );
            // для начала сохраняем данные о том, сколько патрульный
            // потратил времени и какой маршрут он прошел пока не достиг локации задачи
            CassandraDataControl
                    .getInstance()
                    .getGetHistory()
                    .apply( PatrulActivityRequest
                            .builder()
                            .endDate( TimeInspector
                                    .getInspector()
                                    .getGetNewDate()
                                    .get() )
                            .startDate( patrul.getTaskDate() )
                            .patrulUUID( patrul.getPassportNumber() )
                            .build() )
                    .map( positionInfos -> new TaskTimingStatistics( patrul, taskTypes, patrulStatus, positionInfos ) )
                    .subscribe( new CustomSubscriber( 2 ) );
            return patrulStatus; };

    public Patrul changeTaskStatus ( final Patrul patrul, final Status status, final Card card ) {
        patrul.setStatus( status );
        switch ( patrul.getStatus() ) {
            case CANCEL, FINISHED -> {
                if ( super.checkEquality.test( status, FINISHED ) ) this.updateTotalTimeConsumption.accept( patrul, CARD_102 );
                else card.remove( patrul );
                card.update();
                patrul.free(); }
            case ACCEPTED -> patrul.update( 1 ); // fixing time when patrul started this task
            case ARRIVED -> card.update( patrul, this.saveTaskTiming.apply( patrul, CARD_102 ) );
            case ATTACHED -> patrul.update( CARD_102, card.getLatitude(), card.getLongitude(), card.getUUID().toString() ); }

        if ( status.compareTo( CANCEL ) != 0 ) card.update( patrul ); // обновляем данные патрульного в списке патрульных задачи

        if ( card.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks // обновляем данные о текущей задаче, если она еще не завершена
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask(
                        patrul.getStatus(),
                        card,
                        card.getUUID().toString(),
                        card.getCardId().toString(),
                        card.getStatus(),
                        CARD_102,
                        card.getPatruls() ) );

        return this.saveNotification(
                card.getUUID(),
                card.getCardId().toString(),
                patrul,
                card,
                status,
                CARD_102 ); }

    public Patrul changeTaskStatus ( final Patrul patrul, final Status status, final EventCar eventCar ) {
        patrul.setStatus( status );
        switch ( patrul.getStatus() ) {
            case CANCEL, FINISHED -> {
                if ( super.checkEquality.test( status, FINISHED ) ) this.updateTotalTimeConsumption.accept( patrul, FIND_FACE_EVENT_CAR );
                else eventCar.remove( patrul );
                eventCar.update();
                patrul.free(); }
            case ACCEPTED -> patrul.update( 1 ); // fixing time when patrul started this task
            case ARRIVED -> eventCar.update( patrul, this.saveTaskTiming.apply( patrul, FIND_FACE_EVENT_CAR ) );
            case ATTACHED -> patrul.update( FIND_FACE_EVENT_CAR, eventCar.getLatitude(), eventCar.getLongitude(), eventCar.getUUID().toString() ); }

        if ( eventCar.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks // обновляем данные о текущей задаче, если она еще не завершена
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask(
                        patrul.getStatus(),
                        eventCar,
                        eventCar.getUUID().toString(),
                        eventCar.getId(),
                        eventCar.getStatus(),
                        FIND_FACE_EVENT_CAR,
                        eventCar.getPatruls() ) );

        if ( status.compareTo( CANCEL ) != 0 ) eventCar.update( patrul ); // обновляем данные патрульного в списке патрульных задачи

        return this.saveNotification(
                eventCar.getUUID(),
                eventCar.getId(),
                patrul,
                eventCar,
                status,
                FIND_FACE_EVENT_CAR ); }

    public Patrul changeTaskStatus ( final Patrul patrul, final Status status, final EventFace eventFace ) {
        patrul.setStatus( status );
        switch ( patrul.getStatus() ) {
            case CANCEL, FINISHED -> {
                if ( super.checkEquality.test( status, FINISHED ) ) this.updateTotalTimeConsumption.accept( patrul, FIND_FACE_EVENT_FACE );
                else eventFace.remove( patrul );
                eventFace.update();
                patrul.free(); }
            case ACCEPTED -> patrul.update( 1 ); // fixing time when patrul started this task
            case ARRIVED -> eventFace.update( patrul, this.saveTaskTiming.apply( patrul, FIND_FACE_PERSON ) );
            case ATTACHED -> patrul.update( FIND_FACE_EVENT_FACE, eventFace.getLatitude(), eventFace.getLongitude(), eventFace.getUUID().toString() ); }

        if ( status.compareTo( CANCEL ) != 0 ) eventFace.update( patrul ); // обновляем данные патрульного в списке патрульных задачи

        if ( eventFace.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks // обновляем данные о текущей задаче, если она еще не завершена
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask(
                        patrul.getStatus(),
                        eventFace,
                        eventFace.getUUID().toString(),
                        eventFace.getId(),
                        eventFace.getStatus(),
                        FIND_FACE_EVENT_FACE,
                        eventFace.getPatruls() ) );

        return this.saveNotification(
                eventFace.getUUID(),
                eventFace.getId(),
                patrul,
                eventFace,
                status,
                FIND_FACE_EVENT_FACE ); }

    public Patrul changeTaskStatus ( final Patrul patrul, final Status status, final EventBody eventBody ) {
        patrul.setStatus( status );
        switch ( patrul.getStatus() ) {
            case CANCEL, FINISHED -> {
                if ( super.checkEquality.test( status, FINISHED ) ) this.updateTotalTimeConsumption.accept( patrul, FIND_FACE_EVENT_BODY );
                else eventBody.remove( patrul );
                eventBody.update();
                patrul.free(); }
            case ACCEPTED -> patrul.update( 1 ); // fixing time when patrul started this task
            case ARRIVED -> eventBody.update( patrul, this.saveTaskTiming.apply( patrul, FIND_FACE_PERSON ) );
            case ATTACHED -> patrul.update( FIND_FACE_EVENT_BODY, eventBody.getLatitude(), eventBody.getLongitude(), eventBody.getUUID().toString() ); }

        if ( status.compareTo( CANCEL ) != 0 ) eventBody.update( patrul ); // обновляем данные патрульного в списке патрульных задачи
        if ( eventBody.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks // обновляем данные о текущей задаче, если она еще не завершена
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask(
                        patrul.getStatus(),
                        eventBody,
                        eventBody.getUUID().toString(),
                        eventBody.getId(),
                        eventBody.getStatus(),
                        FIND_FACE_EVENT_BODY,
                        eventBody.getPatruls() ) );

        return this.saveNotification(
                eventBody.getUUID(),
                eventBody.getId(),
                patrul,
                eventBody,
                status,
                FIND_FACE_EVENT_BODY ); }

    public Patrul changeTaskStatus ( final Patrul patrul, final Status status, final CarEvent carEvents ) {
        patrul.setStatus( status );
        switch ( patrul.getStatus() ) {
            case CANCEL, FINISHED -> {
                if ( super.checkEquality.test( status, FINISHED ) ) this.updateTotalTimeConsumption.accept( patrul, FIND_FACE_CAR );
                else carEvents.remove( patrul );
                carEvents.update();
                patrul.free(); }
            case ACCEPTED -> patrul.update( 1 ); // fixing time when patrul started this task
            case ARRIVED -> carEvents.update( patrul, this.saveTaskTiming.apply( patrul, FIND_FACE_CAR ) );
            case ATTACHED -> patrul.update( FIND_FACE_CAR, carEvents.getDataInfo(), carEvents.getUUID().toString() ); }

        if ( status.compareTo( CANCEL ) != 0 ) carEvents.update( patrul ); // обновляем данные патрульного в списке патрульных задачи

        if ( carEvents.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks // обновляем данные о текущей задаче, если она еще не завершена
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask(
                        patrul.getStatus(),
                        carEvents,
                        carEvents.getUUID().toString(),
                        carEvents.getId(),
                        carEvents.getStatus(),
                        FIND_FACE_CAR,
                        carEvents.getPatruls() ) );

        return this.saveNotification(
                carEvents.getUUID(),
                carEvents.getId(),
                patrul,
                carEvents,
                status,
                FIND_FACE_CAR ); }

    public Patrul changeTaskStatus ( final Patrul patrul, final Status status, final FaceEvent faceEvent ) {
        patrul.setStatus( status );
        switch ( patrul.getStatus() ) {
            case CANCEL, FINISHED -> {
                if ( super.checkEquality.test( status, FINISHED ) ) this.updateTotalTimeConsumption.accept( patrul, FIND_FACE_PERSON );
                else faceEvent.remove( patrul );
                faceEvent.update();
                patrul.free(); }
            case ACCEPTED -> patrul.update( 1 ); // fixing time when patrul started this task
            case ARRIVED -> faceEvent.update( patrul, this.saveTaskTiming.apply( patrul, FIND_FACE_PERSON ) );
            case ATTACHED -> patrul.update( FIND_FACE_PERSON, faceEvent.getDataInfo(), faceEvent.getUUID().toString() ); }
        if ( status.compareTo( CANCEL ) != 0 ) faceEvent.update( patrul ); // обновляем данные патрульного в списке патрульных задачи
        if ( faceEvent.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks // обновляем данные о текущей задаче, если она еще не завершена
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask(
                        patrul.getStatus(),
                        faceEvent,
                        faceEvent.getUUID().toString(),
                        faceEvent.getId(),
                        faceEvent.getStatus(),
                        FIND_FACE_PERSON,
                        faceEvent.getPatruls() ) );

        return this.saveNotification(
                faceEvent.getUUID(),
                faceEvent.getId(),
                patrul,
                faceEvent,
                status,
                FIND_FACE_PERSON ); }

    public Patrul changeTaskStatus ( final Patrul patrul, final Status status, final EscortTuple escortTuple ) {
        patrul.setStatus( status );
        switch ( patrul.getStatus() ) {
            case ATTACHED -> {
                patrul.setTaskTypes( TaskTypes.ESCORT );
                patrul.setTaskId( escortTuple.getUuid().toString() ); }
            case CANCEL, FINISHED -> {
                if ( super.checkEquality.test( status, FINISHED ) ) patrul.getListOfTasks().put( patrul.getTaskId(), ESCORT.name() );
                else escortTuple.getPatrulList().remove( patrul.getUuid() );
                CassandraDataControlForEscort
                        .getInstance()
                        .getGetCurrentTupleOfCar()
                        .apply( patrul.getUuidForEscortCar() )
                        .subscribe( new CustomSubscriber( 1 ) );
                patrul.setUuidForEscortCar( null );
                patrul.setUuidOfEscort( null );
                patrul.free(); }
            case ACCEPTED -> patrul.update( 1 ); }

        CassandraDataControl
                .getInstance()
                .getUpdatePatrulAfterTask()
                .accept( patrul );

        return patrul; }

    public Patrul changeTaskStatus ( final Patrul patrul, final Status status, final SelfEmploymentTask selfEmploymentTask ) {
        patrul.setStatus( status );
        switch ( patrul.getStatus() ) {
            case ARRIVED -> {
                if ( super.checkEquality.test( selfEmploymentTask.getTaskStatus(), ARRIVED ) ) patrul.update( 1 );
                patrul.update( SELF_EMPLOYMENT, patrul.getLatitude(), patrul.getLongitude(), selfEmploymentTask.getUuid().toString() );
                selfEmploymentTask.update( patrul, this.saveTaskTiming.apply( patrul, SELF_EMPLOYMENT ) ); }
            case CANCEL, FINISHED -> {
                if ( super.checkEquality.test( status, FINISHED ) ) this.updateTotalTimeConsumption.accept( patrul, SELF_EMPLOYMENT );
                else selfEmploymentTask.remove( patrul );
                selfEmploymentTask.update();
                patrul.free(); }
            case ATTACHED, ACCEPTED -> {
                patrul.update( 1 );
                patrul.update( SELF_EMPLOYMENT,
                        selfEmploymentTask.getLatOfAccident(),
                        selfEmploymentTask.getLanOfAccident(),
                        selfEmploymentTask.getUuid().toString() ); } }

        if ( status.compareTo( CANCEL ) != 0 ) selfEmploymentTask.update( patrul ); // обновляем данные патрульного в списке патрульных задачи

        if ( selfEmploymentTask.getTaskStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask(
                        patrul.getStatus(),
                        selfEmploymentTask,
                        selfEmploymentTask.getUuid().toString(),
                        selfEmploymentTask.getUuid().toString(),
                        selfEmploymentTask.getTaskStatus(),
                        SELF_EMPLOYMENT,
                        selfEmploymentTask.getPatruls() ) );

        return this.saveNotification(
                selfEmploymentTask.getUuid(),
                selfEmploymentTask.getAddress(),
                patrul,
                selfEmploymentTask,
                status,
                SELF_EMPLOYMENT ); }

    public Mono< ApiResponseModel > getListOfPatrulTasks ( final Patrul patrul, final Integer page, final Integer size ) {
        return Flux.fromStream( patrul.getListOfTasks().keySet().stream() )
                .skip( Long.valueOf( page ) * Long.valueOf( size ) )
                .take( size )
                .parallel( super.checkDifference.apply( size ) )
                .runOn( Schedulers.parallel() )
                .flatMap( key -> switch ( TaskTypes.valueOf( patrul.getListOfTasks().get( key ) ) ) {
                        case CARD_102 -> CassandraDataControlForTasks
                                .getInstance()
                                .getGetTask()
                                .apply( key )
                                .map( row -> super.deserialize( row.getString( "object" ), Card.class ) )
                                .map( card -> FinishedTask
                                        .builder()
                                        .taskTypes( CARD_102 )
                                        .task( card.getFabula() )
                                        .createdDate( card.getCreated_date().getTime() )
                                        .cardDetails( new CardDetails( card, patrul, "ru", DataValidateInspector.getInstance() ) )
                                        .reportForCard( card
                                                .getReportForCardList()
                                                .get( this.getReportIndex.apply( card.getReportForCardList(), patrul.getUuid() ) ) )
                                        .totalTimeConsumption( card
                                                .getPatrulStatuses()
                                                .containsKey( patrul.getPassportNumber() )
                                                ? card
                                                .getPatrulStatuses()
                                                .get( patrul.getPassportNumber() )
                                                .getTotalTimeConsumption() : 0 )
                                        .build() );

                        case FIND_FACE_CAR -> CassandraDataControlForTasks
                                .getInstance()
                                .getGetTask()
                                .apply( key )
                                .map( row -> super.deserialize( row.getString("object" ), CarEvent.class ) )
                                .map( carEvent -> FinishedTask
                                        .builder()
                                        .taskTypes( FIND_FACE_CAR )
                                        .task( carEvent.getName() )
                                        .createdDate( TimeInspector
                                                .getInspector()
                                                .getConvertTimeToLong()
                                                .apply( carEvent.getCreated_date() ) )
                                        .cardDetails( new CardDetails( new CarDetails( carEvent, DataValidateInspector.getInstance() ) ) )
                                        .reportForCard( carEvent
                                                .getReportForCardList()
                                                .get( this.getReportIndex.apply( carEvent.getReportForCardList(), patrul.getUuid() ) ) )
                                        .totalTimeConsumption( carEvent
                                                .getPatrulStatuses()
                                                .containsKey( patrul.getPassportNumber() )
                                                ? carEvent
                                                .getPatrulStatuses()
                                                .get( patrul.getPassportNumber() )
                                                .getTotalTimeConsumption() : 0 )
                                        .build() );

                        case FIND_FACE_PERSON -> CassandraDataControlForTasks
                                .getInstance()
                                .getGetTask()
                                .apply( key )
                                .map( row -> super.deserialize( row.getString("object" ), FaceEvent.class ) )
                                .map( faceEvent -> FinishedTask
                                        .builder()
                                        .taskTypes( FIND_FACE_PERSON )
                                        .task( faceEvent.getName() )
                                        .cardDetails( new CardDetails( new PersonDetails( faceEvent, DataValidateInspector.getInstance() ) ) )
                                        .createdDate( TimeInspector
                                                .getInspector()
                                                .getConvertTimeToLong()
                                                .apply( faceEvent.getCreated_date() ) )
                                        .reportForCard( faceEvent
                                                .getReportForCardList()
                                                .get( this.getReportIndex.apply( faceEvent.getReportForCardList(), patrul.getUuid() ) ) )
                                        .totalTimeConsumption( faceEvent
                                                .getPatrulStatuses()
                                                .containsKey( patrul.getPassportNumber() )
                                                ? faceEvent
                                                .getPatrulStatuses()
                                                .get( patrul.getPassportNumber() )
                                                .getTotalTimeConsumption() : 0 )
                                        .build() );

                        case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                                .getInstance()
                                .getGetTask()
                                .apply( key )
                                .map( row -> super.deserialize( row.getString("object" ), EventCar.class ) )
                                .map( eventCar -> FinishedTask
                                        .builder()
                                        .task( eventCar.getId() )
                                        .taskTypes( FIND_FACE_EVENT_CAR )
                                        .createdDate( eventCar.getCreated_date().getTime() )
                                        .cardDetails( new CardDetails( new CarDetails( eventCar, DataValidateInspector.getInstance() ) ) )
                                        .reportForCard( eventCar
                                                .getReportForCardList()
                                                .get( this.getReportIndex.apply( eventCar.getReportForCardList(), patrul.getUuid() ) ) )
                                        .totalTimeConsumption( eventCar
                                                .getPatrulStatuses()
                                                .containsKey( patrul.getPassportNumber() )
                                                ? eventCar
                                                .getPatrulStatuses()
                                                .get( patrul.getPassportNumber() )
                                                .getTotalTimeConsumption() : 0 )
                                        .build() );

                        case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                                .getInstance()
                                .getGetTask()
                                .apply( key )
                                .map( row -> super.deserialize( row.getString("object" ), EventBody.class ) )
                                .map( eventBody -> FinishedTask
                                        .builder()
                                        .task( eventBody.getId() )
                                        .taskTypes( FIND_FACE_EVENT_BODY )
                                        .createdDate( eventBody.getCreated_date().getTime() )
                                        .cardDetails( new CardDetails( new PersonDetails( eventBody, DataValidateInspector.getInstance() ) ) )
                                        .reportForCard( eventBody
                                                .getReportForCardList()
                                                .get( this.getReportIndex.apply( eventBody.getReportForCardList(), patrul.getUuid() ) ) )
                                        .totalTimeConsumption( eventBody
                                                .getPatrulStatuses()
                                                .containsKey( patrul.getPassportNumber() )
                                                ? eventBody
                                                .getPatrulStatuses()
                                                .get( patrul.getPassportNumber() )
                                                .getTotalTimeConsumption() : 0 )
                                        .build() );

                        case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                                .getInstance()
                                .getGetTask()
                                .apply( key )
                                .map( row -> super.deserialize( row.getString( "object" ), EventFace.class ) )
                                .map( eventFace -> FinishedTask
                                        .builder()
                                        .task( eventFace.getId() )
                                        .taskTypes( FIND_FACE_EVENT_FACE )
                                        .createdDate( eventFace.getCreated_date().getTime() )
                                        .cardDetails( new CardDetails( new PersonDetails( eventFace, DataValidateInspector.getInstance() ) ) )
                                        .reportForCard( eventFace
                                                .getReportForCardList()
                                                .get( this.getReportIndex.apply( eventFace.getReportForCardList(), patrul.getUuid() ) ) )
                                        .totalTimeConsumption( eventFace
                                                .getPatrulStatuses()
                                                .containsKey( patrul.getPassportNumber() )
                                                ? eventFace
                                                .getPatrulStatuses()
                                                .get( patrul.getPassportNumber() )
                                                .getTotalTimeConsumption() : 0 )
                                        .build() );

                        default -> CassandraDataControlForTasks
                                .getInstance()
                                .getGetTask()
                                .apply( key )
                                .map( row -> super.deserialize( row.getString("object" ), SelfEmploymentTask.class ) )
                                .map( selfEmploymentTask -> FinishedTask
                                        .builder()
                                        .taskTypes( SELF_EMPLOYMENT )
                                        .task( selfEmploymentTask.getDescription() )
                                        .createdDate( selfEmploymentTask.getIncidentDate().getTime() )
                                        .cardDetails( new CardDetails( selfEmploymentTask, "ru", patrul ) )
                                        .totalTimeConsumption( selfEmploymentTask
                                                .getPatrulStatuses()
                                                .containsKey( patrul.getPassportNumber() )
                                                ? selfEmploymentTask
                                                .getPatrulStatuses()
                                                .get( patrul.getPassportNumber() )
                                                .getTotalTimeConsumption() : 0 )
                                        .reportForCard( selfEmploymentTask
                                                .getReportForCards()
                                                .get( this.getReportIndex.apply( selfEmploymentTask.getReportForCards(), patrul.getUuid() ) ) )
                                        .build() ); } )
                .sequential()
                .publishOn( Schedulers.single() )
                .collectList()
                .flatMap( finishedTasks -> super.getFunction().apply(
                        Map.of( "message", "Your list of tasks",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( finishedTasks )
                                        .build() ) ) ); }

    private final BiFunction< List< ReportForCard >, UUID, Integer > getReportIndex = ( reportForCardList, uuid ) -> {
            for ( int i = 0; i < reportForCardList.size(); i++ ) if ( reportForCardList.get( i )
                    .getUuidOfPatrul()
                    .compareTo( uuid ) == 0 ) return i;
            return 0; };

    // сохраняет рапорт от патрульного
    public final BiFunction< Patrul, ReportForCard, Mono< ApiResponseModel > > saveReportForTask = ( patrul, reportForCard ) -> CassandraDataControlForTasks
            .getInstance()
            .getGetTask()
            .apply( patrul.getTaskId() )
            .flatMap( row -> switch ( patrul.getTaskTypes() ) {
                case CARD_102 -> super.getFunction().apply(
                        Map.of( "message", "Report from: "
                                + this.changeTaskStatus( patrul, FINISHED, super.deserialize( row.getString( "object" ), Card.class ).update( reportForCard ) ).getName()
                                + " was saved" ) );

                case SELF_EMPLOYMENT -> super.getFunction().apply(
                        Map.of( "message", "Report from: "
                                + this.changeTaskStatus( patrul, FINISHED, super.deserialize( row.getString( "object" ), SelfEmploymentTask.class ).update( reportForCard ) ).getName()
                                + " was saved" ) );

                case FIND_FACE_CAR -> super.getFunction().apply(
                        Map.of( "message", "Report from: "
                                + this.changeTaskStatus( patrul, FINISHED, super.deserialize( row.getString( "object" ), CarEvent.class ).update( reportForCard ) ).getName()
                                + " was saved" ) );

                case FIND_FACE_PERSON -> super.getFunction().apply(
                        Map.of( "message", "Report from: "
                                + this.changeTaskStatus( patrul, FINISHED, super.deserialize( row.getString( "object" ), FaceEvent.class ).update( reportForCard ) ).getName()
                                + " was saved" ) );

                case FIND_FACE_EVENT_CAR -> super.getFunction().apply(
                        Map.of( "message", "Report from: "
                                + this.changeTaskStatus( patrul, FINISHED, super.deserialize( row.getString( "object" ), EventCar.class ).update( reportForCard ) ).getName()
                                + " was saved" ) );

                case FIND_FACE_EVENT_FACE -> super.getFunction().apply(
                        Map.of( "message", "Report from: "
                                + this.changeTaskStatus( patrul, FINISHED, super.deserialize( row.getString( "object" ), EventFace.class ).update( reportForCard ) ).getName()
                                + " was saved" ) );

                case FIND_FACE_EVENT_BODY -> super.getFunction().apply(
                        Map.of( "message", "Report from: "
                                + this.changeTaskStatus( patrul, FINISHED, super.deserialize( row.getString( "object" ), EventBody.class ).update( reportForCard ) ).getName()
                                + " was saved" ) );

                default -> super.getError().apply( 3 ); } );

    public final BiFunction< Patrul, Status, Mono< ApiResponseModel > > changeTaskStatus = ( patrul, status ) ->
            patrul.getTaskTypes().compareTo( ESCORT ) == 0
                    ? CassandraDataControlForEscort
                    .getInstance()
                    .getGetCurrentTupleOfEscort()
                    .apply( patrul.getTaskId() )
                    .flatMap( escortTuple -> super.getFunction().apply(
                            Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, escortTuple ).getPassportNumber()
                                            + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) )
                    : CassandraDataControlForTasks
                    .getInstance()
                    .getGetTask()
                    .apply( patrul.getTaskId() )
                    .flatMap( row -> switch ( patrul.getTaskTypes() ) {
                        case CARD_102 -> super.getFunction().apply(
                                Map.of( "message", "Patrul: "
                                                + this.changeTaskStatus( patrul, status, super.deserialize( row.getString( "object" ), Card.class ) ).getPassportNumber()
                                                + " changed his status task to: " + status,
                                        "success", CassandraDataControl
                                                .getInstance()
                                                .getUpdatePatrulStatus()
                                                .apply( patrul, status ) ) );

                        case SELF_EMPLOYMENT -> super.getFunction().apply(
                                Map.of( "message", "Patrul: "
                                                + this.changeTaskStatus( patrul, status, super.deserialize( row.getString( "object" ), SelfEmploymentTask.class ) ).getPassportNumber()
                                                + " changed his status task to: " + status,
                                        "success", CassandraDataControl
                                                .getInstance()
                                                .getUpdatePatrulStatus()
                                                .apply( patrul, status ) ) );

                        case FIND_FACE_CAR -> super.getFunction().apply(
                                Map.of( "message", "Patrul: "
                                                + this.changeTaskStatus( patrul, status, super.deserialize( row.getString( "object" ), CarEvent.class ) ).getPassportNumber()
                                                + " changed his status task to: " + status,
                                        "success", CassandraDataControl
                                                .getInstance()
                                                .getUpdatePatrulStatus()
                                                .apply( patrul, status ) ) );

                        case FIND_FACE_PERSON -> super.getFunction().apply(
                                Map.of( "message", "Patrul: "
                                                + this.changeTaskStatus( patrul, status, super.deserialize( row.getString( "object" ), FaceEvent.class ) ).getPassportNumber()
                                                + " changed his status task to: " + status,
                                        "success", CassandraDataControl
                                                .getInstance()
                                                .getUpdatePatrulStatus()
                                                .apply( patrul, status ) ) );

                        case FIND_FACE_EVENT_CAR -> super.getFunction().apply(
                                Map.of( "message", "Patrul: "
                                                + this.changeTaskStatus( patrul, status, super.deserialize( row.getString( "object" ), EventCar.class ) ).getPassportNumber()
                                                + " changed his status task to: " + status,
                                        "success", CassandraDataControl
                                                .getInstance()
                                                .getUpdatePatrulStatus()
                                                .apply( patrul, status ) ) );

                        case FIND_FACE_EVENT_BODY -> super.getFunction().apply(
                                Map.of( "message", "Patrul: "
                                                + this.changeTaskStatus( patrul, status, super.deserialize( row.getString( "object" ), EventBody.class ) ).getPassportNumber()
                                                + " changed his status task to: " + status,
                                        "success", CassandraDataControl
                                                .getInstance()
                                                .getUpdatePatrulStatus()
                                                .apply( patrul, status ) ) );

                        default -> super.getFunction().apply(
                                Map.of( "message", "Patrul: "
                                                + this.changeTaskStatus( patrul, status, super.deserialize( row.getString( "object" ), EventFace.class ) ).getPassportNumber()
                                                + " changed his status task to: " + status,
                                        "success", CassandraDataControl
                                                .getInstance()
                                                .getUpdatePatrulStatus()
                                                .apply( patrul, status ) ) ); } );

    // по запросу проверяет какая задача дана конкретному патрульному
    // после чего возвращает краткое ( ACTIVE_TASK ), полное ( CARD_DETAILS ) или же по дефолту убирает патрульного из задачи
    public final BiFunction< Patrul, TaskTypes, Mono< ApiResponseModel > > getTaskData = ( patrul, taskTypes ) -> switch ( patrul.getTaskTypes() ) {
        case CARD_102 -> CassandraDataControlForTasks
                .getInstance()
                .getGetTask()
                .apply( patrul.getTaskId() )
                .map( row -> super.deserialize( row.getString( "object" ), Card.class ) )
                .flatMap( card -> super.getFunction().apply( switch ( taskTypes ) {
                    case CARD_DETAILS -> Map.of( "message", "Your task details",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new CardDetails( card, patrul, "ru", DataValidateInspector.getInstance() ) )
                                    .type( CARD_102.name() )
                                    .build() );

                    case ACTIVE_TASK -> Map.of( "message", "U have " + CARD_102 + " Task",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new ActiveTask(
                                            card,
                                            CARD_102,
                                            patrul.getStatus(),
                                            card.getStatus(),
                                            card.getUUID().toString() ) )
                                    .type( CARD_102.name() )
                                    .build() );

                    default -> Map.of( "message", this.changeTaskStatus( patrul, CANCEL, card ).getName()
                            + " was removed from " + card.getCardId() ); } ) );

        case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                .getInstance()
                .getGetTask()
                .apply( patrul.getTaskId() )
                .map( row -> super.deserialize( row.getString("object" ), EventBody.class ) )
                .flatMap( eventBody -> super.getFunction().apply( switch ( taskTypes ) {
                    case CARD_DETAILS -> Map.of( "message", "Your task details ",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new CardDetails( new PersonDetails( eventBody, DataValidateInspector.getInstance() ) ) )
                                    .type( FIND_FACE_PERSON.name() )
                                    .build() );

                    case ACTIVE_TASK -> Map.of( "message", "U have " + FIND_FACE_EVENT_BODY + " Task",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new ActiveTask(
                                            eventBody,
                                            FIND_FACE_EVENT_BODY,
                                            patrul.getStatus(),
                                            eventBody.getStatus(),
                                            eventBody.getUUID().toString() ) )
                                    .type( FIND_FACE_EVENT_BODY.name() )
                                    .build() );

                    default -> Map.of( "message", this.changeTaskStatus( patrul, CANCEL, eventBody ).getName()
                            + " was removed from " + eventBody.getId() ); } ) );

        case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                .getInstance()
                .getGetTask()
                .apply( patrul.getTaskId() )
                .map( row -> super.deserialize( row.getString( "object" ), EventFace.class) )
                .flatMap( eventFace -> super.getFunction().apply( switch ( taskTypes ) {
                    case CARD_DETAILS -> Map.of( "message", "Your task details",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new CardDetails( new PersonDetails( eventFace, DataValidateInspector.getInstance() ) ) )
                                    .type( FIND_FACE_PERSON.name() )
                                    .build() );

                    case ACTIVE_TASK -> Map.of( "message", "U have " + FIND_FACE_EVENT_FACE + " Task",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new ActiveTask(
                                            eventFace,
                                            FIND_FACE_EVENT_FACE,
                                            patrul.getStatus(),
                                            eventFace.getStatus(),
                                            eventFace.getUUID().toString() ) )
                                    .type( FIND_FACE_EVENT_FACE.name() )
                                    .build() );

                    default -> Map.of( "message", this.changeTaskStatus( patrul, CANCEL, eventFace ).getName()
                            + " was removed from " + eventFace.getId() ); } ) );

        case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                .getInstance()
                .getGetTask()
                .apply( patrul.getTaskId() )
                .map( row -> super.deserialize( row.getString("object" ), EventCar.class ) )
                .flatMap( eventCar -> super.getFunction().apply( switch ( taskTypes ) {
                    case CARD_DETAILS -> Map.of( "message", "Your task details",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new CardDetails( new CarDetails( eventCar, DataValidateInspector.getInstance() ) ) )
                                    .type( FIND_FACE_CAR.name() )
                                    .build() );

                    case ACTIVE_TASK -> Map.of( "message", "U have " + FIND_FACE_EVENT_CAR + " Task",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new ActiveTask(
                                            eventCar,
                                            FIND_FACE_EVENT_CAR,
                                            patrul.getStatus(),
                                            eventCar.getStatus(),
                                            eventCar.getUUID().toString() ) )
                                    .type( FIND_FACE_EVENT_CAR.name() )
                                    .build() );

                    default -> Map.of( "message", this.changeTaskStatus( patrul, CANCEL, eventCar ).getName()
                            + " was removed from " + eventCar.getId() ); } ) );

        case FIND_FACE_CAR -> CassandraDataControlForTasks
                .getInstance()
                .getGetTask()
                .apply( patrul.getTaskId() )
                .map( row -> super.deserialize( row.getString("object" ), CarEvent.class ) )
                .flatMap( carEvent -> super.getFunction().apply( switch ( taskTypes ) {
                    case CARD_DETAILS -> Map.of( "message", "Your task details",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new CardDetails( new CarDetails( carEvent, DataValidateInspector.getInstance() ) ) )
                                    .type( FIND_FACE_CAR.name() )
                                    .build() );

                    case ACTIVE_TASK -> Map.of( "message", "U have " + FIND_FACE_CAR + " Task",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new ActiveTask(
                                            carEvent,
                                            FIND_FACE_CAR,
                                            patrul.getStatus(),
                                            carEvent.getStatus(),
                                            carEvent.getUUID().toString() ) )
                                    .type( FIND_FACE_CAR.name() )
                                    .build() );

                    default -> Map.of( "message", this.changeTaskStatus( patrul, CANCEL, carEvent ).getName()
                            + " was removed from " + carEvent.getId() ); } ) );

        case FIND_FACE_PERSON -> CassandraDataControlForTasks
                .getInstance()
                .getGetTask()
                .apply( patrul.getTaskId() )
                .map( row -> super.deserialize( row.getString("object" ), FaceEvent.class ) )
                .flatMap( faceEvent -> super.getFunction().apply( switch ( taskTypes ) {
                    case CARD_DETAILS -> Map.of( "message", "Your task details",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new CardDetails( new PersonDetails( faceEvent, DataValidateInspector.getInstance() ) ) )
                                    .type( FIND_FACE_PERSON.name() )
                                    .build() );

                    case ACTIVE_TASK -> Map.of( "message", "U have " + FIND_FACE_PERSON + " Task",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new ActiveTask(
                                            faceEvent,
                                            FIND_FACE_PERSON,
                                            patrul.getStatus(),
                                            faceEvent.getStatus(),
                                            faceEvent.getUUID().toString() ) )
                                    .type( FIND_FACE_PERSON.name() )
                                    .build() );

                    default -> Map.of( "message", this.changeTaskStatus( patrul, CANCEL, faceEvent ).getName()
                            + " was removed from " + faceEvent.getId() ); } ) );

        case ESCORT -> CassandraDataControlForEscort
                .getInstance()
                .getGetCurrentTupleOfEscort()
                .apply( patrul.getTaskId() )
                .flatMap( escortTuple -> CassandraDataControlForEscort
                        .getInstance()
                        .getGetCurrentTupleOfCar()
                        .apply( escortTuple.getTupleOfCarsList().get(
                                escortTuple
                                        .getPatrulList()
                                        .indexOf( patrul.getUuid() ) ) )
                        .flatMap( tupleOfCar -> super.getFunction().apply(
                                Map.of( "message", "Your task details",
                                        "data", com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                                .data( new CardDetails( escortTuple, "ru", tupleOfCar ) )
                                                .type( ESCORT.name() )
                                                .build() ) ) ) );

        case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                .getInstance()
                .getGetTask()
                .apply( patrul.getTaskId() )
                .map( row -> super.deserialize( row.getString("object" ), SelfEmploymentTask.class ) )
                .flatMap( selfEmploymentTask -> super.getFunction().apply( switch ( taskTypes ) {
                    case CARD_DETAILS -> Map.of( "message", "Your task details",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                    .data( new CardDetails( selfEmploymentTask, "ru", patrul ) )
                                    .type( ESCORT.name() )
                                    .build() );

                    case ACTIVE_TASK -> Map.of( "message", "U have " + SELF_EMPLOYMENT + " Task",
                            "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                    .builder()
                                    .data( new ActiveTask(
                                            selfEmploymentTask,
                                            SELF_EMPLOYMENT,
                                            patrul.getStatus(),
                                            selfEmploymentTask.getTaskStatus(),
                                            selfEmploymentTask.getUuid().toString() ) )
                                    .type( SELF_EMPLOYMENT.name() )
                                    .build() );

                    default -> Map.of( "message", this.changeTaskStatus( patrul, CANCEL, selfEmploymentTask ).getName()
                            + " was removed from " + selfEmploymentTask.getUuid() ); } ) );

        default -> super.getFunction().apply(
                Map.of( "message", "U have no any Task",
                        "code", 201,
                        "success", false ) ); };
}