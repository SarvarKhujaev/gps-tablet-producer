package com.ssd.mvd.gpstabletsservice.inspectors;

import java.util.UUID;
import java.util.Date;
import java.util.Base64;
import java.nio.charset.StandardCharsets;

import com.ssd.mvd.gpstabletsservice.constants.Status;
import com.ssd.mvd.gpstabletsservice.constants.TaskTypes;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.constants.CassandraCommands;
import com.ssd.mvd.gpstabletsservice.constants.CassandraDataTypes;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.PatrulFIOData;
import com.ssd.mvd.gpstabletsservice.task.entityForPapilon.modelForGai.ModelForCar;

public class StringOperations extends Archive {
    protected final String taskDetailsMessage = "Your task details";
    protected final String noneTaskIsAttached = "U have no tasks, thus u cannot send report";

    protected String getMessage ( final Status status ) {
        return String.join(
                " ",
                switch ( status ) {
                    // in case when Patrul wants to leave his account
                    case LOGOUT -> "log out at:";

                    case ACCEPTED -> "accepted new task at:";

                    // when Patrul wants to set in pause his work
                    case SET_IN_PAUSE -> "put in pause at:";

                    // uses when at the end of the day User finishes his job
                    case STOP_TO_WORK -> "stopped to work at:";

                    // uses to when User wants to back to work after pause
                    case START_TO_WORK -> "started to work at:";

                    // uses to start to work every day in the morning
                    case RETURNED_TO_WORK -> "returned to work at:";

                    case ARRIVED -> "arrived to given task location at:";

                    // by default, it means to log in to account
                    default -> "with simCard";
                },
                new Date().toInstant().toString()
        );
    }

    protected String getMessage ( final TaskTypes taskTypes ) {
        return "You have: %s task".formatted( taskTypes );
    }

    protected String getSuccessMessage (
            final String className,
            final String operation
    ) {
        return String.join(
                " ",
                className,
                "was",
                operation,
                "successfully"
        );
    }

    protected String getFailMessage (
            final String className
    ) {
        return String.join(
                " ",
                "This",
                className,
                "does not exists"
        );
    }

    protected String getMessage (
            final Patrul patrul
    ) {
        return String.join(
                " ",
                "Report from:",
                patrul.getPatrulFIOData().getName(),
                "was saved"
        );
    }

    protected String getMessage (
            final Patrul patrul,
            final Status status
    ) {
        return String.join(
                " ",
                "Patrul:",
                patrul.getPassportNumber(),
                "changed his status task to:",
                status.name()
        );
    }

    protected String getMessage (
            final Patrul patrul,
            final TaskCommonParams taskCommonParams
    ) {
        return String.join(
                    " ",
                    patrul.getPatrulFIOData().getName(),
                    "was removed from",
                    taskCommonParams.getTaskId()
            );
    }

    protected String splitWordAndJoin ( final String name ) {
        final String[] temp = name.split( " " );
        return temp.length > 3
                ? String.join( " ",
                temp[ 0 ].split( "/" )[1],
                temp[ 1 ].split( "/" )[1],
                temp[ 3 ].split( "/" )[1],
                temp[ 4 ] )
                : String.join( " ", temp );
    }

    /*
    генерируем сообщение с началом Транзакции
     */
    protected StringBuilder newStringBuilder () {
        return new StringBuilder( CassandraCommands.BEGIN_BATCH );
    }

    protected StringBuilder newStringBuilder ( final String s ) {
        return new StringBuilder( s );
    }

    protected String removeAllDotes ( final String s ) {
        return s.replaceAll( "'", "" );
    }

    protected String concatNames ( final Patrul patrul ) {
        return Base64
                .getEncoder()
                .encodeToString(
                        String.join( "@",
                                        patrul.getUuid().toString(),
                                        patrul.getPassportNumber(),
                                        patrul.getPatrulAuthData().getPassword(),
                                        patrul.getPatrulMobileAppInfo().getSimCardNumber(),
                                        super.generateToken() )
                                .getBytes( StandardCharsets.UTF_8 ) );
    }

    public String concatNames ( final Object object ) {
        return String.join( "", String.valueOf( object ).split( "[.]" ) );
    }

    public String concatNames ( final ModelForCar modelForCar ) {
        return String.join( " ",
                modelForCar.getModel(),
                modelForCar.getVehicleType(),
                modelForCar.getColor() );
    }

    public String concatNames ( final PatrulFIOData patrulFIOData ) {
        return String.join( " ",
                patrulFIOData.getName(),
                patrulFIOData.getSurname(),
                patrulFIOData.getFatherName() );
    }

    /*
    принимает параметр для Cassandra, который является типом TEXТ,
    и добавляет в начало и конец апострафы
    */
    protected String joinWithAstrix ( final Object value ) {
        return "$$" + value + "$$";
    }

    /*
    принимает параметр для Cassandra, который является типом TIMESTAMP,
    и добавляет в начало и конец апострафы
    */
    protected String joinWithAstrix ( final Date date ) {
        return "'" + date.toInstant() + "'";
    }

    /*
        принимает параметр для Cassandra, который относиться к Collection,
        и добавляет в начало и конец (), {} или []
        в зависимости от типа коллекции
    */
    protected String joinTextWithCorrectCollectionEnding (
            final String textToJoin,
            final CassandraDataTypes cassandraDataTypes
    ) {
        return switch ( cassandraDataTypes ) {
            case MAP, SET -> "{" + textToJoin + "}";
            case LIST -> "[" + textToJoin + "]";
            default -> "(" + textToJoin + ")";
        };
    }

    protected String generateID () {
        return "ID = '%s'".formatted( UUID.randomUUID() );
    }
}
