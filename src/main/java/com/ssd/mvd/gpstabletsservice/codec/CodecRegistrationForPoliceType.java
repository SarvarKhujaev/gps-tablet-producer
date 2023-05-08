package com.ssd.mvd.gpstabletsservice.codec;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;

import com.ssd.mvd.gpstabletsservice.entity.PoliceType;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;

import java.nio.ByteBuffer;

public final class CodecRegistrationForPoliceType extends TypeCodec< PoliceType > {
    private final TypeCodec< UDTValue > innerCodec;
    private final UserType userType;

    public CodecRegistrationForPoliceType ( final TypeCodec< UDTValue > innerCodec, final Class< PoliceType > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    private PoliceType toAddress ( final UDTValue value ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( value )
            ? new PoliceType ( value ) : null; }

    private UDTValue toUDTValue ( final PoliceType policeType ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( policeType )
            ? userType.newValue()
            .setUUID( "uuid", policeType.getUuid() )
            .setString( "icon", policeType.getIcon() )
            .setString( "icon2", policeType.getIcon2() )
            .setString( "policeType", policeType.getPoliceType() ) : null; }

    @Override
    public PoliceType parse( final String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" ) ?
            null : toAddress( innerCodec.parse( value ) ); }

    @Override
    public String format ( final PoliceType policeType ) throws InvalidTypeException { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( policeType )
            ? innerCodec.format( toUDTValue( policeType ) ) : "NULL"; }

    @Override
    public ByteBuffer serialize ( final PoliceType value, final ProtocolVersion protocolVersion ) throws InvalidTypeException { return innerCodec.serialize( this.toUDTValue( value ), protocolVersion ); }

    @Override
    public PoliceType deserialize ( final ByteBuffer bytes, final ProtocolVersion protocolVersion ) throws InvalidTypeException {
        return this.toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }
}
