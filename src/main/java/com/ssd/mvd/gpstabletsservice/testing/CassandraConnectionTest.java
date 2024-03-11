package com.ssd.mvd.gpstabletsservice.testing;

import com.ssd.mvd.gpstabletsservice.database.CassandraDataControl;
import junit.framework.TestCase;

/*
проверяем соединение с БД Cassandra
*/
public final class CassandraConnectionTest extends TestCase {
    @Override
    public void setUp () {
        super.setName( CassandraDataControl.class.getName() );

        System.out.println( "Launching: " + super.getName() );

        /*
        Launch the Database connection
        */
        CassandraDataControl.getInstance();
    }

    @Override
    public void tearDown () {
        System.out.println( "Closing: " + super.getName() + " test case" );
        CassandraDataControl.getInstance().close();
    }

    /*
    checks and make sure that Cassandra Cluster was established
    and session from Cluster was initiated
    */
    public void testConnectionWasEstablished () {
        assertNotNull( CassandraDataControl.getInstance() );
        assertNotNull( CassandraDataControl.getInstance().getCluster() );
        assertNotNull( CassandraDataControl.getInstance().getSession() );
        assertFalse( CassandraDataControl.getInstance().getCluster().isClosed() );
        assertFalse( CassandraDataControl.getInstance().getSession().isClosed() );
    }
}
