package com.ssd.mvd.gpstabletsservice.codec;

import java.nio.ByteBuffer;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.exceptions.InvalidTypeException;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;

public class CodecRegistrationForViolationsInformation extends TypeCodec< ViolationsInformation > {
    private final TypeCodec< UDTValue > innerCodec;
    private final UserType userType;

    public CodecRegistrationForViolationsInformation ( final TypeCodec< UDTValue > innerCodec, final Class< ViolationsInformation > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ViolationsInformation deserialize ( final ByteBuffer bytes, final ProtocolVersion protocolVersion ) throws InvalidTypeException {
        return this.toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public ByteBuffer serialize ( final ViolationsInformation value, final ProtocolVersion protocolVersion ) throws InvalidTypeException { return innerCodec.serialize( this.toUDTValue( value ), protocolVersion ); }

    @Override
    public String format( final ViolationsInformation violationsInformation ) throws InvalidTypeException { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( violationsInformation )
            ? innerCodec.format( toUDTValue( violationsInformation ) ) : "NULL"; }

    @Override
    public ViolationsInformation parse( final String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" )
            ? null : toAddress( innerCodec.parse( value ) ); }

    protected UDTValue toUDTValue ( final ViolationsInformation violationsInformation ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( violationsInformation )
            ? userType.newValue()
            .setInt ( "amount", violationsInformation.getAmount() )
            .setInt( "decreeStatus", violationsInformation.getDecreeStatus() )
            .setString( "bill", violationsInformation.getBill() )
            .setString( "model", violationsInformation.getModel() )
            .setString( "owner", violationsInformation.getOwner() )
            .setString( "article", violationsInformation.getArticle() )
            .setString( "address", violationsInformation.getAddress() )
            .setString( "payDate", violationsInformation.getPayDate() )
            .setString( "division", violationsInformation.getDivision() )
            .setString( "violation", violationsInformation.getViolation() )
            .setString( "decreeSerialNumber", violationsInformation.getDecreeSerialNumber() ) : null; }

    protected ViolationsInformation toAddress ( final UDTValue value ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( value )
            ? new ViolationsInformation ( value ) : null; }
}
