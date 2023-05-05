package chatting.configuration;

import chatting.model.MyUserPrincipal;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired private RedisTemplate<String, Object> redisTemplate;
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.setApplicationDestinationPrefixes("/pub");
        config.enableSimpleBroker("/sub");
        config.setUserDestinationPrefix("/user/{userId}");
    }
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-stomp")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                ObjectMapper objectMapper = new ObjectMapper();
                List<String> userSessions = null;
                // 헤더에서 userCd 가져온다.
                List<String> userCdArr = Optional
                        .ofNullable(accessor.getNativeHeader("userCd"))
                        .orElseGet(Collections::emptyList);
                try{
                    // 처음 접속 시도시 유저 데이터를 넣어준다.
                    if (StompCommand.CONNECT.equals(accessor.getCommand())&&userCdArr.size()>0) {
                        // 현재 접속한 userCd
                        String userCd = userCdArr.get(0);
                        // 현재 접속한 세션을 가져온다.
                        String userSession = accessor.getSessionId();
                        System.out.println("접속 요청 - [userCd : "+ userCd+"] [sessionId : "+userSession+"]");

                        //principal 만들어준다 -- 해당 부분은 spring security를 사용하지 않을 경우이기 때문에 추후에 변경될 수 있음
                        Principal principal = new MyUserPrincipal(userCd);
                        accessor.setUser(principal);
                        // userSession redis 조회
                        Object userSessionObj = redisTemplate.opsForValue().get(userCd);

                        if (userSessionObj != null) {
                            // 조회한 value를 역질렬화한다
                            userSessions = objectMapper.readValue(
                                    String.valueOf(userSessionObj)
                                    , new TypeReference<ArrayList<String>>() {});
                            // 값이 있으면 해당 값에 userSession를 추가하여 Redis에 저장
                            userSessions.add(userSession);
                        } else {
                            // 값이 없으면 새로 생성하여 Redis에 저장
                            userSessions = Collections.singletonList(userSession);
                        }
                        redisTemplate.opsForValue().set(userCd, objectMapper.writeValueAsString(userSessions));
                        System.out.println(" **************** [접속]현재 사용자("+userCd+")"+"에 할당되어 있는 Sessions : "+redisTemplate.opsForValue().get(userCd).toString()+" **************** ");
                    }

                    // 사용자 접속 해제시 사용자 큐를 삭제한다.
                    if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                        // 현재 접속한 userCd
                        String userCd = accessor.getUser().getName();
                        // 현재 접속한 세션을 가져온다.
                        String userSession = accessor.getSessionId();
                        System.out.println("접속 해제 요청- [userCd : "+ userCd+"] [sessionId : "+userSession+"]");

                        // userSession redis 조회
                        Object userSessionObj = redisTemplate.opsForValue().get(userCd);

                        if (userSessionObj != null) {
                            // 조회한 value를 역질렬화한다
                            userSessions = objectMapper.readValue(
                                    String.valueOf(userSessionObj)
                                    , new TypeReference<ArrayList<String>>() {});
                            userSessions.remove(accessor.getSessionId());
                            // 남아 있는 세션이 없다면
                            if(userSessions.size() == 0){
                                // 레디스에서 해당 유저를 지운다
                                redisTemplate.delete(userCd);
                            }else{
                                // 있다면 해당 유저의 세션을 업데이트한다.
                                redisTemplate.opsForValue().set(userCd, objectMapper.writeValueAsString(userSessions));
                                System.out.println(" **************** [접속해제]현재 사용자("+userCd+")"+"에 할당되어 있는 Sessions : "+redisTemplate.opsForValue().get(userCd).toString()+" **************** ");
                            }
                        }
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

                return message;
            }
        });
    }
}