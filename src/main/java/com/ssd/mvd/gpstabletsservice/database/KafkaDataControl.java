package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.task.sos_task.SosNotificationForAndroid;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.sos_task.SosNotification;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.entity.Notification;
import com.ssd.mvd.gpstabletsservice.constants.Status;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;

import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.KafkaSender;

import reactor.core.scheduler.Schedulers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import lombok.Data;
import java.util.*;

@Data
public class KafkaDataControl {
    private static KafkaDataControl instance = new KafkaDataControl();
    private final Logger logger = Logger.getLogger( KafkaDataControl.class.toString() );

    private final String KAFKA_BROKER = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.KAFKA_BROKER" );

    private final String GROUP_ID_FOR_KAFKA = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.GROUP_ID_FOR_KAFKA" );

    private final String CAR_TOTAL_DATA = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.CAR_TOTAL_DATA" );

    private final String ACTIVE_TASK = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.ACTIVE_TASK" );

    private final String NOTIFICATION = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.NOTIFICATION" );

    private final String SOS_TOPIC = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.SOS_TOPIC" );

    private final String SOS_TOPIC_FOR_ANDROID_NOTIFICATION = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.SOS_TOPIC_FOR_ANDROID_NOTIFICATION" );

    public static KafkaDataControl getInstance () { return instance != null ? instance : ( instance = new KafkaDataControl() ); }

    private final Supplier< Map< String, Object > > getKafkaSenderOptions = () -> Map.of(
            ProducerConfig.ACKS_CONFIG, "1",
            ProducerConfig.CLIENT_ID_CONFIG, this.getGROUP_ID_FOR_KAFKA(),
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.getKAFKA_BROKER(),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class );

    private final KafkaSender< String, String > kafkaSender = KafkaSender.create(
            SenderOptions.< String, String >create( this.getGetKafkaSenderOptions().get() )
                    .maxInFlight( 1024 ) );

    private KafkaDataControl () { this.getLogger().info( "KafkaDataControl was created" ); }

    private final Consumer< ActiveTask > writeActiveTaskToKafka = activeTask -> this.getKafkaSender()
            .createOutbound()
            .send( Mono.just( new ProducerRecord<>( this.getACTIVE_TASK(),
                        SerDes
                            .getSerDes()
                            .serialize( activeTask ) ) ) )
            .then()
            .doOnError( error -> logger.info( error.getMessage() ) )
            .doOnSuccess( success -> logger.info( "activeTask: " +
                    activeTask.getTaskId() +
                    " was sent at: " + new Date() ) )
            .subscribe();

    // отправляет уведомление андроидам радом с тем кто отправил сос сигнал
    private final BiFunction< Flux< SosNotificationForAndroid >, ApiResponseModel, Mono< ApiResponseModel > > save =
            ( sosNotificationForAndroidFlux, apiResponseModel ) -> {
                this.getKafkaSender()
                        .createOutbound()
                        .send( sosNotificationForAndroidFlux
                                .parallel()
                                .runOn( Schedulers.parallel() )
                                .map( sosNotificationForAndroid -> {
                                    logger.info( "Sending sos notification to: "
                                            + sosNotificationForAndroid.getPatrulPassportSeries()
                                            + " at: " + new Date() );
                                    return new ProducerRecord<>(
                                            this.getSOS_TOPIC_FOR_ANDROID_NOTIFICATION(),
                                            SerDes.getSerDes().serialize( sosNotificationForAndroid ) ); } ) )
                        .then()
                        .doOnError( error -> logger.info( error.getMessage() ) )
                        .doOnSuccess( success -> logger.info( "All notifications were sent" ) )
                        .subscribe();
                return Mono.just( apiResponseModel ); };

    // отправляет уведомление фронту
    private final Function< SosNotification, String > writeSosNotificationToKafka = sosNotification -> {
        this.getKafkaSender()
                .createOutbound()
                .send( Mono.just( new ProducerRecord<>(
                        this.getSOS_TOPIC(),
                        SerDes.getSerDes().serialize( sosNotification ) ) ) )
                .then()
                .doOnError( error -> logger.info( error.getMessage() ) )
                .doOnSuccess( success -> logger.info( "sosNotification from: "
                        + sosNotification.getPatrulUUID() + " was sent to front end"
                        + " at: " + new Date() ) )
                .subscribe();
        return "Sos was saved successfully"; };

    private final Function< CarTotalData, CarTotalData > writeCarTotalDataToKafka = carTotalData -> {
        this.getKafkaSender()
                .createOutbound()
                .send( Mono.just( new ProducerRecord<>(
                        this.getCAR_TOTAL_DATA(),
                        SerDes.getSerDes().serialize( carTotalData ) ) ) )
                .then()
                .doOnError( error -> logger.info( error.getMessage() ) )
                .doOnSuccess( success -> logger.info( "Kafka got carTotalData : "
                        + carTotalData.getGosNumber()
                        + " at: " + new Date() ) )
                .subscribe();
        return carTotalData; };

    private final Consumer< Notification > writeNotificationToKafka = notification ->
        this.getKafkaSender()
                .createOutbound()
                .send( Mono.just( new ProducerRecord<>(
                        this.getNOTIFICATION(),
                        SerDes
                                .getSerDes()
                                .serialize( notification ) ) ) )
                .then()
                .doOnError( error -> logger.info( error.getMessage() ) )
                .doOnSuccess( success -> logger.info( "Kafka got notification: "
                        + notification.getTitle()
                        + " at: " + notification.getNotificationWasCreated() ) )
                .subscribe();

    public void clear () {
        instance = null;
        this.getKafkaSender().close();
        CassandraDataControl.getInstance().delete();
        this.getLogger().info( "Kafka is closed successfully" ); }
}