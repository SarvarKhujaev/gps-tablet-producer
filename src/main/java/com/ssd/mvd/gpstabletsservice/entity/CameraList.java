package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.UDTValue;

public final class CameraList {
    public String getRtspLink() {
        return this.rtspLink;
    }

    public void setRtspLink ( final String rtspLink ) {
        this.rtspLink = rtspLink;
    }

    public String getCameraName() {
        return this.cameraName;
    }

    public void setCameraName ( final String cameraName ) {
        this.cameraName = cameraName;
    }

    private String rtspLink;
    private String cameraName;

    public CameraList ( final UDTValue value ) {
        this.setRtspLink( value.getString( "rtspLink" ) );
        this.setCameraName( value.getString( "cameraName" ) );
    }
}
