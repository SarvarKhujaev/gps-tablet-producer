package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.response.Status;
import lombok.Data;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.ResultSetFuture;

import com.ssd.mvd.gpstabletsservice.task.card.CardDetails;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventCar;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventBody;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromShamsiddin.EventFace;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.CarEvents;
import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.face_events.FaceEvents;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;

@Data
public class CassandraDataControlForTasks {
    private final Session session = CassandraDataControl.getInstance().getSession();
    private final Cluster cluster = CassandraDataControl.getInstance().getCluster();

    private final String dbName = "TABLETS";

    private final String faceCar = "faceCar";
    private final String eventCar = "eventCar";
    private final String eventFace = "eventFace";
    private final String eventBody = "eventBody";
    private final String facePerson = "facePerson";
    private final String carTotalData = "carTotalData";

    private final String reportForCard = "REPORT_FOR_CARD";
    private final String selfEmployment = "SELFEMPLOYMENT";
    private final String violationListType = "VIOLATION_LIST_TYPE";

    private static CassandraDataControlForTasks cassandraDataControl = new CassandraDataControlForTasks();

    private final Logger logger = Logger.getLogger( CassandraDataControl.class.toString() );

    public static CassandraDataControlForTasks getInstance() { return cassandraDataControl != null ? cassandraDataControl
            : ( cassandraDataControl = new CassandraDataControlForTasks() ); }

    private CassandraDataControlForTasks () {
        this.session.execute(
                "CREATE TABLE IF NOT EXISTS "
                        + this.dbName + "." + this.getCarTotalData()
                        + "( gosnumber text PRIMARY KEY, " +
                        "cameraImage text, " +
                        "violationsInformationsList list< frozen <" +
                        this.getViolationListType() + "> >, " +
                        "object text );" );

        this.session.execute(
                "CREATE TABLE IF NOT EXISTS "
                        + this.dbName + "." + this.getEventBody()
                        + "( id text PRIMARY KEY, object text );" );

        this.session.execute(
                "CREATE TABLE IF NOT EXISTS "
                        + this.dbName + "." + this.getEventFace()
                        + "( id text PRIMARY KEY, object text );" );

        this.session.execute(
                "CREATE TABLE IF NOT EXISTS "
                        + this.dbName + "." + this.getEventCar()
                        + "( id text PRIMARY KEY, object text );" );

        this.session.execute(
                "CREATE TABLE IF NOT EXISTS "
                        + this.dbName + "." + this.getFaceCar()
                        + "( id text PRIMARY KEY, object text );" );

        this.session.execute(
                "CREATE TABLE IF NOT EXISTS "
                        + this.dbName + "." + this.getSelfEmployment()
                        + "( id uuid PRIMARY KEY, object text );" );

        this.session.execute(
                "CREATE TABLE IF NOT EXISTS "
                        + this.dbName + "." + this.getFacePerson()
                        + "( id text PRIMARY KEY, object text );" );

        this.logger.info("Starting CassandraDataControl for tasks" ); }

    public Boolean addValue ( CarTotalData carTotalData ) { return this.session
            .execute( "INSERT INTO "
                    + this.dbName + "." + this.carTotalData
                    + "( gosnumber, cameraImage, violationsInformationsList, object ) VALUES('"
                    + carTotalData.getGosNumber() + "', '"
                    + carTotalData.getCameraImage() + "', "
                    + CassandraConverter
                    .getInstance()
                    .convertListOfViolationsToCassandra( carTotalData.getViolationsList().getViolationsInformationsList() )
                    + ", '" + SerDes.getSerDes().serialize( carTotalData ) + "');" ).wasApplied(); }

    public Mono< ApiResponseModel > getAllCarTotalData () {
        return Flux.fromStream(
                        this.session.execute(
                                        "SELECT * FROM "
                                                + this.dbName + "." + this.carTotalData + ";" )
                                .all().stream() )
                .map( row -> SerDes
                        .getSerDes()
                        .deserializeCarTotalData( row.getString( "object" ) ) )
                .collectList()
                .flatMap( carTotalData1 -> Mono.just(
                        ApiResponseModel
                                .builder()
                                .data( com.ssd.mvd.gpstabletsservice.entity.Data
                                                .builder()
                                                .data( carTotalData1 )
                                                .build()
                                ).status( Status.builder()
                                                .message( "CarTotalDataList" )
                                                .code( 200 )
                                                .build()
                                ).build() ) ); }

    public Mono< ApiResponseModel > getWarningCarDetails ( String gosnumber ) { return Mono.just(
            ApiResponseModel.builder()
                    .success( true )
                    .status( com.ssd.mvd.gpstabletsservice.response.Status.builder()
                            .message( "Warning car details" )
                            .code( 200 )
                            .build() )
                    .data( com.ssd.mvd.gpstabletsservice.entity.Data
                            .builder()
                            .data( new CardDetails(
                                    SerDes.getSerDes().deserializeCarTotalData(
                                            this.session
                                                    .execute(
                                                            "select * FROM "
                                                                    + this.dbName + "." + this.carTotalData
                                                                    + " WHERE gosnumber = '" + gosnumber + "';"
                                                    ).one().getString( "object" ) ) ) )
                            .build() ).build() ); }

