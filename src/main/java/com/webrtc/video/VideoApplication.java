package com.webrtc.video;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.socket.config.annotation.EnableWebSocket;

@EnableWebSocket
@SpringBootApplication
public class VideoApplication {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(VideoApplication.class, args);
    }

}
