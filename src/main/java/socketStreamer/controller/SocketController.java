package socketStreamer.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import socketStreamer.model.Envelope;
import socketStreamer.repository.ChannelRepository;
import socketStreamer.service.RedisPublishService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@RequiredArgsConstructor
@RestController
public class SocketController {

    private final RedisPublishService redisPublishService;
    private final ChannelRepository channelRepository;
    @Autowired private RedisTemplate<String, Object> redisTemplate;

    @Value("${jwt.secret.key}")
    private String JWT_SECRET_KEY;

    public Claims getClaims(String jwt) {
        try{
            Key secretKey = Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
            Claims claim = Jwts.parserBuilder().setSigningKey(secretKey).build()
                    .parseClaimsJws(jwt).getBody();
            return claim;
        } catch (
        ExpiredJwtException e) {
            throw new ExpiredJwtException(null, null, "로그인 시간이 만료되었습니다.");
        }
    }

    //websocket "/pub/message"로 들어오는 메시징을 처리한다.
    @MessageMapping("/message")
    public void message(Envelope envelope, @Header("connectionId") String userCd) {
        System.out.println(envelope);
        // Websocket에 발행된 메시지를 redis로 발행한다(publish)
        //String destination = channelRepository.getTopic(envelope.getTopic()).getTopic();
        String destination = envelope.getTopic();
        redisPublishService.publish(destination, envelope.toString());
    }

    @MessageMapping("/enter")
    public void enter(Envelope envelope, @Header("connectionId") String userCd) {
        System.out.println(envelope);
        channelRepository.enterTopic(envelope.getTopic());
    }
}