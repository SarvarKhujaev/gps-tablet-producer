package com.ssd.mvd.gpstabletsservice.task.card;

@lombok.Data
public class Item {
    private String key;
    private Object value;

    public static <T> Item generate( final String key, final T object ) {
        return new Item( key, object );
    }

    private <T> Item ( final String key, final T object ) {
        this.value = object;
        this.key = key;
    }
}
