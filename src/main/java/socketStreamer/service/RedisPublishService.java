package socketStreamer.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisPublishService {
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper(); // ObjectMapper 추가

    public void publish(String topic, String chat) {
        try {
            // JSON 형태로 메시지 구성
            Map<String, Object> message = new HashMap<>();
            message.put("topic", topic);
            message.put("payload", Collections.singletonMap("chat", chat));

            // JSON 문자열로 변환
            String jsonMessage = objectMapper.writeValueAsString(message);
            log.info("Publishing message: {}", jsonMessage);

            // Redis로 전송
            redisTemplate.convertAndSend(topic, jsonMessage);
        } catch (JsonProcessingException e) {
            log.error("메시지 변환 실패", e);
        }
    }
}

