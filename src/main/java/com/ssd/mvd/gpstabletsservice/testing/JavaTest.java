package com.ssd.mvd.gpstabletsservice.testing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith( value = Suite.class )
@Suite.SuiteClasses( value = {
        KafkaConnectionTest.class,
        UnirestConnectionTest.class,
        CassandraConnectionTest.class,
} )
public final class JavaTest {}
