package com.ssd.mvd.gpstabletsservice.testing;

import com.ssd.mvd.gpstabletsservice.kafkaDataSet.KafkaDataControl;
import junit.framework.TestCase;

public class KafkaConnectionTest extends TestCase {
    @Override
    public void setUp () {
        super.setName( KafkaDataControl.getKafkaDataControl().getClass().getName() );

        System.out.println( "Launching: " + super.getName() );
    }

    @Override
    public void tearDown () {
        System.out.println( "Closing: " + super.getName() + " test case" );

        /*
        closing connection to Kafka
        */
        KafkaDataControl.getKafkaDataControl().close();
    }

    public void testKafkaConnection () {
        assertNotNull( KafkaDataControl.getKafkaDataControl() );
    }
}
