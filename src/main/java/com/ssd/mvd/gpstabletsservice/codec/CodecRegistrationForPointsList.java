package com.ssd.mvd.gpstabletsservice.codec;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.tuple.Points;
import java.nio.ByteBuffer;

public class CodecRegistrationForPointsList extends TypeCodec< Points > {
    private final TypeCodec< UDTValue > innerCodec;
    private final UserType userType;

    public CodecRegistrationForPointsList ( final TypeCodec< UDTValue > innerCodec, final Class< Points > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public Points deserialize ( final ByteBuffer bytes, final ProtocolVersion protocolVersion ) throws InvalidTypeException {
        return this.toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public ByteBuffer serialize ( final Points value, final ProtocolVersion protocolVersion ) throws InvalidTypeException { return innerCodec.serialize( this.toUDTValue( value ), protocolVersion ); }

    @Override
    public String format( final Points points ) throws InvalidTypeException { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( points )
            ? innerCodec.format( toUDTValue( points ) ) : "NULL"; }

    @Override
    public Points parse( final String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" ) ?
            null
            : this.toAddress( innerCodec.parse( value ) ); }

    protected UDTValue toUDTValue ( final Points points ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( points )
            ? userType.newValue()
            .setDouble("lat", points.getLat() )
            .setDouble("lng", points.getLng() )
            .setUUID( "pointId", points.getPointId() )
            .setString( "pointName", points.getPointName() ) : null; }

    protected Points toAddress ( final UDTValue value ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( value )
            ? new Points ( value ) : null; }
}
