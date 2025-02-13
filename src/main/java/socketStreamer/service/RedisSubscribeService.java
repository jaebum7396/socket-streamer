package socketStreamer.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
        log.info("메시지 수신 : {}", message);
        try {
            String messageString = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());

            if (messageString == null) {
                log.error("메시지가 null임");
                return;
            }

            if (messageString.startsWith("\"") && messageString.endsWith("\"")) {
                messageString = messageString.substring(1, messageString.length() - 1);
                //log.info("2. 큰따옴표 제거 후: {}", messageString);
            }

            try {
                Envelope envelope = objectMapper.readValue(messageString, Envelope.class);
                String topic = envelope.getTopic();
                ObjectNode payload = envelope.getPayload();

                String payloadString = objectMapper.writeValueAsString(payload);

                messagingTemplate.convertAndSend("/sub/channel/" + topic, payloadString);

            } catch (Exception e) {
                log.error("JSON 파싱 실패", e);
                log.error("실패한 문자열: {}", messageString);
            }

        } catch (Exception e) {
            log.error("전체 프로세스 실패", e);
        }
    }
}