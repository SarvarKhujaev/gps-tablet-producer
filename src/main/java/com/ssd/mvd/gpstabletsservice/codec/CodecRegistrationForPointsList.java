package com.ssd.mvd.gpstabletsservice.codec;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;

import com.ssd.mvd.gpstabletsservice.tuple.Points;
import java.nio.ByteBuffer;

public class CodecRegistrationForPointsList extends TypeCodec< Points > {
    private final TypeCodec<UDTValue> innerCodec;

    private final UserType userType;

    public CodecRegistrationForPointsList ( TypeCodec< UDTValue > innerCodec, Class< Points > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ByteBuffer serialize ( Points value, ProtocolVersion protocolVersion )
            throws InvalidTypeException { return innerCodec.serialize( toUDTValue( value ), protocolVersion ); }

    @Override
    public Points deserialize ( ByteBuffer bytes, ProtocolVersion protocolVersion )
            throws InvalidTypeException {
        return toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public Points parse( String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" ) ?
            null : toAddress( innerCodec.parse( value ) ); }

    @Override
    public String format( Points value ) throws InvalidTypeException { return value == null ? "NULL" :
            innerCodec.format( toUDTValue( value ) ); }

    protected Points toAddress ( UDTValue value ) { return value == null ? null : new Points ( value ); }

    protected UDTValue toUDTValue ( Points value ) { return value == null ? null :
            userType.newValue()
                    .setDouble("lat", value.getLat() )
                    .setDouble("lng", value.getLng() )
                    .setInt("pointId", value.getPointId() )
                    .setString( "pointName", value.getPointName() ); }
}
