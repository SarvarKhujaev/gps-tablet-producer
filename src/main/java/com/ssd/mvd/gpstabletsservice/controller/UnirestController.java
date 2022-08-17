package com.ssd.mvd.gpstabletsservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;

import java.time.Duration;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map;
import lombok.Data;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

@Data
public class UnirestController {
    private final Map< String, Object > fields = new HashMap<>();
    private final Map< String, String > headers = new HashMap<>();
    private static UnirestController serDes = new UnirestController();

    public static UnirestController getInstance () { return serDes != null ? serDes : ( serDes = new UnirestController() ); }

    private UnirestController () {
        Unirest.setObjectMapper( new ObjectMapper() {
            private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

            @Override
            public String writeValue( Object o ) { try { return this.objectMapper.writeValueAsString( o ); } catch (JsonProcessingException e) { throw new RuntimeException(e); } }

            @Override
            public <T> T readValue( String s, Class<T> aClass ) { try { return this.objectMapper.readValue( s, aClass ); } catch (JsonProcessingException e) { throw new RuntimeException(e); } } } );
        this.getHeaders().put( "accept", "application/json" );
        this.getHeaders().put( "token", "JhbGciOiJIUzUxMiJ9.eyJzdWIiOiJBQzEyMzQ1NjciLCJpZCI6IjBlMGMwMjAzLTBiNjYtNDI5NC05OWEwLWZkY2JmMzIyN2RjZiIsInBhc3Nwb3J0TnVtYmVyIjoiQUMxMjM0NT" ); }

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
