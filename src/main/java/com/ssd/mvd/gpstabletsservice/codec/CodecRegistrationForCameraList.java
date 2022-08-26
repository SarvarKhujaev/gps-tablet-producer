package com.ssd.mvd.gpstabletsservice.codec;

import com.ssd.mvd.gpstabletsservice.entity.CameraList;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import java.nio.ByteBuffer;

public class CodecRegistrationForCameraList extends TypeCodec< CameraList > {
    private final TypeCodec<UDTValue> innerCodec;

    private final UserType userType;

    public CodecRegistrationForCameraList ( TypeCodec< UDTValue > innerCodec, Class< CameraList > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ByteBuffer serialize ( CameraList value, ProtocolVersion protocolVersion )
            throws InvalidTypeException { return innerCodec.serialize( toUDTValue( value ), protocolVersion ); }

    @Override
    public CameraList deserialize ( ByteBuffer bytes, ProtocolVersion protocolVersion )
            throws InvalidTypeException {
        return toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public CameraList parse( String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" ) ?
            null : toAddress( innerCodec.parse( value ) ); }

    @Override
    public String format( CameraList value ) throws InvalidTypeException { return value == null ? "NULL" :
            innerCodec.format( toUDTValue( value ) ); }

    protected CameraList toAddress ( UDTValue value ) { return value == null ? null :
            new CameraList ( value ); }

    protected UDTValue toUDTValue ( CameraList value ) { return value == null ? null :
            userType.newValue()
                    .setString ("rtspLink", value.getRtspLink() )
                    .setString ( "cameraName", value.getCameraName() ); }
}
