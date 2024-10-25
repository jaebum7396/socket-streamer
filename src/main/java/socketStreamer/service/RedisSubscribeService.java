package socketStreamer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import socketStreamer.model.Envelope;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscribeService implements MessageListener {
    private final ObjectMapper objectMapper;
    private final RedisTemplate redisTemplate;
    private final SimpMessageSendingOperations messagingTemplate;
    @Override
    public void onMessage(Message envelope, byte[] pattern) {
        try {
            String publishMessage = (String) redisTemplate.getStringSerializer().deserialize(envelope.getBody());

            // topic과 payload만 추출하기 위해 JsonNode로 파싱
            JsonNode rootNode = objectMapper.readTree(publishMessage);
            String topic = rootNode.get("topic").asText();
            String payload = rootNode.get("payload").toString();  // payload는 JSON 문자열 그대로 유지

            // topic과 payload 그대로 전송
            messagingTemplate.convertAndSend("/sub/channel/" + topic, payload);
        } catch (Exception e) {
            // 에러의 전체 스택 트레이스 출력
            log.error("Error processing message: ", e);
            log.error("Failed message content: {}", envelope);
        }
    }
}