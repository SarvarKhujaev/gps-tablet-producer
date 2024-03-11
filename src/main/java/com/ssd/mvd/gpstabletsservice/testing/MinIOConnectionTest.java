package com.ssd.mvd.gpstabletsservice.testing;

import com.ssd.mvd.gpstabletsservice.inspectors.MinIoController;
import junit.framework.TestCase;

public final class MinIOConnectionTest extends TestCase {
    @Override
    public void setUp () {
        super.setName( MinIoController.getInstance().getClass().getName() );

        System.out.println( "Launching: " + super.getName() );
    }

    @Override
    public void tearDown () {
        System.out.println( "Closing: " + super.getName() + " test case" );
    }
}
