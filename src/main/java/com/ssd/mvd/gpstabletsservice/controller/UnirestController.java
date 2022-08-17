package com.ssd.mvd.gpstabletsservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.ssd.mvd.gpstabletsservice.entity.Patrul;

import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;

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

    public Patrul addUser ( Patrul patrul ) {
        this.getFields().put( "id", patrul.getUuid() );
        this.getFields().put( "role", "USER" );
        this.getFields().put( "username", patrul.getSurnameNameFatherName() );
        this.getHeaders().clear();
        this.getHeaders().put( "token", patrul.getToken().split( " " )[1] );
        try { HttpResponse<String> response = Unirest.post( "https://ms.ssd.uz/chat/add-user" )
                .headers( this.getHeaders() )
                .fields( this.getFields() )
                .asString();
            System.out.println( response.getBody() );
            patrul.setToken( null );
            return patrul;
        } catch ( UnirestException e ) { return patrul; } }

    public void deleteUser ( Patrul patrul ) {
        this.getFields().clear();
        this.getFields().put( "id", patrul.getUuid() );
        try { Unirest.post( "http://ms-backend.ssd.uz:3040/delete-user" )
                .headers( this.getHeaders() )
                .fields( this.getFields() )
                .asString();
        } catch ( UnirestException e ) { e.printStackTrace(); } }
}
