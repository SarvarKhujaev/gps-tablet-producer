package com.ssd.mvd.gpstabletsservice.controller;

import lombok.Data;
import java.util.UUID;
import java.time.Duration;

import reactor.core.publisher.Mono;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import org.springframework.boot.web.client.RestTemplateBuilder;

@Data
public class UnirestController {
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
                .doOnError( throwable -> System.out.println( throwable.getMessage() ) )
                .onErrorStop()
                .log()
                .subscribe( req -> restTemplate( patrulId.split( "@" )[1] )
                        .exchange( "https://ms.ssd.uz/chat/delete-user",
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
                .doOnError( throwable -> System.out.println( throwable.getMessage() ) )
                .onErrorStop()
                .log()
                .subscribe( req -> restTemplate( patrul.getSpecialToken() )
                        .exchange("https://ms.ssd.uz/chat/edit-user",
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
                .onErrorStop()
                .doOnError( throwable -> System.out.println( throwable.getMessage() ) )
                .subscribe( req -> this.restTemplate( patrul.getSpecialToken() )
                        .exchange("https://ms.ssd.uz/chat/add-user",
                                HttpMethod.POST,
                                new HttpEntity<>( req, null ),
                                String.class ) );
            patrul.setSpecialToken( null );
        } catch ( Exception e ) { System.out.println( e.getMessage() ); } }

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
