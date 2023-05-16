package com.ssd.mvd.gpstabletsservice.database;

import java.util.*;
import java.lang.reflect.Field;
import java.util.stream.Stream;
import java.util.function.Function;

import com.ssd.mvd.gpstabletsservice.database.codec.*;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.constants.CassandraTables;

public class CassandraConverter extends LogInspector {
    private String temp;
    private String result;

    protected void registerCodec (
            final CassandraTables keyspace,
            final CassandraTables userType,
            final Integer value,
            final Class clazz ) {
        CassandraDataControl
                .getInstance() // create a new codec for PolygonEntity.class
                .getCodecRegistry()
                .register( new CodecRegistration( CassandraDataControl
                        .getInstance()
                        .getCodecRegistry()
                        .codecFor( CassandraDataControl
                                .getInstance()
                                .getCluster()
                                .getMetadata()
                                .getKeyspace( keyspace.name() )
                                .getUserType( userType.name() ) ),
                        clazz,
                        value ) ); }

    protected final Function< Class, String > getALlNames = object -> {
            final StringBuilder result = new StringBuilder( "( " );
            this.getFields.apply( object ).forEach( field -> result.append( field.getName() ).append( ", " ) );
            return result.substring( 0, result.length() - 2 ) + " )"; };

    protected final Function< Class, String > convertClassToCassandra = object -> {
            final StringBuilder result = new StringBuilder( "( " );
            this.getFields.apply( object )
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
            return result.substring( 0, result.toString().length() - 2 ); };

    protected final Function< List< UUID >, String > convertListToCassandra = list -> {
            result = "[";
            list.forEach( s -> result += s + ", " );
            return result.length() == 1 ? result + "]" : result.substring( 0, result.length() - 2 ) + "]"; };

    protected final Function< Object, String > convertClassToCassandraTable = object -> {
            final StringBuilder result = new StringBuilder( "{ " );
            this.getFields.apply( object.getClass() ).forEach( field -> {
                try {
                    result.append( field.getName() ).append( " : " );
                    org.springframework.util.ReflectionUtils.makeAccessible( field );
                    result.append( field.get( object ) instanceof String
                            ? "'" + ( (String) field.get( object ) ).replaceAll( "'", "" ) + "'"
                            : field.get( object ) ).append( ", " ); }
                catch ( final IllegalAccessException e ) { super.logging( e ); } } );
            return result.substring( 0, result.length() - 2 ) + "}"; };

    protected final Function< List< ? >, String > convertListOfPointsToCassandra = pointsList -> {
            result = "[";
            ( super.getCheckParam().test( pointsList ) ? pointsList : new ArrayList<>() )
                    .forEach( points -> result += this.convertClassToCassandraTable.apply( points ) + ", " );
            return result.length() == 1 ? result + "]" : result.substring( 0, result.length() - 2 ) + "]"; };

    protected final Function< Map< String, String >, String > convertMapToCassandra = listOfTasks -> {
            result = "{";
            listOfTasks.keySet().forEach( s -> result += "'" + s + "' : '" + listOfTasks.get( s ) + "', " );
            return result.length() == 1 ? result + "}" : result.substring( 0, result.length() - 2 ) + "}"; };

    protected final Function< Map< UUID, String >, String > convertSosMapToCassandra = listOfTasks -> {
            temp = "{";
            listOfTasks.keySet().forEach( key -> temp += "" + key + " : '" + listOfTasks.get( key ) + "', ");
            return temp.length() == 1 ? temp + "}" : temp.substring( 0, temp.length() - 2 ) + "}"; };

    private final Function< Class, Stream< Field > > getFields = object -> Arrays.stream( object.getDeclaredFields() ).toList().stream();
}
