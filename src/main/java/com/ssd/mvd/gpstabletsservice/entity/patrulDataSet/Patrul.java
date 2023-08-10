package com.ssd.mvd.gpstabletsservice.entity.patrulDataSet;

import com.ssd.mvd.gpstabletsservice.task.findFaceFromAssomidin.car_events.DataInfo;
import com.ssd.mvd.gpstabletsservice.inspectors.DataValidateInspector;
import static com.ssd.mvd.gpstabletsservice.constants.Status.FREE;
import com.ssd.mvd.gpstabletsservice.inspectors.TimeInspector;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.constants.Status;

import com.datastax.driver.core.UDTValue;
import com.datastax.driver.core.Row;

import java.util.function.Supplier;
import java.time.Duration;
import java.util.*;

@lombok.Data
@lombok.NoArgsConstructor
@lombok.AllArgsConstructor
public final class Patrul {
    private Date taskDate; // for registration of exact time when patrul started to deal with task
    private Date lastActiveDate; // shows when user was online lastly
    private Date startedToWorkDate; // the time
    private Date dateOfRegistration;

    private Double distance;
    private Double latitude; // the current location of the user
    private Double longitude; // the current location of the user
    private Double latitudeOfTask;
    private Double longitudeOfTask;

    private UUID uuid; // own id of the patrul
    private UUID organ; // choosing from dictionary
    private UUID sos_id; // choosing from dictionary
    private UUID uuidOfEscort; // UUID of the Escort which this car is linked to
    private UUID uuidForPatrulCar; // choosing from dictionary
    private UUID uuidForEscortCar; // choosing from dictionary

    private Long regionId;
    private Long mahallaId;
    private Long districtId; // choosing from dictionary
    private Long totalActivityTime;

    private Integer batteryLevel;
    private Boolean inPolygon;
    private Boolean tuplePermission; // показывает можно ли патрульному участвовать в кортеже

    private String name;
    private String rank;
    private String email;
    private String login;
    private String taskId;
    private String carType; // модель машины
    private String surname;
    private String password;
    private String carNumber;
    private String organName;
    private String regionName;
    private String policeType; // choosing from dictionary
    private String fatherName;
    private String dateOfBirth;
    private String phoneNumber;
    private String districtName;
    private String specialToken;
    private String tokenForLogin;
    private String simCardNumber;
    private String passportNumber;
    private String patrulImageLink;
    private String surnameNameFatherName; // Ф.И.О

    private Status status; // busy, free by default, available or not available
    private TaskTypes taskTypes; // task type which was attached to the current patrul
    private Map< String, String > listOfTasks = new HashMap<>(); // the list which will store ids of all tasks which have been completed by Patrul

    public UUID getUuid () { return this.uuid != null ? uuid : ( this.uuid = UUID.randomUUID() ); }

    public Boolean check () { return switch ( this.getPoliceType() ) {
        case "TTG", "PI" -> Duration.between( new Date().toInstant(), this.getTaskDate().toInstant() ).toMinutes() <= 30;
        default -> TimeInspector
                .getInspector()
                .getCheckDate()
                .test( this.getTaskDate().toInstant() ); }; }

    public String getSurnameNameFatherName () {
        return Optional.ofNullable( this.surnameNameFatherName )
                .filter( s -> this.surnameNameFatherName != null
                        && this.surnameNameFatherName.contains( "NULL" )
                        && this.surnameNameFatherName.contains( "null" ) )
                .orElse( ( this.surnameNameFatherName = String.join( " ", this.getName(), this.getSurname(), this.getFatherName() ) ) ); }

    // освобождаем патрульного от таска
    public void free () {
        this.setTaskId( null );
        this.setStatus( Status.FREE );
        this.setTaskTypes( TaskTypes.FREE ); }

    // присваиваем изначальные значения для нового патрульного
    public void begin () {
        this.setStatus( FREE );
        this.setInPolygon( false );
        this.setTotalActivityTime( 0L );
        this.setTaskTypes( TaskTypes.FREE );
        this.setListOfTasks( new HashMap<>() ); }

