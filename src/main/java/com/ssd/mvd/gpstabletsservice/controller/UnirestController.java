package com.ssd.mvd.gpstabletsservice.controller;

import java.util.List;
import java.util.UUID;
import java.time.Duration;
import java.util.function.*;

import com.google.gson.Gson;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.ObjectMapper;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.boot.web.client.RestTemplateBuilder;

import com.ssd.mvd.gpstabletsservice.entity.Regions;
import com.ssd.mvd.gpstabletsservice.constants.Errors;
import com.ssd.mvd.gpstabletsservice.entity.RegionData;
import com.ssd.mvd.gpstabletsservice.task.sos_task.Address;
import com.ssd.mvd.gpstabletsservice.inspectors.LogInspector;
import com.ssd.mvd.gpstabletsservice.entity.patrulDataSet.Patrul;
import com.ssd.mvd.gpstabletsservice.GpsTabletsServiceApplication;
import com.ssd.mvd.gpstabletsservice.subscribers.CustomSubscriber;

@lombok.Data
public class UnirestController extends LogInspector {
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
    private final static UnirestController serDes = new UnirestController();

    public static UnirestController getInstance () {
        return serDes;
    }

    private UnirestController () {
        Unirest.setObjectMapper( new ObjectMapper() {
            private final com.fasterxml.jackson.databind.ObjectMapper objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();

            @Override
            public String writeValue( Object o ) {
                try {
                    return this.objectMapper.writeValueAsString( o );
                }
                catch ( JsonProcessingException e ) {
                    throw new RuntimeException(e);
                } }

            @Override
            public <T> T readValue( String s, Class<T> aClass ) {
                try {
                    return this.objectMapper.readValue( s, aClass );
                }
                catch ( JsonProcessingException e ) {
                    throw new RuntimeException(e);
                }
            }
        } );
    }

    private final Function< String, RestTemplate > restTemplate = token -> new RestTemplateBuilder()
            .setConnectTimeout( Duration.ofSeconds( 10 ) )
            .setReadTimeout( Duration.ofSeconds( 60 ) )
            .defaultHeader( "token", token )
            .build();

    private final Consumer< String > deleteUser = patrulId -> {
            try {
                super.convert( new Req( UUID.fromString( patrulId.split( "@" )[0] ) ) )
                    .onErrorContinue( super::logging )
                    .subscribe(
                            new CustomSubscriber<>(
                                req -> this.getRestTemplate()
                                        .apply( patrulId.split( "@" )[1] )
                                        .exchange( this.getCHAT_SERVICE_DOMAIN() + "/"
                                                        + this.getCHAT_SERVICE_PREFIX()
                                                        + "/delete-user",
                                                HttpMethod.POST,
                                                new HttpEntity<>( req, null ),
                                                String.class )
                    ) );
            } catch ( final Exception e ) {
                super.logging( e );
            }
    };

    private final Consumer< Patrul > updateUser = patrul -> {
            if ( !super.objectIsNotNull( patrul.getPatrulTokenInfo().getSpecialToken() ) ) {
                return;
            }

            try {
                super.convert( new Req( patrul ) )
                    .onErrorContinue( super::logging )
                    .subscribe(
                            new CustomSubscriber<>(
                                    req -> this.getRestTemplate().apply( patrul.getPatrulTokenInfo().getSpecialToken() )
                                            .exchange( this.getCHAT_SERVICE_DOMAIN() + "/"
                                                            + this.getCHAT_SERVICE_PREFIX()
                                                            + "/edit-user",
                                                    HttpMethod.POST,
                                                    new HttpEntity<>( req, null ),
                                                    String.class )
                            )
                    );
            } catch ( final Exception e ) {
                super.logging( e );
            }
    };

    private final Consumer< Patrul > addUser = patrul -> {
            try {
                super.convert( new Req( patrul ) )
                    .onErrorContinue( super::logging )
                    .onErrorStop()
                    .subscribe( new CustomSubscriber<>(
                            req -> this.getRestTemplate()
                                    .apply( patrul.getPatrulTokenInfo().getSpecialToken() )
                                    .exchange( this.getCHAT_SERVICE_DOMAIN() + "/"
                                                    + this.getCHAT_SERVICE_PREFIX()
                                                    + "/add-user",
                                            HttpMethod.POST,
                                            new HttpEntity<>( req, null ),
                                            String.class )
                    ) );

                patrul.getPatrulTokenInfo().setSpecialToken( null );
            } catch ( final HttpClientErrorException e ) {
                super.logging( e );
            }
    };

    private <T> List<T> stringToArrayList ( final String object, final Class< T[] > clazz ) {
        return super.convertArrayToList( this.getGson().fromJson( object, clazz ) );
    }

    private final BiFunction< Double, Double, String > getAddressByLocation = ( latitude, longitude ) -> {
            try {
                return this.stringToArrayList(
                    Unirest.get(
                            this.getADDRESS_LOCATION_API()
                                + latitude + "," + longitude
                                + "&limit=5&format=json&addressdetails=1" )
                            .asJson()
                            .getBody()
                            .getArray()
                            .toString(),
                    Address[].class )
                .get( 0 )
                .getDisplay_name();
            } catch ( final Exception e ) {
                super.logging( e );
                return Errors.DATA_NOT_FOUND.name();
            }
    };

    private final Function< Long, List< RegionData > > getRegions = regionId -> {
            try {
                return this.getGson().fromJson(
                        Unirest.get( regionId > 0
                                    ? "http://10.254.1.1:1234/region-dictionary/api/v1/front/getDistrictByRegion/" + regionId
                                    : "http://10.254.1.1:1234/region-dictionary/api/v1/front/getAllRegion" )
                            .asJson()
                            .getBody()
                            .toString(),
                            Regions.class )
                        .getResData();
            } catch ( final Exception e ) {
                super.logging( e );
                return super.emptyList();
            }
    };

    public static class Req {
        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public void setRole(Role role) {
            this.role = role;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        private UUID id;
        private Role role;
        private String username;

        public Req ( final UUID uuid ) {
            this.setId( uuid );
        }

        public Req ( final Patrul patrul ) {
            this.setUsername( patrul.getPatrulFIOData().getSurnameNameFatherName() );
            this.setId( patrul.getUuid() );
            this.setRole( Role.USER );
        }
    }

    public enum Role {
        OPERATOR,
        USER
    }
}
