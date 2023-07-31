package com.ssd.mvd.gpstabletsservice.database.codec;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PositionInfo;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.entity.polygons.PolygonEntity;
import com.ssd.mvd.gpstabletsservice.entity.polygons.PolygonType;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.entity.CameraList;
import com.ssd.mvd.gpstabletsservice.entity.PoliceType;
import com.ssd.mvd.gpstabletsservice.tuple.Points;
import com.datastax.driver.core.ProtocolVersion;
import com.datastax.driver.core.TypeCodec;
import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.UserType;
import java.nio.ByteBuffer;

public final class CodecRegistration extends TypeCodec< Object > {
    private final TypeCodec< UDTValue > innerCodec;
    private final UserType userType;
    private final Integer value;

    public CodecRegistration ( final TypeCodec< UDTValue > innerCodec,
                               final Class< Object > javaType,
                               final Integer value ) {
        super( innerCodec.getCqlType(), javaType );
        this.value = value;
        this.innerCodec = innerCodec;
        this.userType = (UserType)innerCodec.getCqlType(); }

    @Override
    public ByteBuffer serialize ( final Object o, final ProtocolVersion protocolVersion ) throws InvalidTypeException {
        return innerCodec.serialize( this.toUDTValue( o ), protocolVersion ); }

    @Override
    public Object deserialize ( final ByteBuffer bytes, final ProtocolVersion protocolVersion ) throws InvalidTypeException {
        return this.toAddress( innerCodec.deserialize( bytes, protocolVersion ) ); }

    @Override
    public Object parse( final String value ) throws InvalidTypeException {
        return value == null || value.isEmpty() || value.equalsIgnoreCase("NULL" )
                ? null : this.toAddress( innerCodec.parse( value ) ); }

    @Override
    public String format( final Object o ) throws InvalidTypeException {
        return DataValidateInspector
                .getInstance()
                .checkParam
                .test( o )
                ? innerCodec.format( this.toUDTValue( o ) ) : "NULL"; }

    private Object toAddress ( final UDTValue udtValue ) { return DataValidateInspector
            .getInstance()
            .checkParam
            .test( udtValue )
            ? switch ( this.value ) {
                    case 1 -> new Patrul( udtValue );
                    case 2 -> new CameraList( udtValue );
                    case 3 -> new Points ( udtValue );
                    case 4 -> new PositionInfo ( udtValue );
                    case 5 -> new ReportForCard ( udtValue );
                    case 6 -> new PolygonEntity( udtValue.getDouble("lat" ), udtValue.getDouble("lng" ) );
                    case 7 -> new PolygonType( udtValue );
                    case 8 -> new PoliceType( udtValue );
                    default -> new ViolationsInformation ( udtValue ); }
            : null; }