    public void update ( final Integer value ) {
        final Date date = TimeInspector.getInspector().getGetNewDate().get();
        switch ( value ) {
            case 2 -> {
                this.setTaskDate( date );
                this.setLastActiveDate( date );
                this.setStartedToWorkDate( date );
                this.setDateOfRegistration( date ); }
            case 1 -> this.setTaskDate( date );
            default -> this.setStartedToWorkDate( date ); } }

    public void update ( final TaskTypes taskTypes ) { this.getListOfTasks().putIfAbsent( this.getTaskId(), taskTypes.name() ); }

    public void update ( final String simCardNumber ) {
        this.setSimCardNumber( simCardNumber );
        this.setTokenForLogin ( DataValidateInspector
                .getInstance()
                .concatNames
                .apply( this, 4 ) ); }

    public void update ( final TaskTypes taskTypes,
                         final Double latitudeOfTask,
                         final Double longitudeOfTask,
                         final String taskId ) {
        this.setLongitudeOfTask( longitudeOfTask );
        this.setLatitudeOfTask( latitudeOfTask );
        this.setTaskTypes( taskTypes );
        this.setTaskId( taskId ); }

    public void update ( final TaskTypes taskTypes,
                         final DataInfo dataInfo,
                         final String taskId ) {
        if ( DataValidateInspector
                .getInstance()
                .checkParam
                .test( dataInfo )
                && DataValidateInspector
                .getInstance()
                .checkParam
                .test( dataInfo.getCadaster() ) ) {
            this.setLongitudeOfTask( dataInfo.getCadaster().getLongitude() );
            this.setLatitudeOfTask( dataInfo.getCadaster().getLatitude() ); }
        this.setTaskTypes( taskTypes );
        this.setTaskId( taskId ); }

    public Patrul ( final Row row ) { Optional.ofNullable( row ).ifPresent( row1 -> {
            this.setTaskDate( row.getTimestamp( "taskDate" ) );
            this.setLastActiveDate( row.getTimestamp( "lastActiveDate" ) );
            this.setStartedToWorkDate( row.getTimestamp( "startedToWorkDate" ) );
            this.setDateOfRegistration( row.getTimestamp( "dateOfRegistration" ) );

            this.setDistance( row.getDouble( "distance" ) );
            this.setLatitude( row.getDouble( "latitude" ) );
            this.setLongitude( row.getDouble( "longitude" ) );
            this.setLatitudeOfTask( row.getDouble( "latitudeOfTask" ) );
            this.setLongitudeOfTask( row.getDouble( "longitudeOfTask" ) );

            this.setUuid( row.getUUID( "uuid" ) );
            this.setOrgan( row.getUUID( "organ" ) );
            this.setSos_id( row.getUUID( "sos_id" ) );
            this.setUuidOfEscort( row.getUUID( "uuidOfEscort" ) );
            this.setUuidForPatrulCar( row.getUUID( "uuidForPatrulCar" ) );
            this.setUuidForEscortCar( row.getUUID( "uuidForEscortCar" ) );

            this.setRegionId( row.getLong( "regionId" ) );
            this.setMahallaId( row.getLong( "mahallaId" ) );
            this.setDistrictId( row.getLong( "districtId" ) );
            this.setTotalActivityTime( row.getLong( "totalActivityTime" ) );

            this.setInPolygon( row.getBool( "inPolygon" ) );
            this.setBatteryLevel( row.getInt( "batteryLevel" ) );
            this.setTuplePermission( row.getBool( "tuplePermission" ) );

            this.setName( row.getString( "name" ) );
            this.setRank( row.getString( "rank" ) );
            this.setEmail( row.getString( "email" ) );
            this.setLogin( row.getString( "login" ) );
            this.setTaskId( row.getString( "taskId" ) );
            this.setCarType( row.getString( "carType" ) );
            this.setSurname( row.getString( "surname" ) );
            this.setPassword( row.getString( "password" ) );
            this.setCarNumber( row.getString( "carNumber" ) );
            this.setOrganName( row.getString( "organName" ) );
            this.setRegionName( row.getString( "regionName" ) );
            this.setPoliceType( row.getString( "policeType" ) );
            this.setFatherName( row.getString( "fatherName" ) );
            this.setDateOfBirth( row.getString( "dateOfBirth" ) );
            this.setPhoneNumber( row.getString( "phoneNumber" ) );
            this.setDistrictName( row.getString( "districtName" ) );
            this.setSpecialToken( row.getString( "specialToken" ) );
            this.setTokenForLogin( row.getString( "tokenForLogin" ) );
            this.setSimCardNumber( row.getString( "simCardNumber" ) );
            this.setPassportNumber( row.getString( "passportNumber" ) );
            this.setPatrulImageLink( row.getString( "patrulImageLink" ) );
            this.setSurnameNameFatherName( row.getString( "surnameNameFatherName" ) );

            this.setStatus( Status.valueOf( row.getString( "status" ) ) );
            this.setTaskTypes( TaskTypes.valueOf( row.getString( "taskTypes" ) ) );
            this.setListOfTasks( row.getMap( "listOfTasks", String.class, String.class ) ); } ); }

