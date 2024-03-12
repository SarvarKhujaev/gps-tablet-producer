package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.UDTValue;
import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;

public final class CameraList implements ObjectCommonMethods< CameraList > {
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

    public static CameraList empty () {
        return new CameraList();
    }

    private CameraList () {}

    @Override
    public CameraList generate( final UDTValue udtValue ) {
        this.setCameraName( udtValue.getString( "cameraName" ) );
        this.setRtspLink( udtValue.getString( "rtspLink" ) );
        return this;
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue
                .setString ("rtspLink", this.getRtspLink() )
                .setString ( "cameraName", this.getCameraName() );
    }
}
