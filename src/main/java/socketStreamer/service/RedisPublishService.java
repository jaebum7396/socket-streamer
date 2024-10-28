package socketStreamer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisPublishService {
    private final RedisTemplate<String, Object> redisTemplate;
    public void publish(String topic, Object payload) {
        log.info("payload : " + payload);
        redisTemplate.convertAndSend(topic, payload);
    }
}