    public Patrul ( final UDTValue row ) {
        this.setTaskDate( row.getTimestamp( "taskDate" ) );
        this.setLastActiveDate( row.getTimestamp( "lastActiveDate" ) );
        this.setStartedToWorkDate( row.getTimestamp( "startedToWorkDate" ) );
        this.setDateOfRegistration( row.getTimestamp( "dateOfRegistration" ) );

        this.setDistance( row.getDouble( "distance" ) );
        this.setLatitude( row.getDouble( "latitude" ) );
        this.setLongitude( row.getDouble( "longitude" ) );
        this.setLatitudeOfTask( row.getDouble( "latitudeOfTask" ) );
        this.setLongitudeOfTask( row.getDouble( "longitudeOfTask" ) );

        this.setUuid( row.getUUID( "uuid" ) );
        this.setOrgan( row.getUUID( "organ" ) );
        this.setSos_id( row.getUUID( "sos_id" ) );
        this.setUuidOfEscort( row.getUUID( "uuidOfEscort" ) );
        this.setUuidForPatrulCar( row.getUUID( "uuidForPatrulCar" ) );
        this.setUuidForEscortCar( row.getUUID( "uuidForEscortCar" ) );

        this.setRegionId( row.getLong( "regionId" ) );
        this.setMahallaId( row.getLong( "mahallaId" ) );
        this.setDistrictId( row.getLong( "districtId" ) );
        this.setTotalActivityTime( row.getLong( "totalActivityTime" ) );

        this.setInPolygon( row.getBool( "inPolygon" ) );
        this.setBatteryLevel( row.getInt( "batteryLevel" ) );
        this.setTuplePermission( row.getBool( "tuplePermission" ) );

        this.setName( row.getString( "name" ) );
        this.setRank( row.getString( "rank" ) );
        this.setEmail( row.getString( "email" ) );
        this.setLogin( row.getString( "login" ) );
        this.setTaskId( row.getString( "taskId" ) );
        this.setCarType( row.getString( "carType" ) );
        this.setSurname( row.getString( "surname" ) );
        this.setPassword( row.getString( "password" ) );
        this.setCarNumber( row.getString( "carNumber" ) );
        this.setOrganName( row.getString( "organName" ) );
        this.setRegionName( row.getString( "regionName" ) );
        this.setPoliceType( row.getString( "policeType" ) );
        this.setFatherName( row.getString( "fatherName" ) );
        this.setDateOfBirth( row.getString( "dateOfBirth" ) );
        this.setPhoneNumber( row.getString( "phoneNumber" ) );
        this.setSpecialToken( row.getString( "specialToken" ) );
        this.setDistrictName( row.getString( "districtName" ) );
        this.setTokenForLogin( row.getString( "tokenForLogin" ) );
        this.setSimCardNumber( row.getString( "simCardNumber" ) );
        this.setPassportNumber( row.getString( "passportNumber" ) );
        this.setPatrulImageLink( row.getString( "patrulImageLink" ) );
        this.setSurnameNameFatherName( row.getString( "surnameNameFatherName" ) );

        this.setStatus( Status.valueOf( row.getString( "status" ) ) );
        this.setTaskTypes( TaskTypes.valueOf( row.getString( "taskTypes" ) ) );
        this.setListOfTasks( row.getMap( "listOfTasks", String.class, String.class ) ); }
}
