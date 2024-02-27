package com.ssd.mvd.gpstabletsservice.entity;

@lombok.Data
@lombok.Builder
public final class Data< T, V > {
    private String type;
    private V total;
    private T data;

    public static <T> Data from ( final T value ) {
        return Data
                .builder()
                .data( value )
                .build();
    }

    public static <T> Data from ( final T value, final Long aLong ) {
        return Data
                .builder()
                .data( value )
                .total( aLong )
                .build();
    }

    public static <T> Data from ( final T value, final String type ) {
        return Data
                .builder()
                .data( value )
                .type( type )
                .build();
    }
}
