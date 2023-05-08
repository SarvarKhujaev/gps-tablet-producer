package com.ssd.mvd.gpstabletsservice.codec;

import java.nio.ByteBuffer;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.exceptions.InvalidTypeException;

import com.ssd.mvd.gpstabletsservice.task.card.PositionInfo;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;

public final class CodecRegistrationForPositionInfo extends TypeCodec< PositionInfo > {
    private final TypeCodec< UDTValue > innerCodec;
    private final UserType userType;

    public CodecRegistrationForPositionInfo ( final TypeCodec< UDTValue > innerCodec, final Class< PositionInfo > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ByteBuffer serialize ( final PositionInfo positionInfo, final ProtocolVersion protocolVersion ) throws InvalidTypeException { return innerCodec.serialize( this.toUDTValue( positionInfo ), protocolVersion ); }

    @Override
    public PositionInfo deserialize ( final ByteBuffer bytes, final ProtocolVersion protocolVersion ) throws InvalidTypeException {
        return this.toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public String format( final PositionInfo positionInfo ) throws InvalidTypeException { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( positionInfo )
            ? innerCodec.format( toUDTValue( positionInfo ) ) : "NULL"; }

    @Override
    public PositionInfo parse( final String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" )
            ? null : toAddress( innerCodec.parse( value ) ); }

    private UDTValue toUDTValue ( final PositionInfo positionInfo ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( positionInfo )
            ? userType.newValue()
            .setDouble ( "lat", positionInfo.getLat() )
            .setDouble ( "lng", positionInfo.getLng() )
            : null; }

    private PositionInfo toAddress ( final UDTValue value ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( value )
            ? new PositionInfo ( value ) : null; }
}
