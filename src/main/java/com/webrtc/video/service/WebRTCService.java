package com.webrtc.video.service;

import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WebRTCService {

    @Autowired
    private KurentoClient kurentoClient;

    public MediaPipeline createPipeline() {
        return kurentoClient.createMediaPipeline();
    }

    public WebRtcEndpoint createWebRtcEndpoint(MediaPipeline pipeline) {
        return new WebRtcEndpoint.Builder(pipeline).build();
    }
}

