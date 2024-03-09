package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.DataInfo;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import com.ssd.mvd.gpstabletsservice.interfaces.ObjectCommonMethods;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.datastax.driver.core.utils.UUIDs;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

import java.time.Duration;
import java.util.*;

public final class Patrul extends DataValidateInspector implements ObjectCommonMethods< Patrul > {
    public UUID getUuid () {
        return this.uuid;
    }

    public void setUuid ( final UUID uuid ) {
        this.uuid = uuid;
    }

    public long getTotalActivityTime() {
        return this.totalActivityTime;
    }

    public void setTotalActivityTime( final long totalActivityTime ) {
        this.totalActivityTime = totalActivityTime;
    }

    public boolean getInPolygon() {
        return this.inPolygon;
    }

    public void setInPolygon( final boolean inPolygon ) {
        this.inPolygon = inPolygon;
    }

    public boolean getTuplePermission() {
        return this.tuplePermission;
    }

    public void setTuplePermission( final boolean tuplePermission ) {
        this.tuplePermission = tuplePermission;
    }

    public String getRank() {
        return this.rank;
    }

    public void setRank( final String rank ) {
        this.rank = rank;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail( final String email ) {
        this.email = email;
    }

    public String getOrganName() {
        return this.organName;
    }

    public void setOrganName( final String organName ) {
        this.organName = organName;
    }

    public String getPoliceType() {
        return this.policeType;
    }

    public void setPoliceType( final String policeType ) {
        this.policeType = policeType;
    }

    public String getDateOfBirth() {
        return this.dateOfBirth;
    }

    public void setDateOfBirth( final String dateOfBirth ) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPassportNumber() {
        return this.passportNumber;
    }

    public void setPassportNumber( final String passportNumber ) {
        this.passportNumber = passportNumber;
    }

    public String getPatrulImageLink() {
        return this.patrulImageLink;
    }

    public void setPatrulImageLink( final String patrulImageLink ) {
        this.patrulImageLink = patrulImageLink;
    }

    public PatrulFIOData getPatrulFIOData() {
        return this.patrulFIOData;
    }

    public void setPatrulFIOData( final PatrulFIOData patrulFIOData ) {
        this.patrulFIOData = patrulFIOData;
    }

    public PatrulCarInfo getPatrulCarInfo() {
        return this.patrulCarInfo;
    }

    public void setPatrulCarInfo( final PatrulCarInfo patrulCarInfo ) {
        this.patrulCarInfo = patrulCarInfo;
    }

    public PatrulDateData getPatrulDateData() {
        return this.patrulDateData;
    }

    public void setPatrulDateData( final PatrulDateData patrulDateData ) {
        this.patrulDateData = patrulDateData;
    }

    public PatrulAuthData getPatrulAuthData() {
        return this.patrulAuthData;
    }

    public void setPatrulAuthData( final PatrulAuthData patrulAuthData ) {
        this.patrulAuthData = patrulAuthData;
    }

    public PatrulTaskInfo getPatrulTaskInfo() {
        return this.patrulTaskInfo;
    }

    public void setPatrulTaskInfo( final PatrulTaskInfo patrulTaskInfo ) {
        this.patrulTaskInfo = patrulTaskInfo;
    }

    public PatrulTokenInfo getPatrulTokenInfo() {
        return this.patrulTokenInfo;
    }

    public void setPatrulTokenInfo( final PatrulTokenInfo patrulTokenInfo ) {
        this.patrulTokenInfo = patrulTokenInfo;
    }

    public PatrulRegionData getPatrulRegionData() {
        return this.patrulRegionData;
    }

    public void setPatrulRegionData( final PatrulRegionData patrulRegionData ) {
        this.patrulRegionData = patrulRegionData;
    }

    public PatrulLocationData getPatrulLocationData() {
        return this.patrulLocationData;
    }

    public void setPatrulLocationData( final PatrulLocationData patrulLocationData ) {
        this.patrulLocationData = patrulLocationData;
    }

    public PatrulUniqueValues getPatrulUniqueValues() {
        return this.patrulUniqueValues;
    }

    public void setPatrulUniqueValues( final PatrulUniqueValues patrulUniqueValues ) {
        this.patrulUniqueValues = patrulUniqueValues;
    }

    public PatrulMobileAppInfo getPatrulMobileAppInfo() {
        return this.patrulMobileAppInfo;
    }

    public void setPatrulMobileAppInfo(
            final PatrulMobileAppInfo patrulMobileAppInfo
    ) {
        this.patrulMobileAppInfo = patrulMobileAppInfo;
    }

    // уникальное ID патрульного
    private UUID uuid;

    private long totalActivityTime;

    private boolean inPolygon;
    private boolean tuplePermission; // показывает можно ли патрульному участвовать в кортеже

    private String rank;
    private String email;
    private String organName;
    private String policeType; // choosing from dictionary
    private String dateOfBirth;
    private String passportNumber;
    private String patrulImageLink;

    private PatrulFIOData patrulFIOData;
    private PatrulCarInfo patrulCarInfo;
    private PatrulDateData patrulDateData;
    private PatrulAuthData patrulAuthData;
    private PatrulTaskInfo patrulTaskInfo;
    private PatrulTokenInfo patrulTokenInfo;
    private PatrulRegionData patrulRegionData;
    private PatrulLocationData patrulLocationData;
    private PatrulUniqueValues patrulUniqueValues;
    private PatrulMobileAppInfo patrulMobileAppInfo;

    public boolean check () {
        return switch ( this.getPoliceType() ) {
            case "TTG", "PI" -> Duration.between(
                    super.newDate().toInstant(),
                    this.getPatrulDateData().getTaskDate().toInstant()
            ).toMinutes() <= 30;

            default -> super.checkDate( this.getPatrulDateData().getTaskDate().toInstant() );
        };
    }

    /*
        отвязываем патрульного от Эскорта
    */
    public void linkWithEscortCar (
            final UUID uuidOfEscort,
            final UUID uuidForEscortCar
    ) {
        this.getPatrulUniqueValues().setUuidForEscortCar( uuidForEscortCar );
        this.getPatrulUniqueValues().setUuidOfEscort( uuidOfEscort );
    }

    // присваиваем изначальные значения для нового патрульного
    public void setDefaultValuesInTheBeginning () {
        this.setInPolygon( false );
        this.setTotalActivityTime( 0L );
        this.setUuid( UUIDs.timeBased() );
        this.setPatrulLocationData( PatrulLocationData.empty() );
        this.getPatrulAuthData().setInitialPasswordAndLogin( this );
        this.setPatrulDateData( PatrulDateData.empty().update( 2 ) );
        this.setPatrulTaskInfo( PatrulTaskInfo.empty().setInitialValues() );
        this.setPatrulTokenInfo( PatrulTokenInfo.empty().setInitialValues() );
        this.setPatrulUniqueValues( PatrulUniqueValues.empty().setInitialValues() );
        this.setPatrulMobileAppInfo( PatrulMobileAppInfo.empty().setInitialValues() );
        this.getPatrulFIOData().setSurnameNameFatherName( this.getPatrulFIOData().getSurnameNameFatherName() );
    }

    /*
    когда патрульный выходит из системы, то обнуляем его данные
    о сим карте и токен
    */
    public void update () {
        this.getPatrulTokenInfo().setTokenForLogin( null );
        this.getPatrulMobileAppInfo().setSimCardNumber( null );
    }

    public void update ( final String simCardNumber ) {
        this.getPatrulMobileAppInfo().setSimCardNumber( simCardNumber );
        this.getPatrulTokenInfo().setTokenForLogin ( super.concatNames( this ) );
    }

    public void update (
            final TaskTypes taskTypes,
            final double latitudeOfTask,
            final double longitudeOfTask,
            final String taskId
    ) {
        this.getPatrulTaskInfo().setTaskId( taskId );
        this.getPatrulTaskInfo().setTaskTypes( taskTypes );
        this.getPatrulLocationData().setLatitudeOfTask( latitudeOfTask );
        this.getPatrulLocationData().setLongitudeOfTask( longitudeOfTask );
    }

    public void update (
            final TaskTypes taskTypes,
            final DataInfo dataInfo,
            final String taskId
    ) {
        this.getPatrulTaskInfo().setTaskId( taskId );
        this.getPatrulTaskInfo().setTaskTypes( taskTypes );
        this.getPatrulLocationData().changeLocationFromCadastre( dataInfo );
    }

    public static Patrul empty () {
        return new Patrul();
    }

    private Patrul () {}

    public Patrul ( final Row row ) {
        this.generate( row );
    }

    @Override
    public Patrul generate( final Row row ) {
        super.checkAndSetParams(
                row,
                row1 -> {
                    this.setUuid( row.getUUID( "uuid" ) );
                    this.setInPolygon( row.getBool( "inPolygon" ) );
                    this.setTuplePermission( row.getBool( "tuplePermission" ) );
                    this.setTotalActivityTime( row.getLong( "totalActivityTime" ) );

                    this.setRank( row.getString( "rank" ) );
                    this.setEmail( row.getString( "email" ) );
                    this.setOrganName( row.getString( "organName" ) );
                    this.setPoliceType( row.getString( "policeType" ) );
                    this.setDateOfBirth( row.getString( "dateOfBirth" ) );
                    this.setPassportNumber( row.getString( "passportNumber" ) );
                    this.setPatrulImageLink( row.getString( "patrulImageLink" ) );

                    this.setPatrulCarInfo( PatrulCarInfo.empty().generate( row.getUDTValue( "patrulCarInfo" ) ) );
                    this.setPatrulFIOData( PatrulFIOData.empty().generate( row.getUDTValue( "patrulFIOData" ) ) );
                    this.setPatrulDateData( PatrulDateData.empty().generate( row.getUDTValue( "patrulDateData" ) ) );
                    this.setPatrulTaskInfo( PatrulTaskInfo.empty().generate( row.getUDTValue( "patrulTaskInfo" ) ) );
                    this.setPatrulAuthData( PatrulAuthData.empty().generate( row.getUDTValue( "patrulAuthData" ) ) );
                    this.setPatrulTokenInfo( PatrulTokenInfo.empty().generate( row.getUDTValue( "patrulTokenInfo" ) ) );
                    this.setPatrulRegionData( PatrulRegionData.empty().generate( row.getUDTValue( "patrulRegionData" ) ) );
                    this.setPatrulLocationData( PatrulLocationData.empty().generate( row.getUDTValue( "patrulLocationData" ) ) );
                    this.setPatrulUniqueValues( PatrulUniqueValues.empty().generate( row.getUDTValue( "patrulUniqueValues" ) ) );
                    this.setPatrulMobileAppInfo( PatrulMobileAppInfo.empty().generate( row.getUDTValue( "patrulMobileAppInfo" ) ) );
                }
        );

        return this;
    }

    @Override
    public Patrul generate( final UDTValue udtValue ) {
        super.checkAndSetParams(
                udtValue,
                udtValue1 -> {
                    this.setUuid( udtValue.getUUID( "uuid" ) );
                    this.setInPolygon( udtValue.getBool( "inPolygon" ) );
                    this.setTuplePermission( udtValue.getBool( "tuplePermission" ) );
                    this.setTotalActivityTime( udtValue.getLong( "totalActivityTime" ) );

                    this.setRank( udtValue.getString( "rank" ) );
                    this.setEmail( udtValue.getString( "email" ) );
                    this.setOrganName( udtValue.getString( "organName" ) );
                    this.setPoliceType( udtValue.getString( "policeType" ) );
                    this.setDateOfBirth( udtValue.getString( "dateOfBirth" ) );
                    this.setPassportNumber( udtValue.getString( "passportNumber" ) );
                    this.setPatrulImageLink( udtValue.getString( "patrulImageLink" ) );

                    this.setPatrulFIOData( PatrulFIOData.empty().generate( udtValue ) );
                    this.setPatrulCarInfo( PatrulCarInfo.empty().generate( udtValue ) );
                    this.setPatrulDateData( PatrulDateData.empty().generate( udtValue ) );
                    this.setPatrulTaskInfo( PatrulTaskInfo.empty().generate( udtValue ) );
                    this.setPatrulAuthData( PatrulAuthData.empty().generate( udtValue ) );
                    this.setPatrulTokenInfo( PatrulTokenInfo.empty().generate( udtValue ) );
                    this.setPatrulRegionData( PatrulRegionData.empty().generate( udtValue ) );
                    this.setPatrulLocationData( PatrulLocationData.empty().generate( udtValue ) );
                    this.setPatrulUniqueValues( PatrulUniqueValues.empty().generate( udtValue ) );
                }
        );
        return this;
    }

    @Override
    public UDTValue fillUdtByEntityParams( final UDTValue udtValue ) {
        return udtValue.setUUID( "uuid", this.getUuid() )

                .setLong( "totalActivityTime", this.getTotalActivityTime() )

                .setBool( "inPolygon", this.getInPolygon() )
                .setBool( "tuplePermission", this.getTuplePermission() )

                .setString( "rank", this.getRank() )
                .setString( "email", this.getEmail() )
                .setString( "organName", this.getOrganName() )
                .setString( "policeType", this.getPoliceType() )
                .setString( "dateOfBirth", this.getDateOfBirth() )
                .setString( "passportNumber", this.getPassportNumber() )
                .setString( "patrulImageLink", this.getPatrulImageLink() );
    }
}
