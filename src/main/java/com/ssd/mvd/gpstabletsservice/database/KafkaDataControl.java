package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.task.sos_task.SosNotificationForAndroid;
import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.ActiveTask;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.sos_task.SosNotification;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;
import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;
import com.ssd.mvd.gpstabletsservice.entity.Notification;

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
import java.util.*;

@lombok.Data
public final class KafkaDataControl extends SerDes {
    private static KafkaDataControl instance = new KafkaDataControl();

    private final String CAR_TOTAL_DATA = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.KAFKA_VARIABLES.KAFKA_TOPICS.CAR_TOTAL_DATA" );

    private final String ACTIVE_TASK = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.KAFKA_VARIABLES.KAFKA_TOPICS.ACTIVE_TASK" );

    private final String NOTIFICATION = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.KAFKA_VARIABLES.KAFKA_TOPICS.NOTIFICATION" );

    private final String SOS_TOPIC = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.KAFKA_VARIABLES.KAFKA_TOPICS.SOS_TOPIC" );

    private final String SOS_TOPIC_FOR_ANDROID_NOTIFICATION = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.KAFKA_VARIABLES.KAFKA_TOPICS.SOS_TOPIC_FOR_ANDROID_NOTIFICATION" );

    public static KafkaDataControl getInstance () { return instance != null ? instance : ( instance = new KafkaDataControl() ); }

    private final Supplier< Map< String, Object > > getKafkaSenderOptions = () -> Map.of(
            ProducerConfig.ACKS_CONFIG, "1",
            ProducerConfig.CLIENT_ID_CONFIG, GpsTabletsServiceApplication
                    .context
                    .getEnvironment()
                    .getProperty( "variables.KAFKA_VARIABLES.GROUP_ID_FOR_KAFKA" ),
            ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, GpsTabletsServiceApplication
                    .context
                    .getEnvironment()
                    .getProperty( "variables.KAFKA_VARIABLES.KAFKA_BROKER" ),
            ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class,
            ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class );

    private final KafkaSender< String, String > kafkaSender = KafkaSender.create(
            SenderOptions.< String, String >create( this.getGetKafkaSenderOptions().get() )
                    .maxInFlight( 1024 ) );

    private KafkaDataControl () { super.logging( "KafkaDataControl was created" ); }

    private final Consumer< ActiveTask > writeActiveTaskToKafka = activeTask -> this.getKafkaSender()
            .createOutbound()
            .send( Mono.just( new ProducerRecord<>( this.getACTIVE_TASK(), super.serialize( activeTask ) ) ) )
            .then()
            .doOnError( super::logging )
            .doOnSuccess( success -> super.logging( "activeTask: " +
                    activeTask.getTaskId() + " was sent at: "
                    + TimeInspector
                    .getInspector()
                    .getGetNewDate() ) )
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
                                    super.logging( "Sending sos notification to: "
                                            + sosNotificationForAndroid.getPatrulPassportSeries()
                                            + " at: " + TimeInspector
                                            .getInspector()
                                            .getGetNewDate() );
                                    return new ProducerRecord<>(
                                            this.getSOS_TOPIC_FOR_ANDROID_NOTIFICATION(),
                                            super.serialize( sosNotificationForAndroid ) ); } ) )
                        .then()
                        .doOnError( error -> super.logging( error.getMessage() ) )
                        .doOnSuccess( success -> super.logging( "All notifications were sent" ) )
                        .subscribe();
                return Mono.just( apiResponseModel ); };

    // отправляет уведомление фронту
    private final Function< SosNotification, String > writeSosNotificationToKafka = sosNotification -> {
            this.getKafkaSender()
                    .createOutbound()
                    .send( Mono.just( new ProducerRecord<>( this.getSOS_TOPIC(), super.serialize( sosNotification ) ) ) )
                    .then()
                    .doOnError( error -> super.logging( error.getMessage() ) )
                    .doOnSuccess( success -> super.logging( "sosNotification from: "
                            + sosNotification.getPatrulUUID() + " was sent to front end"
                            + " at: " + TimeInspector
                            .getInspector()
                            .getGetNewDate() ) )
                    .subscribe();
            return "Sos was saved successfully"; };

    private final Function< CarTotalData, CarTotalData > writeCarTotalDataToKafka = carTotalData -> {
            this.getKafkaSender()
                    .createOutbound()
                    .send( Mono.just( new ProducerRecord<>( this.getCAR_TOTAL_DATA(), super.serialize( carTotalData ) ) ) )
                    .then()
                    .doOnError( error -> super.logging( error.getMessage() ) )
                    .doOnSuccess( success -> super.logging( "Kafka got carTotalData : "
                            + carTotalData.getGosNumber()
                            + " at: " + TimeInspector
                            .getInspector()
                            .getGetNewDate() ) )
                    .subscribe();
            return carTotalData; };

    private final Consumer< Notification > writeNotificationToKafka = notification ->
            this.getKafkaSender()
                    .createOutbound()
                    .send( Mono.just( new ProducerRecord<>( this.getNOTIFICATION(), super.serialize( notification ) ) ) )
                    .then()
                    .doOnError( error -> super.logging( error.getMessage() ) )
                    .doOnSuccess( success -> super.logging( "Kafka got notification: "
                            + notification.getTitle()
                            + " for: " + notification.getPassportSeries()
                            + " at: " + notification.getNotificationWasCreated() ) )
                    .subscribe();

    public void clear () {
        instance = null;
        this.getKafkaSender().close();
        super.logging( "Kafka is closed successfully" ); }
}