package com.ssd.mvd.gpstabletsservice.database.codec;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;

import java.nio.ByteBuffer;

public final class CodecRegistration extends TypeCodec< Object > {
    private final ObjectCommonMethods< Object > objectCommonMethods;
    private final TypeCodec< UDTValue > innerCodec;
    private final UserType userType;


    public CodecRegistration (
            final TypeCodec< UDTValue > innerCodec,
            final Class< Object > javaType
    ) {
        super( innerCodec.getCqlType(), javaType );
        this.objectCommonMethods = new ObjectCommonMethods<>() {
            @Override
            public Object generate ( final UDTValue udtValue ) {
                return null;
            }

            @Override
            public UDTValue fillUdtByEntityParams ( final UDTValue udtValue ) {
                return null;
            }
        };
        this.innerCodec = innerCodec;
        this.userType = (UserType) innerCodec.getCqlType();
    }

    @Override
    public ByteBuffer serialize ( final Object o, final ProtocolVersion protocolVersion ) throws InvalidTypeException {
        return innerCodec.serialize( this.toUDTValue( o ), protocolVersion );
    }

    @Override
    public Object deserialize ( final ByteBuffer bytes, final ProtocolVersion protocolVersion ) throws InvalidTypeException {
        return this.toAddress( innerCodec.deserialize( bytes, protocolVersion ) );
    }

    @Override
    public Object parse( final String value ) throws InvalidTypeException {
        return value == null || value.isEmpty() || value.equalsIgnoreCase("NULL" )
                ? null : this.toAddress( innerCodec.parse( value ) );
    }

    @Override
    public String format( final Object o ) throws InvalidTypeException {
        return DataValidateInspector
                .getInstance()
                .objectIsNotNull( o )
                ? innerCodec.format( this.toUDTValue( o ) ) : "NULL";
    }

    private Object toAddress ( final UDTValue udtValue ) {
        return DataValidateInspector
                .getInstance()
                .objectIsNotNull( udtValue )
                ? this.objectCommonMethods.generate( udtValue )
                : null;
    }

    private UDTValue toUDTValue ( final Object o ) {
        return DataValidateInspector
                .getInstance()
                .objectIsNotNull( o )
                ? this.objectCommonMethods.fillUdtByEntityParams( userType.newValue() )
                : null;
    }
}
