package socketStreamer.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import socketStreamer.model.MyUserPrincipal;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.Principal;
import java.util.*;

@Slf4j
public class CustomChannelInterceptor implements ChannelInterceptor {

    private final RedisTemplate<String, Object> redisTemplate;
    private String JWT_SECRET_KEY;

    // Constructor to inject the RedisTemplate
    public CustomChannelInterceptor(RedisTemplate<String, Object> redisTemplate, String JWT_SECRET_KEY) {
        this.redisTemplate = redisTemplate;
        this.JWT_SECRET_KEY = JWT_SECRET_KEY;
    }

    public Claims getClaims(String jwt) {
        Key secretKey = Keys.hmacShaKeyFor(JWT_SECRET_KEY.getBytes(StandardCharsets.UTF_8));
        Claims claim = Jwts.parserBuilder().setSigningKey(secretKey).build()
                .parseClaimsJws(jwt).getBody();
        return claim;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        //System.out.println("AuthorizationArr : "+AuthorizationArr);
        try{
            // 처음 접속 시도시 유저 데이터를 넣어준다.
            if (StompCommand.CONNECT.equals(accessor.getCommand())
                //&&AuthorizationArr.size()>0
            ) {
                // 헤더에서 userId 가져온다.
                List<String> AuthorizationArr = Optional
                        .ofNullable(accessor.getNativeHeader("Authorization"))
                        .orElseGet(Collections::emptyList);
                // 현재 접속한 userCd
                String userCd = "";
                if(AuthorizationArr.size() == 0){
                    String AuthorizationStr = AuthorizationArr.get(0);
                    Claims claim = getClaims(AuthorizationStr);
                    userCd = claim.get("userCd", String.class);
                } else {
                    List<String> connectionIdArr = Optional
                            .ofNullable(accessor.getNativeHeader("connectionId"))
                            .orElseGet(Collections::emptyList);
                    userCd = connectionIdArr.get(0);
                }

                // 현재 접속한 세션을 가져온다.
                String userSession = accessor.getSessionId();
                log.info("접속 요청 - [userCd : "+ userCd+"] [sessionId : "+userSession+"]");

                // 레디스에 사용자와 세션 정보 저장
                redisTemplate.opsForSet().add("userSessions:" + userCd, userSession);

                // Retrieve connected users from Redis
                Set<String> connectedUsers = getAllConnectedUsers();
                log.info("Connected Users: " + connectedUsers);

                //principal 만들어준다
                Principal principal = new MyUserPrincipal(userCd);
                accessor.setUser(principal);
            }

            if(accessor.getCommand() != null){
                // 사용자 접속 해제시 사용자 큐를 삭제한다.
                if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                    if (accessor.getUser() != null) {
                        // 현재 접속한 userCd
                        String userCd = accessor.getUser().getName();
                        // 현재 접속한 세션을 가져온다.
                        String userSession = accessor.getSessionId();
                        log.info("접속 해제 요청- [userCd : " + userCd + "] [sessionId : " + userSession + "]");

                        // 레디스에서 사용자와 세션 정보 제거
                        redisTemplate.opsForSet().remove("userSessions:" + userCd, userSession);

                        // Retrieve connected users from Redis
                        Set<String> connectedUsers = getAllConnectedUsers();
                        log.info("Connected Users: " + connectedUsers);
                    } else {
                        // accessor.getUser()가 null인 경우에 대한 처리
                    }
                }
            }else{

            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return message;
    }

    private Set<String> getAllConnectedUsers() {
        Set<String> userCodes = redisTemplate.keys("userSessions:*");
        return userCodes;
    }
}