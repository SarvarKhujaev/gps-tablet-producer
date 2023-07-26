package com.ssd.mvd.gpstabletsservice.database;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.WriteType;
import com.datastax.driver.core.ConsistencyLevel;
import com.datastax.driver.core.policies.RetryPolicy;
import com.datastax.driver.core.exceptions.DriverException;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;

public final class CustomRetryPolicy extends LogInspector implements RetryPolicy {
    private final Integer unavailableAttempts;
    private final Integer writeAttempts;
    private final Integer readAttempts;

    public CustomRetryPolicy ( final Integer readAttempts,
                               final Integer writeAttempts,
                               final Integer unavailableAttempts ) {
        this.unavailableAttempts = unavailableAttempts;
        this.writeAttempts = writeAttempts;
        this.readAttempts = readAttempts; }

    @Override
    public RetryDecision onReadTimeout ( final Statement stmnt,
                                         final ConsistencyLevel cl,
                                         final int requiredResponses,
                                         final int receivedResponses,
                                         final boolean dataReceived,
                                         final int rTime ) {
        super.logging( "Error in onReadTimeout: " + stmnt );
        return dataReceived
                ? RetryDecision.ignore()
                : rTime < this.readAttempts
                ? RetryDecision.retry( cl )
                : RetryDecision.rethrow(); }

    @Override
    public RetryDecision onWriteTimeout ( final Statement stmnt,
                                          final ConsistencyLevel cl,
                                          final WriteType wt,
                                          final int requiredResponses,
                                          final int receivedResponses,
                                          final int wTime ) {
        super.logging( "Error in onWriteTimeout: " + stmnt );
        return wTime < this.writeAttempts ? RetryDecision.retry( cl ) : RetryDecision.rethrow(); }

    @Override
    public RetryDecision onUnavailable ( final Statement stmnt,
                                         final ConsistencyLevel cl,
                                         final int requiredResponses,
                                         final int receivedResponses,
                                         final int uTime ) {
        super.logging( "Error in onUnavailable: " + stmnt );
        return uTime < this.unavailableAttempts
                ? RetryDecision.retry( ConsistencyLevel.QUORUM )
                : RetryDecision.rethrow(); }

    @Override
    public RetryDecision onRequestError ( final Statement statement,
                                          final ConsistencyLevel consistencyLevel,
                                          final DriverException e,
                                          final int i ) {
        super.logging( "Error in onRequestError: " + statement );
        CassandraDataControl.getInstance().delete( new Throwable( "Error in onRequestError: " + statement ) );
        return null; }

    @Override
    public void init ( final Cluster cluster ) { super.logging( "Init: " + cluster.getClusterName() ); }

    @Override
    public void close() { super.logging( "Retry closed" ); }
}
