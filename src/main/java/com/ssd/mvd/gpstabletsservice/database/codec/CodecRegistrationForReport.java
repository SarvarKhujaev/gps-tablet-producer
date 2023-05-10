package com.ssd.mvd.gpstabletsservice.database.codec;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import java.nio.ByteBuffer;

public final class CodecRegistrationForReport extends TypeCodec< ReportForCard > {
    private final TypeCodec<UDTValue> innerCodec;
    private final UserType userType;

    public CodecRegistrationForReport ( final TypeCodec< UDTValue > innerCodec, final Class< ReportForCard > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ReportForCard deserialize ( final ByteBuffer bytes, final ProtocolVersion protocolVersion ) throws InvalidTypeException {
        return this.toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public ByteBuffer serialize ( final ReportForCard value, final ProtocolVersion protocolVersion ) throws InvalidTypeException { return innerCodec.serialize( this.toUDTValue( value ), protocolVersion ); }

    @Override
    public String format( final ReportForCard reportForCard ) throws InvalidTypeException { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( reportForCard )
            ? innerCodec.format( this.toUDTValue( reportForCard ) ) : "NULL"; }

    @Override
    public ReportForCard parse( final String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" )
            ? null : toAddress( innerCodec.parse( value ) ); }

    private UDTValue toUDTValue ( final ReportForCard reportForCard ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( reportForCard )
            ? userType.newValue()
            .setDouble("lat", reportForCard.getLat() )
            .setDouble( "lan", reportForCard.getLan() )
            .setString( "title", reportForCard.getTitle() )
            .setTimestamp( "date", reportForCard.getDate() )
            .setList( "imagesIds", reportForCard.getImagesIds() )
            .setString( "description", reportForCard.getDescription() )
            .setString( "passportSeries", reportForCard.getPassportSeries() )
            : null; }

    private ReportForCard toAddress ( final UDTValue value ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( value )
            ? new ReportForCard ( value ) : null; }
}
