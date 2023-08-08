package com.ssd.mvd.gpstabletsservice.inspectors;

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

    protected <T> Mono< T > convert ( final T o ) { return Optional.ofNullable( o ).isPresent() ? Mono.just( o ) : Mono.empty(); }

    protected final Function< Map< String, ? >, Mono< ApiResponseModel > > function =
            map -> this.convert( ApiResponseModel
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

    protected final Supplier< ApiResponseModel > errorResponse = () -> ApiResponseModel
            .builder() // in case of wrong login
            .status( Status
                    .builder()
                    .message( "Server error" )
                    .code( 201 )
                    .build() )
            .success( false )
            .build();

    protected final Function< Integer, Mono< ApiResponseModel > > error = value -> this.convert(
            ApiResponseModel
                    .builder()
                    .status( Status
                            .builder()
                            .message( switch ( value ) {
                                case 0 -> "Wrong Login or password";
                                case 1 -> "You were removed from task, due to fac that u r late for more then 24 hours";
                                case 2 -> "Wrong Params";
                                default -> "Server error"; } )
                            .code( 201 )
                            .build() )
                    .success( false )
                    .build() );

    private final Set< String > detailsList = Set.of( "Ф.И.О", "", "ПОДРАЗДЕЛЕНИЕ", "ДАТА И ВРЕМЯ", "ID",
            "ШИРОТА", "ДОЛГОТА", "ВИД ПРОИСШЕСТВИЯ", "НАЧАЛО СОБЫТИЯ", "КОНЕЦ СОБЫТИЯ",
            "КОЛ.СТВО ПОСТРАДАВШИХ", "КОЛ.СТВО ПОГИБШИХ", "ФАБУЛА" );

    protected final List< String > fields = List.of(
            "F.I.O",
            "Tug'ilgan sana",
            "Telefon raqam",
            "Unvon",
            "Viloyat",
            "Tuman/Shahar",
            "Patrul turi",
            "Oxirgi faollik vaqti",
            "Ishlashni boshlagan vaqti",
            "Ro'yxatdan o'tgan vaqti",
            "Umumiy faollik vaqti",
            "Planshet quvvati" );

    protected final Supplier< String > generateToken = () -> {
            final byte[] bytes = new byte[ 24 ];
            this.getSecureRandom().nextBytes( bytes );
            return this.getEncoder().encodeToString( bytes ); };
}
