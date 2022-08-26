package com.ssd.mvd.gpstabletsservice.codec;

import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.ssd.mvd.gpstabletsservice.entity.PolygonEntity;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;

import java.nio.ByteBuffer;

public class CodecRegistrationForViolationsInformation extends TypeCodec< ViolationsInformation > {
    private final TypeCodec<UDTValue> innerCodec;

    private final UserType userType;

    public CodecRegistrationForViolationsInformation ( TypeCodec< UDTValue > innerCodec, Class< ViolationsInformation > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ByteBuffer serialize ( ViolationsInformation value, ProtocolVersion protocolVersion )
            throws InvalidTypeException { return innerCodec.serialize( toUDTValue( value ), protocolVersion ); }

    @Override
    public ViolationsInformation deserialize ( ByteBuffer bytes, ProtocolVersion protocolVersion )
            throws InvalidTypeException {
        return toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public ViolationsInformation parse( String value ) throws InvalidTypeException { return value == null ||
            value.isEmpty() ||
            value.equalsIgnoreCase("NULL" ) ?
            null : toAddress( innerCodec.parse( value ) ); }

    @Override
    public String format( ViolationsInformation value ) throws InvalidTypeException { return value == null ? "NULL" :
            innerCodec.format( toUDTValue( value ) ); }

    protected ViolationsInformation toAddress ( UDTValue value ) { return value == null ? null :
            new ViolationsInformation ( value ); }

    protected UDTValue toUDTValue ( ViolationsInformation value ) { return value == null ? null :
            userType.newValue()
                    .setInt ( "amount", value.getAmount() )
                    .setInt( "decreeStatus", value.getDecreeStatus() )
                    .setString( "bill", value.getBill() )
                    .setString( "model", value.getModel() )
                    .setString( "owner", value.getOwner() )
                    .setString( "article", value.getArticle() )
                    .setString( "address", value.getAddress() )
                    .setString( "payDate", value.getPayDate() )
                    .setString( "division", value.getDivision() )
                    .setString( "violation", value.getViolation() )
                    .setString( "decreeSerialNumber", value.getDecreeSerialNumber() ); }
}
