package com.ssd.mvd.gpstabletsservice.controller;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;
import java.util.Arrays;
import java.time.Duration;

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

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.task.sos_task.Address;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;

@Data
@Slf4j
public class UnirestController {
    private final String ADDRESS_LOCATION_API = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.ADDRESS_LOCATION_API" );

    private final String CHAT_SERVICE_DOMAIN = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.CHAT_SERVICE_DOMAIN" );

    private final String CHAT_SERVICE_PREFIX = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.CHAT_SERVICE_PREFIX" );
    private static UnirestController serDes = new UnirestController();

    private final Gson gson = new Gson();

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
                .onErrorContinue( (throwable, o) -> log.error( "Error in addUser of UnirestController: "
                        + throwable.getMessage() + " : " + o ) )
                .onErrorStop()
                .subscribe( req -> this.getRestTemplate()
                        .apply( patrulId.split( "@" )[1] )
                        .exchange( this.CHAT_SERVICE_DOMAIN + "/"
                                + this.CHAT_SERVICE_PREFIX
                                + "/delete-user",
                                HttpMethod.POST,
                                new HttpEntity<>( req, null ),
                                String.class ) );
        } catch ( Exception e ) { log.error( "Error deleting user: " + e.getMessage() ); } };

    private final Consumer< Patrul > updateUser = patrul -> {
        if ( patrul.getSpecialToken() == null ) return;
        try { Mono.just( new Req() )
                .map( req -> {
                    req.setUsername( patrul.getSurnameNameFatherName() );
                    req.setId( patrul.getUuid() );
                    req.setRole( Role.USER );
                    return req; } )
                .onErrorContinue( (throwable, o) -> log.error( "Error in addUser of UnirestController: "
                        + throwable.getMessage() + " : " + o ) )
                .onErrorStop()
                .subscribe( req -> this.getRestTemplate()
                        .apply( patrul.getSpecialToken() )
                        .exchange( this.CHAT_SERVICE_DOMAIN + "/"
                                        + this.CHAT_SERVICE_PREFIX
                                        + "/edit-user",
                                HttpMethod.POST,
                                new HttpEntity<>( req, null ),
                                String.class ) );
        } catch ( Exception e ) { log.error( "Error in updateUser: " + e.getMessage() ); } };

    private final Consumer< Patrul > addUser = patrul -> {
        try { Mono.just( new Req() )
                .map( req -> {
                    req.setUsername( patrul.getSurnameNameFatherName() );
                    req.setId( patrul.getUuid() );
                    req.setRole( Role.USER );
                    return req; } )
                .onErrorContinue( (throwable, o) -> log.error( "Error in addUser of UnirestController: "
                        + throwable.getMessage() + " : " + o ) )
                .onErrorStop()
                .subscribe( req -> this.getRestTemplate()
                        .apply( patrul.getSpecialToken() )
                        .exchange( this.CHAT_SERVICE_DOMAIN + "/"
                                        + this.CHAT_SERVICE_PREFIX
                                        + "/add-user",
                                HttpMethod.POST,
                                new HttpEntity<>( req, null ),
                                String.class ) );
            patrul.setSpecialToken( null );
        } catch ( HttpClientErrorException e ) { log.error( "Error: " + e.getMessage() ); } };

    private <T> List<T> stringToArrayList ( String object, Class< T[] > clazz ) { return Arrays.asList( this.getGson().fromJson( object, clazz ) ); }

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
            log.error( e.getMessage() );
            return "address not found"; } };

    @Data
    public static class Req {
        private UUID id;
        private Role role;
        private String username; }

    public enum Role {
        OPERATOR,
        USER
    }
}
