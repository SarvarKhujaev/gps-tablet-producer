package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

public final class TabletUsageData {
    public static TabletUsageData generate ( final Long value, final String string ) {
        return new TabletUsageData( value, string );
    }

    private TabletUsageData( final Long value, final String string ) {
        this.totalUsage = value;
        this.date = string;
    }

    public String getDate() {
        return this.date;
    }

    private final Long totalUsage;
    private final String date;
}