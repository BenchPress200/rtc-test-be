package com.webrtc.video.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.kurento.client.KurentoClient;
import org.kurento.client.MediaPipeline;
import org.kurento.client.WebRtcEndpoint;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Controller
@RequiredArgsConstructor
public class WebRTCController extends TextWebSocketHandler {

    private final KurentoClient kurentoClient;
    private final ConcurrentHashMap<String, WebRtcEndpoint> participants = new ConcurrentHashMap<>();
    private MediaPipeline pipeline;
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("Connection established: " + session.getId());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        System.out.println("Received: " + payload);

        JsonNode jsonMessage = mapper.readTree(payload);
        String event = jsonMessage.get("event").asText();

        switch (event) {
            case "offer":
                handleOffer(session, jsonMessage);
                break;
            case "answer":
                handleAnswer(session, jsonMessage);
                break;
            case "iceCandidate":
                handleIceCandidate(session, jsonMessage);
                break;
            case "leaveRoom":
                handleLeaveRoom(session);
                break;
            default:
                System.out.println("Unknown event: " + event);
        }
    }

    private void handleOffer(WebSocketSession session, JsonNode jsonMessage) throws Exception {
        String sdpOffer = jsonMessage.get("sdpOffer").asText();

        if (pipeline == null) {
            pipeline = kurentoClient.createMediaPipeline();
        }

        WebRtcEndpoint webRtcEndpoint = new WebRtcEndpoint.Builder(pipeline).build();
        participants.put(session.getId(), webRtcEndpoint);

        webRtcEndpoint.addOnIceCandidateListener(event -> {
            try {
                JsonNode candidate = mapper.createObjectNode()
                        .put("event", "iceCandidate")
                        .put("candidate", mapper.writeValueAsString(event.getCandidate()));
                session.sendMessage(new TextMessage(candidate.toString()));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        try {
            String sdpAnswer = webRtcEndpoint.processOffer(sdpOffer);
            String response = mapper.createObjectNode()
                    .put("event", "answer")
                    .put("sdpAnswer", sdpAnswer)
                    .toString();
            session.sendMessage(new TextMessage(response));
        } catch (Exception e) {
            e.printStackTrace();
        }

        webRtcEndpoint.gatherCandidates();
    }

    private void handleAnswer(WebSocketSession session, JsonNode jsonMessage) {
        // 클라이언트가 WebRtcPeer에서 받은 SDP Answer를 처리하는 부분
        // 주로 P2P 구조에서는 필요하지 않으나, 확장 시 사용될 수 있음
    }

    private void handleIceCandidate(WebSocketSession session, JsonNode jsonMessage) {
        JsonNode candidateNode = jsonMessage.get("candidate");
        if (candidateNode != null && participants.containsKey(session.getId())) {
            try {
                String candidateJson = candidateNode.asText();
                WebRtcEndpoint webRtcEndpoint = participants.get(session.getId());
                webRtcEndpoint.addIceCandidate(mapper.readValue(candidateJson, org.kurento.client.IceCandidate.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void handleLeaveRoom(WebSocketSession session) throws Exception {
        if (participants.containsKey(session.getId())) {
            participants.get(session.getId()).release();
            participants.remove(session.getId());
        }

        JsonNode response = mapper.createObjectNode().put("event", "participantLeft");
        session.sendMessage(new TextMessage(response.toString()));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        System.out.println("Transport error: " + exception.getMessage());
        session.close(CloseStatus.SERVER_ERROR);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("Connection closed: " + session.getId());
        handleLeaveRoom(session);
    }

    @PostMapping("/start")
    public void start() {
        pipeline = kurentoClient.createMediaPipeline();
    }

    @PostMapping("/stop")
    public void stop() {
        if (pipeline != null) {
            pipeline.release();
            pipeline = null;
        }
    }
}