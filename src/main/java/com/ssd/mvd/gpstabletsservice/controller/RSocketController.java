package com.ssd.mvd.gpstabletsservice.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

@Controller
public class RSocketController {
    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter  () { return new WebSocketHandlerAdapter(); }
}
