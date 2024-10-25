package socketStreamer.service;

import com.fasterxml.jackson.core.JsonParser;
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
    public void onMessage(Message message, byte[] pattern) {
        try {
            String messageString = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());

            // JsonNode로 파싱
            JsonNode jsonNode = objectMapper.readTree(messageString);
            String topic = jsonNode.get("topic").asText();
            String payload = jsonNode.get("payload").toString();

            messagingTemplate.convertAndSend("/sub/channel/" + topic, payload);

        } catch (Exception e) {
            log.error("Error processing message: " + e.getMessage());
        }
    }
}