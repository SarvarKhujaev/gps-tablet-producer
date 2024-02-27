package com.ssd.mvd.gpstabletsservice.subscribers;

import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import org.reactivestreams.Subscription;
import org.reactivestreams.Subscriber;
import java.util.function.Consumer;

public final class CustomSubscriber<T> extends LogInspector implements Subscriber<T> {
    private final Consumer< T > objectConsumer;
    private Subscription subscription;

    public CustomSubscriber( final Consumer< T > objectConsumer ) {
        this.objectConsumer = objectConsumer;
    }

    @Override
    public void onSubscribe( final Subscription subscription ) {
        this.subscription = subscription;
        this.subscription.request( 1 );
    }

    @Override
    public void onNext( final T o ) {
        this.objectConsumer.accept( o );
        this.subscription.request( 1 );
    }

    @Override
    public void onError( final Throwable throwable ) {
        super.logging( throwable );
    }

    @Override
    public void onComplete() {
        super.logging( this.getClass().getName() + " has completed its work" );
    }
}
