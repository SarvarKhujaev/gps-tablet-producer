package com.ssd.mvd.gpstabletsservice.entity;

import com.ssd.mvd.gpstabletsservice.database.KafkaConsumer;
import com.ssd.mvd.gpstabletsservice.database.KafkaDataControl;
import lombok.Data;

import java.time.Instant;

@Data
public class Trackers {
    private Instant date = Instant.now(); // last updated time
    private final String topicName; // id of the user who this tracker is linked to
    private final KafkaConsumer kafkaConsumer; // unique consumer for each Tracker

    public Trackers setStatus ( Boolean status ) {
        if ( status ) this.setDate( Instant.now() );
        this.kafkaConsumer.changeStatus( status );
        return this; }

    public Trackers ( String passportSeries ) {
        this.topicName = passportSeries; // id of patrul who is using this tracker
        // for each tracker we create exact Topic in Kafka and respective Consumer then the system launch this consumer
        new Thread( ( this.kafkaConsumer = new KafkaConsumer( KafkaDataControl.getInstance().getNewTopic( this.getTopicName() ) ) ), this.kafkaConsumer.getTopicName() ).start(); }
}
