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

    public boolean deleteUser ( Patrul patrul ) {
        ReqId reqId = new ReqId();
        reqId.setId( patrul.getUuid() );
        HttpEntity<?> entity = new HttpEntity<>( reqId, null );
        var res = restTemplate( patrul.getToken() )
                .exchange( "https://ms.ssd.uz/chat/delete-user", HttpMethod.POST, entity, String.class );
        return res.getStatusCodeValue() == 200; }

    public RestTemplate restTemplate( String token ) { return new RestTemplateBuilder()
                .setConnectTimeout( Duration.ofSeconds( 10 ) )
                .setReadTimeout( Duration.ofSeconds( 60 ) )
                .defaultHeader("token", token )
                .build(); }

    public Patrul addUser ( Patrul patrul ) {
        Req req = new Req();
        req.setId( patrul.getUuid() );
        req.setRole( Role.USER );
        req.setUsername( patrul.getSurnameNameFatherName() );
        HttpEntity<?> entity = new HttpEntity<>( req, null );
        var res = restTemplate( patrul
                .getToken()
                .split( " " )[1] )
                .exchange("https://ms.ssd.uz/chat/add-user", HttpMethod.POST, entity, String.class );
        System.out.println( res.getBody() );
        return patrul; }

    @Data
    public static class Req {
        private UUID id;
        private Role role;
        private String username;
    }

    @Data
    public static class ReqId {
        private UUID id;
    }

    public enum Role {
        OPERATOR,
        USER
    }
}
