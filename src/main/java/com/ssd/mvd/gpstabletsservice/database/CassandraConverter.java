package com.ssd.mvd.gpstabletsservice.database;

import java.util.*;
import java.lang.reflect.Field;
import java.util.stream.Stream;
import java.util.function.Function;

import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.constants.CassandraDataTypes;

public class CassandraConverter extends LogInspector {
    protected CassandraConverter () {}

    /*
    принимает экземпляра Class и возвращает список названий всех его параметров
    */
    protected final Function< Class, String > getALlParamsNamesForClass = object -> {
            final StringBuilder result = super.newStringBuilder( "" );
            this.getFields.apply( object ).forEach( field -> result.append( field.getName() ).append( ", " ) );
            return super.joinTextWithCorrectCollectionEnding(
                    result.substring( 0, result.length() - 2 ),
                    CassandraDataTypes.BOOLEAN
            );
    };

    /*
    для экземпляра Java класса конвертирует каждый его параметр,
    в Cassandra подобный тип данных
     */
    private final Function< Class, CassandraDataTypes > getCorrectDataType = type -> {
        if ( type.equals( String.class ) || type.isEnum() ) {
            return CassandraDataTypes.TEXT;
        }
        else if ( type.equals( UUID.class ) ) {
            return CassandraDataTypes.UUID;
        }
        else if ( type.equals( long.class ) ) {
            return CassandraDataTypes.BIGINT;
        }
        else if ( type.equals( int.class ) ) {
            return CassandraDataTypes.INT;
        }
        else if ( type.equals( double.class ) ) {
            return CassandraDataTypes.DOUBLE;
        }
        else if ( type.equals( Date.class ) ) {
            return CassandraDataTypes.TIMESTAMP;
        }
        else if ( type.equals( byte.class ) ) {
            return CassandraDataTypes.TINYINT;
        }
        else {
            return CassandraDataTypes.BOOLEAN;
        }
    };

    /*
    для экземпляра Java класса конвертирует каждый его параметр,
    в Cassandra подобный тип данных
     */
    protected final Function< Class, String > convertClassToCassandra = object -> {
            final StringBuilder result = super.newStringBuilder( "( " );

            this.getFields.apply( object )
                    .filter( field -> field.getType().equals( String.class )
                            ^ field.getType().equals( int.class )
                            ^ field.getType().equals( double.class )
                            ^ field.getType().equals( byte.class )
                            ^ field.getType().equals( UUID.class )
                            ^ field.getType().equals( long.class )
                            ^ field.getType().equals( Date.class )
                            ^ field.getType().isEnum()
                            ^ field.getType().equals( boolean.class ) )
                    .forEach( field -> result
                            .append( field.getName() )
                            .append( " " )
                            .append( this.getCorrectDataType.apply( field.getType() ) )
                            .append( ", " ) );

            return result.substring( 0, result.toString().length() - 2 );
    };

    /*
    принимает список объектов и конвертирует в понятную для Cassandra команду
    Например:
        [ 'asd', 'asd', 'asd', 'asd' ]
     */
    protected final Function< List< ? >, String > convertListToCassandra = list -> {
            final StringBuilder stringBuilder = super.newStringBuilder( "" );
            super.analyze( list, s -> stringBuilder.append( s ).append( ", " ) );
            return stringBuilder.length() == 1
                    ? super.joinTextWithCorrectCollectionEnding( stringBuilder.toString(), CassandraDataTypes.LIST )
                    : super.joinTextWithCorrectCollectionEnding(
                            stringBuilder.substring( 0, stringBuilder.length() - 2 ),
                            CassandraDataTypes.LIST
                    );
    };

