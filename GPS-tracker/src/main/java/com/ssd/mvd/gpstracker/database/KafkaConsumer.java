package com.ssd.mvd.gpstracker.database;

import com.ssd.mvd.gpstracker.entity.TimeInspector;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class KafkaConsumer implements Runnable {
    public Boolean status;
    private final String topicName;
    private ConsumerRecords< String, String > records;
    private final Logger logger = Logger.getLogger( KafkaConsumer.class.toString() );
    private final org.apache.kafka.clients.consumer.KafkaConsumer< String, String > kafkaConsumer;

    public String getTopicName () { return this.topicName; }

    public void changeStatus( Boolean aBoolean ) { this.status = aBoolean; } // temporary switch off reading the Kafka

    private Map< String, Object > consumer () {
        Map< String, Object > props = new HashMap<>();
        props.put( ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false ); // this parameter is response for auto commit all offsets
        props.put( ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest" ); // consumer will start to read only fresh data from kafka
        props.put( ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, false ); // disable to create new topic for consumer
        props.put( ConsumerConfig.GROUP_ID_CONFIG, KafkaDataControl.getInstance().ID ); // common ID for all consumers
        props.put( ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, KafkaDataControl.getInstance().PATH ); // ip and port
        props.put( ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class );
        props.put( ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, org.apache.kafka.common.serialization.StringDeserializer.class );
        return props;
    }

    public KafkaConsumer ( String imei ) {
        // create new Consumer for Tracker and subscribe it to its current Topic
        ( this.kafkaConsumer = new org.apache.kafka.clients.consumer.KafkaConsumer<>( this.consumer() ) ).subscribe( Collections.singletonList( String.valueOf( imei ) ) ); // create new consumer and subscribe for each tracker
        this.logger.info( "Consumer: " + imei + " was created" );
        this.changeStatus( true );
        this.topicName = imei; }

    private void readFromKafka () { if ( !( this.records = this.kafkaConsumer.poll( Duration.ofSeconds( 2 ) ) ).isEmpty() ) {
            records.forEach( record -> Archive.getAchieve().save( this.getTopicName(), record.value() ) ); // saving all data toDataSave class
        this.kafkaConsumer.commitAsync( ( map, e ) -> { if ( e != null ) this.logger.info( "Smth wrong during reading of offset: " + e.getCause() ); } ); } } // committing offset asynchronously

    public void clear () {
        this.records = null;
        this.kafkaConsumer.close();
        this.logger.info( "The consumer is closed" ); }

    public void run () {
        this.kafkaConsumer.seekToBeginning( this.kafkaConsumer.assignment() );
        while ( Archive.getAchieve().getFlag() ) {
            if ( this.status ) this.readFromKafka();
            try { Thread.sleep( TimeInspector.getInspector().getTimestampForArchive() ); } catch ( InterruptedException e ) { throw new RuntimeException(e); } }
        this.kafkaConsumer.commitAsync(); // commits all offsets before cleaning
        this.clear(); } // cleaning the consumer memory
}
