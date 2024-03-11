package com.ssd.mvd.gpstabletsservice;

import org.junit.runner.JUnitCore;
import junit.extensions.RepeatedTest;
import junit.framework.JUnit4TestAdapter;

import com.ssd.mvd.gpstabletsservice.testing.JavaTest;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;

import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GpsTabletsServiceApplication {
    public static ApplicationContext context;

    public static void main( final String[] args ) {
        context = SpringApplication.run( GpsTabletsServiceApplication.class, args );

        /*
        запускаем тесты
        */
        new LogInspector(
                new JUnitCore().run(
                        new RepeatedTest(
                                new JUnit4TestAdapter( JavaTest.class ), 2
                        )
                )
        ).close();
    }
}