    private UDTValue toUDTValue ( final Object o ) {
        return DataValidateInspector
                .getInstance()
                .checkParam
                .test( o )
                ? switch ( this.value ) {
                    case 1 -> userType.newValue()
                                .setTimestamp( "taskDate", ( (Patrul) o ).getTaskDate() )
                                .setTimestamp( "lastActiveDate", ( (Patrul) o ).getLastActiveDate() )
                                .setTimestamp( "startedToWorkDate", ( (Patrul) o ).getStartedToWorkDate() )
                                .setTimestamp( "dateOfRegistration", ( (Patrul) o ).getDateOfRegistration() )

                                .setDouble( "distance", ( (Patrul) o ).getDistance() )
                                .setDouble( "latitude", ( (Patrul) o ).getLatitude() )
                                .setDouble( "longitude", ( (Patrul) o ).getLongitude() )
                                .setDouble( "latitudeOfTask", ( (Patrul) o ).getLatitudeOfTask() )
                                .setDouble( "longitudeOfTask", ( (Patrul) o ).getLongitudeOfTask() )

                                .setUUID( "uuid", ( (Patrul) o ).getUuid() )
                                .setUUID( "organ", ( (Patrul) o ).getOrgan() )
                                .setUUID( "uuidOfEscort", ( (Patrul) o ).getUuidOfEscort() )

                                .setLong( "regionId", ( (Patrul) o ).getRegionId() )
                                .setLong( "mahallaId", ( (Patrul) o ).getMahallaId() )
                                .setLong( "districtId", ( (Patrul) o ).getDistrictId() )
                                .setLong( "totalActivityTime", ( (Patrul) o ).getTotalActivityTime() )

                                .setBool( "inPolygon", ( (Patrul) o ).getInPolygon() )
                                .setBool( "tuplePermission", ( (Patrul) o ).getTuplePermission() )

                                .setString( "name", ( (Patrul) o ).getName() )
                                .setString( "rank", ( (Patrul) o ).getRank() )
                                .setString( "email", ( (Patrul) o ).getEmail() )
                                .setString( "login", ( (Patrul) o ).getLogin() )
                                .setString( "taskId", ( (Patrul) o ).getTaskId() )
                                .setString( "carType", ( (Patrul) o ).getCarType() )
                                .setString( "surname", ( (Patrul) o ).getSurname() )
                                .setString( "password", ( (Patrul) o ).getPassword() )
                                .setString( "carNumber", ( (Patrul) o ).getCarNumber() )
                                .setString( "organName", ( (Patrul) o ).getOrganName() )
                                .setString( "regionName", ( (Patrul) o ).getRegionName() )
                                .setString( "policeType", ( (Patrul) o ).getPoliceType() )
                                .setString( "fatherName", ( (Patrul) o ).getFatherName() )
                                .setString( "dateOfBirth", ( (Patrul) o ).getDateOfBirth() )
                                .setString( "phoneNumber", ( (Patrul) o ).getPhoneNumber() )
                                .setString( "specialToken", ( (Patrul) o ).getSpecialToken() )
                                .setString( "tokenForLogin", ( (Patrul) o ).getTokenForLogin() )
                                .setString( "simCardNumber", ( (Patrul) o ).getSimCardNumber() )
                                .setString( "passportNumber", ( (Patrul) o ).getPassportNumber() )
                                .setString( "patrulImageLink", ( (Patrul) o ).getPatrulImageLink() )

                                .setString( "status", ( (Patrul) o ).getStatus().name() )
                                .setMap( "listOfTasks", ( (Patrul) o ).getListOfTasks() )
                                .setString( "taskTypes", ( (Patrul) o ).getTaskTypes().name() );

                    case 2 -> userType.newValue()
                            .setString ("rtspLink", ( (CameraList) o ).getRtspLink() )
                            .setString ( "cameraName", ( (CameraList) o ).getCameraName() );

                    case 3 -> userType.newValue()
                            .setDouble("lat", ( (Points) o ).getLat() )
                            .setDouble("lng", ( (Points) o ).getLng() )
                            .setUUID( "pointId", ( (Points) o ).getPointId() )
                            .setString( "pointName", ( (Points) o ).getPointName() );

                    case 4 -> userType.newValue()
                            .setDouble ( "lat", ( (PositionInfo) o ).getLat() )
                            .setDouble ( "lng", ( (PositionInfo) o ).getLng() );

                    case 5 -> userType.newValue()
                            .setDouble("lat", ( (ReportForCard) o ).getLat() )
                            .setDouble( "lan", ( (ReportForCard) o ).getLan() )
                            .setString( "title", ( (ReportForCard) o ).getTitle() )
                            .setTimestamp( "date", ( (ReportForCard) o ).getDate() )
                            .setList( "imagesIds", ( (ReportForCard) o ).getImagesIds() )
                            .setString( "description", ( (ReportForCard) o ).getDescription() )
                            .setString( "passportSeries", ( (ReportForCard) o ).getPassportSeries() );

                    case 6 -> userType.newValue()
                            .setDouble("lat", ( (PolygonEntity) o ).getLat() )
                            .setDouble("lng", ( (PolygonEntity) o ).getLng() );

                    case 7 -> userType.newValue()
                            .setUUID( "uuid", ( (PolygonType) o ).getUuid() )
                            .setString( "name", ( (PolygonType) o ).getName() );

                    case 8 -> userType.newValue()
                            .setUUID( "uuid", ( (PoliceType) o ).getUuid() )
                            .setString( "icon", ( (PoliceType) o ).getIcon() )
                            .setString( "icon2", ( (PoliceType) o ).getIcon2() )
                            .setString( "policeType", ( (PoliceType) o ).getPoliceType() );

                    default -> userType.newValue()
                            .setInt ( "amount", ( (ViolationsInformation) o ).getAmount() )
                            .setInt( "decreeStatus", ( (ViolationsInformation) o ).getDecreeStatus() )
                            .setString( "bill", ( (ViolationsInformation) o ).getBill() )
                            .setString( "model", ( (ViolationsInformation) o ).getModel() )
                            .setString( "owner", ( (ViolationsInformation) o ).getOwner() )
                            .setString( "article", ( (ViolationsInformation) o ).getArticle() )
                            .setString( "address", ( (ViolationsInformation) o ).getAddress() )
                            .setString( "payDate", ( (ViolationsInformation) o ).getPayDate() )
                            .setString( "division", ( (ViolationsInformation) o ).getDivision() )
                            .setString( "violation", ( (ViolationsInformation) o ).getViolation() )
                            .setString( "decreeSerialNumber", ( (ViolationsInformation) o ).getDecreeSerialNumber() ); }
                : null; }
}
