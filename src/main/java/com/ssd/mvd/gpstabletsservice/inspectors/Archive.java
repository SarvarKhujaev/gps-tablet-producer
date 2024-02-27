package com.ssd.mvd.gpstabletsservice.inspectors;

import java.util.*;
import java.security.SecureRandom;
import reactor.core.publisher.Mono;

import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;

@lombok.Data
public class Archive extends CollectionsInspector {
    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder();

    protected Archive () {}

    protected <T> Mono< T > convert ( final T o ) {
        return Optional.ofNullable( o ).isPresent() ? Mono.just( o ) : Mono.empty();
    }

    protected Mono< ApiResponseModel > function (
            final Map< String, ? > map
    ) {
        return this.convert( ApiResponseModel
                .builder() // in case of wrong login
                .status( Status
                        .builder()
                        .message( map.get( "message" ).toString() )
                        .code( map.containsKey( "code" )
                                ? Long.parseLong( map.get( "code" ).toString() )
                                : 200 )
                        .build() )
                .data( map.containsKey( "data" )
                        ? ( com.ssd.mvd.gpstabletsservice.entity.Data ) map.get( "data" )
                        : com.ssd.mvd.gpstabletsservice.entity.Data.builder().build() )
                .success( !map.containsKey( "success" ) )
                .build() );
    }

    protected ApiResponseModel errorResponse () {
        return ApiResponseModel
                .builder() // in case of wrong login
                .status( Status
                        .builder()
                        .message( "Server error" )
                        .code( 201 )
                        .build() )
                .success( false )
                .build();
    }

    protected Mono< ApiResponseModel > errorResponse ( final String message ) {
        return this.convert(
                ApiResponseModel
                        .builder()
                        .status( Status
                                .builder()
                                .message( message )
                                .code( 201 )
                                .build() )
                        .success( false )
                        .build() );
    }

    protected UUID decode ( final String token ) {
        return UUID.fromString(
                new String( Base64
                        .getDecoder()
                        .decode( token ) )
                        .split( "@" )[ 0 ] );
    }

    protected String generateToken () {
        final byte[] bytes = new byte[ 24 ];
        this.getSecureRandom().nextBytes( bytes );
        return this.getEncoder().encodeToString( bytes );
    }
}
