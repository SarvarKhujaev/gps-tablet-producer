package com.ssd.mvd.gpstabletsservice.codec;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;

import com.ssd.mvd.gpstabletsservice.entity.PoliceType;
import java.nio.ByteBuffer;

public class CodecRegistrationForPoliceType extends TypeCodec< PoliceType > {
    private final TypeCodec< UDTValue > innerCodec;

    private final UserType userType;

    public CodecRegistrationForPoliceType ( TypeCodec< UDTValue > innerCodec, Class< PoliceType > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ByteBuffer serialize ( PoliceType value, ProtocolVersion protocolVersion )
            throws InvalidTypeException { return innerCodec.serialize( toUDTValue( value ), protocolVersion ); }

    @Override
    public PoliceType deserialize ( ByteBuffer bytes, ProtocolVersion protocolVersion )
            throws InvalidTypeException {
        return toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public PoliceType parse( String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" ) ?
            null : toAddress( innerCodec.parse( value ) ); }

    @Override
    public String format ( PoliceType value ) throws InvalidTypeException { return value == null ? "NULL" :
            innerCodec.format( toUDTValue( value ) ); }

    protected PoliceType toAddress ( UDTValue value ) { return value == null ? null : new PoliceType ( value ); }

    protected UDTValue toUDTValue ( PoliceType value ) { return value == null ? null :
            userType.newValue()
                    .setUUID( "uuid", value.getUuid() )
                    .setString( "icon", value.getIcon() )
                    .setString( "icon2", value.getIcon2() )
                    .setString( "policeType", value.getPoliceType() ); }
}
