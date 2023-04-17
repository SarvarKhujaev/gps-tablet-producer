package com.ssd.mvd.gpstabletsservice.database;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import java.security.SecureRandom;
import reactor.core.publisher.Mono;

import com.ssd.mvd.gpstabletsservice.response.Status;
import com.ssd.mvd.gpstabletsservice.response.ApiResponseModel;

@lombok.Data
public class Archive {
    private final SecureRandom secureRandom = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder();

    private final Function< Map< String, ? >, Mono< ApiResponseModel > > function =
            map -> Mono.just( ApiResponseModel
            .builder() // in case of wrong login
            .status( Status
                    .builder()
                    .message( map.get( "message" ).toString() )
                    .code( map.containsKey( "code" ) ?
                            Long.parseLong( map.get( "code" ).toString() ) : 200 )
                    .build() )
            .data( map.containsKey( "data" ) ?
                    (com.ssd.mvd.gpstabletsservice.entity.Data) map.get( "data" )
                    : com.ssd.mvd.gpstabletsservice.entity.Data.builder().build() )
            .success( !map.containsKey( "success" ) )
            .build() );

    private final Supplier< ApiResponseModel > errorResponse = () -> ApiResponseModel
            .builder() // in case of wrong login
            .status( Status
                    .builder()
                    .message( "Server error" )
                    .code( 201 )
                    .build() )
            .success( false )
            .build();

    private final Supplier< Mono< ApiResponseModel > > errorResponseForWrongParams = () -> Mono.just(
            ApiResponseModel
                    .builder() // in case of wrong login
                    .status( Status
                            .builder()
                            .message( "Wrong Params" )
                            .code( 201 )
                            .build() )
                    .success( false )
                    .build() );

    // возвращает сообзение о слишком большой задержке прихода в точку назначения
    private final Supplier< Mono< ApiResponseModel > > errorResponseForLateComing = () -> Mono.just(
            ApiResponseModel
                    .builder() // in case of wrong login
                    .status( Status
                            .builder()
                            .message( "You were removed from task, due to fac that u r late for more then 24 hours" )
                            .code( 201 )
                            .build() )
                    .success( false )
                    .build() );

    private final List< String > detailsList = List.of( "Ф.И.О", "", "ПОДРАЗДЕЛЕНИЕ", "ДАТА И ВРЕМЯ", "ID",
            "ШИРОТА", "ДОЛГОТА", "ВИД ПРОИСШЕСТВИЯ", "НАЧАЛО СОБЫТИЯ", "КОНЕЦ СОБЫТИЯ",
            "КОЛ.СТВО ПОСТРАДАВШИХ", "КОЛ.СТВО ПОГИБШИХ", "ФАБУЛА" );

    private final Supplier< String > generateToken = () -> {
        final byte[] bytes = new byte[ 24 ];
        this.getSecureRandom().nextBytes( bytes );
        return this.getEncoder().encodeToString( bytes ); };
}
