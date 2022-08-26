package com.ssd.mvd.gpstabletsservice.codec;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.ssd.mvd.gpstabletsservice.entity.PolygonType;

import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;

import java.nio.ByteBuffer;

public class CodecRegistrationForPolygonType extends TypeCodec< PolygonType > {
    private final TypeCodec<UDTValue> innerCodec;

    private final UserType userType;

    public CodecRegistrationForPolygonType ( TypeCodec< UDTValue > innerCodec, Class< PolygonType > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ByteBuffer serialize ( PolygonType value, ProtocolVersion protocolVersion )
            throws InvalidTypeException { return innerCodec.serialize( toUDTValue( value ), protocolVersion ); }

    @Override
    public PolygonType deserialize ( ByteBuffer bytes, ProtocolVersion protocolVersion )
            throws InvalidTypeException {
        return toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public PolygonType parse( String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" ) ?
            null : toAddress( innerCodec.parse( value ) ); }

    @Override
    public String format( PolygonType value ) throws InvalidTypeException { return value == null ? "NULL" :
            innerCodec.format( toUDTValue( value ) ); }

    protected PolygonType toAddress ( UDTValue value ) { return value == null ? null : new PolygonType ( value ); }

    protected UDTValue toUDTValue ( PolygonType value ) { return value == null ? null :
            userType.newValue()
                    .setUUID( "uuid", value.getUuid() )
                    .setString( "name", value.getName() ); }
}
