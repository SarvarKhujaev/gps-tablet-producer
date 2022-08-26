package com.ssd.mvd.gpstabletsservice.database;

import java.util.*;
import lombok.Data;
import java.lang.reflect.Field;
import java.util.stream.Stream;

import com.ssd.mvd.gpstabletsservice.codec.*;
import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;

@Data
public class CassandraConverter {
    private String result;

    private static CassandraConverter cassandraConverter = new CassandraConverter();

    public static CassandraConverter getInstance() { return cassandraConverter; }

    public Stream< Field > getFields ( Class object ) { return Arrays.stream(
                    object.getDeclaredFields() )
            .toList()
            .stream(); }

    public String getALlNames ( Class object ) {
        StringBuilder result = new StringBuilder( "( " );
        this.getFields( object )
            .forEach( field -> result.append( field.getName() ).append( ", " ) );
        return result.substring( 0, result.length() - 2 ) + " )"; }

    public String convertListToCassandra ( List< UUID > list ) {
        result = "[";
        list.forEach( s -> result += s + ", " );
        return result.length() == 1 ? result + "]" : result.substring( 0, result.length() - 2 ) + "]"; }

    public String convertListOfStringToCassandra ( List< String > images ) {
        result = "[";
        images.forEach( s -> result += s + "', '" );
        return result.length() == 1 ? result + "]" : result.substring( 0, result.length() - 3 ) + "]"; }

    public String convertListOfReportToCassandra ( List< ReportForCard > images ) {
        result = "[";
        images.forEach( s -> result += this.convertClassToCassandraTable( s ) + ", " );
        return result.length() == 1 ? result + "]" : result.substring( 0, result.length() - 3 ) + "]"; }

    public String convertListOfCameraListToCassandra ( List< CameraList > cameraLists ) {
        result = "[";
        cameraLists.forEach ( cameraList -> result += "{ "
                + "rtspLink : " + cameraList.getRtspLink() + ", "
                + "cameraName : " + cameraList.getCameraName() + " }, " );
        result = result.substring( 0, result.length() - 2 );
        return result + "]"; }

    public String convertListOfPolygonEntityToCassandra ( List< PolygonEntity > latlngs ) {
        result = "[";
        latlngs.forEach( polygonEntity -> result += "{ "
                + "lat : " + polygonEntity.getLat() + ", "
                + "lng : " + polygonEntity.getLng() + " }, " );
        return result.length() == 1 ? result + "]" : result.substring( 0, result.length() - 2 ) + "]"; }

    public String convertListOfViolationsToCassandra ( List< ViolationsInformation > images ) {
        result = "[";
        images.forEach( s -> result += this.convertClassToCassandraTable( s ) + ", " );
        return result.length() == 1 ? result + "]" : result.substring( 0, result.length() - 3 ) + "]"; }

    public String convertMapToCassandra ( Map< String, String > listOfTasks ) {
        result = "{";
        listOfTasks.keySet().forEach( s -> result += "'" + s + "' : '" + listOfTasks.get( s ) + "', " );
        return result.length() == 1 ? result + "}" : result.substring( 0, result.length() - 2 ) + "}"; }

    public String convertMapOfPatrulToCassandra ( Map< UUID, Patrul > listOfTasks ) {
        result = "{";
        listOfTasks.keySet().forEach( s -> result += "'" + s + "' : " +
                this.convertClassToCassandraTable( listOfTasks.get( s ) ) + ", " );
        return result.length() == 1 ? result + "}" : result.substring( 0, result.length() - 2 ) + "}"; }

    public String convertClassToCassandra ( Class object ) {
        StringBuilder result = new StringBuilder( "( " );
        this.getFields( object )
                .filter(
                        field -> field.getType().equals( String.class )
                                ^ field.getType().equals( Integer.class )
                                ^ field.getType().equals( Double.class )
                                ^ field.getType().equals( UUID.class )
                                ^ field.getType().equals( Long.class )
                                ^ field.getType().equals( Date.class )
                                ^ field.getType().equals( Boolean.class ) )
                .forEach( field -> {
                        result.append( field.getName() );
                    if ( field.getType().equals( String.class ) ) result.append( " text, " );
                    else if ( field.getType().equals( UUID.class ) ) result.append( " uuid, " );
                    else if ( field.getType().equals( Long.class ) ) result.append( " bigint, " );
                    else if ( field.getType().equals( Integer.class ) ) result.append( " int, " );
                    else if ( field.getType().equals( Double.class ) ) result.append( " double, " );
                    else if ( field.getType().equals( Date.class ) ) result.append( " timestamp, " );
                    else if ( field.getType().equals( Boolean.class ) ) result.append( " boolean, " ); } );
        return result.substring( 0, result.toString().length() - 2 ); }

