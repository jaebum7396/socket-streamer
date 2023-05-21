package socketStreamer.configuration;

import socketStreamer.model.MyUserPrincipal;
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
        config.enableSimpleBroker("/sub","/direct");
        config.setUserDestinationPrefix("/user");
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

                List<String> AuthorizationArr = Optional
                        .ofNullable(accessor.getNativeHeader("Authorization"))
                        .orElseGet(Collections::emptyList);

                System.out.println("AuthorizationArr : "+AuthorizationArr);
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
                    }

                    // 사용자 접속 해제시 사용자 큐를 삭제한다.
                    if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                        // 현재 접속한 userCd
                        String userCd = accessor.getUser().getName();
                        // 현재 접속한 세션을 가져온다.
                        String userSession = accessor.getSessionId();
                        System.out.println("접속 해제 요청- [userCd : "+ userCd+"] [sessionId : "+userSession+"]");
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }

                return message;
            }
        });
    }
}