    /*
    принимает объект и конвертирует в понятную для Cassandra команду
    с названием параметр как ключ и его значением из объекта
    Например:
        {
            id: 48,
            name: 'test;
        }
     */
    protected final Function< Object, String > convertClassToCassandraTable = object -> {
            final StringBuilder result = super.newStringBuilder( "" );
            this.getFields.apply( object.getClass() ).forEach( field -> {
                try {
                    result.append( field.getName() ).append( " : " );
                    org.springframework.util.ReflectionUtils.makeAccessible( field );
                    if ( field.get( object ) instanceof String ) {
                        result.append( super.joinWithAstrix( super.concatNames( field.get( object ) ) ) );
                    }
                    else if ( field.get( object ) instanceof Date ) {
                        result.append( super.joinWithAstrix(( (Date) field.get( object ) )) );
                    }
                    else if ( field.get( object ) instanceof Map ) {
                        result.append( this.convertMapToCassandra.apply( (Map<String, String>) field.get( object ) ) );
                    }
                    else if ( field.get( object ) instanceof List) {
                        result.append( this.convertListToCassandra.apply( (List<?>) field.get( object ) ) );
                    }
                    else if ( field.get( object ) instanceof Enum<?> ) {
                        result.append( super.joinWithAstrix( field.get( object ) ) );
                    } else {
                        result.append( field.get( object ) );
                    }
                    result.append( ", " );
                } catch ( final IllegalAccessException e ) { super.logging( e ); }
            } );

            return super.joinTextWithCorrectCollectionEnding(
                    result.substring( 0, result.length() - 2 ),
                    CassandraDataTypes.MAP
            );
    };

    protected final Function< List< ? >, String > convertListOfPointsToCassandra = pointsList -> {
            final StringBuilder stringBuilder = super.newStringBuilder( "" );

            super.analyze(
                    super.isCollectionNotEmpty( pointsList )
                            ? pointsList
                            : super.newList(),
                    points -> stringBuilder.append( this.convertClassToCassandraTable.apply( points ) ).append( ", " )
            );

            return stringBuilder.length() == 1
                    ? super.joinTextWithCorrectCollectionEnding(
                            stringBuilder.toString(),
                            CassandraDataTypes.LIST )
                    : super.joinTextWithCorrectCollectionEnding(
                            stringBuilder.substring( 0, stringBuilder.length() - 2 ),
                            CassandraDataTypes.LIST );
    };

    /*
    принимает Map из ключей и значений и конвертирует в понятную для Cassandra команду
    Например:
        {
            id: 48,
            name: 'test;
        }
     */
    protected final Function< Map< String, String >, String > convertMapToCassandra = listOfTasks -> {
            final StringBuilder stringBuilder = super.newStringBuilder( "" );

            super.analyze(
                    listOfTasks,
                    ( key, value ) -> stringBuilder
                            .append( super.joinWithAstrix( key ) )
                            .append( " : " )
                            .append( super.joinWithAstrix( key ) )
                            .append( "', " )
                    );

            return stringBuilder.length() == 1
                    ? super.joinTextWithCorrectCollectionEnding(
                            stringBuilder.toString(),
                            CassandraDataTypes.MAP
                    )
                    : super.joinTextWithCorrectCollectionEnding(
                            stringBuilder.substring( 0, stringBuilder.length() - 2 ),
                            CassandraDataTypes.MAP
                    );
    };

    protected final Function< Map< UUID, String >, String > convertSosMapToCassandra = listOfTasks -> {
            final StringBuilder stringBuilder = super.newStringBuilder( "" );

            super.analyze(
                    listOfTasks,
                    ( key, value ) -> stringBuilder
                            .append( key )
                            .append( " : " )
                            .append( super.joinWithAstrix( value ) )
                            .append( ", " )
            );

            return stringBuilder.length() == 1
                    ? super.joinTextWithCorrectCollectionEnding(
                            stringBuilder.toString(),
                            CassandraDataTypes.MAP
                    )
                    : super.joinTextWithCorrectCollectionEnding(
                            stringBuilder.substring( 0, stringBuilder.length() - 2 ),
                            CassandraDataTypes.MAP
                    );
    };

    /*
    принимает объект класса и возвращает список всех его аттрибутов
     */
    private final Function< Class, Stream< Field > > getFields = object -> Arrays.stream( object.getDeclaredFields() ).toList().stream();
}
