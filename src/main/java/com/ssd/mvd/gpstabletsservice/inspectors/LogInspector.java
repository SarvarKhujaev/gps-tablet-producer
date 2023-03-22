package com.ssd.mvd.gpstabletsservice.inspectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@lombok.Data
public class LogInspector extends DataValidateInspector {
    private final Logger LOGGER = LogManager.getLogger( "LOGGER_WITH_JSON_LAYOUT" );

    public Logger getLOGGER() { return LOGGER; }

    public void logging ( Throwable error ) { this.getLOGGER().error( "Error: " + error.getMessage() ); }

    public void logging ( Throwable error, Object o ) { this.getLOGGER().error("Error: {} and reason: {}: ",
            error.getMessage(), o ); }

    public void logging ( String message ) { this.getLOGGER().info( message ); }
}
