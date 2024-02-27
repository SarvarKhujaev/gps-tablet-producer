package com.ssd.mvd.gpstabletsservice.database.codec;

import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PositionInfo;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.entity.polygons.PolygonEntity;
import com.ssd.mvd.gpstabletsservice.entity.polygons.PolygonType;
import com.datastax.driver.core.exceptions.InvalidTypeException;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.*;
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
    private final byte value;

    public CodecRegistration (
            final TypeCodec< UDTValue > innerCodec,
            final Class< Object > javaType,
            final byte value ) {
        super( innerCodec.getCqlType(), javaType );
        this.value = value;
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
                ? switch ( this.value ) {
                        case 1 -> new Patrul( udtValue );
                        case 2 -> new CameraList( udtValue );
                        case 3 -> new Points ( udtValue );
                        case 4 -> new PositionInfo ( udtValue );
                        case 5 -> new ReportForCard ( udtValue );
                        case 6 -> PolygonEntity.generate( udtValue );
                        case 7 -> new PolygonType( udtValue );
                        case 8 -> new PoliceType( udtValue );
                        case 9 -> PatrulCarInfo.generate( udtValue );
                        case 10 -> PatrulFIOData.generate( udtValue );
                        case 11 -> PatrulTaskInfo.generate( udtValue );
                        case 12 -> PatrulDateData.generate( udtValue );
                        case 13 -> PatrulAuthData.generate( udtValue );
                        case 14 -> PatrulTokenInfo.generate( udtValue );
                        case 15 -> PatrulRegionData.generate( udtValue );
                        case 16 -> PatrulMobileAppInfo.generate( udtValue );
                        case 17 -> PatrulUniqueValues.generate( udtValue );
                        case 18 -> PatrulLocationData.generate( udtValue );
                        default -> new ViolationsInformation ( udtValue );
        }
                : null;
    }

    private UDTValue toUDTValue ( final Object o ) {
        return DataValidateInspector
                .getInstance()
                .objectIsNotNull( o )
                ? switch ( this.value ) {
                    case 1 -> userType.newValue()
                                .setUUID( "uuid", ( (Patrul) o ).getUuid() )

                                .setLong( "totalActivityTime", ( (Patrul) o ).getTotalActivityTime() )

                                .setBool( "inPolygon", ( (Patrul) o ).getInPolygon() )
                                .setBool( "tuplePermission", ( (Patrul) o ).getTuplePermission() )

                                .setString( "rank", ( (Patrul) o ).getRank() )
                                .setString( "email", ( (Patrul) o ).getEmail() )
                                .setString( "organName", ( (Patrul) o ).getOrganName() )
                                .setString( "policeType", ( (Patrul) o ).getPoliceType() )
                                .setString( "dateOfBirth", ( (Patrul) o ).getDateOfBirth() )
                                .setString( "passportNumber", ( (Patrul) o ).getPassportNumber() )
                                .setString( "patrulImageLink", ( (Patrul) o ).getPatrulImageLink() );

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

                    case 9 -> userType.newValue()
                            .setString( "carType", ( (PatrulCarInfo) o ).getCarType()  )
                            .setString( "carNumber", ( (PatrulCarInfo) o ).getCarNumber() );

                    case 10 -> userType.newValue()
                            .setString( "name", ( (PatrulFIOData) o ).getName() )
                            .setString( "surname", ( (PatrulFIOData) o ).getSurname() )
                            .setString( "fatherName", ( (PatrulFIOData) o ).getFatherName() )
                            .setString( "surnameNameFatherName", ( (PatrulFIOData) o ).getSurnameNameFatherName() );

                    case 11 -> userType.newValue()
                            .setString( "taskId", ( (PatrulTaskInfo) o ).getTaskId() )
                            .setString( "status", ( (PatrulTaskInfo) o ).getStatus().name() )
                            .setString( "taskTypes", ( (PatrulTaskInfo) o ).getTaskTypes().name() )
                            .setMap( "listOfTasks", ( (PatrulTaskInfo) o ).getListOfTasks() );

                    case 12 -> userType.newValue()
                            .setTimestamp( "taskDate", ( (PatrulDateData) o ).getTaskDate() )
                            .setTimestamp( "lastActiveDate", ( (PatrulDateData) o ).getLastActiveDate() )
                            .setTimestamp( "startedToWorkDate", ( (PatrulDateData) o ).getStartedToWorkDate() )
                            .setTimestamp( "dateOfRegistration", ( (PatrulDateData) o ).getDateOfRegistration() );

                    case 13 -> userType.newValue()
                            .setString( "login", ( (PatrulAuthData) o ).getLogin() )
                            .setString( "password", ( (PatrulAuthData) o ).getPassword() );

                    case 14 -> userType.newValue()
                            .setString( "specialToken", ( ( (PatrulTokenInfo) o ).getSpecialToken() ) )
                            .setString( "tokenForLogin", ( ( (PatrulTokenInfo) o ).getTokenForLogin() ) );

                    case 15 -> userType.newValue()
                            .setLong( "regionId", ( (PatrulRegionData) o ).getRegionId() )
                            .setLong( "mahallaId", ( (PatrulRegionData) o ).getMahallaId() )
                            .setLong( "districtId", ( (PatrulRegionData) o ).getDistrictId() )
                            .setString( "regionName", ( (PatrulRegionData) o ).getRegionName() )
                            .setString( "districtName", ( (PatrulRegionData) o ).getDistrictName() );

                    case 16 -> userType.newValue()
                            .setString( "phoneNumber", ( (PatrulMobileAppInfo) o ).getPhoneNumber() )
                            .setString( "simCardNumber", ( (PatrulMobileAppInfo) o ).getSimCardNumber() )
                            .setByte( "batteryLevel", ( (PatrulMobileAppInfo) o ).getBatteryLevel() );

                    case 17 -> userType.newValue()
                            .setUUID( "organ", ( (PatrulUniqueValues) o ).getOrgan() )
                            .setUUID( "sos_id", ( (PatrulUniqueValues) o ).getSos_id() )
                            .setUUID( "uuidOfEscort", ( (PatrulUniqueValues) o ).getUuidOfEscort() )
                            .setUUID( "uuidForPatrulCar", ( (PatrulUniqueValues) o ).getUuidForPatrulCar() )
                            .setUUID( "uuidForEscortCar", ( (PatrulUniqueValues) o ).getUuidForEscortCar() );

                    case 18 -> userType.newValue()
                            .setDouble( "distance", ( (PatrulLocationData) o ).getDistance() )
                            .setDouble( "latitude", ( (PatrulLocationData) o ).getLatitude() )
                            .setDouble( "longitude", ( (PatrulLocationData) o ).getLongitude() );

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
                            .setString( "decreeSerialNumber", ( (ViolationsInformation) o ).getDecreeSerialNumber() );
        }
                : null;
    }
}
