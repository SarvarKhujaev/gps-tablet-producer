package com.ssd.mvd.gpstabletsservice.entity;

import com.ssd.mvd.gpstabletsservice.task.sos_task.PatrulSos;

public final class Point {
    public Double getLatitude() {
        return this.latitude;
    }

    public void setLatitude ( final Double latitude ) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return this.longitude;
    }

    public void setLongitude ( final Double longitude ) {
        this.longitude = longitude;
    }

    private Double latitude;
    private Double longitude;

    public static Point from (
            final PatrulSos patrulSos
    ) {
        return new Point( patrulSos );
    }

    private Point (
            final PatrulSos patrulSos
    ) {
        this.longitude = patrulSos.getLongitude();
        this.latitude = patrulSos.getLatitude();
    }
}
