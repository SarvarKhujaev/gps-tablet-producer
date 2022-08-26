package com.ssd.mvd.gpstabletsservice.codec;

import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import java.nio.ByteBuffer;

public class CodecRegistrationForPatrul extends TypeCodec< Patrul > {
    private final TypeCodec< UDTValue > innerCodec;

    private final UserType userType;

    public CodecRegistrationForPatrul ( TypeCodec< UDTValue > innerCodec, Class< Patrul > javaType ) {
        super( innerCodec.getCqlType(), javaType );
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ByteBuffer serialize ( Patrul patrul, ProtocolVersion protocolVersion )
            throws InvalidTypeException { return innerCodec.serialize( toUDTValue( patrul ), protocolVersion ); }

    @Override
    public Patrul deserialize ( ByteBuffer bytes, ProtocolVersion protocolVersion )
            throws InvalidTypeException {
        return toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public Patrul parse( String value ) throws InvalidTypeException {
        return value == null || value.isEmpty() || value.equalsIgnoreCase("NULL" ) ?
                null : toAddress( innerCodec.parse( value ) ); }

    @Override
    public String format( Patrul value ) throws InvalidTypeException {
        return value == null ? "NULL" : innerCodec.format( toUDTValue( value ) ); }

    protected Patrul toAddress ( UDTValue value ) { return value == null ? null : new Patrul ( value ); }

    protected UDTValue toUDTValue ( Patrul patrul ) {
        return patrul == null ? null : userType.newValue()
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
                .setString( "taskTypes", patrul.getTaskTypes().name() ); }
}
