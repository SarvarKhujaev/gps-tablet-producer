package com.ssd.mvd.gpstabletsservice.controller;

import java.util.List;
import java.util.UUID;
import java.util.Arrays;
import java.time.Duration;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.BiFunction;

import com.google.gson.Gson;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.ObjectMapper;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.boot.web.client.RestTemplateBuilder;

import com.ssd.mvd.gpstabletsservice.constants.Errors;
import com.ssd.mvd.gpstabletsservice.task.sos_task.Address;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;

@lombok.Data
public final class UnirestController extends LogInspector {
    private final String ADDRESS_LOCATION_API = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.UNIREST_VARIABLES.ADDRESS_LOCATION_API" );

    private final String CHAT_SERVICE_DOMAIN = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.UNIREST_VARIABLES.CHAT_SERVICE_DOMAIN" );

    private final String CHAT_SERVICE_PREFIX = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.UNIREST_VARIABLES.CHAT_SERVICE_PREFIX" );

    private final Gson gson = new Gson();
    private static UnirestController serDes = new UnirestController();

    public static UnirestController getInstance () { return serDes != null ? serDes : ( serDes = new UnirestController() ); }

    private UnirestController () { Unirest.setObjectMapper( new ObjectMapper() {
            private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

            @Override
            public String writeValue( Object o ) {
                try { return this.objectMapper.writeValueAsString( o ); }
                catch ( JsonProcessingException e ) { throw new RuntimeException(e); } }

            @Override
            public <T> T readValue( String s, Class<T> aClass ) {
                try { return this.objectMapper.readValue( s, aClass ); }
                catch ( JsonProcessingException e ) { throw new RuntimeException(e); } } } ); }

    private final Function< String, RestTemplate > restTemplate = token -> new RestTemplateBuilder()
            .setConnectTimeout( Duration.ofSeconds( 10 ) )
            .setReadTimeout( Duration.ofSeconds( 60 ) )
            .defaultHeader( "token", token )
            .build();

    private final Consumer< String > deleteUser = patrulId -> {
            try { Mono.just( new Req() )
                    .map( req -> {
                        req.setId( UUID.fromString( patrulId.split( "@" )[0] ) );
                        return req; } )
                    .onErrorContinue( super::logging )
                    .subscribe( req -> this.getRestTemplate()
                            .apply( patrulId.split( "@" )[1] )
                            .exchange( this.getCHAT_SERVICE_DOMAIN() + "/"
                                    + this.getCHAT_SERVICE_PREFIX()
                                    + "/delete-user",
                                    HttpMethod.POST,
                                    new HttpEntity<>( req, null ),
                                    String.class ) );
            } catch ( Exception e ) { super.logging( e ); } };

    private final Consumer< Patrul > updateUser = patrul -> {
            if ( patrul.getSpecialToken() == null ) return;
            try { Mono.just( new Req() )
                    .map( req -> {
                        req.setUsername( patrul.getSurnameNameFatherName() );
                        req.setId( patrul.getUuid() );
                        req.setRole( Role.USER );
                        return req; } )
                    .onErrorContinue( super::logging )
                    .subscribe( req -> this.getRestTemplate().apply( patrul.getSpecialToken() )
                            .exchange( this.getCHAT_SERVICE_DOMAIN() + "/"
                                            + this.getCHAT_SERVICE_PREFIX()
                                            + "/edit-user",
                                    HttpMethod.POST,
                                    new HttpEntity<>( req, null ),
                                    String.class ) );
            } catch ( Exception e ) { super.logging( e ); } };

    private final Consumer< Patrul > addUser = patrul -> {
            try { Mono.just( new Req() )
                    .map( req -> {
                        req.setUsername( patrul.getSurnameNameFatherName() );
                        req.setId( patrul.getUuid() );
                        req.setRole( Role.USER );
                        return req; } )
                    .onErrorContinue( super::logging )
                    .onErrorStop()
                    .subscribe( req -> this.getRestTemplate()
                            .apply( patrul.getSpecialToken() )
                            .exchange( this.getCHAT_SERVICE_DOMAIN() + "/"
                                            + this.getCHAT_SERVICE_PREFIX()
                                            + "/add-user",
                                    HttpMethod.POST,
                                    new HttpEntity<>( req, null ),
                                    String.class ) );
                patrul.setSpecialToken( null );
            } catch ( HttpClientErrorException e ) { super.logging( e ); } };

    private <T> List<T> stringToArrayList ( final String object, final Class< T[] > clazz ) { return Arrays.asList( this.getGson().fromJson( object, clazz ) ); }

    private final BiFunction< Double, Double, String > getAddressByLocation = ( latitude, longitude ) -> {
            try { return this.stringToArrayList(
                    Unirest.get( this.getADDRESS_LOCATION_API()
                                + latitude + "," + longitude
                                + "&limit=5&format=json&addressdetails=1" )
                            .asJson()
                            .getBody()
                            .getArray()
                            .toString(),
                    Address[].class )
                .get( 0 )
                .getDisplay_name();
            } catch ( Exception e ) {
                super.logging( e );
                return Errors.DATA_NOT_FOUND.name(); } };

    @lombok.Data
    public static class Req {
        private UUID id;
        private Role role;
        private String username; }

    public enum Role {
        OPERATOR,
        USER
    }
}
