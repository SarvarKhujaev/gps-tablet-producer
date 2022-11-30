package com.ssd.mvd.gpstabletsservice.entity;

import lombok.Data;
import java.util.Map;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.ssd.mvd.gpstabletsservice.database.*;
import com.ssd.mvd.gpstabletsservice.task.card.*;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.tuple.EscortTuple;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import static com.ssd.mvd.gpstabletsservice.constants.Status.*;
import static com.ssd.mvd.gpstabletsservice.constants.Status.FREE;
import static com.ssd.mvd.gpstabletsservice.constants.TaskTypes.*;
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

@Data
public final class TaskInspector {
    private static TaskInspector taskInspector;

    public static TaskInspector getInstance () { return taskInspector != null ? taskInspector : new TaskInspector(); }

    private String generateText ( Patrul patrul, Status status ) { return switch ( status ) {
            case ATTACHED -> patrul.getName()
                    + " got new task: " + patrul.getTaskId()
                    + " " + patrul.getTaskTypes();

            case ARRIVED -> patrul.getName()
                    + " : " + patrul.getTaskTypes()
                    + " arrived task location: "
                    + " at: " + new Date();

            case ACCEPTED -> patrul.getName()
                    + " ACCEPTED his task: " + patrul.getTaskId()
                    + " " + patrul.getTaskTypes()
                    + " at: " + new Date();

            case FINISHED -> patrul.getName()
                    + " completed his task "
                    + " at: " + new Date();

            default -> patrul.getName()
                    + " has been canceled from task "
                    + " at: " + new Date(); }; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, Card card ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) {
                    // сохраняем времся выполнения
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                                    .apply( patrul, TimeInspector
                                            .getInspector()
                                            .getGetTimeDifference()
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
                            .getRemove()
                            .accept( card.getCardId().toString() );
                    KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( card ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( CARD_102 );
                patrul.setLatitudeOfTask( card.getLatitude() );
                patrul.setTaskId( card.getCardId().toString() ); // saving card id into patrul object
                patrul.setLongitudeOfTask( card.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> {
                PatrulStatus patrulStatus = new PatrulStatus( patrul );
                card.getPatrulStatuses()
                        .put( patrul.getPassportNumber(), patrulStatus );

                CassandraDataControlForTasks
                        .getInstance()
                        .getSaveTaskTimeStatistics()
                        .accept( new TaskTimingStatistics(
                                patrul,
                                card.getCardId().toString(),
                                CARD_102,
                                patrulStatus ) ); } }
        if ( status.compareTo( CANCEL ) != 0 ) card.getPatruls().put( patrul.getUuid(), patrul );

        CassandraDataControl
                .getInstance()
                .getUpdatePatrulAfterTask()
                .accept( patrul );

        if ( card.getStatus().compareTo( FINISHED ) != 0 )
            CassandraDataControlForTasks
                .getInstance()
                .addValue( card.getCardId().toString(), new ActiveTask( card ) );

        CassandraDataControlForTasks
                .getInstance()
                .getSaveCard102()
                .accept( card );

        KafkaDataControl
                .getInstance()
                .getWriteNotificationToKafka()
                .accept( CassandraDataControl
                            .getInstance()
                            .getSaveNotification()
                        .apply( new Notification(
                                patrul,
                                card,
                                this.generateText( patrul, status ) ) ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EventCar eventCar ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                                    .apply( patrul, TimeInspector
                                            .getInspector()
                                            .getGetTimeDifference()
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
                            .getRemove()
                            .accept( eventCar.getId() );
                    KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( eventCar ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskId( eventCar.getId() ); // saving card id into patrul object
                patrul.setTaskTypes( FIND_FACE_EVENT_CAR );
                patrul.setLatitudeOfTask( eventCar.getLatitude() );
                patrul.setLongitudeOfTask( eventCar.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> {
                PatrulStatus patrulStatus = new PatrulStatus( patrul );
                eventCar.getPatrulStatuses()
                        .put( patrul.getPassportNumber(), patrulStatus );

                CassandraDataControlForTasks
                        .getInstance()
                        .getSaveTaskTimeStatistics()
                        .accept( new TaskTimingStatistics(
                                patrul,
                                eventCar.getId(),
                                FIND_FACE_EVENT_CAR,
                                patrulStatus ) ); } }
        if ( eventCar.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks
                .getInstance()
                .addValue( eventCar.getId(), new ActiveTask( eventCar ) );

        if ( status.compareTo( CANCEL ) != 0 ) eventCar.getPatruls().put( patrul.getUuid(), patrul );

        CassandraDataControl
                .getInstance()
                .getUpdatePatrulAfterTask()
                .accept( patrul );

        CassandraDataControlForTasks
                .getInstance()
                .getSaveEventCar()
                .accept( eventCar );

        KafkaDataControl
                .getInstance()
                .getWriteNotificationToKafka()
                .accept( CassandraDataControl
                        .getInstance()
                        .getSaveNotification()
                        .apply( new Notification(
                                patrul,
                                eventCar,
                                this.generateText( patrul, status ) ) ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EventFace eventFace ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                            .apply( patrul, TimeInspector
                                    .getInspector()
                                    .getGetTimeDifference()
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
                            .getRemove()
                            .accept( eventFace.getId() );
                    KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( eventFace ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskId( eventFace.getId() ); // saving card id into patrul object
                patrul.setTaskTypes( FIND_FACE_EVENT_FACE );
                patrul.setLatitudeOfTask( eventFace.getLatitude() );
                patrul.setLongitudeOfTask( eventFace.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> {
                PatrulStatus patrulStatus = new PatrulStatus( patrul );
                eventFace
                        .getPatrulStatuses()
                        .putIfAbsent( patrul.getPassportNumber(), patrulStatus );

                CassandraDataControlForTasks
                        .getInstance()
                        .getSaveTaskTimeStatistics()
                        .accept( new TaskTimingStatistics(
                                patrul,
                                eventFace.getId(),
                                FIND_FACE_EVENT_FACE,
                                patrulStatus ) ); } }
        if ( status.compareTo( CANCEL ) != 0 ) eventFace.getPatruls().put( patrul.getUuid(), patrul );
        if ( eventFace.getStatus().compareTo( FINISHED ) != 0 )
            CassandraDataControlForTasks
                .getInstance()
                .addValue( eventFace.getId(), new ActiveTask( eventFace ) );

        CassandraDataControlForTasks
                .getInstance()
                .getSaveEventFace()
                .accept( eventFace );

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
                                eventFace,
                                this.generateText( patrul, status ) ) ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EventBody eventBody ) {
        patrul.setStatus( status );
        switch ( patrul.getStatus() ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                            .apply( patrul, TimeInspector
                                    .getInspector()
                                    .getGetTimeDifference()
                                    .apply( patrul.getTaskDate().toInstant() ) );
                    patrul.getListOfTasks()
                            .putIfAbsent( patrul.getTaskId(), FIND_FACE_EVENT_BODY.name() ); }
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
                            .getRemove()
                            .accept( eventBody.getId() );
                    KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( eventBody ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskId( eventBody.getId() ); // saving card id into patrul object
                patrul.setTaskTypes( FIND_FACE_EVENT_BODY );
                patrul.setLatitudeOfTask( eventBody.getLatitude() );
                patrul.setLongitudeOfTask( eventBody.getLongitude() ); }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> {
                PatrulStatus patrulStatus = new PatrulStatus( patrul );
                eventBody.getPatrulStatuses()
                        .putIfAbsent( patrul.getPassportNumber(), patrulStatus );

                CassandraDataControlForTasks
                        .getInstance()
                        .getSaveTaskTimeStatistics()
                        .accept( new TaskTimingStatistics(
                                patrul,
                                eventBody.getId(),
                                FIND_FACE_EVENT_BODY,
                                patrulStatus ) ); } }
        if ( status.compareTo( CANCEL ) != 0 ) eventBody.getPatruls().put( patrul.getUuid(), patrul );
        if ( eventBody.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks
                .getInstance()
                .addValue( eventBody.getId(), new ActiveTask( eventBody ) );

        CassandraDataControlForTasks
                .getInstance()
                .getSaveEventBody()
                .accept( eventBody );

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
                                eventBody,
                                this.generateText( patrul, status ) ) ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, CarEvent carEvents ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                            .apply( patrul, TimeInspector
                                    .getInspector()
                                    .getGetTimeDifference()
                                    .apply( patrul.getTaskDate().toInstant() ) );

                    patrul.getListOfTasks()
                            .putIfAbsent( patrul.getTaskId(), FIND_FACE_CAR.name() ); }
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
                            .getRemove()
                            .accept( carEvents.getId() );
                    KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( carEvents ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskTypes( FIND_FACE_CAR );
                patrul.setTaskId( carEvents.getId() ); // saving card id into patrul object
                if ( carEvents.getDataInfo() != null
                        && carEvents.getDataInfo().getData() != null ) {
                    patrul.setLatitudeOfTask( carEvents.getDataInfo().getData().getLatitude() );
                    patrul.setLongitudeOfTask( carEvents.getDataInfo().getData().getLongitude() ); } }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> {
                PatrulStatus patrulStatus = new PatrulStatus( patrul );
                carEvents.getPatrulStatuses()
                        .putIfAbsent( patrul.getPassportNumber(), patrulStatus );

                CassandraDataControlForTasks
                        .getInstance()
                        .getSaveTaskTimeStatistics()
                        .accept( new TaskTimingStatistics(
                                patrul,
                                carEvents.getId(),
                                FIND_FACE_CAR,
                                patrulStatus ) ); } }
        if ( status.compareTo( CANCEL ) != 0 ) carEvents.getPatruls().put( patrul.getUuid(), patrul );
        if ( carEvents.getStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks
                .getInstance()
                .addValue( carEvents.getId(), new ActiveTask( carEvents ) );

        CassandraDataControlForTasks
                .getInstance()
                .getSaveCarEvent()
                .accept( carEvents );

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
                                carEvents,
                                this.generateText( patrul, status ) ) ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, FaceEvent faceEvent ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                            .apply( patrul, TimeInspector
                                    .getInspector()
                                    .getGetTimeDifference()
                                    .apply( patrul.getTaskDate().toInstant() ) );

                    patrul.getListOfTasks()
                            .putIfAbsent( patrul.getTaskId(), FIND_FACE_PERSON.name() ); }
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
                            .getRemove()
                            .accept( faceEvent.getId() );
                    KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( faceEvent ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ATTACHED -> {
                patrul.setTaskId( faceEvent.getId() ); // saving card id into patrul object
                patrul.setTaskTypes( FIND_FACE_PERSON );
                if ( faceEvent.getDataInfo() != null
                        && faceEvent.getDataInfo().getData() != null ) {
                    patrul.setLatitudeOfTask( faceEvent.getDataInfo().getData().getLatitude() );
                    patrul.setLongitudeOfTask( faceEvent.getDataInfo().getData().getLongitude() ); } }
            case ACCEPTED -> patrul.setTaskDate( new Date() ); // fixing time when patrul started this task
            case ARRIVED -> {
                PatrulStatus patrulStatus = new PatrulStatus( patrul );
                faceEvent.getPatrulStatuses()
                        .putIfAbsent( patrul.getPassportNumber(), patrulStatus );

                CassandraDataControlForTasks
                        .getInstance()
                        .getSaveTaskTimeStatistics()
                        .accept( new TaskTimingStatistics(
                                patrul,
                                faceEvent.getId(),
                                FIND_FACE_PERSON,
                                patrulStatus ) ); } }
        if ( status.compareTo( CANCEL ) != 0 ) faceEvent.getPatruls().put( patrul.getUuid(), patrul );
        if ( faceEvent.getStatus().compareTo( FINISHED ) != 0 )
            CassandraDataControlForTasks
                .getInstance()
                .addValue( faceEvent.getId(), new ActiveTask( faceEvent ) );

        CassandraDataControlForTasks
                .getInstance()
                .getSaveFaceEvent()
                .accept( faceEvent );

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
                                faceEvent,
                                this.generateText( patrul, status ) ) ) );
        return patrul; }

    public Patrul changeTaskStatus ( Patrul patrul, Status status, EscortTuple escortTuple ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) patrul.getListOfTasks()
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
                                    .getUpdateTupleOfcar()
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

    public Patrul changeTaskStatus ( Patrul patrul, Status status, SelfEmploymentTask selfEmploymentTask ) {
        patrul.setStatus( status );
        switch ( ( patrul.getStatus() ) ) {
            case CANCEL, FINISHED -> {
                if ( status.compareTo( FINISHED ) == 0 ) {
                    CassandraDataControlForTasks
                            .getInstance()
                            .getUpdateTotalTimeConsumption()
                            .apply( patrul, TimeInspector
                                    .getInspector()
                                    .getGetTimeDifference()
                                    .apply( patrul.getTaskDate().toInstant() ) );

                    patrul.getListOfTasks()
                            .putIfAbsent( patrul.getTaskId(), SELF_EMPLOYMENT.name() ); }
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
                            .getRemove()
                            .accept( selfEmploymentTask.getUuid().toString() );

                    KafkaDataControl
                            .getInstance()
                            .getWriteActiveTaskToKafka()
                            .accept( new ActiveTask( selfEmploymentTask ) ); }
                patrul.setTaskTypes( TaskTypes.FREE );
                patrul.setStatus( FREE );
                patrul.setTaskId( null ); }
            case ARRIVED -> {
                patrul.setTaskTypes( SELF_EMPLOYMENT );
                patrul.setTaskId( selfEmploymentTask.getUuid().toString() );
                PatrulStatus patrulStatus = new PatrulStatus( patrul );
                selfEmploymentTask.getPatrulStatuses()
                        .putIfAbsent( patrul.getPassportNumber(), patrulStatus );

                CassandraDataControlForTasks
                        .getInstance()
                        .getSaveTaskTimeStatistics()
                        .accept( new TaskTimingStatistics(
                                patrul,
                                selfEmploymentTask.getUuid().toString(),
                                SELF_EMPLOYMENT,
                                patrulStatus ) ); }
            case ATTACHED, ACCEPTED -> {
                patrul.setTaskTypes( SELF_EMPLOYMENT );
                patrul.setTaskId( selfEmploymentTask.getUuid().toString() ); // saving card id into patrul object
                patrul.setLatitudeOfTask( selfEmploymentTask.getLatOfAccident() );
                patrul.setLongitudeOfTask( selfEmploymentTask.getLanOfAccident() ); } }
        if ( status.compareTo( CANCEL ) != 0 ) selfEmploymentTask.getPatruls().put( patrul.getUuid(), patrul );
        if ( selfEmploymentTask.getTaskStatus().compareTo( FINISHED ) != 0 ) CassandraDataControlForTasks
                .getInstance()
                .addValue( selfEmploymentTask.getUuid().toString(),
                    new ActiveTask( selfEmploymentTask ) );

        CassandraDataControlForTasks
                .getInstance()
                .getSaveSelfEmploymentTask()
                .accept( selfEmploymentTask );

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
                                selfEmploymentTask,
                                this.generateText( patrul, status ) ) ) );
        return patrul; }

    public Mono< ApiResponseModel > getListOfPatrulTasks ( Patrul patrul, Integer page, Integer size ) {
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
                                            .get( this.getReportIndex( card
                                                            .getReportForCardList(),
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
                .flatMap( finishedTasks -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "Your list of tasks",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( finishedTasks )
                                        .build() ) ) ); }

    public Mono< ApiResponseModel > saveReportForTask ( Patrul patrul, ReportForCard reportForCard ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCard102()
                    .apply( patrul.getTaskId() )
                    .flatMap( card -> {
                    card.getReportForCardList().add( reportForCard );
                    return CassandraDataControl
                            .getInstance()
                            .update( TaskInspector
                                    .getInstance()
                                    .changeTaskStatus( patrul, Status.FINISHED, card ) )
                            .flatMap( apiResponseModel -> Archive
                                    .getArchive()
                                    .getFunction()
                                    .apply( Map.of( "message", "Report from: "
                                            + patrul.getName() + " was saved" ) ) ); } );

            case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetSelfEmploymentTask()
                    .apply( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> {
                        selfEmploymentTask.getReportForCards().add( reportForCard );
                        return CassandraDataControl
                                .getInstance()
                                .update( TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul, Status.FINISHED, selfEmploymentTask ) )
                                .flatMap( apiResponseModel -> Archive
                                        .getArchive()
                                        .getFunction()
                                        .apply( Map.of( "message", "Report from: "
                                                + patrul.getName() + " was saved" ) ) ); } );

            case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventBody()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventBody -> {
                        eventBody.getReportForCardList().add( reportForCard );
                        return CassandraDataControl
                                .getInstance()
                                .update( TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul, Status.FINISHED, eventBody ) )
                                .flatMap( apiResponseModel -> Archive
                                        .getArchive()
                                        .getFunction()
                                        .apply( Map.of( "message", "Report from: "
                                                + patrul.getName() + " was saved" ) ) ); } );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventFace()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventFace -> {
                        eventFace.getReportForCardList().add( reportForCard );
                        return CassandraDataControl
                                .getInstance()
                                .update( TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul, Status.FINISHED, eventFace ) )
                                .flatMap( apiResponseModel -> Archive
                                        .getArchive()
                                        .getFunction()
                                        .apply( Map.of( "message", "Report from: "
                                                + patrul.getName() + " was saved" ) ) ); } );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCarEvents()
                    .apply( patrul.getTaskId() )
                    .flatMap( carEvents -> {
                        carEvents.getReportForCardList().add( reportForCard );
                        return CassandraDataControl
                                .getInstance()
                                .update( TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul, Status.FINISHED, carEvents ) )
                                .flatMap( apiResponseModel -> Archive
                                        .getArchive()
                                        .getFunction()
                                        .apply( Map.of( "message", "Report from: "
                                                + patrul.getName() + " was saved" ) ) ); } );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetFaceEvents()
                    .apply( patrul.getTaskId() )
                    .flatMap( faceEvents -> {
                        faceEvents.getReportForCardList().add( reportForCard );
                        return CassandraDataControl
                                .getInstance()
                                .update( TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul, Status.FINISHED, faceEvents ) )
                                .flatMap( apiResponseModel -> Archive
                                        .getArchive()
                                        .getFunction()
                                        .apply( Map.of( "message", "Report from: "
                                                + patrul.getName() + " was saved" ) ) ); } );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventCar()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventCar -> {
                        eventCar.getReportForCardList().add( reportForCard );
                        return CassandraDataControl
                                .getInstance()
                                .update( TaskInspector
                                        .getInstance()
                                        .changeTaskStatus( patrul, Status.FINISHED, eventCar ) )
                                .flatMap( apiResponseModel -> Archive
                                        .getArchive()
                                        .getFunction()
                                        .apply( Map.of( "message", "Report from: "
                                                + patrul.getName() + " was saved" ) ) ); } );

            default -> Archive
                    .getArchive()
                    .getFunction()
                    .apply( Map.of( "message", "U have no tasks, thus u cannot send report",
                            "code", 201,
                            "success", false ) ); }; }

    private Integer getReportIndex ( List< ReportForCard > reportForCardList, UUID uuid ) {
        for ( int i = 0; i < reportForCardList.size(); i++ ) if ( reportForCardList.get( i )
                .getUuidOfPatrul()
                .compareTo( uuid ) == 0 ) return i;
        return 0; }

    public Mono< ApiResponseModel > changeTaskStatus ( Patrul patrul, Status status ) {
        return switch ( patrul.getTaskTypes() ) {
            case CARD_102 -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCard102()
                    .apply( patrul.getTaskId() )
                    .flatMap( card -> Archive
                            .getArchive()
                            .getFunction()
                            .apply( Map.of( "message", "Patrul: " + this.changeTaskStatus( patrul, status, card )
                                    .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl.getInstance().login( patrul, status ) ) ) );

            case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetSelfEmploymentTask()
                    .apply( UUID.fromString( patrul.getTaskId() ) )
                    .flatMap( selfEmploymentTask -> Archive
                            .getArchive()
                            .getFunction()
                            .apply( Map.of( "message", "Patrul: " + this.changeTaskStatus( patrul, status, selfEmploymentTask )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl.getInstance().login( patrul, status ) ) ) );

            case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventCar()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventCar -> Archive
                            .getArchive()
                            .getFunction()
                            .apply( Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, eventCar )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .login( patrul, status ) ) ) );

            case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventFace()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventFace -> Archive
                            .getArchive()
                            .getFunction()
                            .apply( Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, eventFace )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .login( patrul, status ) ) ) );

            case FIND_FACE_PERSON -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetFaceEvents()
                    .apply( patrul.getTaskId() )
                    .flatMap( faceEvents -> Archive
                            .getArchive()
                            .getFunction()
                            .apply( Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, faceEvents )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .login( patrul, status ) ) ) );

            case FIND_FACE_CAR -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetCarEvents()
                    .apply( patrul.getTaskId() )
                    .flatMap( carEvents -> Archive
                            .getArchive()
                            .getFunction()
                            .apply( Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, carEvents )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .login( patrul, status ) ) ) );

            case ESCORT -> CassandraDataControlForEscort
                    .getInstance()
                    .getGetCurrentTupleOfEscort()
                    .apply( patrul.getTaskId() )
                    .flatMap( escortTuple -> Archive
                            .getArchive()
                            .getFunction()
                            .apply( Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, escortTuple )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .login( patrul, status ) ) ) );

            default -> CassandraDataControlForTasks
                    .getInstance()
                    .getGetEventBody()
                    .apply( patrul.getTaskId() )
                    .flatMap( eventBody -> Archive
                            .getArchive()
                            .getFunction()
                            .apply( Map.of( "message", "Patrul: "
                                            + this.changeTaskStatus( patrul, status, eventBody )
                                            .getPassportNumber() + " changed his status task to: " + status,
                                    "success", CassandraDataControl
                                            .getInstance()
                                            .login( patrul, status ) ) ) ); }; }

    private final Function< Patrul, Mono< ApiResponseModel > > getCurrentActiveTask = patrul -> switch ( patrul.getTaskTypes() ) {
        case CARD_102 -> CassandraDataControlForTasks
                .getInstance()
                .getGetCard102()
                .apply( patrul.getTaskId() )
                .flatMap( card -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "U have " + CARD_102.name() + " Task",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new ActiveTask( card, patrul.getStatus() ) )
                                        .type( CARD_102.name() )
                                        .build() ) ) );

        case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                .getInstance()
                .getGetSelfEmploymentTask()
                .apply( UUID.fromString( patrul.getTaskId() ) )
                .flatMap( selfEmploymentTask -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "U have " + SELF_EMPLOYMENT.name() + " Task",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new ActiveTask( selfEmploymentTask, patrul.getStatus() ) )
                                        .type( SELF_EMPLOYMENT.name() )
                                        .build() ) ) );

        case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                .getInstance()
                .getGetEventCar()
                .apply( patrul.getTaskId() )
                .flatMap( eventCar -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "U have " + FIND_FACE_EVENT_CAR.name() + " Task",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new ActiveTask( eventCar, patrul.getStatus() ) )
                                        .type( FIND_FACE_EVENT_CAR.name() )
                                        .build() ) ) );

        case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                .getInstance()
                .getGetEventBody()
                .apply( patrul.getTaskId() )
                .flatMap( eventBody -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "U have " + FIND_FACE_EVENT_BODY.name() + " Task",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new ActiveTask( eventBody, patrul.getStatus() ) )
                                        .type( FIND_FACE_EVENT_BODY.name() )
                                        .build() ) ) );

        case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                .getInstance()
                .getGetEventFace()
                .apply( patrul.getTaskId() )
                .flatMap( eventFace -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "U have " + FIND_FACE_EVENT_FACE.name() + " Task",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new ActiveTask( eventFace, patrul.getStatus() ) )
                                        .type( FIND_FACE_EVENT_FACE.name() )
                                        .build() ) ) );

        case FIND_FACE_CAR -> CassandraDataControlForTasks
                .getInstance()
                .getGetCarEvents()
                .apply( patrul.getTaskId() )
                .flatMap( eventFace -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "U have " + FIND_FACE_CAR.name() + " Task",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new ActiveTask( eventFace, patrul.getStatus() ) )
                                        .type( FIND_FACE_CAR.name() )
                                        .build() ) ) );

        case FIND_FACE_PERSON -> CassandraDataControlForTasks
                .getInstance()
                .getGetFaceEvents()
                .apply( patrul.getTaskId() )
                .flatMap( eventFace -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "U have " + FIND_FACE_PERSON.name() + " Task",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new ActiveTask( eventFace, patrul.getStatus() ) )
                                        .type( FIND_FACE_PERSON.name() )
                                        .build() ) ) );

        case ESCORT -> CassandraDataControlForEscort
                .getInstance()
                .getGetCurrentTupleOfEscort()
                .apply( patrul.getTaskId() )
                .flatMap( escortTuple -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "U have " + ESCORT.name() + " Task",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new ActiveTask( escortTuple, patrul.getStatus() ) )
                                        .type( ESCORT.name() )
                                        .build() ) ) );

        default -> Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", "U have no any Task",
                        "code", 201,
                        "success", false ) ); };

    private final Function< Patrul, Mono< ApiResponseModel > > removePatrulFromTask = patrul -> switch ( patrul.getTaskTypes() ) {
        case CARD_102 -> CassandraDataControlForTasks
                .getInstance()
                .getGetCard102()
                .apply( patrul.getTaskId() )
                .flatMap( card -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", this.changeTaskStatus( patrul, CANCEL, card )
                                .getName() + " was removed from " + card.getCardId() ) ) );

        case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                .getInstance()
                .getGetEventCar()
                .apply( patrul.getTaskId() )
                .flatMap( card -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", this.changeTaskStatus( patrul, CANCEL, card )
                                .getName() + " was removed from " + card.getId() ) ) );

        case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                .getInstance()
                .getGetEventBody()
                .apply( patrul.getTaskId() )
                .flatMap( card -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", this.changeTaskStatus( patrul, CANCEL, card )
                                .getName() + " was removed from " + card.getId() ) ) );

        case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                .getInstance()
                .getGetEventFace()
                .apply( patrul.getTaskId() )
                .flatMap( card -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", this.changeTaskStatus( patrul, CANCEL, card )
                                .getName() + " was removed from " + card.getId() ) ) );

        case FIND_FACE_CAR -> CassandraDataControlForTasks
                .getInstance()
                .getGetCarEvents()
                .apply( patrul.getTaskId() )
                .flatMap( carEvent -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", this.changeTaskStatus( patrul, CANCEL, carEvent )
                                .getName() + " was removed from " + carEvent.getId() ) ) );

        case FIND_FACE_PERSON -> CassandraDataControlForTasks
                .getInstance()
                .getGetFaceEvents()
                .apply( patrul.getTaskId() )
                .flatMap( faceEvent -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", this.changeTaskStatus( patrul, CANCEL, faceEvent )
                                .getName() + " was removed from " + faceEvent.getId() ) ) );

        default -> CassandraDataControlForTasks
                .getInstance()
                .getGetSelfEmploymentTask()
                .apply( UUID.fromString( patrul.getTaskId() ) )
                .flatMap( selfEmploymentTask -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", this.changeTaskStatus( patrul, CANCEL, selfEmploymentTask )
                                .getName() + " was removed from " + selfEmploymentTask.getUuid() ) ) ); };

    private final Function< Patrul, Mono< ApiResponseModel > > getTaskDetails = patrul -> switch ( patrul.getTaskTypes() ) {
        case CARD_102 -> CassandraDataControlForTasks
                .getInstance()
                .getGetCard102()
                .apply( patrul.getTaskId() )
                .flatMap( card -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "Your task details",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new CardDetails( card, patrul, "ru" ) )
                                        .type( CARD_102.name() )
                                        .build() ) ) );

        case FIND_FACE_EVENT_BODY -> CassandraDataControlForTasks
                .getInstance()
                .getGetEventBody()
                .apply( patrul.getTaskId() )
                .flatMap( eventBody -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "Your task details",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new CardDetails( new PersonDetails( eventBody ) ) )
                                        .type( FIND_FACE_PERSON.name() )
                                        .build() ) ) );

        case FIND_FACE_EVENT_FACE -> CassandraDataControlForTasks
                .getInstance()
                .getGetEventFace()
                .apply( patrul.getTaskId() )
                .flatMap( eventFace -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "Your task details",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new CardDetails( new PersonDetails( eventFace ) ) )
                                        .type( FIND_FACE_PERSON.name() )
                                        .build() ) ) );

        case FIND_FACE_EVENT_CAR -> CassandraDataControlForTasks
                .getInstance()
                .getGetEventCar()
                .apply( patrul.getTaskId() )
                .flatMap( eventCar -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "Your task details",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new CardDetails( new CarDetails( eventCar ) ) )
                                        .type( FIND_FACE_CAR.name() )
                                        .build() ) ) );

        case FIND_FACE_CAR -> CassandraDataControlForTasks
                .getInstance()
                .getGetCarEvents()
                .apply( patrul.getTaskId() )
                .flatMap( eventCar -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "Your task details",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data
                                        .builder()
                                        .data( new CardDetails( new CarDetails( eventCar ) ) )
                                        .type( FIND_FACE_CAR.name() )
                                        .build() ) ) );

        case FIND_FACE_PERSON -> CassandraDataControlForTasks
                .getInstance()
                .getGetFaceEvents()
                .apply( patrul.getTaskId() )
                .flatMap( faceEvent -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "Your task details",
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
                        .flatMap( tupleOfCar -> Archive
                                .getArchive()
                                .getFunction()
                                .apply( Map.of( "message", "Your task details",
                                        "data", com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                                .data( new CardDetails( escortTuple, "ru", tupleOfCar ) )
                                                .type( ESCORT.name() )
                                                .build() ) ) ) );

        case SELF_EMPLOYMENT -> CassandraDataControlForTasks
                .getInstance()
                .getGetSelfEmploymentTask()
                .apply( UUID.fromString( patrul.getTaskId() ) )
                .flatMap( selfEmploymentTask -> Archive
                        .getArchive()
                        .getFunction()
                        .apply( Map.of( "message", "Your task details",
                                "data", com.ssd.mvd.gpstabletsservice.entity.Data.builder()
                                        .data( new CardDetails( selfEmploymentTask, "ru", patrul ) )
                                        .type( ESCORT.name() )
                                        .build() ) ) );

        default -> Archive
                .getArchive()
                .getFunction()
                .apply( Map.of( "message", "You have no tasks",
                        "success", false,
                        "code", 201 ) ); };
}