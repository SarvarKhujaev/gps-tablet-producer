package com.ssd.mvd.gpstabletsservice.codec;

import com.datastax.driver.core.*;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.ssd.mvd.gpstabletsservice.entity.PolygonEntity;

import java.nio.ByteBuffer;

public class CodecRegistrationForPolygonEntity extends TypeCodec< PolygonEntity > {
    private final TypeCodec< UDTValue > innerCodec;

    private final UserType userType;

    public CodecRegistrationForPolygonEntity ( TypeCodec< UDTValue > innerCodec, Class< PolygonEntity > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ByteBuffer serialize ( PolygonEntity value, ProtocolVersion protocolVersion )
            throws InvalidTypeException { return innerCodec.serialize( toUDTValue( value ), protocolVersion ); }

    @Override
    public PolygonEntity deserialize ( ByteBuffer bytes, ProtocolVersion protocolVersion )
            throws InvalidTypeException {
        return toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public PolygonEntity parse( String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" ) ?
                null : toAddress( innerCodec.parse( value ) ); }

    @Override
    public String format( PolygonEntity value ) throws InvalidTypeException { return value == null ? "NULL" :
            innerCodec.format( toUDTValue( value ) ); }

    protected PolygonEntity toAddress ( UDTValue value ) { return value == null ? null :
            new PolygonEntity (
                value.getDouble("lat" ),
                value.getDouble("lng" ) ); }

    protected UDTValue toUDTValue ( PolygonEntity value ) { return value == null ? null :
            userType.newValue()
                .setDouble("lat", value.getLat() )
                .setDouble("lng", value.getLng() ); }
}
