package com.ssd.mvd.gpstabletsservice.entity;

import com.datastax.driver.core.UDTValue;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CameraList {
    private String rtspLink;
    private String cameraName;

    public CameraList ( UDTValue value ) {
        this.setRtspLink( value.getString( "rtspLink" ) );
        this.setCameraName( value.getString( "cameraName" ) ); }
}
