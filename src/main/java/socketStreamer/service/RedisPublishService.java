package socketStreamer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import socketStreamer.model.Envelope;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisPublishService {
    private final RedisTemplate<String, Object> redisTemplate;
    public void publish(Envelope envelope) {
        log.info("payload : " + envelope.getPayload());
        redisTemplate.convertAndSend(envelope.getTopic(), envelope);
    }
}