    public String convertClassToCassandraTable ( Object object ) {
        StringBuilder result = new StringBuilder( "{ " );
        this.getFields( object.getClass() )
                .forEach( field -> {
                    try {
                        result.append( field.getName() )
                                .append( " : " );
                        org.springframework.util.ReflectionUtils.makeAccessible( field );
                        result.append( field.get( object ) instanceof String ?
                                        "'" + ( (String) field.get( object ) ).replaceAll( "'", "" ) + "'"
                                : field.get( object ) ).append( ", " );
                    } catch ( IllegalAccessException e ) { e.printStackTrace(); }
                } ); return result.substring( 0, result.length() - 2 ) + "}"; }

    public void registerCodecForPatrul ( String dbName, String userType ) {
        CassandraDataControl.getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry().register(
                        new CodecRegistrationForPatrul (
                                CassandraDataControl
                                        .getInstance()
                                        .getCodecRegistry()
                                        .codecFor(
                                                CassandraDataControl
                                                        .getInstance()
                                                        .getCluster()
                                                        .getMetadata()
                                                        .getKeyspace( dbName )
                                                        .getUserType( userType )
                                        ), Patrul.class ) ); }

    public void registerCodecForReport ( String dbName, String userType ) {
        CassandraDataControl.getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry().register(
                        new CodecRegistrationForReport (
                                CassandraDataControl
                                        .getInstance()
                                        .getCodecRegistry()
                                        .codecFor(
                                                CassandraDataControl
                                                        .getInstance()
                                                        .getCluster()
                                                        .getMetadata()
                                                        .getKeyspace( dbName )
                                                        .getUserType( userType )
                                        ), ReportForCard.class ) ); }

    public void registerCodecForPoliceType ( String dbName, String userType ) {
        CassandraDataControl.getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry()
                .register(
                        new CodecRegistrationForPoliceType (
                                CassandraDataControl
                                        .getInstance()
                                        .getCodecRegistry()
                                        .codecFor(
                                                CassandraDataControl
                                                        .getInstance()
                                                        .getCluster()
                                                        .getMetadata()
                                                        .getKeyspace( dbName )
                                                        .getUserType( userType )
                                        ), PoliceType.class ) ); }

    public void registerCodecForCameraList ( String dbName, String userType ) {
        CassandraDataControl.getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry().register(
                        new CodecRegistrationForCameraList (
                                CassandraDataControl
                                        .getInstance()
                                        .getCodecRegistry()
                                        .codecFor(
                                                CassandraDataControl
                                                        .getInstance()
                                                        .getCluster()
                                                        .getMetadata()
                                                        .getKeyspace( dbName )
                                                        .getUserType( userType )
                                        ), CameraList.class ) ); }

    public void registerCodecForPolygonEntity ( String dbName, String userType ) {
        System.out.println( "Codec is ready" );
        CassandraDataControl.getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry()
                .register (
                        new CodecRegistrationForPolygonEntity (
                                CassandraDataControl
                                        .getInstance()
                                        .getCodecRegistry()
                                        .codecFor(
                                                CassandraDataControl
                                                        .getInstance()
                                                        .getCluster()
                                                        .getMetadata()
                                                        .getKeyspace( dbName )
                                                        .getUserType( userType )
                                        ), PolygonEntity.class ) ); }

    public void registerCodecForViolationsInformation ( String dbName, String userType ) {
        CassandraDataControl.getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry().register(
                        new CodecRegistrationForViolationsInformation (
                                CassandraDataControl
                                        .getInstance()
                                        .getCodecRegistry()
                                        .codecFor(
                                                CassandraDataControl
                                                        .getInstance()
                                                        .getCluster()
                                                        .getMetadata()
                                                        .getKeyspace( dbName )
                                                        .getUserType( userType )
                                        ), ViolationsInformation.class ) ); }

    public void registerCodecForPolygonType ( String dbName, String userType ) {
        CassandraDataControl.getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry().register(
                        new CodecRegistrationForPolygonType (
                                CassandraDataControl
                                        .getInstance()
                                        .getCodecRegistry()
                                        .codecFor(
                                                CassandraDataControl
                                                        .getInstance()
                                                        .getCluster()
                                                        .getMetadata()
                                                        .getKeyspace( dbName )
                                                        .getUserType( userType )
                                        ), PolygonType.class ) ); }
}