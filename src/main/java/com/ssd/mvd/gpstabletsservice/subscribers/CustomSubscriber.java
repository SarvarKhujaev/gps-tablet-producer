package com.ssd.mvd.gpstabletsservice.subscribers;

import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.TaskTimingStatistics;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForEscort;
import com.ssd.mvd.gpstabletsservice.database.CassandraDataControlForTasks;
import com.ssd.mvd.gpstabletsservice.controller.UnirestController;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.tuple.PolygonForEscort;
import com.ssd.mvd.gpstabletsservice.tuple.TupleTotalData;
import com.ssd.mvd.gpstabletsservice.tuple.TupleOfCar;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Row;

import org.reactivestreams.Subscription;
import org.reactivestreams.Subscriber;
import java.util.List;

public final class CustomSubscriber extends LogInspector implements Subscriber< Object > {
    private TupleTotalData tupleTotalData;
    private Subscription subscription;
    private Session session;

    private final Integer value;
    private String token;

    public CustomSubscriber ( final Integer value ) { this.value = value; }

    public CustomSubscriber ( final Integer value, final String token ) {
        this.token = token;
        this.value = value; }

    public CustomSubscriber ( final Integer value, final TupleTotalData tupleTotalData ) {
        this.tupleTotalData = tupleTotalData;
        this.value = value; }

    public CustomSubscriber ( final Integer value, final String token, final Session session ) {
        this.session = session;
        this.value = value;
        this.token = token; }

    @Override
    public void onSubscribe( final Subscription subscription ) {
        this.subscription = subscription;
        this.subscription.request( 1 ); }

    @Override
    public void onError( final Throwable throwable ) { super.logging( throwable ); }

    @Override
    public void onNext( final Object o ) {
        switch ( this.value ) {
            case 1 -> {
                super.logging( "TupleOfCar: " + ( (TupleOfCar) o ).getUuid() );
                ( (TupleOfCar) o ).setUuidOfPatrul( null );
                CassandraDataControlForEscort
                        .getInstance()
                        .getUnlinkTupleOfCarFromPatrul()
                        .accept( ( (TupleOfCar) o ) ); }
            case 2 -> {
                super.logging( "Subscriber got TaskTimingStatistics: " + ( (TaskTimingStatistics) o ).getTaskId() );
                CassandraDataControlForTasks
                        .getInstance()
                        .getSaveTaskTimeStatistics()
                        .accept( (TaskTimingStatistics) o ); }
            case 3 -> {
                super.logging( "Subscriber got patruls list: " + ( ( List< Patrul > ) o ).size() );
                ( ( List< Patrul > ) o )
                        .parallelStream()
                        .forEach( patrul -> {
                            patrul.setSpecialToken( this.token );
                            UnirestController
                                    .getInstance()
                                    .getAddUser()
                                    .accept( patrul ); } ); }
            case 4 -> {
                super.logging( "Updating patrul's policeType: " + this.token );
                this.session.execute( "UPDATE "
                        + CassandraTables.TABLETS.name() + "."
                        + CassandraTables.PATRULS.name()
                        + " SET policeType = '" + this.token + "'"
                        + " WHERE uuid = " + ( (Row) o ).getUUID( "uuid" ) + ";" ); }
            case 5 -> super.logging( "ResultSet: " + ( (ResultSet) o ).wasApplied() );
            case 6 -> this.tupleTotalData.setPolygonForEscort( ( PolygonForEscort ) o );
            case 7 -> this.tupleTotalData.getPatrulList().add( ( Patrul ) o );
            case 8 -> this.tupleTotalData.getTupleOfCarList().add( ( TupleOfCar ) o );
            default -> super.logging( "Message: " + o ); }
        this.subscription.request( 1 ); }

    @Override
    public void onComplete() { super.logging( "Subscriber completed its work" ); }
}
