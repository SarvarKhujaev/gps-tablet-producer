package com.ssd.mvd.gpstabletsservice.database;

import java.util.*;
import java.lang.reflect.Field;
import java.util.stream.Stream;

import com.ssd.mvd.gpstabletsservice.entity.*;
import com.ssd.mvd.gpstabletsservice.tuple.Points;
import com.ssd.mvd.gpstabletsservice.database.codec.*;
import com.ssd.mvd.gpstabletsservice.task.card.ReportForCard;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.entity.polygons.PolygonType;
import com.ssd.mvd.gpstabletsservice.entity.polygons.PolygonEntity;
import com.ssd.mvd.gpstabletsservice.task.taskStatisticsSer.PositionInfo;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ViolationsInformation;

@lombok.Data
public class CassandraConverter extends LogInspector {
    private String temp;
    private String result;

    public String getALlNames ( final Class object ) {
        final StringBuilder result = new StringBuilder( "( " );
        this.getFields( object ).forEach( field -> result.append( field.getName() ).append( ", " ) );
        return result.substring( 0, result.length() - 2 ) + " )"; }

    private Stream< Field > getFields ( final Class object ) { return Arrays.stream( object.getDeclaredFields() )
            .toList()
            .stream(); }

    public String convertClassToCassandra ( final Class object ) {
        final StringBuilder result = new StringBuilder( "( " );
        this.getFields( object )
                .filter( field -> field.getType().equals( String.class )
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

    public String convertListToCassandra ( final List< UUID > list ) {
        result = "[";
        list.forEach( s -> result += s + ", " );
        return result.length() == 1 ? result + "]" : result.substring( 0, result.length() - 2 ) + "]"; }

    public String convertClassToCassandraTable ( final Object object ) {
        final StringBuilder result = new StringBuilder( "{ " );
        this.getFields( object.getClass() ).forEach( field -> {
            try {
                result.append( field.getName() ).append( " : " );
                org.springframework.util.ReflectionUtils.makeAccessible( field );
                result.append( field.get( object ) instanceof String
                        ? "'" + ( (String) field.get( object ) ).replaceAll( "'", "" ) + "'"
                        : field.get( object ) ).append( ", " ); }
            catch ( IllegalAccessException e ) { super.logging( e ); } } );
        return result.substring( 0, result.length() - 2 ) + "}"; }

    public String convertListOfPointsToCassandra ( final List< ? > pointsList ) {
        result = "[";
        ( super.getCheckParam().test( pointsList ) ? pointsList : new ArrayList<>() )
                .forEach( points -> result += this.convertClassToCassandraTable( points ) + ", " );
        return result.length() == 1 ? result + "]" : result.substring( 0, result.length() - 2 ) + "]"; }

    public String convertMapToCassandra ( final Map< String, String > listOfTasks ) {
        result = "{";
        listOfTasks.keySet().forEach( s -> result += "'" + s + "' : '" + listOfTasks.get( s ) + "', " );
        return result.length() == 1 ? result + "}" : result.substring( 0, result.length() - 2 ) + "}"; }

    public String convertSosMapToCassandra ( final Map< UUID, String > listOfTasks ) {
        temp = "{";
        listOfTasks.keySet().forEach( key -> temp += "" + key + " : '" + listOfTasks.get( key ) + "', ");
        return temp.length() == 1 ? temp + "}" : temp.substring( 0, temp.length() - 2 ) + "}"; }

    public void registerCodecForPatrul ( final String dbName, final String userType ) {
        CassandraDataControl
                .getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry()
                .register( new CodecRegistrationForPatrul ( CassandraDataControl
                        .getInstance()
                        .getCodecRegistry()
                        .codecFor( CassandraDataControl
                                .getInstance()
                                .getCluster()
                                .getMetadata()
                                .getKeyspace( dbName )
                                .getUserType( userType ) ),
                        Patrul.class ) ); }

    public void registerCodecForReport ( final String dbName, final String userType ) {
        CassandraDataControl
                .getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry()
                .register( new CodecRegistrationForReport ( CassandraDataControl
                        .getInstance()
                        .getCodecRegistry()
                        .codecFor( CassandraDataControl
                                .getInstance()
                                .getCluster()
                                .getMetadata()
                                .getKeyspace( dbName )
                                .getUserType( userType ) ),
                        ReportForCard.class ) ); }

    public void registerCodecForPoliceType ( final String dbName, final String userType ) {
        CassandraDataControl
                .getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry()
                .register( new CodecRegistrationForPoliceType( CassandraDataControl
                        .getInstance()
                        .getCodecRegistry()
                        .codecFor( CassandraDataControl
                                .getInstance()
                                .getCluster()
                                .getMetadata()
                                .getKeyspace( dbName )
                                .getUserType( userType ) ),
                        PoliceType.class ) ); }

    public void registerCodecForCameraList ( final String dbName, final String userType ) {
        CassandraDataControl
                .getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry()
                .register( new CodecRegistrationForCameraList ( CassandraDataControl
                        .getInstance()
                        .getCodecRegistry()
                        .codecFor( CassandraDataControl
                                .getInstance()
                                .getCluster()
                                .getMetadata()
                                .getKeyspace( dbName )
                                .getUserType( userType ) ),
                                CameraList.class ) ); }

    public void registerCodecForPointsList ( final String dbName, final String userType ) {
        CassandraDataControl
                .getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry()
                .register ( new CodecRegistrationForPointsList( CassandraDataControl
                                        .getInstance()
                                        .getCodecRegistry()
                                        .codecFor( CassandraDataControl
                                                        .getInstance()
                                                        .getCluster()
                                                        .getMetadata()
                                                        .getKeyspace( dbName )
                                                        .getUserType( userType ) ),
                                Points.class ) ); }

    public void registerCodecForPolygonType ( final String dbName, final String userType ) {
        CassandraDataControl
                .getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry()
                .register( new CodecRegistrationForPolygonType(
                                CassandraDataControl
                                        .getInstance()
                                        .getCodecRegistry()
                                        .codecFor( CassandraDataControl
                                                        .getInstance()
                                                        .getCluster()
                                                        .getMetadata()
                                                        .getKeyspace( dbName )
                                                        .getUserType( userType ) ),
                                PolygonType.class ) ); }

    public void registerCodecForPositionInfo ( final String dbName, final String userType ) {
        CassandraDataControl
                .getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry()
                .register( new CodecRegistrationForPositionInfo( CassandraDataControl
                        .getInstance()
                        .getCodecRegistry()
                        .codecFor( CassandraDataControl
                                .getInstance()
                                .getCluster()
                                .getMetadata()
                                .getKeyspace( dbName )
                                .getUserType( userType ) ),
                        PositionInfo.class ) ); }

    public void registerCodecForPolygonEntity ( final String dbName, final String userType ) {
        CassandraDataControl
                .getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry()
                .register ( new CodecRegistrationForPolygonEntity( CassandraDataControl
                                        .getInstance()
                                        .getCodecRegistry()
                                        .codecFor ( CassandraDataControl
                                                        .getInstance()
                                                        .getCluster()
                                                        .getMetadata()
                                                        .getKeyspace( dbName )
                                                        .getUserType( userType ) ),
                                PolygonEntity.class ) ); }

    public void registerCodecForViolationsInformation ( final String dbName, final String userType ) {
        CassandraDataControl
                .getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry()
                .register( new CodecRegistrationForViolationsInformation ( CassandraDataControl
                                        .getInstance()
                                        .getCodecRegistry()
                                        .codecFor( CassandraDataControl
                                                        .getInstance()
                                                        .getCluster()
                                                        .getMetadata()
                                                        .getKeyspace( dbName )
                                                        .getUserType( userType ) ),
                                ViolationsInformation.class ) ); }
}
