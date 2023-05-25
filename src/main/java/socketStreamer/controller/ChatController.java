package socketStreamer.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Header;
import socketStreamer.model.Chat;
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
public class ChatController {

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
    public void message(Chat chat, @Header("Authorization") String token) {
        Claims claims = getClaims(token);
        String userCd = claims.get("userCd", String.class);
        chat.setUserCd(userCd);
        System.out.println(chat);
        // Websocket에 발행된 메시지를 redis로 발행한다(publish)
        String destination = channelRepository.getTopic(chat.getDomainCd()).getTopic();
        redisPublishService.publish(destination, chat);
    }

    @MessageMapping("/enter")
    public void enter(Chat chat, @Header("Authorization") String token) {
        Claims claims = getClaims(token);
        String userCd = claims.get("userCd", String.class);
        chat.setUserCd(userCd);
        System.out.println(chat);
        if (chat.getTransferType() == 1) {
            channelRepository.enterTopic(chat.getDomainCd());
        }
    }
}