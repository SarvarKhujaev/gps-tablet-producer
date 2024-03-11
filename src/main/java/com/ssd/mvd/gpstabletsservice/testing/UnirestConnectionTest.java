package com.ssd.mvd.gpstabletsservice.testing;

import com.ssd.mvd.gpstabletsservice.controller.UnirestController;
import junit.framework.TestCase;

public final class UnirestConnectionTest extends TestCase {
    @Override
    public void setUp () {
        super.setName( UnirestController.getInstance().getClass().getName() );

        System.out.println( "Launching: " + super.getName() );
    }

    @Override
    public void tearDown () {
        System.out.println( "Closing: " + super.getName() + " test case" );
    }
}
