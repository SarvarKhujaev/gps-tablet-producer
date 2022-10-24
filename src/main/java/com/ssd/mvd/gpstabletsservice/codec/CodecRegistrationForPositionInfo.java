package com.ssd.mvd.gpstabletsservice.codec;

import java.nio.ByteBuffer;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.ProtocolVersion;
import com.ssd.mvd.gpstabletsservice.task.card.PositionInfo;
import com.datastax.driver.core.exceptions.InvalidTypeException;

public class CodecRegistrationForPositionInfo extends TypeCodec< PositionInfo > {
    private final TypeCodec< UDTValue > innerCodec;

    private final UserType userType;

    public CodecRegistrationForPositionInfo ( TypeCodec< UDTValue > innerCodec, Class< PositionInfo > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ByteBuffer serialize ( PositionInfo value, ProtocolVersion protocolVersion )
            throws InvalidTypeException { return innerCodec.serialize( toUDTValue( value ), protocolVersion ); }

    @Override
    public PositionInfo deserialize ( ByteBuffer bytes, ProtocolVersion protocolVersion )
            throws InvalidTypeException {
        return toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public PositionInfo parse( String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" ) ?
            null : toAddress( innerCodec.parse( value ) ); }

    @Override
    public String format( PositionInfo value ) throws InvalidTypeException { return value == null ? "NULL" :
            innerCodec.format( toUDTValue( value ) ); }

    protected PositionInfo toAddress ( UDTValue value ) { return value == null ? null : new PositionInfo ( value ); }

    protected UDTValue toUDTValue ( PositionInfo value ) { return value == null ? null :
            userType.newValue()
                    .setDouble ( "lat", value.getLat() )
                    .setDouble ( "lng", value.getLng() ); }
}
