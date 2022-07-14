package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.payload.ReqLocationExchange;
import com.ssd.mvd.gpstabletsservice.entity.Notification;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.task.card.Card;
import com.ssd.mvd.gpstabletsservice.entity.Data;

import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.apache.kafka.clients.admin.AdminClient;

import org.jetbrains.annotations.NotNull;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

public class KafkaDataControl {
    private Properties properties;
    private final AdminClient client;
    private final KafkaTemplate< String, String > kafkaTemplate;
    private static KafkaDataControl instance = new KafkaDataControl();
    private final Logger logger = Logger.getLogger( KafkaDataControl.class.toString() );
    @Value( "${KAFKA_BROKER}")
    public String PATH;
    @Value( "${GROUP_ID_FOR_KAFKA}" )
    public String ID;

    private Properties setProperties () {
        this.properties = new Properties();
        this.properties.put( AdminClientConfig.CLIENT_ID_CONFIG, this.ID );
        this.properties.put( AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.PATH );
        return properties; }

    public String getNewTopic ( String imei ) {
        this.client.createTopics( Collections.singletonList( TopicBuilder.name( imei ).partitions(5 ).replicas(3 ).build() ) );
        this.logger.info( "Topic: " + imei + " was created" );
        return imei; }

    public static KafkaDataControl getInstance () { return instance != null ? instance : ( instance = new KafkaDataControl() ); }

    private KafkaDataControl () {
        this.kafkaTemplate = this.kafkaTemplate();
        this.logger.info( "KafkaDataControl was created" );
        this.client = KafkaAdminClient.create( this.setProperties() );
        this.getNewTopic( Status.NOTIFICATION.name().toLowerCase() );
        this.getNewTopic( Status.CARD_FINAL.name().toLowerCase() );
        this.getNewTopic( Status.NEW_CARS.name().toLowerCase() );
        this.getNewTopic( "GpsTabletsData" ); }

    private KafkaTemplate< String, String > kafkaTemplate () {
        Map< String, Object > map = new HashMap<>();
        map.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.PATH );
        map.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class );
        map.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class );
        return new KafkaTemplate<>( new DefaultKafkaProducerFactory<>( map ) ); }

    public Card writeToKafka ( Card card ) {
        this.kafkaTemplate.send( Status.CARD_FINAL.name().toLowerCase(), SerDes.getSerDes().serialize( card ) ).addCallback( new ListenableFutureCallback<>() {
            @Override
            public void onFailure( @NotNull Throwable ex ) { logger.warning("Kafka does not work since: " + LocalDateTime.now() ); }

            @Override
            public void onSuccess( SendResult< String, String > result ) { logger.info("Kafka got Card: " + card.getId() + " with offset: " + result.getRecordMetadata().offset() ); }
        } ); return card; }

    public Data writeToKafka ( Data data ) {
        this.kafkaTemplate.send( Status.NEW_CARS.name().toLowerCase(), SerDes.getSerDes().serialize( data ) ).addCallback( new ListenableFutureCallback<>() {
            @Override
            public void onFailure( @NotNull Throwable ex ) { logger.warning("Kafka does not work since: " + LocalDateTime.now() ); }

            @Override
            public void onSuccess( SendResult< String, String > result ) { logger.info("Kafka got Data: " + data.getType() + " with offset: " + result.getRecordMetadata().offset() ); }
        } ); return null; }

    public void writeToKafka ( ReqLocationExchange trackers ) {
        this.kafkaTemplate.send( "GpsTabletsData", SerDes.getSerDes().serialize( trackers ) ).addCallback( new ListenableFutureCallback<>() {
            @Override
            public void onFailure( @NotNull Throwable ex ) { logger.warning("Kafka does not work since: " + LocalDateTime.now() ); }

            @Override
            public void onSuccess( SendResult< String, String > result ) { logger.info("Kafka got: " + trackers.getDate() + " with offset: " + result.getRecordMetadata().offset() ); }
        } ); }

    public Notification writeToKafka ( Notification notification ) {
        this.kafkaTemplate.send( Status.NOTIFICATION.name().toLowerCase(), SerDes.getSerDes().serialize( notification ) ).addCallback( new ListenableFutureCallback<>() {
            @Override
            public void onFailure( @NotNull Throwable ex ) { logger.warning("Kafka does not work since: " + LocalDateTime.now() ); }

            @Override
            public void onSuccess( SendResult< String, String > result ) { logger.info("Kafka got: " + notification.getTitle() + " with offset: " + result.getRecordMetadata().offset() ); }
        } ); return notification; }

    public void clear () {
            CassandraDataControl.getInstance().delete();
            RedisDataControl.getRedis().clear();
            Archive.getAchieve().clear();
            this.kafkaTemplate.destroy();
            this.kafkaTemplate.flush();
            this.properties.clear();
            this.client.close();
            instance = null; }
}
