package com.ssd.mvd.gpstabletsservice.interfaces;

public interface ServiceCommonMethods {
    default void close( final Throwable throwable ) {};

    void close();
}
