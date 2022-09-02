package com.ssd.mvd.gpstabletsservice.controller;

import lombok.Data;
import java.util.UUID;
import java.time.Duration;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import org.springframework.boot.web.client.RestTemplateBuilder;

@Data
public class UnirestController {
    private static UnirestController serDes = new UnirestController();

    public static UnirestController getInstance () { return serDes != null ? serDes : ( serDes = new UnirestController() ); }

    public RestTemplate restTemplate( String token ) { return new RestTemplateBuilder()
                .setConnectTimeout( Duration.ofSeconds( 10 ) )
                .setReadTimeout( Duration.ofSeconds( 60 ) )
                .defaultHeader("token", token )
                .build(); }

    public boolean deleteUser ( String patrulId ) {
        ReqId reqId = new ReqId();
        reqId.setId( UUID.fromString( patrulId.split( "@" )[0] ) );
        System.out.println( reqId.getId() );
        return restTemplate( patrulId.split( "@" )[1] )
                .exchange( "https://ms.ssd.uz/chat/delete-user",
                        HttpMethod.POST,
                        new HttpEntity<>( reqId, null ),
                        String.class )
                .getStatusCodeValue() == 200; }

    public Boolean updateUser ( Patrul patrul ) {
        if ( patrul.getSpecialToken() == null ) return false;
        Req req = new Req();
        req.setRole( Role.USER );
        req.setId( patrul.getUuid() );
        req.setUsername( patrul.getSurnameNameFatherName() );
        System.out.println( req );
        return restTemplate( patrul
                .getSpecialToken() )
                .exchange("https://ms.ssd.uz/chat/edit-user",
                        HttpMethod.POST,
                        new HttpEntity<>( req, null ),
                        String.class )
                .getStatusCode()
                .is2xxSuccessful(); }

    public Boolean addUser ( Patrul patrul ) {
        Req req = new Req();
        req.setRole( Role.USER );
        req.setId( patrul.getUuid() );
        req.setUsername( patrul.getSurnameNameFatherName() );
        System.out.println( req );
        return restTemplate( patrul.getSpecialToken() )
                .exchange("https://ms.ssd.uz/chat/add-user",
                        HttpMethod.POST,
                        new HttpEntity<>( req, null ),
                        String.class )
                .getStatusCode().is2xxSuccessful(); }

    @Data
    public static class ReqId {
        private UUID id;
    }

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
