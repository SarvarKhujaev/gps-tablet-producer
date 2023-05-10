package com.ssd.mvd.gpstabletsservice.database.codec;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.entity.CameraList;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import java.nio.ByteBuffer;

public final class CodecRegistrationForCameraList extends TypeCodec< CameraList > {
    private final TypeCodec< UDTValue > innerCodec;
    private final UserType userType;

    public CodecRegistrationForCameraList ( final TypeCodec< UDTValue > innerCodec, final Class< CameraList > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ByteBuffer serialize ( final CameraList cameraList, final ProtocolVersion protocolVersion ) throws InvalidTypeException { return innerCodec.serialize( this.toUDTValue( cameraList ), protocolVersion ); }

    @Override
    public CameraList deserialize ( final ByteBuffer bytes, final ProtocolVersion protocolVersion ) throws InvalidTypeException {
        return this.toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public String format( final CameraList value ) throws InvalidTypeException { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( value )
            ? innerCodec.format( toUDTValue( value ) ) : "NULL"; }

    @Override
    public CameraList parse( final String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" )
            ? null : toAddress( innerCodec.parse( value ) ); }

    private UDTValue toUDTValue ( final CameraList cameraList ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( cameraList )
            ? userType.newValue()
                    .setString ("rtspLink", cameraList.getRtspLink() )
                    .setString ( "cameraName", cameraList.getCameraName() ) : null; }

    private CameraList toAddress ( final UDTValue udtValue ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( udtValue )
            ? new CameraList ( udtValue ) : null; }
}
