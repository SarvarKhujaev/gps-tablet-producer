package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.UDTValue;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public class CameraList {
    private String rtspLink;
    private String cameraName;

    public CameraList ( final UDTValue value ) {
        this.setRtspLink( value.getString( "rtspLink" ) );
        this.setCameraName( value.getString( "cameraName" ) ); }
}
