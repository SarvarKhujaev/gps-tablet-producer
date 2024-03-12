package com.ssd.mvd.gpstabletsservice.publisher;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.reactivestreams.Subscription;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Publisher;

/*
кастомный Publisher, который принимает сообщение и название топика для Кафки

А также отслеживает состояние передачи сообщения и обрабатывает ошибки или сообщение об удаче
*/
public final class CustomPublisher implements Publisher< ProducerRecord< String, String > > {
    private final ProducerRecord< String, String > producerRecord;

    public static CustomPublisher from (
            final String topic,
            final String message
    ) {
        return new CustomPublisher( topic, message );
    }

    private CustomPublisher (
            final String topic,
            final String message
    ) {
        this.producerRecord = new ProducerRecord<>( topic, message );
    }

    @Override
    public void subscribe( final Subscriber subscriber ) {
        subscriber.onSubscribe( new Subscription() {
            @Override
            public void request( final long l ) {
                subscriber.onNext( producerRecord );
                subscriber.onComplete();
            }

            @Override
            public void cancel() {
                subscriber.onError( new Exception( "Message was not sent!!!" ) );
            }
        } );
    }
}
