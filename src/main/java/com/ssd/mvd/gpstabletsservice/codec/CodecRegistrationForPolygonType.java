package com.ssd.mvd.gpstabletsservice.codec;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.entity.PolygonType;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import java.nio.ByteBuffer;

public class CodecRegistrationForPolygonType extends TypeCodec< PolygonType > {
    private final TypeCodec<UDTValue> innerCodec;
    private final UserType userType;

    public CodecRegistrationForPolygonType ( final TypeCodec< UDTValue > innerCodec, final Class< PolygonType > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public PolygonType deserialize ( final ByteBuffer bytes, final ProtocolVersion protocolVersion ) throws InvalidTypeException {
        return this.toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public ByteBuffer serialize ( final PolygonType value, final ProtocolVersion protocolVersion ) throws InvalidTypeException { return innerCodec.serialize( this.toUDTValue( value ), protocolVersion ); }

    @Override
    public String format( final PolygonType polygonType ) throws InvalidTypeException { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( polygonType )
            ? innerCodec.format( this.toUDTValue( polygonType ) ) : "NULL"; }

    @Override
    public PolygonType parse( final String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" ) ?
            null : toAddress( innerCodec.parse( value ) ); }

    protected UDTValue toUDTValue ( final PolygonType polygonType ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( polygonType )
            ? userType.newValue()
            .setUUID( "uuid", polygonType.getUuid() )
            .setString( "name", polygonType.getName() ) : null; }

    protected PolygonType toAddress ( final UDTValue value ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( value )
            ? new PolygonType ( value ) : null; }
}
