package com.ssd.mvd.gpstabletsservice.codec;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;

import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import java.nio.ByteBuffer;

public class CodecRegistrationForReport extends TypeCodec< ReportForCard > {
    private final TypeCodec<UDTValue> innerCodec;

    private final UserType userType;

    public CodecRegistrationForReport ( TypeCodec< UDTValue > innerCodec, Class< ReportForCard > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ByteBuffer serialize ( ReportForCard value, ProtocolVersion protocolVersion )
            throws InvalidTypeException { return innerCodec.serialize( toUDTValue( value ), protocolVersion ); }

    @Override
    public ReportForCard deserialize ( ByteBuffer bytes, ProtocolVersion protocolVersion )
            throws InvalidTypeException {
        return toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public ReportForCard parse( String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" ) ?
            null : toAddress( innerCodec.parse( value ) ); }

    @Override
    public String format( ReportForCard value ) throws InvalidTypeException { return value == null ? "NULL" :
            innerCodec.format( toUDTValue( value ) ); }

    protected ReportForCard toAddress ( UDTValue value ) { return value == null ? null : new ReportForCard ( value ); }

    protected UDTValue toUDTValue ( ReportForCard value ) { return value == null ? null :
            userType.newValue()
                    .setDouble("lat", value.getLat() )
                    .setDouble( "lan", value.getLan() )
                    .setString( "title", value.getTitle() )
                    .setTimestamp( "date", value.getDate() )
                    .setList( "imagesIds", value.getImagesIds() )
                    .setString( "description", value.getDescription() )
                    .setString( "passportSeries", value.getPassportSeries() ); }
}
