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
import static com.ssd.mvd.gpstabletsservice.constants.Status.FREE;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.*;
import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import com.ssd.mvd.gpstabletsservice.entity.notifications.Notification;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PatrulStatus;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForEscort;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FIND_FACE_CAR;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.SELF_EMPLOYMENT;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.TaskTimingStatistics;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.patrulRequests.PatrulActivityRequest;

@lombok.Data
public final class TaskInspector extends SerDes {
    private static TaskInspector taskInspector = new TaskInspector();

    public static TaskInspector getInstance () { return taskInspector != null ? taskInspector : new TaskInspector(); }

    private Patrul saveNotification (
            final UUID uuid,
            final String taskId,
            final Patrul patrul,
            final Object object,
            final Status status,
            final TaskTypes taskTypes ) {
            CassandraDataControlForTasks
                    .getInstance()
                    .saveTask( uuid,
                            taskId,
                            taskTypes,
                            object );

            CassandraDataControl
                    .getInstance()
                    .getUpdatePatrulAfterTask()
                    .accept( patrul );

            KafkaDataControl
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
                                        case ACCEPTED -> patrul.getName() + " ACCEPTED his task: " + patrul.getTaskId() + " " + patrul.getTaskTypes() + " at: "
                                                + TimeInspector
                                                .getInspector()
                                                .getGetNewDate()
                                                .get();
                                        case ARRIVED -> patrul.getName() + " : " + patrul.getTaskTypes() + " arrived task location: " + " at: "
                                                + TimeInspector
                                                .getInspector()
                                                .getGetNewDate()
                                                .get();
                                        case ATTACHED -> patrul.getName() + " got new task: " + patrul.getTaskId() + " " + patrul.getTaskTypes();
                                        case FINISHED -> patrul.getName() + " completed his task at: "
                                                + TimeInspector
                                                .getInspector()
                                                .getGetNewDate()
                                                .get();
                                        default -> patrul.getName() + " has been canceled from task at: "
                                                + TimeInspector
                                                .getInspector()
                                                .getGetNewDate()
                                                .get(); },
                                    taskTypes ) ) );
            return patrul; }

    private final BiConsumer< Patrul, TaskTypes > updateTotalTimeConsumption = ( patrul, taskTypes ) -> {
            CassandraDataControlForTasks
                    .getInstance()
                    .getUpdateTotalTimeConsumption()
                    .apply( patrul, TimeInspector
                            .getInspector()
                            .getGetTimeDifferenceInSeconds()
                            .apply( patrul.getTaskDate().toInstant() ) );
            patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), taskTypes.name() ); };

    private final BiFunction< Patrul, TaskTypes, PatrulStatus > saveTaskTiming = ( patrul, taskTypes ) -> {
            final PatrulStatus patrulStatus = new PatrulStatus( patrul );
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
                if ( super.getCheckEquality().test( status, FINISHED ) ) this.getUpdateTotalTimeConsumption().accept( patrul, CARD_102 );
                else card.getPatruls().remove( CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteRowFromTaskTimingTable()
                            .apply( patrul ) );
                if ( card.getPatruls().size() == card.getReportForCardList().size() ) {
                    card.setStatus( FINISHED );
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( card.getUUID().toString() );
                    if ( card.getPatruls().size() != 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask(
                                    card,
                                    card.getUUID().toString(),
                                    card.getStatus(),
                                    CARD_102,
                                    card.getPatruls() ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( CARD_102 );
                patrul.setLatitudeOfTask( card.getLatitude() );
                patrul.setTaskId( card.getUUID().toString() ); // saving card id into patrul object
                patrul.setLongitudeOfTask( card.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( TimeInspector
                    .getInspector()
                    .getGetNewDate()
                    .get() ); // fixing time when patrul started this task
            case ARRIVED -> card.getPatrulStatuses().put(
                    patrul.getPassportNumber(),
                    this.getSaveTaskTiming().apply( patrul, CARD_102 ) ); }

        if ( status.compareTo( CANCEL ) != 0 ) card.getPatruls().put( patrul.getUuid(), patrul );

        if ( card.getStatus().compareTo( FINISHED ) != 0 )
            CassandraDataControlForTasks
                    .getInstance()
                    .getSaveActiveTask()
                    .accept( new ActiveTask(
                            card,
                            card.getUUID().toString(),
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
                if ( super.getCheckEquality().test( status, FINISHED ) ) this.getUpdateTotalTimeConsumption().accept( patrul, FIND_FACE_EVENT_CAR );
                else eventCar.getPatruls().remove(
                        CassandraDataControlForTasks
                                .getInstance()
                                .getDeleteRowFromTaskTimingTable()
                                .apply( patrul ) );
                if ( eventCar.getPatruls().size() == eventCar.getReportForCardList().size() ) {
                    eventCar.setStatus( FINISHED );
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( eventCar.getUUID().toString() );
                    if ( eventCar.getPatruls().size() > 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask(
                                    eventCar,
                                    eventCar.getUUID().toString(),
                                    eventCar.getStatus(),
                                    FIND_FACE_EVENT_CAR,
                                    eventCar.getPatruls() ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( FIND_FACE_EVENT_CAR );
                patrul.setTaskId( eventCar.getUUID().toString() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( eventCar.getLatitude() );
                patrul.setLongitudeOfTask( eventCar.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( TimeInspector
                    .getInspector()
                    .getGetNewDate()
                    .get() ); // fixing time when patrul started this task
            case ARRIVED -> eventCar.getPatrulStatuses().put(
                    patrul.getPassportNumber(),
                    this.getSaveTaskTiming().apply( patrul, FIND_FACE_EVENT_CAR ) ); }

        if ( eventCar.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask(
                        eventCar,
                        eventCar.getUUID().toString(),
                        eventCar.getStatus(),
                        FIND_FACE_EVENT_CAR,
                        eventCar.getPatruls() ) );

        if ( status.compareTo( CANCEL ) != 0 ) eventCar.getPatruls().put( patrul.getUuid(), patrul );

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
                if ( super.getCheckEquality().test( status, FINISHED ) ) this.getUpdateTotalTimeConsumption().accept( patrul, FIND_FACE_EVENT_FACE );
                else eventFace.getPatruls().remove(
                        CassandraDataControlForTasks
                                .getInstance()
                                .getDeleteRowFromTaskTimingTable()
                                .apply( patrul ) );
                if ( eventFace.getPatruls().size() == eventFace.getReportForCardList().size() ) {
                    eventFace.setStatus( FINISHED );
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( eventFace.getUUID().toString() );
                    if ( eventFace.getPatruls().size() > 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask(
                                    eventFace,
                                    eventFace.getUUID().toString(),
                                    eventFace.getStatus(),
                                    FIND_FACE_EVENT_FACE,
                                    eventFace.getPatruls() ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( FIND_FACE_EVENT_FACE );
                patrul.setTaskId( eventFace.getUUID().toString() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( eventFace.getLatitude() );
                patrul.setLongitudeOfTask( eventFace.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( TimeInspector
                    .getInspector()
                    .getGetNewDate()
                    .get() ); // fixing time when patrul started this task
            case ARRIVED -> eventFace.getPatrulStatuses().putIfAbsent(
                    patrul.getPassportNumber(),
                    this.getSaveTaskTiming().apply( patrul, FIND_FACE_PERSON ) ); }

        if ( status.compareTo( CANCEL ) != 0 ) eventFace.getPatruls().put( patrul.getUuid(), patrul );

        if ( eventFace.getStatus().compareTo( FINISHED ) != 0 )
            CassandraDataControlForTasks
                    .getInstance()
                    .getSaveActiveTask()
                    .accept( new ActiveTask(
                            eventFace,
                            eventFace.getUUID().toString(),
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
                if ( super.getCheckEquality().test( status, FINISHED ) ) this.getUpdateTotalTimeConsumption().accept( patrul, FIND_FACE_EVENT_BODY );
                else eventBody.getPatruls().remove(
                        CassandraDataControlForTasks
                                .getInstance()
                                .getDeleteRowFromTaskTimingTable()
                                .apply( patrul ) );
                if ( eventBody.getPatruls().size() == eventBody.getReportForCardList().size() ) {
                    eventBody.setStatus( FINISHED );
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( eventBody.getUUID().toString() );
                    if ( eventBody.getPatruls().size() > 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask(
                                    eventBody,
                                    eventBody.getUUID().toString(),
                                    eventBody.getStatus(),
                                    FIND_FACE_EVENT_BODY,
                                    eventBody.getPatruls() ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( FIND_FACE_EVENT_BODY );
                patrul.setTaskId( eventBody.getUUID().toString() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( eventBody.getLatitude() );
                patrul.setLongitudeOfTask( eventBody.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( TimeInspector
                    .getInspector()
                    .getGetNewDate()
                    .get() ); // fixing time when patrul started this task
            case ARRIVED -> eventBody.getPatrulStatuses().putIfAbsent(
                    patrul.getPassportNumber(), this.getSaveTaskTiming().apply( patrul, FIND_FACE_PERSON ) ); }
        if ( status.compareTo( CANCEL ) != 0 ) eventBody.getPatruls().put( patrul.getUuid(), patrul );
        if ( eventBody.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask(
                        eventBody,
                        eventBody.getUUID().toString(),
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
                if ( super.getCheckEquality().test( status, FINISHED ) ) this.getUpdateTotalTimeConsumption().accept( patrul, FIND_FACE_CAR );
                else carEvents.getPatruls().remove(
                        CassandraDataControlForTasks
                                .getInstance()
                                .getDeleteRowFromTaskTimingTable()
                                .apply( patrul ) );
                if ( carEvents.getPatruls().size() == carEvents.getReportForCardList().size() ) {
                    carEvents.setStatus( FINISHED );
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( carEvents.getUUID().toString() );
                    if ( carEvents.getPatruls().size() > 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask(
                                    carEvents,
                                    carEvents.getUUID().toString(),
                                    carEvents.getStatus(),
                                    FIND_FACE_CAR,
                                    carEvents.getPatruls() ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( FIND_FACE_CAR );
                patrul.setTaskId( carEvents.getUUID().toString() ); // saving card id into patrul object
                if ( DataValidateInspector
                        .getInstance()
                        .getCheckParam()
                        .test( carEvents.getDataInfo() )
                        && DataValidateInspector
                        .getInstance()
                        .getCheckParam()
                        .test( carEvents.getDataInfo().getData() ) ) {
                    patrul.setLatitudeOfTask( carEvents.getDataInfo().getData().getLatitude() );
                    patrul.setLongitudeOfTask( carEvents.getDataInfo().getData().getLongitude() ); } }
            case ACCEPTED -> patrul.setTaskDate( TimeInspector
                    .getInspector()
                    .getGetNewDate()
                    .get() ); // fixing time when patrul started this task
            case ARRIVED -> carEvents.getPatrulStatuses().putIfAbsent(
                    patrul.getPassportNumber(), this.getSaveTaskTiming().apply( patrul, FIND_FACE_CAR ) ); }

        if ( status.compareTo( CANCEL ) != 0 ) carEvents.getPatruls().put( patrul.getUuid(), patrul );

        if ( carEvents.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask(
                        carEvents,
                        carEvents.getUUID().toString(),
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
                if ( super.getCheckEquality().test( status, FINISHED ) ) this.getUpdateTotalTimeConsumption().accept( patrul, FIND_FACE_PERSON );
                else faceEvent.getPatruls().remove(
                        CassandraDataControlForTasks
                                .getInstance()
                                .getDeleteRowFromTaskTimingTable()
                                .apply( patrul ) );
                if ( faceEvent.getPatruls().size() == faceEvent.getReportForCardList().size() ) {
                    faceEvent.setStatus( FINISHED );
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( faceEvent.getUUID().toString() );
                    if ( faceEvent.getPatruls().size() > 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask(
                                    faceEvent,
                                    faceEvent.getUUID().toString(),
                                    faceEvent.getStatus(),
                                    FIND_FACE_PERSON,
                                    faceEvent.getPatruls() ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( FIND_FACE_PERSON );
                patrul.setTaskId( faceEvent.getUUID().toString() ); // saving card id into patrul object
                if ( DataValidateInspector
                        .getInstance()
                        .getCheckParam()
                        .test( faceEvent.getDataInfo() )
                        && DataValidateInspector
                        .getInstance()
                        .getCheckParam()
                        .test( faceEvent.getDataInfo().getData() ) ) {
                    patrul.setLatitudeOfTask( faceEvent.getDataInfo().getData().getLatitude() );
                    patrul.setLongitudeOfTask( faceEvent.getDataInfo().getData().getLongitude() ); } }
            case ACCEPTED -> patrul.setTaskDate( TimeInspector
                    .getInspector()
                    .getGetNewDate()
                    .get() ); // fixing time when patrul started this task
            case ARRIVED -> faceEvent.getPatrulStatuses().putIfAbsent(
                        patrul.getPassportNumber(), this.getSaveTaskTiming().apply( patrul, FIND_FACE_PERSON ) ); }
        if ( status.compareTo( CANCEL ) != 0 ) faceEvent.getPatruls().put( patrul.getUuid(), patrul );
        if ( faceEvent.getStatus().compareTo( FINISHED ) != 0 )
            CassandraDataControlForTasks
                    .getInstance()
                    .getSaveActiveTask()
                    .accept( new ActiveTask(
                            faceEvent,
                            faceEvent.getUUID().toString(),
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
            case CANCEL, FINISHED -> {
                if ( super.getCheckEquality().test( status, FINISHED ) ) patrul.getListOfTasks().put( patrul.getTaskId(), ESCORT.name() );
                else escortTuple.getPatrulList().remove( patrul.getUuid() );
                CassandraDataControlForEscort
                        .getInstance()
                        .getGetCurrentTupleOfCar()
                        .apply( patrul.getUuidForEscortCar() )
                        .subscribe( new CustomSubscriber( 1 ) );
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setUuidForEscortCar( null );
                patrul.setUuidOfEscort( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( TaskTypes.ESCORT );
                patrul.setTaskId( escortTuple.getUuid().toString() ); }
            case ACCEPTED -> patrul.setTaskDate( TimeInspector
                    .getInspector()
                    .getGetNewDate()
                    .get() ); }

        CassandraDataControl
                .getInstance()
                .getUpdatePatrulAfterTask()
                .accept( patrul );

        return patrul; }

    public Patrul changeTaskStatus ( final Patrul patrul, final Status status, final SelfEmploymentTask selfEmploymentTask ) {
        patrul.setStatus( status );
        switch ( patrul.getStatus() ) {
            case ARRIVED -> {
                patrul.setTaskTypes( SELF_EMPLOYMENT );
                patrul.setTaskId( selfEmploymentTask.getUuid().toString() );
                selfEmploymentTask.getPatrulStatuses().putIfAbsent(
                        patrul.getPassportNumber(), this.getSaveTaskTiming().apply( patrul, SELF_EMPLOYMENT ) ); }
            case CANCEL, FINISHED -> {
                if ( super.getCheckEquality().test( status, FINISHED ) ) this.getUpdateTotalTimeConsumption().accept( patrul, SELF_EMPLOYMENT );
                else selfEmploymentTask.getPatruls().remove(
                        CassandraDataControlForTasks
                                .getInstance()
                                .getDeleteRowFromTaskTimingTable()
                                .apply( patrul ) );
                if ( selfEmploymentTask.getPatruls().size() == selfEmploymentTask.getReportForCards().size() ) {
                    selfEmploymentTask.setTaskStatus( FINISHED );

                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( selfEmploymentTask.getUuid().toString() );

                    if ( selfEmploymentTask.getPatruls().size() > 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask(
                                    selfEmploymentTask,
                                    selfEmploymentTask.getUuid().toString(),
                                    selfEmploymentTask.getTaskStatus(),
                                    SELF_EMPLOYMENT,
                                    selfEmploymentTask.getPatruls() ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED, ACCEPTED -> {
                patrul.setTaskTypes( SELF_EMPLOYMENT );
                patrul.setTaskId( selfEmploymentTask.getUuid().toString() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( selfEmploymentTask.getLatOfAccident() );
                patrul.setLongitudeOfTask( selfEmploymentTask.getLanOfAccident() ); } }

        if ( status.compareTo( CANCEL ) != 0 ) selfEmploymentTask.getPatruls().put( patrul.getUuid(), patrul );

        if ( selfEmploymentTask.getTaskStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask(
                        selfEmploymentTask,
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
                .parallel( super.getCheckDifference().apply( size ) )
                .runOn( Schedulers.parallel() )
                .flatMap( key -> switch ( TaskTypes.valueOf( patrul.getListOfTasks().get( key ) ) ) {
                        case CARD_102 -> CassandraDataControlForTasks
                                .getInstance()
                                .getGetRowDemo()
                                .apply( key )
                    .map( row -> super.deserialize( row.getString( "object" ), Card.class ) )
                                .map( card -> FinishedTask
                                        .builder()
                                        .taskTypes( CARD_102 )
                                        .task( card.getFabula() )
                                        .createdDate( card.getCreated_date().getTime() )
                                        .cardDetails( new CardDetails( card, patrul, "ru" ) )
                                        .reportForCard( card
                                                .getReportForCardList()
                                                .get( this.getGetReportIndex().apply( card.getReportForCardList(), patrul.getUuid() ) ) )
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
                                .getGetRowDemo()
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
                                        .cardDetails( new CardDetails( new CarDetails( carEvent ) ) )
                                        .reportForCard( carEvent
                                                .getReportForCardList()
                                                .get( this.getGetReportIndex().apply(
                                                        carEvent.getReportForCardList(), patrul.getUuid() ) ) )
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
                                .getGetRowDemo()
                                .apply( key )
                    .map( row -> super.deserialize( row.getString("object" ), FaceEvent.class ) )
                                .map( faceEvent -> FinishedTask
                                        .builder()
                                        .taskTypes( FIND_FACE_PERSON )
                                        .task( faceEvent.getName() )
                                        .cardDetails( new CardDetails( new PersonDetails( faceEvent ) ) )
                                        .createdDate( TimeInspector
                                                .getInspector()
                                                .getConvertTimeToLong()
                                                .apply( faceEvent.getCreated_date() ) )
                                        .reportForCard( faceEvent
                                                .getReportForCardList()
                                                .get( this.getGetReportIndex().apply(
                                                        faceEvent.getReportForCardList(), patrul.getUuid() ) ) )
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
                                .getGetRowDemo()
                                .apply( key )
                    .map( row -> super.deserialize( row.getString("object" ), EventCar.class ) )
                                .map( eventCar -> FinishedTask
                                        .builder()
                                        .task( eventCar.getId() )
                                        .taskTypes( FIND_FACE_EVENT_CAR )
                                        .createdDate( eventCar.getCreated_date().getTime() )
                                        .cardDetails( new CardDetails( new CarDetails( eventCar ) ) )
                                        .reportForCard( eventCar
                                                .getReportForCardList()
                                                .get( this.getGetReportIndex().apply(
                                                        eventCar.getReportForCardList(), patrul.getUuid() ) ) )
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
                                .getGetRowDemo()
                                .apply( key )
                    .map( row -> super.deserialize( row.getString("object" ), EventBody.class ) )
                                .map( eventBody -> FinishedTask
                                        .builder()
                                        .task( eventBody.getId() )
                                        .taskTypes( FIND_FACE_EVENT_BODY )
                                        .createdDate( eventBody.getCreated_date().getTime() )
                                        .cardDetails( new CardDetails( new PersonDetails( eventBody ) ) )
                                        .reportForCard( eventBody
                                                .getReportForCardList()
                                                .get( this.getGetReportIndex().apply(
                                                        eventBody.getReportForCardList(), patrul.getUuid() ) ) )
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
                                .getGetRowDemo()
                                .apply( key )
                    .map( row -> super.deserialize( row.getString( "object" ), EventFace.class ) )
                                .map( eventFace -> FinishedTask
                                        .builder()
                                        .task( eventFace.getId() )
                                        .taskTypes( FIND_FACE_EVENT_FACE )
                                        .createdDate( eventFace.getCreated_date().getTime() )
                                        .cardDetails( new CardDetails( new PersonDetails( eventFace ) ) )
                                        .reportForCard( eventFace
                                                .getReportForCardList()
                                                .get( this.getGetReportIndex().apply(
                                                        eventFace.getReportForCardList(), patrul.getUuid() ) ) )
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
                                .getGetRowDemo()
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
                                                .get( this.getGetReportIndex().apply(
                                                        selfEmploymentTask.getReportForCards(), patrul.getUuid() ) ) )
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

    private final BiFunction< Patrul, ReportForCard, Mono< ApiResponseModel > > saveReportForTask = ( patrul, reportForCard ) -> switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString( "object" ), Card.class ) )
                    .flatMap( card -> {
                        card.getReportForCardList().add( reportForCard );
                        return super.getFunction().apply(
                                Map.of( "message", "Report from: "
                                        + this.changeTaskStatus( patrul, Status.FINISHED, card ).getName()
                                        + " was saved" ) ); } );

            case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), SelfEmploymentTask.class ) )
                    .flatMap( selfEmploymentTask -> {
                        selfEmploymentTask.getReportForCards().add( reportForCard );
                        return super.getFunction().apply(
                                Map.of( "message", "Report from: "
                                        + this.changeTaskStatus( patrul, Status.FINISHED, selfEmploymentTask ).getName()
                                        + " was saved" ) ); } );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), EventBody.class ) )
                    .flatMap( eventBody -> {
                        eventBody.getReportForCardList().add( reportForCard );
                        return super.getFunction().apply(
                                Map.of( "message", "Report from: "
                                        + this.changeTaskStatus( patrul, Status.FINISHED, eventBody ).getName()
                                        + " was saved" ) ); } );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString( "object" ), EventFace.class ) )
                    .flatMap( eventFace -> {
                        eventFace.getReportForCardList().add( reportForCard );
                        return super.getFunction().apply(
                                Map.of( "message", "Report from: "
                                        + this.changeTaskStatus( patrul, Status.FINISHED, eventFace ).getName()
                                        + " was saved" ) ); } );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), CarEvent.class ) )
                    .flatMap( carEvents -> {
                        carEvents.getReportForCardList().add( reportForCard );
                        return super.getFunction().apply(
                                Map.of( "message", "Report from: "
                                        + this.changeTaskStatus( patrul, Status.FINISHED, carEvents ).getName()
                                        + " was saved" ) ); } );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), FaceEvent.class ) )
                    .flatMap( faceEvents -> {
                        faceEvents.getReportForCardList().add( reportForCard );
                        return super.getFunction().apply(
                                Map.of( "message", "Report from: "
                                        + this.changeTaskStatus( patrul, Status.FINISHED, faceEvents ).getName() + " was saved" ) ); } );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), EventCar.class ) )
                    .flatMap( eventCar -> {
                        eventCar.getReportForCardList().add( reportForCard );
                        return super.getFunction().apply(
                                Map.of( "message", "Report from: "
                                        + this.changeTaskStatus( patrul, Status.FINISHED, eventCar ).getName()
                                        + " was saved" ) ); } );

            default -> super.getFunction().apply(
                    Map.of( "message", "U have no tasks, thus u cannot send report",
                            "code", 201,
                            "success", false ) ); };

    private final BiFunction< Patrul, Status, Mono< ApiResponseModel > > changeTaskStatus = ( patrul, status ) -> switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString( "object" ), Card.class ) )
                    .flatMap( card -> super.getFunction().apply(
                            Map.of( "message", "Patrul: "
                                    + this.changeTaskStatus( patrul, status, card ).getPassportNumber()
                                    + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) );

            case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), SelfEmploymentTask.class ) )
                    .flatMap( selfEmploymentTask -> super.getFunction().apply(
                            Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, selfEmploymentTask ).getPassportNumber()
                                            + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), EventCar.class ) )
                    .flatMap( eventCar -> super.getFunction().apply(
                            Map.of( "message", "Patrul: " + this.changeTaskStatus( patrul, status, eventCar ).getPassportNumber()
                                            + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString( "object" ), EventFace.class ) )
                    .flatMap( eventFace -> super.getFunction().apply(
                            Map.of( "message", "Patrul: " + this.changeTaskStatus( patrul, status, eventFace ).getPassportNumber()
                                            + " changed his status task to: " + status,
                                        "success", CassandraDataControl
                                                .getInstance()
                                                .getUpdatePatrulStatus()
                                                .apply( patrul, status ) ) ) );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), FaceEvent.class ) )
                    .flatMap( faceEvents -> super.getFunction().apply(
                            Map.of( "message", "Patrul: " + this.changeTaskStatus( patrul, status, faceEvents )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                        .getInstance()
                                        .getUpdatePatrulStatus()
                                        .apply( patrul, status ) ) ) );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), CarEvent.class ) )
                    .flatMap( carEvents -> super.getFunction().apply(
                            Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, carEvents ).getPassportNumber()
                                            + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) );

            case ESCORT -> CassandraDataControlForEscort
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
                                            .apply( patrul, status ) ) ) );

            default -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), EventBody.class ) )
                    .flatMap( eventBody -> super.getFunction().apply(
                            Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, eventBody ).getPassportNumber()
                                            + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) ); };

    // по запросу проверяет какая задача дана конкретному патрульному
    // после чего возвращает краткое ( ACTIVE_TASK ), полное ( CARD_DETAILS ) или же по дефолту убирает патрульного из задачи
    private final BiFunction< Patrul, TaskTypes, Mono< ApiResponseModel > > getTaskData = ( patrul, taskTypes ) -> switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString( "object" ), Card.class ) )
                    .flatMap( card -> super.getFunction().apply( switch ( taskTypes ) {
                        case CARD_DETAILS -> Map.of( "message", "Your task details",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new CardDetails( card, patrul, "ru" ) )
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
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), EventBody.class ) )
                    .flatMap( eventBody -> super.getFunction().apply( switch ( taskTypes ) {
                        case CARD_DETAILS -> Map.of( "message", "Your task details",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new CardDetails( new PersonDetails( eventBody ) ) )
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
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString( "object" ), EventFace.class) )
                    .flatMap( eventFace -> super.getFunction().apply( switch ( taskTypes ) {
                        case CARD_DETAILS -> Map.of( "message", "Your task details",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new CardDetails( new PersonDetails( eventFace ) ) )
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
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), EventCar.class ) )
                    .flatMap( eventCar -> super.getFunction().apply( switch ( taskTypes ) {
                        case CARD_DETAILS -> Map.of( "message", "Your task details",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new CardDetails( new CarDetails( eventCar ) ) )
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
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), CarEvent.class ) )
                    .flatMap( carEvent -> super.getFunction().apply( switch ( taskTypes ) {
                        case CARD_DETAILS -> Map.of( "message", "Your task details",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new CardDetails( new CarDetails( carEvent ) ) )
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
                    .getGetRowDemo()
                    .apply( patrul.getTaskId() )
                    .map( row -> super.deserialize( row.getString("object" ), FaceEvent.class ) )
                    .flatMap( faceEvent -> super.getFunction().apply( switch ( taskTypes ) {
                        case CARD_DETAILS -> Map.of( "message", "Your task details",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new CardDetails( new PersonDetails( faceEvent ) ) )
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
                    .getGetRowDemo()
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