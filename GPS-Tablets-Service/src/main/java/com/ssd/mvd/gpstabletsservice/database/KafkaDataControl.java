package com.ssd.mvd.gpstabletsservice.database;

import com.ssd.mvd.gpstabletsservice.task.selfEmploymentTask.SelfEmploymentTask;
import com.ssd.mvd.gpstabletsservice.payload.ReqLocationExchange;
import com.ssd.mvd.gpstabletsservice.entity.Notification;
import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.task.card.Card;

import org.springframework.util.concurrent.ListenableFutureCallback;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.KafkaStreams;
import reactor.core.publisher.Mono;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Properties;
import java.util.HashMap;
import java.util.Map;

public class KafkaDataControl {
    private Properties properties;
    private final AdminClient client;
    public final String ID = "SSD.GPS.TABLETS";
    private final KafkaTemplate< String, String > kafkaTemplate;
    private static KafkaDataControl instance = new KafkaDataControl();
    private final Logger logger = Logger.getLogger( KafkaDataControl.class.toString() );
    public final String PATH = "10.254.1.209:9092, 10.254.1.211:9092, 10.254.1.212:9092";

    private KafkaStreams kafkaStreams;
    private final StreamsBuilder builder = new StreamsBuilder();

    private Properties setProperties () {
        this.properties = new Properties();
        this.properties.put( AdminClientConfig.CLIENT_ID_CONFIG, this.ID );
        this.properties.put( AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, this.PATH );
        return properties; }

    private Properties setStreamProperties () {
        this.properties.clear();
        this.properties.put( StreamsConfig.APPLICATION_ID_CONFIG, this.ID );
        this.properties.put( StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.PATH );
        this.properties.put( StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        this.properties.put( StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName() );
        return this.properties; }

    public String getNewTopic ( String imei ) {
        this.client.createTopics( Collections.singletonList( TopicBuilder.name( imei ).partitions(5 ).replicas(3 ).build() ) );
        this.logger.info( "Topic: " + imei + " was created" );
        return imei; }

    public static KafkaDataControl getInstance () { return instance != null ? instance : ( instance = new KafkaDataControl() ); }

    private KafkaDataControl () {
        this.kafkaTemplate = this.kafkaTemplate();
        this.logger.info( "KafkaDataControl was created" );
        this.client = KafkaAdminClient.create( this.setProperties() );
        this.getNewTopic( Status.SELF_EMPLOYMENT.name().toLowerCase() );
        this.getNewTopic( Status.NOTIFICATION.name().toLowerCase() );
        this.getNewTopic( Status.CARD_FINAL.name().toLowerCase() );
//        this.start();
    }

    private void start () {
        KStream< String, String > kStream = this.builder.stream( "api_102_card_0.0.3", Consumed.with( Serdes.String(), Serdes.String() ) );
        kStream.peek( ( key, value ) -> this.logger.info( value ) ).mapValues( value -> Archive.getAchieve().save( SerDes.getSerDes().deserializeCard( value ) ) );
        this.kafkaStreams = new KafkaStreams( this.builder.build(), this.setStreamProperties() );
        this.kafkaStreams.start(); }

    private KafkaTemplate< String, String > kafkaTemplate () {
        Map< String, Object > map = new HashMap<>();
        map.put( ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.PATH );
        map.put( ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class );
        map.put( ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringSerializer.class );
        return new KafkaTemplate<>( new DefaultKafkaProducerFactory<>( map ) ); }

    public void writeToKafka ( Card card ) {
        this.kafkaTemplate.send( Status.CARD_FINAL.name().toLowerCase(), SerDes.getSerDes().serialize( card ) ).addCallback( new ListenableFutureCallback<>() {
            @Override
            public void onFailure( Throwable ex ) { logger.warning("Kafka does not work since: " + LocalDateTime.now() ); }

            @Override
            public void onSuccess( SendResult< String, String > result ) { logger.info("Kafka got: " + card.getCardId() + " with offset: " + result.getRecordMetadata().offset() ); }
        } ); }

    public Notification writeToKafka ( Notification notification ) {
        this.kafkaTemplate.send( Status.NOTIFICATION.name().toLowerCase(), SerDes.getSerDes().serialize( notification ) ).addCallback( new ListenableFutureCallback<>() {
            @Override
            public void onFailure( Throwable ex ) { logger.warning("Kafka does not work since: " + LocalDateTime.now() ); }

            @Override
            public void onSuccess( SendResult< String, String > result ) { logger.info("Kafka got: " + notification.getTitle() + " with offset: " + result.getRecordMetadata().offset() ); }
        } ); return notification; }

    public SelfEmploymentTask writeToKafka ( SelfEmploymentTask selfEmploymentTask ) {
        this.kafkaTemplate.send( Status.SELF_EMPLOYMENT.name().toLowerCase(), SerDes.getSerDes().serialize( selfEmploymentTask ) ).addCallback( new ListenableFutureCallback<>() {
            @Override
            public void onFailure( Throwable ex ) { logger.warning("Kafka does not work since: " + LocalDateTime.now() ); }

            @Override
            public void onSuccess( SendResult< String, String > result ) { logger.info("Kafka got: " + selfEmploymentTask.getUuid() + " with offset: " + result.getRecordMetadata().offset() ); }
        } ); return selfEmploymentTask; }

    public void writeToKafka ( String passportSeries, ReqLocationExchange trackers ) {
        this.kafkaTemplate.send( passportSeries, SerDes.getSerDes().serialize( trackers ) ).addCallback( new ListenableFutureCallback<>() {
            @Override
            public void onFailure( Throwable ex ) { logger.warning("Kafka does not work since: " + LocalDateTime.now() ); }

            @Override
            public void onSuccess( SendResult< String, String > result ) { logger.info("Kafka got: " + trackers.getDate() + " with offset: " + result.getRecordMetadata().offset() ); }
        } ); }

    public void clear () {
        try { Mono.just( this.client.deleteTopics( this.client.listTopics().names().get() ) ).subscribe(); } catch (InterruptedException | ExecutionException e ) { e.printStackTrace(); }
        finally { this.logger.info( "Kafka was closed" );
            CassandraDataControl.getInstance().delete();
            RedisDataControl.getRedis().clear();
            Archive.getAchieve().clear();
            this.kafkaTemplate.destroy();
            this.kafkaTemplate.flush();
            this.kafkaStreams.close();
            this.kafkaStreams.close();
            this.properties.clear();
            this.client.close();
            instance = null; } }
}
