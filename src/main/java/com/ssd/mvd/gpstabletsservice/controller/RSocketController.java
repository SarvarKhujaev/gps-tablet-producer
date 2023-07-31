package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.stereotype.Controller;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

@Controller
public final class RSocketController {
    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter  () { return new WebSocketHandlerAdapter(); }
}
