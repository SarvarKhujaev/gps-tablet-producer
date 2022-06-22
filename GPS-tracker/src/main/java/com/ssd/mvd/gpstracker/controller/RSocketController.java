package com.ssd.mvd.gpstracker.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

@Controller
public class RSocketController {
    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter  () { return new WebSocketHandlerAdapter(); }
//
//    @MessageMapping( "hello" )
//    public Mono< String > hello () { return Mono.just( "hello" ); }
//
//    @MessageMapping( value = "cassandra" )
//    public Flux< Row > getAll ( String parameter ) { return CassandraDataControl.getInstance().getAll( parameter ); }
}
