package com.ssd.mvd.gpstabletsservice.controller;

import java.util.UUID;
import java.time.Duration;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.boot.web.client.RestTemplateBuilder;

import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;

@Data
@Slf4j
public class UnirestController {
    private final String CHAT_SERVICE_DOMAIN = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.CHAT_SERVICE_DOMAIN" );

    private final String CHAT_SERVICE_PREFIX = GpsTabletsServiceApplication
            .context
            .getEnvironment()
            .getProperty( "variables.CHAT_SERVICE_PREFIX" );
    private static UnirestController serDes = new UnirestController();

    public static UnirestController getInstance () { return serDes != null ? serDes : ( serDes = new UnirestController() ); }

    public RestTemplate restTemplate ( String token ) { return new RestTemplateBuilder()
                .setConnectTimeout( Duration.ofSeconds( 10 ) )
                .setReadTimeout( Duration.ofSeconds( 60 ) )
                .defaultHeader( "token", token )
                .build(); }

    public void deleteUser ( String patrulId ) {
        try { Mono.just( new Req() )
                .map( req -> {
                    req.setId( UUID.fromString( patrulId.split( "@" )[0] ) );
                    return req; } )
                .onErrorContinue( (throwable, o) -> log.error( "Error in addUser of UnirestController: "
                        + throwable.getMessage() + " : " + o ) )
                .onErrorStop()
                .subscribe( req -> restTemplate( patrulId.split( "@" )[1] )
                        .exchange( this.CHAT_SERVICE_DOMAIN + "/"
                                + this.CHAT_SERVICE_PREFIX
                                + "/delete-user",
                                HttpMethod.POST,
                                new HttpEntity<>( req, null ),
                                String.class ) );
        } catch ( Exception e ) { System.out.println( "Error deleting user: " + e.getMessage() ); } }

    public void updateUser ( Patrul patrul ) {
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
                .subscribe( req -> restTemplate( patrul.getSpecialToken() )
                        .exchange( this.CHAT_SERVICE_DOMAIN + "/"
                                        + this.CHAT_SERVICE_PREFIX
                                        + "/edit-user",
                                HttpMethod.POST,
                                new HttpEntity<>( req, null ),
                                String.class ) );
        } catch ( Exception e ) { Mono.error( e ).subscribe( System.out::println ); } }

    public void addUser ( Patrul patrul ) {
        try { Mono.just( new Req() )
                .map( req -> {
                        req.setUsername( patrul.getSurnameNameFatherName() );
                        req.setId( patrul.getUuid() );
                        req.setRole( Role.USER );
                        return req; } )
                .onErrorContinue( (throwable, o) -> log.error( "Error in addUser of UnirestController: "
                + throwable.getMessage() + " : " + o ) )
                .onErrorStop()
                .subscribe( req -> this.restTemplate( patrul.getSpecialToken() )
                        .exchange( this.CHAT_SERVICE_DOMAIN + "/"
                                        + this.CHAT_SERVICE_PREFIX
                                        + "/add-user",
                                HttpMethod.POST,
                                new HttpEntity<>( req, null ),
                                String.class ) );
            patrul.setSpecialToken( null );
        } catch ( HttpClientErrorException e ) { System.out.println( "Error: " + e.getMessage() ); } }

    @Data
    public static class ReqId { private UUID id; }

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
