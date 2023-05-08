package com.ssd.mvd.gpstabletsservice.codec;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.entity.PolygonEntity;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.*;
import java.nio.ByteBuffer;

public final class CodecRegistrationForPolygonEntity extends TypeCodec< PolygonEntity > {
    private final TypeCodec< UDTValue > innerCodec;
    private final UserType userType;

    public CodecRegistrationForPolygonEntity ( final TypeCodec< UDTValue > innerCodec, final Class< PolygonEntity > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public PolygonEntity deserialize ( final ByteBuffer bytes, final ProtocolVersion protocolVersion ) throws InvalidTypeException {
        return this.toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public ByteBuffer serialize ( final PolygonEntity value, final ProtocolVersion protocolVersion ) throws InvalidTypeException { return innerCodec.serialize( this.toUDTValue( value ), protocolVersion ); }

    @Override
    public String format( final PolygonEntity polygonEntity ) throws InvalidTypeException { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( polygonEntity )
            ? innerCodec.format( this.toUDTValue( polygonEntity ) ) : "NULL"; }

    @Override
    public PolygonEntity parse( final String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" ) ?
                null : toAddress( innerCodec.parse( value ) ); }

    private UDTValue toUDTValue ( final PolygonEntity polygonEntity ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( polygonEntity )
            ? userType.newValue()
            .setDouble("lat", polygonEntity.getLat() )
            .setDouble("lng", polygonEntity.getLng() ) : null; }

    private PolygonEntity toAddress ( final UDTValue value ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( value )
            ? new PolygonEntity ( value.getDouble("lat" ), value.getDouble("lng" ) ) : null; }
}
