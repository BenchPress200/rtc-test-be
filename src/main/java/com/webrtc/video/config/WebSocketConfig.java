package com.webrtc.video.config;

import com.webrtc.video.controller.WebRTCController;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final WebRTCController webRTCController;

    public WebSocketConfig(WebRTCController webRTCController) {
        this.webRTCController = webRTCController;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webRTCController, "/ws").setAllowedOrigins("*");
    }
}