    public List< ViolationsInformation > getViolationsInformationList ( String gosnumber ) { return this.session
            .execute(
                    "SELECT * FROM "
                            + this.dbName + "." + this.getCarTotalData()
                            + " WHERE gosnumber = '" + gosnumber + "';"
            ).one().getList( "violationsInformationsList", ViolationsInformation.class ); }

    public Mono< EventBody > getEventBody ( String id ) { return Mono.just(
            SerDes.getSerDes().deserializeEventBody(
                    this.session.execute(
                            "select * from "
                                    + this.dbName + "." + this.getEventBody()
                                    + " where id = '" + id + "';"
                    ).one().getString( "object" ) ) ); }

    public Mono< EventFace > getEventFace ( String id ) { return Mono.just(
            SerDes.getSerDes().deserializeEventFace(
                    this.session.execute(
                            "select * from "
                                    + this.dbName + "." + this.eventFace
                                    + " where id = '" + id + "';"
                    ).one().getString( "object" ) ) ); }

    public Mono< EventCar > getEventCar ( String id ) { return Mono.just(
            SerDes.getSerDes().deserializeEventCar(
                    this.session.execute(
                            "select * from "
                                    + this.dbName + "." + this.eventCar
                                    + " where id = '" + id + "';"
                    ).one().getString( "object" ) ) ); }

    public Mono< FaceEvents > getFaceEvents ( String id ) { return Mono.just(
            SerDes.getSerDes().deserializeFaceEvents(
                    this.session.execute(
                            "select * from "
                                    + this.dbName + "." + this.facePerson
                                    + " where id = '" + id + "';"
                    ).one().getString( "object" ) ) ); }

    public Mono< CarEvents > getCarEvents ( String id ) { return Mono.just(
            SerDes.getSerDes().deserializeCarEvents(
                    this.session.execute(
                            "select * from "
                                    + this.dbName + "." + this.faceCar
                                    + " where id = '" + id + "';"
                    ).one().getString( "object" ) ) ); }

    public Boolean addValue ( EventCar eventCar ) { return this.session
            .executeAsync( "INSERT INTO "
            + this.dbName + "." + this.eventCar
            + "( id, camera, matched, date, confidence, object ) VALUES('"
            + eventCar.getId() + "', "
            + eventCar.getCamera() + ", "
            + eventCar.getMatched() + ", '"
            + eventCar.getCreated_date().toInstant() + "', "
            + eventCar.getConfidence() + ", '"
            + SerDes.getSerDes().serialize( eventCar ) + "');" ).isDone(); }

    public Boolean addValue ( EventFace eventFace ) { return this.session
            .executeAsync( "INSERT INTO "
            + this.dbName + "." + this.eventFace
            + "( id, camera, matched, date, confidence, object ) VALUES('"
            + eventFace.getId() + "', "
            + eventFace.getCamera() + ", "
            + eventFace.getMatched() + ", '"
            + eventFace.getCreated_date().toInstant() + "', "
            + eventFace.getConfidence() + ", '"
            + SerDes.getSerDes().serialize( eventFace ) + "');" ).isDone(); }

    public Boolean addValue ( EventBody eventBody ) { return this.session
            .executeAsync( "INSERT INTO "
            + this.dbName + "." + this.eventBody
            + "( id, camera, matched, date, confidence, object ) VALUES('"
            + eventBody.getId() + "', "
            + eventBody.getCamera() + ", "
            + eventBody.getMatched() + ", '"
            + eventBody.getCreated_date().toInstant() + "', "
            + eventBody.getConfidence() + ", '"
            + SerDes.getSerDes().serialize( eventBody ) + "');" ).isDone(); }

    public ResultSetFuture addValue ( CarEvents carEvents ) { return this.session
            .executeAsync( "INSERT INTO "
                    + this.dbName + "." + this.faceCar
                    + "(id, object) VALUES ('"
                    + carEvents.getId() + "', '"
                    + SerDes.getSerDes().serialize( carEvents ) + "');" ); }

    public ResultSetFuture addValue ( FaceEvents faceEvents ) { return this.session.executeAsync( "INSERT INTO "
            + this.dbName + "." + this.facePerson
            + "(id, object) VALUES ('"
            + faceEvents.getId() + "', '"
            + SerDes.getSerDes().serialize( faceEvents ) + "');" ); }

    public Flux< SelfEmploymentTask > getSelfEmploymentTasks () { return Flux.fromStream(
                    this.session.execute(
                            "select * from "
                                    + this.dbName + "." + this.selfEmployment + ";"
                    ).all().stream() )
            .map( row -> SerDes.getSerDes()
                    .deserializeSelfEmploymentTask( row.getString( "object" ) ) ); }

    public Boolean addValue ( SelfEmploymentTask selfEmploymentTask ) { return this.session
            .executeAsync( "INSERT INTO "
                    + this.dbName + "." + this.selfEmployment +
                    " ( id, object ) VALUES("
                    + selfEmploymentTask.getUuid() + ", '"
                    + SerDes.getSerDes().serialize( selfEmploymentTask )
                        + "');" ).isDone(); }

    public Mono< SelfEmploymentTask > getSelfEmploymentTask ( UUID id ) { return Mono.just(
                    this.session.execute(
                            "select * from "
                                    + this.dbName + "." + this.selfEmployment
                                    + " where id = " + id + ";"
                    ).one() )
            .map( row -> SerDes.getSerDes()
                    .deserializeSelfEmploymentTask( row.getString( "object" ) ) ); }
}
