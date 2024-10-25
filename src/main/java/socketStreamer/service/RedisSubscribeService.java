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
        try {
            String messageString = (String) redisTemplate.getStringSerializer().deserialize(message.getBody());
            log.info("1. Redis에서 받은 원본 메시지: {}", messageString);

            if (messageString == null) {
                log.error("메시지가 null임");
                return;
            }

            if (messageString.startsWith("\"") && messageString.endsWith("\"")) {
                messageString = messageString.substring(1, messageString.length() - 1);
                log.info("2. 큰따옴표 제거 후: {}", messageString);
            }

            log.info("3. Envelope로 변환 시도 전 messageString의 클래스: {}", messageString.getClass());
            log.info("4. messageString의 처음 100자: {}", messageString.substring(0, Math.min(messageString.length(), 100)));

            try {
                Envelope envelope = objectMapper.readValue(messageString, Envelope.class);
                log.info("5. Envelope 변환 성공");
                String topic = envelope.getTopic();
                ObjectNode payload = envelope.getPayload();

                String payloadString = objectMapper.writeValueAsString(payload);
                log.info("6. 변환된 payload: {}", payloadString);

                messagingTemplate.convertAndSend("/sub/channel/" + topic, payloadString);
                log.info("7. 메시지 전송 완료");

            } catch (Exception e) {
                log.error("JSON 파싱 실패", e);
                log.error("실패한 문자열: {}", messageString);
            }

        } catch (Exception e) {
            log.error("전체 프로세스 실패", e);
        }
    }
}