package com.ssd.mvd.gpstabletsservice.inspectors;

import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.BiFunction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.ssd.mvd.gpstabletsservice.database.*;
import com.ssd.mvd.gpstabletsservice.task.card.*;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.tuple.EscortTuple;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.entity.Notification;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import static com.ssd.mvd.gpstabletsservice.constants.Status.FREE;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.*;
import com.ssd.mvd.gpstabletsservice.request.PatrulActivityRequest;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.tuple.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.FinishedTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.FIND_FACE_CAR;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.SELF_EMPLOYMENT;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvent;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvent;

@lombok.Data
public final class TaskInspector extends DataValidateInspector {
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
                                    this.getGenerateText().apply( patrul, status ),
                                    taskTypes ) ) );
            return patrul; }

    private final BiFunction< Patrul, TaskTypes, PatrulStatus > saveTaskTiming = ( patrul, taskTypes ) -> {
            final PatrulStatus patrulStatus = new PatrulStatus( patrul );
            CassandraDataControl
                    .getInstance()
                    .getGetHistory()
                    .apply( PatrulActivityRequest
                            .builder()
                            .endDate( new Date() )
                            .startDate( patrul.getTaskDate() )
                            .patrulUUID( patrul.getPassportNumber() )
                            .build() )
                    .subscribe( positionInfos -> CassandraDataControlForTasks
                            .getInstance()
                            .getSaveTaskTimeStatistics()
                            .accept( new TaskTimingStatistics(
                                    patrul,
                                    patrul.getTaskId(),
                                    taskTypes,
                                    patrulStatus,
                                    positionInfos ) ) );
            return patrulStatus; };

    private final BiFunction< Patrul, Status, String > generateText = ( patrul, status ) -> switch ( status ) {
            case ACCEPTED -> patrul.getName() + " ACCEPTED his task: " + patrul.getTaskId() + " " + patrul.getTaskTypes() + " at: " + new Date();
            case ARRIVED -> patrul.getName() + " : " + patrul.getTaskTypes() + " arrived task location: " + " at: " + new Date();
            case ATTACHED -> patrul.getName() + " got new task: " + patrul.getTaskId() + " " + patrul.getTaskTypes();
            case FINISHED -> patrul.getName() + " completed his task at: " + new Date();
            default -> patrul.getName() + " has been canceled from task at: " + new Date(); };

    public Patrul changeTaskStatus ( final Patrul patrul, final Status status, final Card card ) {
        patrul.setStatus( status );
        switch ( patrul.getStatus() ) {
            case CANCEL, FINISHED -> {
                if ( super.getCheckEquality().apply( status, FINISHED ) ) {
                    // сохраняем времся выполнения
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                                    .apply( patrul, TimeInspector
                                            .getInspector()
                                            .getGetTimeDifferenceInSeconds()
                                            .apply( patrul.getTaskDate().toInstant() ) );
                    patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), CARD_102.name() ); }
                else {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteRowFromTaskTimingTable()
                            .accept( patrul );
                    card.getPatruls().remove( patrul.getUuid() ); }
                if ( card.getPatruls().size() == card.getReportForCardList().size() ) {
                    card.setStatus( FINISHED );
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( card.getUUID().toString() );
                    if ( card.getPatruls().size() != 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( card ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( CARD_102 );
                patrul.setLatitudeOfTask( card.getLatitude() );
                patrul.setTaskId( card.getUUID().toString() ); // saving card id into patrul object
                patrul.setLongitudeOfTask( card.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> card.getPatrulStatuses().put(
                    patrul.getPassportNumber(),
                    this.getSaveTaskTiming().apply( patrul, CARD_102 ) ); }

        if ( status.compareTo( CANCEL ) != 0 ) card.getPatruls().put( patrul.getUuid(), patrul );

        if ( card.getStatus().compareTo( FINISHED ) != 0 )
            CassandraDataControlForTasks
                    .getInstance()
                    .getSaveActiveTask()
                    .accept( new ActiveTask( card ) );

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
                if ( super.getCheckEquality().apply( status, FINISHED ) ) {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                                    .apply( patrul, TimeInspector
                                            .getInspector()
                                            .getGetTimeDifferenceInSeconds()
                                            .apply( patrul.getTaskDate().toInstant() ) );
                    patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), FIND_FACE_EVENT_CAR.name() ); }
                else {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteRowFromTaskTimingTable()
                            .accept( patrul );
                    eventCar.getPatruls().remove( patrul.getUuid() ); }
                if ( eventCar.getPatruls().size() == eventCar.getReportForCardList().size() ) {
                    eventCar.setStatus( FINISHED );
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( eventCar.getUUID().toString() );
                    if ( eventCar.getPatruls().size() > 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( eventCar ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( FIND_FACE_EVENT_CAR );
                patrul.setTaskId( eventCar.getUUID().toString() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( eventCar.getLatitude() );
                patrul.setLongitudeOfTask( eventCar.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> eventCar.getPatrulStatuses().put(
                    patrul.getPassportNumber(),
                    this.getSaveTaskTiming().apply( patrul, FIND_FACE_EVENT_CAR ) ); }

        if ( eventCar.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask( eventCar ) );

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
                if ( super.getCheckEquality().apply( status, FINISHED ) ) {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                            .apply( patrul, TimeInspector
                                    .getInspector()
                                    .getGetTimeDifferenceInSeconds()
                                    .apply( patrul.getTaskDate().toInstant() ) );
                    patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), FIND_FACE_EVENT_FACE.name() ); }
                else {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteRowFromTaskTimingTable()
                            .accept( patrul );
                    eventFace.getPatruls().remove( patrul.getUuid() ); }
                if ( eventFace.getPatruls().size() == eventFace.getReportForCardList().size() ) {
                    eventFace.setStatus( FINISHED );
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( eventFace.getUUID().toString() );
                    if ( eventFace.getPatruls().size() > 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( eventFace ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( FIND_FACE_EVENT_FACE );
                patrul.setTaskId( eventFace.getUUID().toString() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( eventFace.getLatitude() );
                patrul.setLongitudeOfTask( eventFace.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> eventFace.getPatrulStatuses().putIfAbsent(
                    patrul.getPassportNumber(),
                    this.getSaveTaskTiming().apply( patrul, FIND_FACE_PERSON ) ); }

        if ( status.compareTo( CANCEL ) != 0 ) eventFace.getPatruls().put( patrul.getUuid(), patrul );

        if ( eventFace.getStatus().compareTo( FINISHED ) != 0 )
            CassandraDataControlForTasks
                    .getInstance()
                    .getSaveActiveTask()
                    .accept( new ActiveTask( eventFace ) );

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
                if ( super.getCheckEquality().apply( status, FINISHED ) ) {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                            .apply( patrul, TimeInspector
                                    .getInspector()
                                    .getGetTimeDifferenceInSeconds()
                                    .apply( patrul.getTaskDate().toInstant() ) );
                    patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), FIND_FACE_EVENT_BODY.name() ); }
                else {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteRowFromTaskTimingTable()
                            .accept( patrul );
                    eventBody.getPatruls().remove( patrul.getUuid() ); }
                if ( eventBody.getPatruls().size() == eventBody.getReportForCardList().size() ) {
                    eventBody.setStatus( FINISHED );
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( eventBody.getUUID().toString() );
                    if ( eventBody.getPatruls().size() > 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( eventBody ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( FIND_FACE_EVENT_BODY );
                patrul.setTaskId( eventBody.getUUID().toString() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( eventBody.getLatitude() );
                patrul.setLongitudeOfTask( eventBody.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> eventBody.getPatrulStatuses().putIfAbsent(
                    patrul.getPassportNumber(), this.getSaveTaskTiming().apply( patrul, FIND_FACE_PERSON ) ); }
        if ( status.compareTo( CANCEL ) != 0 ) eventBody.getPatruls().put( patrul.getUuid(), patrul );
        if ( eventBody.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask( eventBody ) );

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
                if ( super.getCheckEquality().apply( status, FINISHED ) ) {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                            .apply( patrul, TimeInspector
                                    .getInspector()
                                    .getGetTimeDifferenceInSeconds()
                                    .apply( patrul.getTaskDate().toInstant() ) );

                    patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), FIND_FACE_CAR.name() ); }
                else {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteRowFromTaskTimingTable()
                            .accept( patrul );
                    carEvents.getPatruls().remove( patrul.getUuid() ); }
                if ( carEvents.getPatruls().size() == carEvents.getReportForCardList().size() ) {
                    carEvents.setStatus( FINISHED );
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( carEvents.getUUID().toString() );
                    if ( carEvents.getPatruls().size() > 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( carEvents ) ); }
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
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> carEvents.getPatrulStatuses().putIfAbsent(
                    patrul.getPassportNumber(), this.getSaveTaskTiming().apply( patrul, FIND_FACE_CAR ) ); }

        if ( status.compareTo( CANCEL ) != 0 ) carEvents.getPatruls().put( patrul.getUuid(), patrul );

        if ( carEvents.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks
                .getInstance()
                .getSaveActiveTask()
                .accept( new ActiveTask( carEvents ) );

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
                if ( super.getCheckEquality().apply( status, FINISHED ) ) {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                            .apply( patrul, TimeInspector
                                    .getInspector()
                                    .getGetTimeDifferenceInSeconds()
                                    .apply( patrul.getTaskDate().toInstant() ) );

                    patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), FIND_FACE_PERSON.name() ); }
                else {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteRowFromTaskTimingTable()
                            .accept( patrul );
                    faceEvent.getPatruls().remove( patrul.getUuid() ); }
                if ( faceEvent.getPatruls().size() == faceEvent.getReportForCardList().size() ) {
                    faceEvent.setStatus( FINISHED );
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( faceEvent.getUUID().toString() );
                    if ( faceEvent.getPatruls().size() > 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( faceEvent ) ); }
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
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> faceEvent.getPatrulStatuses().putIfAbsent(
                        patrul.getPassportNumber(), this.getSaveTaskTiming().apply( patrul, FIND_FACE_PERSON ) ); }
        if ( status.compareTo( CANCEL ) != 0 ) faceEvent.getPatruls().put( patrul.getUuid(), patrul );
        if ( faceEvent.getStatus().compareTo( FINISHED ) != 0 )
            CassandraDataControlForTasks
                    .getInstance()
                    .getSaveActiveTask()
                    .accept( new ActiveTask( faceEvent ) );

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
                if ( super.getCheckEquality().apply( status, FINISHED ) ) patrul.getListOfTasks()
                        .put( patrul.getTaskId(), ESCORT.name() );
                else escortTuple.getPatrulList().remove( patrul.getUuid() );
                CassandraDataControlForEscort
                        .getInstance()
                        .getGetCurrentTupleOfCar()
                        .apply( patrul.getUuidForEscortCar() )
                        .subscribe( tupleOfCar -> {
                            tupleOfCar.setUuidOfPatrul( null );
                            CassandraDataControlForEscort
                                    .getInstance()
                                    .getUpdateTupleOfCar()
                                    .apply( tupleOfCar )
                                    .subscribe(); } );
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setUuidForEscortCar( null );
                patrul.setUuidOfEscort( null );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( TaskTypes.ESCORT );
                patrul.setTaskId( escortTuple.getUuid().toString() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); }

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
                if ( super.getCheckEquality().apply( status, FINISHED ) ) {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                            .apply( patrul, TimeInspector
                                    .getInspector()
                                    .getGetTimeDifferenceInSeconds()
                                    .apply( patrul.getTaskDate().toInstant() ) );

                    patrul.getListOfTasks().putIfAbsent( patrul.getTaskId(), SELF_EMPLOYMENT.name() ); }
                else {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteRowFromTaskTimingTable()
                            .accept( patrul );
                    selfEmploymentTask.getPatruls().remove( patrul.getUuid() ); }
                if ( selfEmploymentTask.getPatruls().size() == selfEmploymentTask.getReportForCards().size() ) {
                    selfEmploymentTask.setTaskStatus( FINISHED );

                    CassandraDataControlForTasks
                            .getInstance()
                            .getDeleteActiveTask()
                            .accept( selfEmploymentTask.getUuid().toString() );

                    if ( selfEmploymentTask.getPatruls().size() > 0 ) KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( selfEmploymentTask ) ); }
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
                .accept( new ActiveTask( selfEmploymentTask ) );

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
                .parallel( size )
                .runOn( Schedulers.parallel() )
                .flatMap( key -> switch ( TaskTypes.valueOf( patrul.getListOfTasks().get( key ) ) ) {
                        case CARD_102 -> CassandraDataControlForTasks
                                .getInstance()
                                .getGetCard102()
                                .apply( key )
                                .map( card -> FinishedTask
                                        .builder()
                                        .taskTypes( CARD_102 )
                                        .task( card.getFabula() )
                                        .createdDate( card.getCreated_date().getTime() )
                                        .cardDetails( new CardDetails( card, patrul, "ru" ) )
                                        .reportForCard( card
                                                .getReportForCardList()
                                                .get( this.getReportIndex( card.getReportForCardList(),
                                                        patrul.getUuid() ) ) )
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
                                .getGetCarEvents()
                                .apply( key )
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
                                                .get( this.getReportIndex( carEvent
                                                        .getReportForCardList(), patrul.getUuid() ) ) )
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
                                .getGetFaceEvents()
                                .apply( key )
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
                                                .get( this.getReportIndex( faceEvent
                                                        .getReportForCardList(), patrul.getUuid() ) ) )
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
                                .getGetEventCar()
                                .apply( key )
                                .map( eventCar -> FinishedTask
                                        .builder()
                                        .task( eventCar.getId() )
                                        .taskTypes( FIND_FACE_EVENT_CAR )
                                        .createdDate( eventCar.getCreated_date().getTime() )
                                        .cardDetails( new CardDetails( new CarDetails( eventCar ) ) )
                                        .reportForCard( eventCar
                                                .getReportForCardList()
                                                .get( this.getReportIndex( eventCar
                                                        .getReportForCardList(), patrul.getUuid() ) ) )
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
                                .getGetEventBody()
                                .apply( key )
                                .map( eventBody -> FinishedTask
                                        .builder()
                                        .task( eventBody.getId() )
                                        .taskTypes( FIND_FACE_EVENT_BODY )
                                        .createdDate( eventBody.getCreated_date().getTime() )
                                        .cardDetails( new CardDetails( new PersonDetails( eventBody ) ) )
                                        .reportForCard( eventBody
                                                .getReportForCardList()
                                                .get( this.getReportIndex( eventBody
                                                        .getReportForCardList(), patrul.getUuid() ) ) )
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
                                .getGetEventFace()
                                .apply( key )
                                .map( eventFace -> FinishedTask
                                        .builder()
                                        .task( eventFace.getId() )
                                        .taskTypes( FIND_FACE_EVENT_FACE )
                                        .createdDate( eventFace.getCreated_date().getTime() )
                                        .cardDetails( new CardDetails( new PersonDetails( eventFace ) ) )
                                        .reportForCard( eventFace
                                                .getReportForCardList()
                                                .get( this.getReportIndex( eventFace
                                                        .getReportForCardList(), patrul.getUuid() ) ) )
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
                                .getGetSelfEmploymentTask()
                                .apply( UUID.fromString( key ) )
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
                                                .get( this.getReportIndex( selfEmploymentTask
                                                        .getReportForCards(), patrul.getUuid() ) ) )
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

    private Integer getReportIndex ( final List< ReportForCard > reportForCardList, final UUID uuid ) {
        for ( int i = 0; i < reportForCardList.size(); i++ ) if ( reportForCardList.get( i )
                .getUuidOfPatrul()
                .compareTo( uuid ) == 0 ) return i;
        return 0; }

    private final BiFunction< Patrul, ReportForCard, Mono< ApiResponseModel > > saveReportForTask = ( patrul, reportForCard ) -> switch ( patrul.getTaskTypes() ) {
                case CARD_102 -> CassandraDataControlForTasks
                        .getInstance()
                        .getGetCard102()
                        .apply( patrul.getTaskId() )
                        .flatMap( card -> {
                            card.getReportForCardList().add( reportForCard );
                            return super.getFunction().apply( Map.of( "message", "Report from: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, Status.FINISHED, card )
                                            .getName() + " was saved" ) ); } );

                case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                        .getInstance()
                        .getGetSelfEmploymentTask()
                        .apply( UUID.fromString( patrul.getTaskId() ) )
                        .flatMap( selfEmploymentTask -> {
                            selfEmploymentTask.getReportForCards().add( reportForCard );
                            return super.getFunction().apply( Map.of( "message", "Report from: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, Status.FINISHED, selfEmploymentTask )
                                            .getName() + " was saved" ) ); } );

                case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                        .getInstance()
                        .getGetEventBody()
                        .apply( patrul.getTaskId() )
                        .flatMap( eventBody -> {
                            eventBody.getReportForCardList().add( reportForCard );
                            return super.getFunction().apply( Map.of( "message", "Report from: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, Status.FINISHED, eventBody )
                                            .getName() + " was saved" ) ); } );

                case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                        .getInstance()
                        .getGetEventFace()
                        .apply( patrul.getTaskId() )
                        .flatMap( eventFace -> {
                            eventFace.getReportForCardList().add( reportForCard );
                            return super.getFunction().apply( Map.of( "message", "Report from: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, Status.FINISHED, eventFace )
                                            .getName() + " was saved" ) ); } );

                case FIND_FACE_CAR -> CassandraDataControlForTasks
                        .getInstance()
                        .getGetCarEvents()
                        .apply( patrul.getTaskId() )
                        .flatMap( carEvents -> {
                            carEvents.getReportForCardList().add( reportForCard );
                            return super.getFunction().apply( Map.of( "message", "Report from: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, Status.FINISHED, carEvents )
                                            .getName() + " was saved" ) ); } );

                case FIND_FACE_PERSON -> CassandraDataControlForTasks
                        .getInstance()
                        .getGetFaceEvents()
                        .apply( patrul.getTaskId() )
                        .flatMap( faceEvents -> {
                            faceEvents.getReportForCardList().add( reportForCard );
                            return super.getFunction().apply( Map.of( "message", "Report from: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, Status.FINISHED, faceEvents )
                                            .getName() + " was saved" ) ); } );

                case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                        .getInstance()
                        .getGetEventCar()
                        .apply( patrul.getTaskId() )
                        .flatMap( eventCar -> {
                            eventCar.getReportForCardList().add( reportForCard );
                            return super.getFunction().apply(
                                    Map.of( "message", "Report from: "
                                            + TaskInspector
                                            .getInstance()
                                            .changeTaskStatus( patrul, Status.FINISHED, eventCar )
                                            .getName() + " was saved" ) ); } );

                default -> super.getFunction().apply(
                        Map.of( "message", "U have no tasks, thus u cannot send report",
                                "code", 201,
                                "success", false ) ); };

    private final BiFunction< Patrul, Status, Mono< ApiResponseModel > > changeTaskStatus = ( patrul, status ) -> switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCard102()
                    .apply( patrul.getTaskId() )
                    .flatMap( card -> super.getFunction().apply( Map.of( "message", "Patrul: " + this.changeTaskStatus( patrul, status, card )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) );

            case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetSelfEmploymentTask()
                    .apply( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> super.getFunction().apply( Map.of( "message", "Patrul: " + this.changeTaskStatus( patrul, status, selfEmploymentTask )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventCar()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventCar -> super.getFunction().apply( Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, eventCar )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventFace()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventFace -> super.getFunction().apply( Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, eventFace )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetFaceEvents()
                    .apply( patrul.getTaskId() )
                    .flatMap( faceEvents -> super.getFunction().apply( Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, faceEvents )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCarEvents()
                    .apply( patrul.getTaskId() )
                    .flatMap( carEvents -> super.getFunction().apply( Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, carEvents )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) );

            case ESCORT -> CassandraDataControlForEscort
                    .getInstance()
                    .getGetCurrentTupleOfEscort()
                    .apply( patrul.getTaskId() )
                    .flatMap( escortTuple -> super.getFunction().apply( Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, escortTuple )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) );

            default -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventBody()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventBody -> super.getFunction().apply( Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, eventBody )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .getUpdatePatrulStatus()
                                            .apply( patrul, status ) ) ) ); };

    private final Function< Patrul, Mono< ApiResponseModel > > getCurrentActiveTask = patrul -> switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCard102()
                    .apply( patrul.getTaskId() )
                    .flatMap( card -> super.getFunction().apply(
                            Map.of( "message", "U have " + CARD_102.name() + " Task",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new ActiveTask( card, patrul.getStatus() ) )
                                            .type( CARD_102.name() )
                                            .build() ) ) );

            case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetSelfEmploymentTask()
                    .apply( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> super.getFunction().apply(
                            Map.of( "message", "U have " + SELF_EMPLOYMENT.name() + " Task",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new ActiveTask( selfEmploymentTask, patrul.getStatus() ) )
                                            .type( SELF_EMPLOYMENT.name() )
                                            .build() ) ) );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventCar()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventCar -> super.getFunction().apply(
                            Map.of( "message", "U have " + FIND_FACE_EVENT_CAR.name() + " Task",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new ActiveTask( eventCar, patrul.getStatus() ) )
                                            .type( FIND_FACE_EVENT_CAR.name() )
                                            .build() ) ) );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventBody()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventBody -> super.getFunction().apply(
                            Map.of( "message", "U have " + FIND_FACE_EVENT_BODY.name() + " Task",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new ActiveTask( eventBody, patrul.getStatus() ) )
                                            .type( FIND_FACE_EVENT_BODY.name() )
                                            .build() ) ) );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventFace()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventFace -> super.getFunction().apply(
                            Map.of( "message", "U have " + FIND_FACE_EVENT_FACE.name() + " Task",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new ActiveTask( eventFace, patrul.getStatus() ) )
                                            .type( FIND_FACE_EVENT_FACE.name() )
                                            .build() ) ) );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCarEvents()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventFace -> super.getFunction().apply(
                            Map.of( "message", "U have " + FIND_FACE_CAR.name() + " Task",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new ActiveTask( eventFace, patrul.getStatus() ) )
                                            .type( FIND_FACE_CAR.name() )
                                            .build() ) ) );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetFaceEvents()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventFace -> super.getFunction().apply(
                            Map.of( "message", "U have " + FIND_FACE_PERSON.name() + " Task",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new ActiveTask( eventFace, patrul.getStatus() ) )
                                            .type( FIND_FACE_PERSON.name() )
                                            .build() ) ) );

            case ESCORT -> CassandraDataControlForEscort
                    .getInstance()
                    .getGetCurrentTupleOfEscort()
                    .apply( patrul.getTaskId() )
                    .flatMap( escortTuple -> super.getFunction().apply(
                            Map.of( "message", "U have " + ESCORT.name() + " Task",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new ActiveTask( escortTuple, patrul.getStatus() ) )
                                            .type( ESCORT.name() )
                                            .build() ) ) );

            default -> super.getFunction().apply(
                    Map.of( "message", "U have no any Task",
                            "code", 201,
                            "success", false ) ); };

    private final Function< Patrul, Mono< ApiResponseModel > > removePatrulFromTask = patrul -> switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCard102()
                    .apply( patrul.getTaskId() )
                    .flatMap( card -> super.getFunction().apply(
                            Map.of( "message", this.changeTaskStatus( patrul, CANCEL, card )
                                    .getName() + " was removed from " + card.getCardId() ) ) );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventCar()
                    .apply( patrul.getTaskId() )
                    .flatMap( card -> super.getFunction().apply(
                            Map.of( "message", this.changeTaskStatus( patrul, CANCEL, card )
                                    .getName() + " was removed from " + card.getId() ) ) );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventBody()
                    .apply( patrul.getTaskId() )
                    .flatMap( card -> super.getFunction().apply(
                            Map.of( "message", this.changeTaskStatus( patrul, CANCEL, card )
                                    .getName() + " was removed from " + card.getId() ) ) );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventFace()
                    .apply( patrul.getTaskId() )
                    .flatMap( card -> super.getFunction().apply(
                            Map.of( "message", this.changeTaskStatus( patrul, CANCEL, card )
                                    .getName() + " was removed from " + card.getId() ) ) );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCarEvents()
                    .apply( patrul.getTaskId() )
                    .flatMap( carEvent -> super.getFunction().apply(
                            Map.of( "message", this.changeTaskStatus( patrul, CANCEL, carEvent )
                                    .getName() + " was removed from " + carEvent.getId() ) ) );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetFaceEvents()
                    .apply( patrul.getTaskId() )
                    .flatMap( faceEvent -> super.getFunction().apply(
                            Map.of( "message", this.changeTaskStatus( patrul, CANCEL, faceEvent )
                                    .getName() + " was removed from " + faceEvent.getId() ) ) );

            default -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetSelfEmploymentTask()
                    .apply( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> super.getFunction().apply(
                            Map.of( "message", this.changeTaskStatus( patrul, CANCEL, selfEmploymentTask )
                                    .getName() + " was removed from " + selfEmploymentTask.getUuid() ) ) ); };

    private final Function< Patrul, Mono< ApiResponseModel > > getTaskDetails = patrul -> switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCard102()
                    .apply( patrul.getTaskId() )
                    .flatMap( card -> super.getFunction().apply(
                            Map.of( "message", "Your task details",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new CardDetails( card, patrul, "ru" ) )
                                            .type( CARD_102.name() )
                                            .build() ) ) );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventBody()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventBody -> super.getFunction().apply(
                            Map.of( "message", "Your task details",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new CardDetails( new PersonDetails( eventBody ) ) )
                                            .type( FIND_FACE_PERSON.name() )
                                            .build() ) ) );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventFace()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventFace -> super.getFunction().apply(
                            Map.of( "message", "Your task details",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new CardDetails( new PersonDetails( eventFace ) ) )
                                            .type( FIND_FACE_PERSON.name() )
                                            .build() ) ) );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventCar()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventCar -> super.getFunction().apply(
                            Map.of( "message", "Your task details",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new CardDetails( new CarDetails( eventCar ) ) )
                                            .type( FIND_FACE_CAR.name() )
                                            .build() ) ) );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCarEvents()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventCar -> super.getFunction().apply(
                            Map.of( "message", "Your task details",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new CardDetails( new CarDetails( eventCar ) ) )
                                            .type( FIND_FACE_CAR.name() )
                                            .build() ) ) );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetFaceEvents()
                    .apply( patrul.getTaskId() )
                    .flatMap( faceEvent -> super.getFunction().apply(
                            Map.of( "message", "Your task details",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                            .builder()
                                            .data( new CardDetails( new PersonDetails( faceEvent ) ) )
                                            .type( FIND_FACE_PERSON.name() )
                                            .build() ) ) );

            case ESCORT -> CassandraDataControlForEscort
                    .getInstance()
                    .getGetCurrentTupleOfEscort()
                    .apply( patrul.getTaskId() )
                    .flatMap( escortTuple -> CassandraDataControlForEscort
                            .getInstance()
                            .getGetCurrentTupleOfCar()
                            .apply( escortTuple.getTupleOfCarsList()
                                    .get( escortTuple
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
                    .getGetSelfEmploymentTask()
                    .apply( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> super.getFunction().apply(
                            Map.of( "message", "Your task details",
                                    "data", com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                            .data( new CardDetails( selfEmploymentTask, "ru", patrul ) )
                                            .type( ESCORT.name() )
                                            .build() ) ) );

            default -> super.getFunction().apply(
                    Map.of( "message", "You have no tasks",
                            "success", false,
                            "code", 201 ) ); };
}