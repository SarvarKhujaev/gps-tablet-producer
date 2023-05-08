package com.ssd.mvd.gpstabletsservice.codec;

import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import java.nio.ByteBuffer;

public final class CodecRegistrationForPatrul extends TypeCodec< Patrul > {
    private final TypeCodec< UDTValue > innerCodec;
    private final UserType userType;

    public CodecRegistrationForPatrul ( TypeCodec< UDTValue > innerCodec, Class< Patrul > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ByteBuffer serialize ( final Patrul patrul, final ProtocolVersion protocolVersion ) throws InvalidTypeException { return innerCodec.serialize( this.toUDTValue( patrul ), protocolVersion ); }

    @Override
    public Patrul deserialize ( final ByteBuffer bytes, final ProtocolVersion protocolVersion ) throws InvalidTypeException {
        return this.toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public String format( final Patrul patrul ) throws InvalidTypeException {
        return DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( patrul )
                ? innerCodec.format( this.toUDTValue( patrul ) ) : "NULL"; }

    @Override
    public Patrul parse( final String value ) throws InvalidTypeException {
        return value == null || value.isEmpty() || value.equalsIgnoreCase("NULL" )
                ? null : toAddress( innerCodec.parse( value ) ); }

    private Patrul toAddress ( final UDTValue udtValue ) { return DataValidateInspector
            .getInstance()
            .getCheckParam()
            .test( udtValue )
            ? new Patrul ( udtValue ) : null; }

    private UDTValue toUDTValue ( final Patrul patrul ) {
        return DataValidateInspector
                .getInstance()
                .getCheckParam()
                .test( patrul )
                ? userType.newValue()
                .setTimestamp( "taskDate", patrul.getTaskDate() )
                .setTimestamp( "lastActiveDate", patrul.getLastActiveDate() )
                .setTimestamp( "startedToWorkDate", patrul.getStartedToWorkDate() )
                .setTimestamp( "dateOfRegistration", patrul.getDateOfRegistration() )

                .setDouble( "distance", patrul.getDistance() )
                .setDouble( "latitude", patrul.getLatitude() )
                .setDouble( "longitude", patrul.getLongitude() )
                .setDouble( "latitudeOfTask", patrul.getLatitudeOfTask() )
                .setDouble( "longitudeOfTask", patrul.getLongitudeOfTask() )

                .setUUID( "uuid", patrul.getUuid() )
                .setUUID( "organ", patrul.getOrgan() )
                .setUUID( "uuidOfEscort", patrul.getUuidOfEscort() )

                .setLong( "regionId", patrul.getRegionId() )
                .setLong( "mahallaId", patrul.getMahallaId() )
                .setLong( "districtId", patrul.getDistrictId() )
                .setLong( "totalActivityTime", patrul.getTotalActivityTime() )

                .setBool( "inPolygon", patrul.getInPolygon() )
                .setBool( "tuplePermission", patrul.getTuplePermission() )

                .setString( "name", patrul.getName() )
                .setString( "rank", patrul.getRank() )
                .setString( "email", patrul.getEmail() )
                .setString( "login", patrul.getLogin() )
                .setString( "taskId", patrul.getTaskId() )
                .setString( "carType", patrul.getCarType() )
                .setString( "surname", patrul.getSurname() )
                .setString( "password", patrul.getPassword() )
                .setString( "carNumber", patrul.getCarNumber() )
                .setString( "organName", patrul.getOrganName() )
                .setString( "regionName", patrul.getRegionName() )
                .setString( "policeType", patrul.getPoliceType() )
                .setString( "fatherName", patrul.getFatherName() )
                .setString( "dateOfBirth", patrul.getDateOfBirth() )
                .setString( "phoneNumber", patrul.getPhoneNumber() )
                .setString( "specialToken", patrul.getSpecialToken() )
                .setString( "tokenForLogin", patrul.getTokenForLogin() )
                .setString( "simCardNumber", patrul.getSimCardNumber() )
                .setString( "passportNumber", patrul.getPassportNumber() )
                .setString( "patrulImageLink", patrul.getPatrulImageLink() )

                .setString( "status", patrul.getStatus().name() )
                .setMap( "listOfTasks", patrul.getListOfTasks() )
                .setString( "taskTypes", patrul.getTaskTypes().name() )
                : null; }
}
