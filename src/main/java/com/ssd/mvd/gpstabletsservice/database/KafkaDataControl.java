package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.CarTotalData;
import com.ssd.mvd.gpstabletsservice.task.sos_task.SosNotification;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import com.ssd.mvd.gpstabletsservice.entity.Notification;

import com.ssd.mvd.gpstabletsservice.task.sos_task.SosNotificationForAndroid;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.AdminClient;

import org.jetbrains.annotations.NotNull;
import lombok.Data;

import java.util.function.Consumer;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

@Data
public class KafkaDataControl {
    private final AdminClient client;
    private final KafkaTemplate< String, String > kafkaTemplate;
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

    private final String SOS = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.SOS_TOPIC" );

    private final String SOS_TOPIC_FOR_ANDROID_NOTIFICATION = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.SOS_TOPIC_FOR_ANDROID_NOTIFICATION" );

    private Properties setProperties () {
        Properties properties = new Properties();
        properties.put( AdminClientConfig.CLIENT_ID_CONFIG, this.getGROUP_ID_FOR_KAFKA() );
        properties.put( AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.getKAFKA_BROKER() );
        return properties; }

    public void getNewTopic ( String imei ) {
        this.getClient()
                .createTopics( Collections.singletonList( TopicBuilder
                        .name( imei )
                        .partitions(5 )
                        .replicas(3 )
                        .build() ) );
        this.logger.info( "Topic: " + imei + " was created" ); }

    public static KafkaDataControl getInstance () { return instance != null ? instance : ( instance = new KafkaDataControl() ); }

    private KafkaTemplate< String, String > kafkaTemplate () {
        Map< String, Object > map = new HashMap<>();
        map.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.getKAFKA_BROKER() );
        map.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class );
        map.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class );
        return new KafkaTemplate<>( new DefaultKafkaProducerFactory<>( map ) ); }

    private KafkaDataControl () {
        this.kafkaTemplate = this.kafkaTemplate();
        this.getLogger().info( "KafkaDataControl was created" );
        this.client = KafkaAdminClient.create( this.setProperties() );
        this.getNewTopic( this.getSOS_TOPIC_FOR_ANDROID_NOTIFICATION() );
        this.getNewTopic( this.getCAR_TOTAL_DATA() );
        this.getNewTopic( this.getNOTIFICATION() );
        this.getNewTopic( this.getACTIVE_TASK() );
        this.getNewTopic( this.getSOS() ); }

    public void writeToKafka ( String card ) {
        this.getKafkaTemplate().send( this.getACTIVE_TASK(), card ).addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onFailure( @NotNull Throwable ex ) { logger.warning("Kafka does not work since: "
                    + LocalDateTime.now() ); }

            @Override
            public void onSuccess( SendResult< String, String > result ) { logger.info("Kafka got ActiveTask: "
                    + " with offset: "
                    + result.getRecordMetadata().offset() ); } } ); }

    private final Consumer< SosNotificationForAndroid > writeToKafkaNotificatioForAndroid =
            sosNotificationForAndroid -> this.getKafkaTemplate().send(
                    this.getSOS_TOPIC_FOR_ANDROID_NOTIFICATION(),
                            SerDes
                                .getSerDes()
                                .serialize( sosNotificationForAndroid ) )
                    .addCallback( new ListenableFutureCallback<>() {
                        @Override
                        public void onFailure( @NotNull Throwable ex ) { logger.warning("Kafka does not work since: "
                                + LocalDateTime.now() ); }

                        @Override
                        public void onSuccess( SendResult< String, String > result ) {
                            logger.info("Sos signal got patrul: "
                                    + sosNotificationForAndroid.getPatrulPassportSeries()
                                    + " with offset: "
                                    + result.getRecordMetadata().offset() ); } } );

    public String writeToKafka ( SosNotification sos ) {
        this.getKafkaTemplate().send( this.getSOS(),
                        SerDes
                        .getSerDes()
                        .serialize( sos ) )
                .addCallback( new ListenableFutureCallback<>() {
            @Override
            public void onFailure( @NotNull Throwable ex ) { logger.warning("Kafka does not work since: "
                    + LocalDateTime.now() ); }

            @Override
            public void onSuccess( SendResult< String, String > result ) {
                logger.info("Kafka got Sos signal from: "
                        + sos.getPatrulUUID()
                        + " with offset: "
                        + result.getRecordMetadata().offset() ); } } );
        return "Sos was saved successfully"; }

    public CarTotalData writeToKafka ( CarTotalData card ) {
        this.getKafkaTemplate().send( this.getCAR_TOTAL_DATA(),
                        SerDes
                                .getSerDes()
                                .serialize( card ) )
                .addCallback( new ListenableFutureCallback<>() {
            @Override
            public void onFailure( @NotNull Throwable ex ) { logger.warning("Kafka does not work since: " + LocalDateTime.now() ); }

            @Override
            public void onSuccess( SendResult< String, String > result ) { logger.info("Kafka got CarTotalData: "
                    + " with offset: "
                    + result.getRecordMetadata().offset() ); }
        } ); return card; }

    public void writeToKafka ( Notification notification ) {
        this.getKafkaTemplate().send( this.getNOTIFICATION(), SerDes.getSerDes().serialize( notification ) )
                .addCallback( new ListenableFutureCallback<>() {
            @Override
            public void onFailure( @NotNull Throwable ex ) { logger.warning("Kafka does not work since: " + LocalDateTime.now() ); }

            @Override
            public void onSuccess( SendResult< String, String > result ) { logger.info("Kafka got notification: "
                    + notification.getTitle()
                    + " at: " + notification.getNotificationWasCreated()
                    + " with offset: " + result.getRecordMetadata().offset() ); } } ); }

    public void clear () {
        instance = null;
        this.getClient().close();
        this.getKafkaTemplate().flush();
        CassandraDataControl.getInstance().delete();
        this.getLogger().info( "Kafka is closed successfully" ); }
}
