package com.ssd.mvd.gpstabletsservice.interfaces;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

public interface ObjectCommonMethods< T > {
    T generate ( final UDTValue udtValue );

    default T generate () {
        return null;
    }

    default T generate ( final Row row ) {
        return null;
    }

    UDTValue fillUdtByEntityParams ( final UDTValue udtValue );
}
