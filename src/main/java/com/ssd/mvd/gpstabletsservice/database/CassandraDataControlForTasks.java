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

import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.task.card.CardDetails;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.request.TaskTimingRequest;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.task.card.TaskTimingStatistics;
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
        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.CARTOTALDATA.name()
                + "( gosnumber text PRIMARY KEY, "
                + "cameraImage text, "
                + "violationsInformationsList list< frozen <"
                + CassandraTables.VIOLATION_LIST_TYPE.name() + "> >, "
                + "object text );" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.EVENTBODY.name()
                + "( id text PRIMARY KEY, object text );" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.EVENTFACE.name()
                + "( id text PRIMARY KEY, object text );" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.EVENTCAR.name()
                + "( id text PRIMARY KEY, object text );" );

        this.session.execute( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.FACECAR.name()
                + "( id text PRIMARY KEY, object text );" );

        this.session.execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.SELFEMPLOYMENT.name()
                + "( id uuid PRIMARY KEY, object text );" );

        this.session.execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.FACEPERSON.name()
                + "( id text PRIMARY KEY, object text );" );

        this.session.execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + TaskTypes.CARD_102
                + "( id text PRIMARY KEY, object text );" );

        this.session.execute ( "CREATE TABLE IF NOT EXISTS "
                + CassandraTables.TABLETS.name() + "."
                + CassandraTables.ACTIVE_TASK.name()
                + "( id text PRIMARY KEY, object text );" );

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
        System.out.println( "Task: " + id );
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

    public void addValue ( Card card ) { this.session
            .executeAsync( "INSERT INTO "
                + CassandraTables.TABLETS.name() + "."
                    + TaskTypes.CARD_102
                + "(id, object) VALUES ('"
                + card.getCardId() + "', '"
                + SerDes.getSerDes().serialize( card ) + "');" ); }

    public Boolean addValue ( EventCar eventCar ) { return this.session
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

    public Boolean addValue ( EventFace eventFace ) { return this.session
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

    public Boolean addValue ( EventBody eventBody ) { return this.session
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

    public Boolean addValue ( CarTotalData carTotalData ) { return this.session
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

    public ResultSetFuture addValue ( CarEvent carEvents ) { return this.session
            .executeAsync( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.FACECAR.name()
                    + "(id, object) VALUES ('"
                    + carEvents.getId() + "', '"
                    + SerDes.getSerDes().serialize( carEvents ) + "');" ); }

    public ResultSetFuture addValue ( FaceEvent faceEvents ) {
        if ( faceEvents.getCreated_date() == null ) faceEvents.setCreated_date( new Date().toString() );
        return this.session.executeAsync( "INSERT INTO "
            + CassandraTables.TABLETS.name() + "."
                + CassandraTables.FACEPERSON.name()
            + "(id, object) VALUES ('"
            + faceEvents.getId() + "', '"
            + SerDes.getSerDes().serialize( faceEvents ) + "');" ); }

    public void addValue ( String id, ActiveTask activeTask ) { this.session
            .executeAsync( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.ACTIVE_TASK.name()
                    + "(id, object) VALUES ('"
                    + id + "', '"
                    + SerDes.getSerDes().serialize( activeTask ) + "');" ); }

    public Boolean addValue ( SelfEmploymentTask selfEmploymentTask ) { return this.session
            .executeAsync( "INSERT INTO "
                    + CassandraTables.TABLETS.name() + "."
                    + CassandraTables.SELFEMPLOYMENT.name() +
                    " ( id, object ) VALUES("
                    + selfEmploymentTask.getUuid() + ", '"
                    + SerDes.getSerDes().serialize( selfEmploymentTask )
                    + "');" ).isDone(); }

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
                    || TaskTypes.valueOf( row.getString("tasktypes" ) )
                    .compareTo( request.getTaskType() ) == 0 )
            .map( TaskTimingStatistics::new );
}
